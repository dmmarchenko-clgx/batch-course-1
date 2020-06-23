package com.github.vendigo.batchcourse;

import java.time.LocalDateTime;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DeliveryDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(final JobExecution jobExecution, final StepExecution stepExecution) {
        String result = LocalDateTime.now().getHour() < 12 ? "PRESENT" : "NOT_PRESENT";
        log.info("Decider result: {}", result);
        return new FlowExecutionStatus(result);
    }
}
