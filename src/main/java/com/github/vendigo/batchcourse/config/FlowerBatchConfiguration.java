package com.github.vendigo.batchcourse.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.vendigo.batchcourse.listener.FlowersSelectionStepExecutionListener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j
public class FlowerBatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final FlowersSelectionStepExecutionListener selectFlowerListener;
    private final Flow deliveryFlow;

    @Bean
    public Job prepareFlowersJob() {
        return jobBuilderFactory.get("prepareFlowers")
            .start(selectFlowersStep())
            .on("TRIM REQUIRED").to(removeThornsStep())
            .next(arrangeFlowersStep())
            .from(selectFlowersStep())
            .on("NO TRIM REQUIRED").to(arrangeFlowersStep())
            .from(arrangeFlowersStep()).on("*").to(deliveryFlow)
            .end()
            .build();
    }

    @Bean
    public Step selectFlowersStep() {
        return stepBuilderFactory.get("selectFlowersStep")
            .tasklet((stepContribution, chunkContext) -> {
                log.info("Selecting flowers");
                return RepeatStatus.FINISHED;
            })
            .listener(selectFlowerListener)
            .build();
    }

    @Bean
    public Step arrangeFlowersStep() {
        return stepBuilderFactory.get("arrangeFlowersStep")
            .tasklet((stepContribution, chunkContext) -> {
                log.info("Arranging flowers");
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    @Bean
    public Step removeThornsStep() {
        return stepBuilderFactory.get("removeThornsStep")
            .tasklet((stepContribution, chunkContext) -> {
                log.info("Removing thorns");
                return RepeatStatus.FINISHED;
            })
            .build();
    }
}
