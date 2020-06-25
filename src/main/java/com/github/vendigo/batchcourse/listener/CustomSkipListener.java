package com.github.vendigo.batchcourse.listener;

import org.springframework.batch.core.SkipListener;

import com.github.vendigo.batchcourse.model.Order;
import com.github.vendigo.batchcourse.model.TrackedOrder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomSkipListener implements SkipListener<Order, TrackedOrder> {

    @Override
    public void onSkipInRead(Throwable throwable) {

    }

    @Override
    public void onSkipInWrite(TrackedOrder trackedOrder, Throwable throwable) {

    }

    @Override
    public void onSkipInProcess(Order order, Throwable throwable) {
        log.info("Skippint processing of item with id: {}", order.getOrderId());
    }
}
