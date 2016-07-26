# silver-bar-orders

_JDK Version_:- `Java 8`

### Description:

* `Order` - Represents an order to BUY or SELL a quantity of silver bars at certain price.

* `OrderSummary` - Represents a summarised view of orders placed into application. Orders are grouped by Price and Order type.

* `OrderCache` - In memory cache which stores the orders in memory once they are registred by user. It also provides functionality to cancel the order.

  User can also query this cache to get a summarised view of all orders which currently exist in the system. 
  
  Orders with same price and order type are grouped together.
  
  The order groups appear in following order -
    `BUY` orders with descending order of price.
    `SELL` orders with ascending order of price.
  
