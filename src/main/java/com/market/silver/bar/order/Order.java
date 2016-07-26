package com.market.silver.bar.order;

import java.math.BigDecimal;

/**
 * Immutable representation of an order.
 *
 * @author Hemant
 */
public class Order {

    private final String userId;

    private final BigDecimal quantity;

    private final BigDecimal pricePerUnit;

    private final OrderType orderType;

    public Order(String userId, BigDecimal quantity, BigDecimal pricePerUnit, OrderType orderType) {
        this.userId = userId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.orderType = orderType;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public OrderType getOrderType() {
        return orderType;
    }
}
