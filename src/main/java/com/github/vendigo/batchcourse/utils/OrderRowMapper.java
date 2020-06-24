package com.github.vendigo.batchcourse.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.github.vendigo.batchcourse.model.Order;

@Component
public class OrderRowMapper implements RowMapper<Order> {

    @Override
    public Order mapRow(ResultSet resultSet, int i) throws SQLException {
        return Order.builder()
            .orderId(resultSet.getLong("order_id"))
            .cost(resultSet.getBigDecimal("cost"))
            .email(resultSet.getString("email"))
            .firstName(resultSet.getString("first_name"))
            .lastName(resultSet.getString("last_name"))
            .itemId(resultSet.getString("item_id"))
            .itemName(resultSet.getString("item_name"))
            .shipDate(resultSet.getDate("ship_date"))
            .build();
    }
}
