package com.market.silver.bar.order;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Immutable representation of summary of orders grouped by price per unit.
 *
 * @author Hemant
 */
public class OrderSummary {

    private final OrderType orderType;

    private final BigDecimal pricePerUnit;

    private final BigDecimal totalQuantity;

    private final List<Order> orders;

    public OrderSummary(OrderType orderType, BigDecimal pricePerUnit, BigDecimal totalQuantity, List<Order> orders) {
        this.orderType = orderType;
        this.pricePerUnit = pricePerUnit;
        this.totalQuantity = totalQuantity;
        this.orders = Collections.unmodifiableList(orders);
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public List<Order> getOrders() {
        return orders; // as the list itself is unmodifiable (refer constructor), the deep copy is not required
    }
}
