package com.pedidofacil.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private OrderItem orderItem;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        product = new Product("Produto Teste", "Marca A", "UN", new BigDecimal("10.00"));
        orderItem = new OrderItem(product, new BigDecimal("2"), new BigDecimal("10.00"));
        order = new Order();
    }

    @Test
    void constructor_createsOrderItemWithValues() {
        OrderItem newOrderItem = new OrderItem(product, new BigDecimal("3"), new BigDecimal("15.50"));
        
        assertEquals(product, newOrderItem.getProduct());
        assertEquals(new BigDecimal("3"), newOrderItem.getQuantity());
        assertEquals(new BigDecimal("15.50"), newOrderItem.getUnitPrice());
        assertEquals(new BigDecimal("46.50"), newOrderItem.getSubtotal());
    }

    @Test
    void constructor_withNullValues_handlesCorrectly() {
        OrderItem newOrderItem = new OrderItem(null, null, null);
        
        assertNull(newOrderItem.getProduct());
        assertNull(newOrderItem.getQuantity());
        assertNull(newOrderItem.getUnitPrice());
        assertNull(newOrderItem.getSubtotal());
    }

    @Test
    void constructor_emptyConstructor_createsEmptyOrderItem() {
        OrderItem emptyOrderItem = new OrderItem();
        
        assertNull(emptyOrderItem.getId());
        assertNull(emptyOrderItem.getProduct());
        assertNull(emptyOrderItem.getQuantity());
        assertNull(emptyOrderItem.getUnitPrice());
        assertNull(emptyOrderItem.getSubtotal());
        assertNull(emptyOrderItem.getOrder());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        // Test ID
        orderItem.setId(1L);
        assertEquals(1L, orderItem.getId());

        // Test Product
        Product newProduct = new Product("Novo Produto", "Marca B", "KG", new BigDecimal("25.00"));
        orderItem.setProduct(newProduct);
        assertEquals(newProduct, orderItem.getProduct());

        // Test Quantity
        orderItem.setQuantity(new BigDecimal("5"));
        assertEquals(new BigDecimal("5"), orderItem.getQuantity());

        // Test UnitPrice
        orderItem.setUnitPrice(new BigDecimal("20.00"));
        assertEquals(new BigDecimal("20.00"), orderItem.getUnitPrice());

        // Test Order
        orderItem.setOrder(order);
        assertEquals(order, orderItem.getOrder());
    }

    @Test
    void getSubtotal_calculatesCorrectly() {
        // Test with different quantities and prices
        orderItem.setQuantity(new BigDecimal("3"));
        orderItem.setUnitPrice(new BigDecimal("15.50"));
        
        assertEquals(new BigDecimal("46.50"), orderItem.getSubtotal());
    }

    @Test
    void getSubtotal_withZeroQuantity_returnsZero() {
        orderItem.setQuantity(BigDecimal.ZERO);
        orderItem.setUnitPrice(new BigDecimal("10.00"));
        
        assertEquals(BigDecimal.ZERO, orderItem.getSubtotal());
    }

    @Test
    void getSubtotal_withZeroUnitPrice_returnsZero() {
        orderItem.setQuantity(new BigDecimal("5"));
        orderItem.setUnitPrice(BigDecimal.ZERO);
        
        assertEquals(BigDecimal.ZERO, orderItem.getSubtotal());
    }

    @Test
    void getSubtotal_withNullValues_returnsNull() {
        orderItem.setQuantity(null);
        orderItem.setUnitPrice(new BigDecimal("10.00"));
        
        assertNull(orderItem.getSubtotal());
        
        orderItem.setQuantity(new BigDecimal("2"));
        orderItem.setUnitPrice(null);
        
        assertNull(orderItem.getSubtotal());
    }

    @Test
    void getSubtotal_withHighPrecision_calculatesCorrectly() {
        orderItem.setQuantity(new BigDecimal("2.5"));
        orderItem.setUnitPrice(new BigDecimal("12.345"));
        
        assertEquals(new BigDecimal("30.8625"), orderItem.getSubtotal());
    }

    @Test
    void getSubtotal_withNegativeValues_handlesCorrectly() {
        // Negative quantity
        orderItem.setQuantity(new BigDecimal("-2"));
        orderItem.setUnitPrice(new BigDecimal("10.00"));
        
        assertEquals(new BigDecimal("-20.00"), orderItem.getSubtotal());
        
        // Negative unit price
        orderItem.setQuantity(new BigDecimal("2"));
        orderItem.setUnitPrice(new BigDecimal("-10.00"));
        
        assertEquals(new BigDecimal("-20.00"), orderItem.getSubtotal());
    }

    @Test
    void quantity_withVariousValues_handlesCorrectly() {
        BigDecimal[] quantities = {
            BigDecimal.ONE,
            new BigDecimal("0.5"),
            new BigDecimal("10"),
            new BigDecimal("100.25"),
            new BigDecimal("0.001")
        };
        
        for (BigDecimal quantity : quantities) {
            orderItem.setQuantity(quantity);
            assertEquals(quantity, orderItem.getQuantity());
        }
    }

    @Test
    void unitPrice_withVariousValues_handlesCorrectly() {
        BigDecimal[] prices = {
            BigDecimal.ZERO,
            new BigDecimal("0.01"),
            new BigDecimal("1.00"),
            new BigDecimal("99.99"),
            new BigDecimal("1000.50")
        };
        
        for (BigDecimal price : prices) {
            orderItem.setUnitPrice(price);
            assertEquals(price, orderItem.getUnitPrice());
        }
    }

    @Test
    void product_withDifferentProducts_handlesCorrectly() {
        Product[] products = {
            new Product("Produto A", "Marca A", "UN", new BigDecimal("10.00")),
            new Product("Produto B", "Marca B", "KG", new BigDecimal("25.50")),
            new Product("Produto C", "Marca C", "L", new BigDecimal("5.75"))
        };
        
        for (Product product : products) {
            orderItem.setProduct(product);
            assertEquals(product, orderItem.getProduct());
        }
    }

    @Test
    void order_withDifferentOrders_handlesCorrectly() {
        Order[] orders = {
            new Order(),
            new Order(),
            new Order()
        };
        
        for (Order order : orders) {
            orderItem.setOrder(order);
            assertEquals(order, orderItem.getOrder());
        }
    }

    @Test
    void allFields_withNullValues_handlesCorrectly() {
        // Set all fields to null
        orderItem.setId(null);
        orderItem.setProduct(null);
        orderItem.setQuantity(null);
        orderItem.setUnitPrice(null);
        orderItem.setOrder(null);

        // Assert all are null
        assertNull(orderItem.getId());
        assertNull(orderItem.getProduct());
        assertNull(orderItem.getQuantity());
        assertNull(orderItem.getUnitPrice());
        assertNull(orderItem.getSubtotal());
        assertNull(orderItem.getOrder());
    }

    @Test
    void getSubtotal_withVeryLargeNumbers_handlesCorrectly() {
        orderItem.setQuantity(new BigDecimal("999999.99"));
        orderItem.setUnitPrice(new BigDecimal("999999.99"));
        
        BigDecimal expected = new BigDecimal("999999.99").multiply(new BigDecimal("999999.99"));
        assertEquals(expected, orderItem.getSubtotal());
    }

    @Test
    void getSubtotal_withVerySmallNumbers_handlesCorrectly() {
        orderItem.setQuantity(new BigDecimal("0.0001"));
        orderItem.setUnitPrice(new BigDecimal("0.0001"));
        
        assertEquals(new BigDecimal("0.00000001"), orderItem.getSubtotal());
    }

    @Test
    void constructor_withZeroValues_handlesCorrectly() {
        OrderItem zeroOrderItem = new OrderItem(product, BigDecimal.ZERO, BigDecimal.ZERO);
        
        assertEquals(product, zeroOrderItem.getProduct());
        assertEquals(BigDecimal.ZERO, zeroOrderItem.getQuantity());
        assertEquals(BigDecimal.ZERO, zeroOrderItem.getUnitPrice());
        assertEquals(BigDecimal.ZERO, zeroOrderItem.getSubtotal());
    }

    @Test
    void getSubtotal_recalculation_worksCorrectly() {
        // Initial values
        orderItem.setQuantity(new BigDecimal("2"));
        orderItem.setUnitPrice(new BigDecimal("10.00"));
        assertEquals(new BigDecimal("20.00"), orderItem.getSubtotal());
        
        // Change quantity
        orderItem.setQuantity(new BigDecimal("3"));
        assertEquals(new BigDecimal("30.00"), orderItem.getSubtotal());
        
        // Change unit price
        orderItem.setUnitPrice(new BigDecimal("15.00"));
        assertEquals(new BigDecimal("45.00"), orderItem.getSubtotal());
    }
}