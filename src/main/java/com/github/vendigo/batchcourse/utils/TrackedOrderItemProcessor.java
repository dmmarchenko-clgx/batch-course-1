package com.github.vendigo.batchcourse.utils;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.github.vendigo.batchcourse.model.Order;
import com.github.vendigo.batchcourse.model.TrackedOrder;

@Component
public class TrackedOrderItemProcessor implements ItemProcessor<Order, TrackedOrder> {

    @Override
    public TrackedOrder process(Order order) {
        return new TrackedOrder(order, UUID.randomUUID().toString());
    }
}
