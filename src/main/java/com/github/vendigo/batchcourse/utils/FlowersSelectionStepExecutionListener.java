package com.github.vendigo.batchcourse.utils;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FlowersSelectionStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(final StepExecution stepExecution) {
        log.info("Execution before step logic");
    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        log.info("Execution after step logic");
        String flowerType = stepExecution.getJobParameters().getString("type");

        return "roses".equals(flowerType) ? new ExitStatus("TRIM REQUIRED") :
               new ExitStatus("NO TRIM REQUIRED");
    }
}
