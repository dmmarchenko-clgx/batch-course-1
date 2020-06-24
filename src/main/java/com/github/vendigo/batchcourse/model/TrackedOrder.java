package com.github.vendigo.batchcourse.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackedOrder extends Order {

    private String trackingNumber;
    private boolean freeShipping;

    public TrackedOrder(Order order, String trackingNumber) {
        super(order.getOrderId(), order.getFirstName(), order.getLastName(),
            order.getEmail(), order.getCost(), order.getItemId(), order.getItemName(), order.getShipDate());
        this.trackingNumber = trackingNumber;
    }

    public TrackedOrder(TrackedOrder trackedOrder) {
        this(trackedOrder, trackedOrder.getTrackingNumber());
    }
}
