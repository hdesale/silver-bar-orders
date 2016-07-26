package com.market.silver.bar.order;

import com.market.silver.bar.order.OrderCache.OrderSummaryKey;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static com.market.silver.bar.order.OrderType.BUY;
import static com.market.silver.bar.order.OrderType.SELL;
import static java.util.stream.Collectors.toCollection;
import static org.junit.Assert.*;

/**
 * JUnit test class for {@link OrderCache}
 *
 * @author Hemant
 */
public class OrderCacheTest {

    private OrderCache orderCache;

    @Before
    public final void setup() {
        orderCache = new OrderCache();
    }

    @Test
    public void testRegister() throws Exception {
        Order order = new Order("test_user", BigDecimal.TEN, BigDecimal.valueOf(300), SELL);
        String orderId = orderCache.register(order);
        assertNotNull(orderId);

        // check if order can be retrieved
        Order orderFromCache = orderCache.get(orderId);
        assertNotNull(orderFromCache);

        // check if it the retrieved order has correct data
        assertOrderData(order, orderFromCache);
    }

    @Test(expected = NullPointerException.class)
    public void testRegisterInvalidOrder() throws Exception {
        // try to register order with no quantity
        Order order = new Order("test_user", null, BigDecimal.valueOf(300), SELL);
        String orderId = orderCache.register(order);
        assertNotNull(orderId);
    }

    @Test
    public void testGet() throws Exception {
        Order order = new Order("test_user", BigDecimal.ONE, BigDecimal.valueOf(100), OrderType.BUY);
        String orderId = orderCache.register(order);
        assertNotNull(orderId);

        Order order1FromCache = orderCache.get(orderId);
        assertNotNull(order1FromCache);

        // check if it the retrieved order has correct data
        assertOrderData(order, order1FromCache);

        // check the retrieval is repeatable as the order is still not cancelled
        Order order2FromCache = orderCache.get(orderId);
        assertNotNull(order2FromCache);
        assertEquals(order1FromCache, order2FromCache);
    }

    @Test(expected = NullPointerException.class)
    public void testGetWithNullOrderId() throws Exception {
        orderCache.get(null);
    }

    @Test
    public void testGetWithNonExistingOrderId() throws Exception {
        Order order = orderCache.get("ORDER_DOES_NOT_EXIST");
        assertNull(order);
    }

    @Test
    public void testCancel() throws Exception {
        Order order = new Order("test_user", BigDecimal.ONE, BigDecimal.valueOf(100), OrderType.BUY);
        String orderId = orderCache.register(order);
        assertNotNull(orderId);

        Order order1FromCache = orderCache.get(orderId);
        assertNotNull(order1FromCache);

        // check if it the retrieved order has correct data
        assertOrderData(order, order1FromCache);

        // cancel the order
        orderCache.cancel(orderId);

        // check if order is removed from cache
        Order order2FromCache = orderCache.get(orderId);
        assertNull(order2FromCache);
    }

    @Test
    public void testGetSummary() throws Exception {
        List<Order> testOrders = getTestOrders();
        registerOrders(testOrders);

        // basic checks
        List<OrderSummary> summaryList = orderCache.getSummary();
        assertNotNull(summaryList);
        assertFalse(summaryList.isEmpty());

        // check size of the order summary list
        LinkedList<OrderSummaryKey> expectedKeys = testOrders.stream()
                .map(OrderCacheTest::createOrderSummaryKey)
                .distinct()
                .sorted(getOrderKeyComparator())
                .collect(toCollection(LinkedList::new));
        assertEquals(expectedKeys.size(), summaryList.size());

        // check each summary group
        summaryList.forEach(summary -> {
            OrderSummaryKey key = expectedKeys.pop(); // keys are ordered so we are checking the order also
            assertEquals(key.getPricePerUnit(), summary.getPricePerUnit());
            assertEquals(key.getOrderType(), summary.getOrderType());
            assertNotNull(summary.getTotalQuantity());
            assertNotNull(summary.getOrders());
            assertFalse(summary.getOrders().isEmpty());
            if (SELL == summary.getOrderType()) {
                assertTestSellOrderSummary(summary);
            } else {
                assertTestBuyOrderSummary(summary);
            }
        });
    }

    private List<Order> getTestOrders() {
        return Arrays.asList(
                new Order("test_user1", BigDecimal.valueOf(3.5), BigDecimal.valueOf(306), SELL),
                new Order("test_user2", BigDecimal.valueOf(1.2), BigDecimal.valueOf(310), SELL),
                new Order("test_user3", BigDecimal.valueOf(1.5), BigDecimal.valueOf(307), SELL),
                new Order("test_user4", BigDecimal.valueOf(2.0), BigDecimal.valueOf(306), SELL),
                new Order("test_user5", BigDecimal.valueOf(3.5), BigDecimal.valueOf(307), BUY),
                new Order("test_user6", BigDecimal.valueOf(1.2), BigDecimal.valueOf(306), BUY),
                new Order("test_user7", BigDecimal.valueOf(1.5), BigDecimal.valueOf(307), BUY),
                new Order("test_user8", BigDecimal.valueOf(2.0), BigDecimal.valueOf(306), BUY));
    }

    private void registerOrders(List<Order> orders) {
        orders.forEach(order -> orderCache.register(order));
    }

    private static OrderSummaryKey createOrderSummaryKey(Order order) {
        return new OrderSummaryKey(order.getOrderType(), order.getPricePerUnit());
    }

    private Comparator<OrderSummaryKey> getOrderKeyComparator() {
        return Comparator.comparing(OrderSummaryKey::getOrderType).thenComparing(key -> {
            BigDecimal price = key.getPricePerUnit();
            return key.getOrderType() == SELL ? price : price.negate();
        }, Comparator.naturalOrder());
    }

    private static void assertTestSellOrderSummary(OrderSummary summary) {
        if (BigDecimal.valueOf(306).equals(summary.getPricePerUnit())) { // check SELL orders with price 306 
            assertEquals(2, summary.getOrders().size());
            assertEquals(BigDecimal.valueOf(5.5), getTotalQuantity(summary.getOrders()));
        } else if (BigDecimal.valueOf(307).equals(summary.getPricePerUnit())) { // check SELL orders with price 307
            assertEquals(1, summary.getOrders().size());
            assertEquals(BigDecimal.valueOf(1.5), getTotalQuantity(summary.getOrders()));
        } else if (BigDecimal.valueOf(310).equals(summary.getPricePerUnit())) { // check SELL orders with price 310
            assertEquals(1, summary.getOrders().size());
            assertEquals(BigDecimal.valueOf(1.2), getTotalQuantity(summary.getOrders()));
        }
    }

    private static void assertTestBuyOrderSummary(OrderSummary summary) {
        if (BigDecimal.valueOf(306).equals(summary.getPricePerUnit())) { // check BUY orders with price 306 
            assertEquals(2, summary.getOrders().size());
            assertEquals(BigDecimal.valueOf(3.2), getTotalQuantity(summary.getOrders()));
        } else if (BigDecimal.valueOf(307).equals(summary.getPricePerUnit())) { // check BUT orders with price 307
            assertEquals(2, summary.getOrders().size());
            assertEquals(BigDecimal.valueOf(5.0), getTotalQuantity(summary.getOrders()));
        }
    }

    private static BigDecimal getTotalQuantity(List<Order> orders) {
        return orders.stream().map(Order::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void assertOrderData(Order expected, Order actual) {
        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getOrderType(), actual.getOrderType());
        assertEquals(expected.getPricePerUnit(), actual.getPricePerUnit());
        assertEquals(expected.getQuantity(), actual.getQuantity());
    }
}