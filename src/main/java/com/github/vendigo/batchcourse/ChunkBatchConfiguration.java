package com.github.vendigo.batchcourse;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ChunkBatchConfiguration {

    private static final String[] COLUMN_NAMES = new String[] { "order_id", "first_name", "last_name", "email", "cost", "item_id", "item_name", "ship_date" };
    private static final int CHUNK_SIZE = 10;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final OrderRowMapper orderRowMapper;
    private final OrderFieldSetMapper orderFieldSetMapper;

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
            .<Order, Order>chunk(CHUNK_SIZE)
            .reader(dbItemReader())
            .writer(list -> log.info("Writing items: {}", list))
            .build();
    }

}
