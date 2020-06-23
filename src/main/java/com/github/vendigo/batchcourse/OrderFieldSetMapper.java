package com.github.vendigo.batchcourse;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;

@Component
public class OrderFieldSetMapper implements FieldSetMapper<Order> {

    @Override
    public Order mapFieldSet(FieldSet fieldSet) {
        return Order.builder()
            .orderId(fieldSet.readLong("order_id"))
            .cost(fieldSet.readBigDecimal("cost"))
            .email(fieldSet.readString("email"))
            .firstName(fieldSet.readString("first_name"))
            .lastName(fieldSet.readString("last_name"))
            .itemId(fieldSet.readString("item_id"))
            .itemName(fieldSet.readString("item_name"))
            .shipDate(fieldSet.readDate("ship_date"))
            .build();
    }
}
