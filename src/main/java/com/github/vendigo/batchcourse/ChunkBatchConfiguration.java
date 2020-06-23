package com.github.vendigo.batchcourse;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ChunkBatchConfiguration {

    private static final String[] COLUMN_NAMES = new String[] { "order_id", "first_name", "last_name", "email", "cost", "item_id", "item_name", "ship_date" };
    private static final String ORDER_SQL = "select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date "
        + " from SHIPPED_ORDER order by order_id";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final OrderRowMapper orderRowMapper;
    private final OrderFieldSetMapper orderFieldSetMapper;

    @Value("classpath:shipped_orders.csv")
    private Resource flatFile;

    @Bean
    public Job chunkBasedJob() {
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
    public ItemReader<Order> dbItemReader() {
        return new JdbcCursorItemReaderBuilder<Order>()
            .dataSource(dataSource)
            .name("jdbcCursorItemReader")
            .sql(ORDER_SQL)
            .rowMapper(orderRowMapper)
            .build();
    }

    @Bean
    public Step chunkBasedStep() {
        return stepBuilderFactory.get("chunkBasedStep")
            .<Order, Order>chunk(3)
            .reader(dbItemReader())
            .writer(list -> log.info("Writing items: {}", list))
            .build();
    }

}
