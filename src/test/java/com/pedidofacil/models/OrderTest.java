package com.pedidofacil.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;
    private Customer customer;
    private Product product1;
    private Product product2;
    private OrderItem item1;
    private OrderItem item2;

    @BeforeEach
    void setUp() {
        order = new Order();
        customer = new Customer("Cliente Teste", "123456789");
        
        product1 = new Product("Produto 1", "Marca A", "UN", new BigDecimal("10.00"));
        product2 = new Product("Produto 2", "Marca B", "KG", new BigDecimal("25.50"));
        
        item1 = new OrderItem(product1, new BigDecimal("2"), new BigDecimal("10.00"));
        item2 = new OrderItem(product2, new BigDecimal("1"), new BigDecimal("25.50"));
    }

    @Test
    void constructor_createsEmptyOrder() {
        Order newOrder = new Order();
        
        assertNull(newOrder.getId());
        assertNull(newOrder.getNumber());
        assertNull(newOrder.getCreatedAt());
        assertNull(newOrder.getPaymentMethod());
        assertEquals(BigDecimal.ZERO, newOrder.getTotal());
        assertNull(newOrder.getCustomer());
        assertNotNull(newOrder.getItems());
        assertTrue(newOrder.getItems().isEmpty());
    }

    @Test
    void addItem_addsItemAndRecalculatesTotal() {
        // Act
        order.addItem(item1);
        order.addItem(item2);

        // Assert
        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("45.50"), order.getTotal());
        assertEquals(order, item1.getOrder());
        assertEquals(order, item2.getOrder());
    }

    @Test
    void removeItem_removesItemAndRecalculatesTotal() {
        // Arrange
        order.addItem(item1);
        order.addItem(item2);

        // Act
        order.removeItem(item1);

        // Assert
        assertEquals(1, order.getItems().size());
        assertEquals(new BigDecimal("25.50"), order.getTotal());
        assertNull(item1.getOrder());
        assertEquals(order, item2.getOrder());
    }

    @Test
    void recalcTotal_calculatesCorrectTotal() {
        // Arrange
        order.getItems().add(item1);
        order.getItems().add(item2);

        // Act
        order.recalcTotal();

        // Assert
        assertEquals(new BigDecimal("45.50"), order.getTotal());
    }

    @Test
    void recalcTotal_withNullSubtotals_handlesCorrectly() {
        // Arrange
        OrderItem itemWithNullSubtotal = new OrderItem(product1, new BigDecimal("1"), new BigDecimal("10.00"));
        itemWithNullSubtotal.setSubtotal(null);
        order.getItems().add(itemWithNullSubtotal);
        order.getItems().add(item2);

        // Act
        order.recalcTotal();

        // Assert
        assertEquals(new BigDecimal("25.50"), order.getTotal());
    }

    @Test
    void recalcTotal_withEmptyItems_returnsZero() {
        // Act
        order.recalcTotal();

        // Assert
        assertEquals(BigDecimal.ZERO, order.getTotal());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        // Test ID
        order.setId(1L);
        assertEquals(1L, order.getId());

        // Test Number
        order.setNumber(123L);
        assertEquals(123L, order.getNumber());

        // Test CreatedAt
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        assertEquals(now, order.getCreatedAt());

        // Test PaymentMethod
        order.setPaymentMethod(PaymentMethod.CASH);
        assertEquals(PaymentMethod.CASH, order.getPaymentMethod());

        // Test Total
        order.setTotal(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), order.getTotal());

        // Test Customer
        order.setCustomer(customer);
        assertEquals(customer, order.getCustomer());

        // Test Items
        List<OrderItem> items = new ArrayList<>();
        items.add(item1);
        order.setItems(items);
        assertEquals(1, order.getItems().size());
        assertEquals(item1, order.getItems().get(0));
    }

    @Test
    void addItem_withNullItem_throwsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            order.addItem(null);
        });
    }

    @Test
    void removeItem_withNullItem_doesNotThrow() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            order.removeItem(null);
        });
    }

    @Test
    void removeItem_withNonExistentItem_doesNotThrow() {
        // Arrange
        OrderItem nonExistentItem = new OrderItem(product1, BigDecimal.ONE, BigDecimal.TEN);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            order.removeItem(nonExistentItem);
        });
    }

    @Test
    void prePersist_setsCreatedAtIfNull() {
        // Arrange
        assertNull(order.getCreatedAt());

        // Act
        order.prePersist();

        // Assert
        assertNotNull(order.getCreatedAt());
    }

    @Test
    void prePersist_doesNotOverrideExistingCreatedAt() {
        // Arrange
        LocalDateTime existingDate = LocalDateTime.of(2023, 1, 1, 10, 0);
        order.setCreatedAt(existingDate);

        // Act
        order.prePersist();

        // Assert
        assertEquals(existingDate, order.getCreatedAt());
    }

    @Test
    void preUpdate_recalculatesTotal() {
        // Arrange
        order.addItem(item1);
        order.setTotal(BigDecimal.ZERO); // Force incorrect total

        // Act
        order.preUpdate();

        // Assert
        assertEquals(new BigDecimal("20.00"), order.getTotal());
    }

    @Test
    void addItem_multipleItems_calculatesCorrectTotal() {
        // Arrange
        OrderItem item3 = new OrderItem(product1, new BigDecimal("3"), new BigDecimal("10.00"));

        // Act
        order.addItem(item1);
        order.addItem(item2);
        order.addItem(item3);

        // Assert
        assertEquals(3, order.getItems().size());
        assertEquals(new BigDecimal("75.50"), order.getTotal());
    }

    @Test
    void removeItem_multipleItems_calculatesCorrectTotal() {
        // Arrange
        order.addItem(item1);
        order.addItem(item2);
        OrderItem item3 = new OrderItem(product1, new BigDecimal("3"), new BigDecimal("10.00"));
        order.addItem(item3);

        // Act
        order.removeItem(item2);

        // Assert
        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("50.00"), order.getTotal());
    }
}