package com.github.vendigo.batchcourse.config;

import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.github.vendigo.batchcourse.listener.FlowersSelectionStepExecutionListener;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j
@EnableScheduling
public class FlowerBatchConfiguration extends QuartzJobBean {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final FlowersSelectionStepExecutionListener selectFlowerListener;
    private final Flow deliveryFlow;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;

    @Scheduled(cron = "0 0 * * * *")
    @SneakyThrows
    public void runJob() {
        JobParametersBuilder paramBuilder = new JobParametersBuilder();
        paramBuilder.addDate("run.date", new Date());
        paramBuilder.addString("type", "roses");
        jobLauncher.run(prepareFlowersJob(), paramBuilder.toJobParameters());
    }

    @Override
    @SneakyThrows
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        JobParameters parameters = new JobParametersBuilder(jobExplorer)
            .getNextJobParameters(prepareFlowersJob())
            .toJobParameters();
        jobLauncher.run(prepareFlowersJob(), parameters);
    }

    @Bean
    public Trigger trigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
            .simpleSchedule()
            .withIntervalInSeconds(30)
            .repeatForever();
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail())
            .withSchedule(scheduleBuilder)
            .build();
    }

    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob(FlowerBatchConfiguration.class)
            .storeDurably()
            .build();
    }

    @Bean
    public Job prepareFlowersJob() {
        return jobBuilderFactory.get("prepareFlowers")
            .incrementer(new RunIdIncrementer())
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
