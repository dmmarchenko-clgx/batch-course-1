package com.github.vendigo.batchcourse;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@Configuration
@AllArgsConstructor
@Slf4j
public class DeliveryBatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DeliveryDecider deliveryDecider;
    private final CorrectItemDecider correctItemDecider;

    @Bean
    public Job deliverPackageJob() {
        return jobBuilderFactory.get("deliverPackageJob")
            .start(packageItemStep())
            .next(driveToAddressStep())
            .on("FAILED").to(storePackageStep())
            .from(driveToAddressStep())
            .on("*").to(deliveryDecider)
            .on("PRESENT").to(givePackageToCustomerStep())
            .next(correctItemDecider).on("CORRECT_ITEM").to(thankCustomerStep())
            .from(correctItemDecider).on("WRONG_ITEM").to(givingRefundStep())
            .from(deliveryDecider).on("NOT_PRESENT").to(leaveAtDoorStep())
            .end()
            .build();
    }

    @Bean
    public Step packageItemStep() {
        return stepBuilderFactory.get("packageItemStep")
            .tasklet((stepContribution, chunkContext) -> {
                String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
                String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();

                log.info("The {} has been packaged on {}", item, date);
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    @Bean
    public Step driveToAddressStep() {
        boolean gotLost = false;

        return stepBuilderFactory.get("driveToAddressStep")
            .tasklet((stepContribution, chunkContext) -> {
                if (gotLost) {
                    throw new RuntimeException("Got lost driving to the address");
                }

                log.info("Successfully arrived at the address.");
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    @Bean
    public Step givePackageToCustomerStep() {
        return stepBuilderFactory.get("givePackageToCustomerStep")
            .tasklet((stepContribution, chunkContext) -> {
                log.info("Given the package to the customer.");
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    @Bean
    public Step storePackageStep() {
        return stepBuilderFactory.get("storePackageStep")
            .tasklet((stepContribution, chunkContext) -> {
                log.info("Storing the package while the customer address is located");
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    @Bean
    public Step leaveAtDoorStep() {
        return stepBuilderFactory.get("leaveAtDoorStep")
            .tasklet((stepContribution, chunkContext) -> {
                log.info("Leaving the package at the door.");
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    @Bean
    public Step thankCustomerStep() {
        return stepBuilderFactory.get("thankCustomerStep")
            .tasklet((stepContribution, chunkContext) -> {
                log.info("Saying thank you to the customer");
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    @Bean
    public Step givingRefundStep() {
        return stepBuilderFactory.get("givingRefundStep")
            .tasklet((stepContribution, chunkContext) -> {
                log.info("Refunding money for the wrong item");
                return RepeatStatus.FINISHED;
            })
            .build();
    }
}
