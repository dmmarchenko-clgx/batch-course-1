package com.github.vendigo.batchcourse.utils;

import java.util.Random;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CorrectItemDecider implements JobExecutionDecider {

    private final Random random = new Random();

    @Override
    public FlowExecutionStatus decide(final JobExecution jobExecution, final StepExecution stepExecution) {
        String result = random.nextFloat() < 0.7 ? "CORRECT_ITEM" : "WRONG_ITEM";
        log.info("Decider result: {}", result);
        return new FlowExecutionStatus(result);
    }
}
