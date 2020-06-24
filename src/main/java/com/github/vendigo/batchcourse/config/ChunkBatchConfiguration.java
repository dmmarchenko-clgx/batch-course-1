package com.github.vendigo.batchcourse.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.github.vendigo.batchcourse.model.Order;
import com.github.vendigo.batchcourse.model.TrackedOrder;
import com.github.vendigo.batchcourse.utils.FreeShippingProcessor;
import com.github.vendigo.batchcourse.utils.OrderFieldSetMapper;
import com.github.vendigo.batchcourse.utils.OrderRowMapper;
import com.github.vendigo.batchcourse.utils.TrackedOrderItemProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ChunkBatchConfiguration {

    private static final String[] FIELD_NAMES = new String[] { "orderId", "firstName", "lastName", "email", "cost", "itemId", "itemName", "shipDate" };
    private static final String[] COLUMN_NAMES = new String[] { "order_id", "first_name", "last_name", "email", "cost", "item_id", "item_name", "ship_date" };
    private static final String INSERT_ORDER_SQL = "INSERT INTO SHIPPED_ORDER_OUTPUT "
        + "(order_id, first_name, last_name, email, item_id, item_name, cost, ship_date) "
        + "VALUES (:orderId, :firstName, :lastName, :email, :itemId, :itemName, :cost, :shipDate)";
    private static final int CHUNK_SIZE = 10;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final OrderRowMapper orderRowMapper;
    private final OrderFieldSetMapper orderFieldSetMapper;
    private final TrackedOrderItemProcessor trackedOrderItemProcessor;
    private final FreeShippingProcessor freeShippingProcessor;

    @Value("classpath:shipped_orders.csv")
    private Resource flatFile;

    @Bean
    public Job chunkBasedJob() throws Exception {
        return jobBuilderFactory.get("chunkBasedJob")
            .start(chunkBasedStep())
            .build();
    }

    @Bean
    public ItemReader<Order> csvItemReader() {
        FlatFileItemReader<Order> itemReader = new FlatFileItemReader<>();
        itemReader.setLinesToSkip(1);
        itemReader.setResource(flatFile);

        DefaultLineMapper<Order> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(COLUMN_NAMES);
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(orderFieldSetMapper);

        itemReader.setLineMapper(lineMapper);

        return itemReader;
    }

    @Bean
    public PagingQueryProvider queryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
        factory.setSelectClause("select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date");
        factory.setFromClause("from SHIPPED_ORDER");
        factory.setSortKey("order_id");
        factory.setDataSource(dataSource);
        return factory.getObject();
    }

    @Bean
    public ItemReader<Order> dbItemReader() throws Exception {
        return new JdbcPagingItemReaderBuilder<Order>()
            .dataSource(dataSource)
            .name("jdbcCursorItemReader")
            .queryProvider(queryProvider())
            .rowMapper(orderRowMapper)
            .pageSize(CHUNK_SIZE)
            .build();
    }

    @Bean
    public Step chunkBasedStep() throws Exception {
        return stepBuilderFactory.get("chunkBasedStep")
            .<Order, TrackedOrder>chunk(CHUNK_SIZE)
            .reader(dbItemReader())
            .processor(compositeItemProcessor())
            .writer(jsonItemWriter())
            .build();
    }

    @Bean
    public ItemProcessor<Order, TrackedOrder> compositeItemProcessor() {
        return new CompositeItemProcessorBuilder<Order, TrackedOrder>()
            .delegates(
                orderValidatingItemProcessor(),
                trackedOrderItemProcessor,
                freeShippingProcessor,
                freeShippingFilterProcessor()
            )
            .build();
    }

    @Bean
    public ItemProcessor<Order, Order> orderValidatingItemProcessor() {
        BeanValidatingItemProcessor<Order> itemProcessor = new BeanValidatingItemProcessor<>();
        itemProcessor.setFilter(true);
        return itemProcessor;
    }

    @Bean
    public ItemProcessor<TrackedOrder, TrackedOrder> freeShippingFilterProcessor() {
        return order -> order.isFreeShipping() ? order : null;
    }

    @Bean
    public ItemWriter<Order> jdbcItemWriter() {
        return new JdbcBatchItemWriterBuilder<Order>()
            .dataSource(dataSource)
            .sql(INSERT_ORDER_SQL)
            .beanMapped()
            .build();
    }

    @Bean
    public ItemWriter<Order> jsonItemWriter() {
        return new JsonFileItemWriterBuilder<Order>()
            .name("jsonItemWriter")
            .resource(new FileSystemResource("out/shipped_orders_output.json"))
            .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
            .build();
    }

    @Bean
    public ItemWriter<Order> flatFileItemWriter() {
        FlatFileItemWriter<Order> itemWriter = new FlatFileItemWriter<>();
        itemWriter.setResource(new FileSystemResource("/out/shipped_orders_output.csv"));
        DelimitedLineAggregator<Order> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        BeanWrapperFieldExtractor<Order> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(FIELD_NAMES);
        lineAggregator.setFieldExtractor(fieldExtractor);
        itemWriter.setLineAggregator(lineAggregator);
        return itemWriter;
    }

}
