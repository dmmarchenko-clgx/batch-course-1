package com.github.vendigo.batchcourse;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j
public class ChunkBatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final SimpleItemReader simpleItemReader;

    @Bean
    public Job chunkBasedJob() {
        return jobBuilderFactory.get("chunkBasedJob")
            .start(chunkBasedStep())
            .build();
    }

    @Bean
    public Step chunkBasedStep() {
        return stepBuilderFactory.get("chunkBasedStep")
            .<String, String>chunk(3)
            .reader(simpleItemReader)
            .writer(list -> log.info("Writing items: {}", list))
            .build();
    }

}
