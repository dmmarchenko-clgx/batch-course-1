package com.github.vendigo.batchcourse.processor;

import java.math.BigDecimal;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.github.vendigo.batchcourse.model.TrackedOrder;

@Component
public class FreeShippingProcessor implements ItemProcessor<TrackedOrder, TrackedOrder> {

    private static final BigDecimal FREE_SHIPPING_COST = BigDecimal.valueOf(50);

    @Override
    public TrackedOrder process(TrackedOrder order) {
        TrackedOrder processedOrder = new TrackedOrder(order);
        processedOrder.setFreeShipping(FREE_SHIPPING_COST.compareTo(order.getCost()) < 0);
        return processedOrder;
    }
}
