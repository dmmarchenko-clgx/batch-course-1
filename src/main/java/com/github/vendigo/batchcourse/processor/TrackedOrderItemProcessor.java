package com.github.vendigo.batchcourse.processor;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.github.vendigo.batchcourse.model.Order;
import com.github.vendigo.batchcourse.model.TrackedOrder;
import com.github.vendigo.batchcourse.utils.OrderProcessingException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TrackedOrderItemProcessor implements ItemProcessor<Order, TrackedOrder> {

    @Override
    public TrackedOrder process(Order order) throws Exception {
        log.info("Processing order with id: {}", order.getOrderId());
        return new TrackedOrder(order, getTrackingNumber());
    }

    private String getTrackingNumber() throws OrderProcessingException {
        if (Math.random() < 0.05) {
            throw new OrderProcessingException();
        }

        return UUID.randomUUID().toString();
    }
}
