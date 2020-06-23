package com.github.vendigo.batchcourse;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class Order {
    private Long orderId;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal cost;
    private String itemId;
    private String itemName;
    private Date shipDate;
}
