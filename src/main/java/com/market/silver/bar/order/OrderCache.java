package com.market.silver.bar.order;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * In memory cache implementation to handle the order management.
 *
 * @author Hemant
 */
public class OrderCache {

    private final Map<String, Order> ordersById = new ConcurrentHashMap<>();

    public String register(Order order) {
        validateOrder(order);
        String orderId = UUID.randomUUID().toString();
        ordersById.put(orderId, order);
        return orderId;
    }

    public Order get(String orderId) {
        requireNonNull(orderId, "Order id can not be null");
        return ordersById.get(orderId);
    }

    public boolean cancel(String orderId) {
        requireNonNull(orderId, "Order id can not be null");
        return ordersById.remove(orderId) != null;
    }

    public List<OrderSummary> getSummary() {
        Map<OrderSummaryKey, List<Order>> groups = getOrderGroups();
        return groups.entrySet()
                .stream()
                .map(entry -> createOrderSummary(entry.getKey(), entry.getValue()))
                .collect(toList());
    }

    private Map<OrderSummaryKey, List<Order>> getOrderGroups() {
        // Currently the order groups map is built every time when this method is called. But in case of
        // high volume of orders, the performance could be improved by maintaining a single map of order
        // groups at class level and keeping it in sync with 'ordersById'.
        return ordersById.values()
                .stream()
                .collect(groupingBy(this::createOrderSummaryKey,
                        () -> new TreeMap<>(getOrderKeyComparator()),
                        toList()));
    }

    private OrderSummary createOrderSummary(OrderSummaryKey key, List<Order> orders) {
        BigDecimal quantity = orders.stream().map(Order::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderSummary(key.getOrderType(), key.getPricePerUnit(), quantity, orders);
    }

    private OrderSummaryKey createOrderSummaryKey(Order order) {
        return new OrderSummaryKey(order.getOrderType(), order.getPricePerUnit());
    }

    private Comparator<OrderSummaryKey> getOrderKeyComparator() {
        return Comparator.comparing(OrderSummaryKey::getOrderType).thenComparing(key -> {
            BigDecimal price = key.getPricePerUnit();
            return key.getOrderType() == OrderType.SELL ? price : price.negate();
        }, Comparator.naturalOrder());
    }

    private static void validateOrder(Order order) {
        requireNonNull(order, "Order can not be null");
        requireNonNull(order.getUserId(), "User id can not be null");
        requireNonNull(order.getOrderType(), "Order type can not be null");
        requireNonNull(order.getPricePerUnit(), "Order price per unit can not be null");
        requireNonNull(order.getQuantity(), "Order quantity can not be null");
    }

    /**
     * Uniquely represents group of orders by order type and price per unit.
     */
    static class OrderSummaryKey {

        private final OrderType orderType;
        private final BigDecimal pricePerUnit;

        OrderSummaryKey(OrderType orderType, BigDecimal pricePerUnit) {
            requireNonNull(orderType);
            requireNonNull(pricePerUnit);
            this.orderType = orderType;
            this.pricePerUnit = pricePerUnit;
        }

        OrderType getOrderType() {
            return orderType;
        }

        BigDecimal getPricePerUnit() {
            return pricePerUnit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrderSummaryKey that = (OrderSummaryKey) o;
            return orderType == that.orderType && Objects.equals(pricePerUnit, that.pricePerUnit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(orderType, pricePerUnit);
        }
    }
}
