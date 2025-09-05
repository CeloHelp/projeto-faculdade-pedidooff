package com.pedidofacil.services;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Order;
import com.pedidofacil.models.OrderItem;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.models.Product;
import com.pedidofacil.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "userHome", "/tmp");
    }

    @Test
    void createOrder_cashPayment_success() {
        List<OrderItem> items = new ArrayList<>();
        Product product1 = new Product("Product 1", "Brand A", "Unit A", BigDecimal.TEN);
        items.add(new OrderItem(product1, BigDecimal.ONE, BigDecimal.TEN));
        PaymentMethod paymentMethod = PaymentMethod.CASH;
        Customer customer = null;

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setNumber(1L);
            return savedOrder;
        });
        when(orderRepository.findMaxNumber()).thenReturn(0L);

        Order createdOrder = orderService.createOrder(items, paymentMethod, customer);

        assertNotNull(createdOrder);
        assertEquals(1L, createdOrder.getNumber());
        assertEquals(paymentMethod, createdOrder.getPaymentMethod());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_creditSale_withCustomer_success() {
        List<OrderItem> items = new ArrayList<>();
        Product product1 = new Product("Product 1", "Brand A", "Unit A", BigDecimal.TEN);
        items.add(new OrderItem(product1, BigDecimal.ONE, BigDecimal.TEN));
        PaymentMethod paymentMethod = PaymentMethod.CREDITSALE;
        Customer customer = new Customer("Test Customer", "123");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setNumber(1L);
            return savedOrder;
        });
        when(orderRepository.findMaxNumber()).thenReturn(0L);

        Order createdOrder = orderService.createOrder(items, paymentMethod, customer);

        assertNotNull(createdOrder);
        assertEquals(1L, createdOrder.getNumber());
        assertEquals(paymentMethod, createdOrder.getPaymentMethod());
        assertEquals(customer, createdOrder.getCustomer());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_creditSale_noCustomer_throwsException() {
        List<OrderItem> items = new ArrayList<>();
        PaymentMethod paymentMethod = PaymentMethod.CREDITSALE;
        Customer customer = null;

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(items, paymentMethod, customer);
        });
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void nextOrderNumber_returnsNextNumber() {
        when(orderRepository.findMaxNumber()).thenReturn(5L);

        Long nextNumber = orderService.nextOrderNumber();

        assertEquals(6L, nextNumber);
        verify(orderRepository, times(1)).findMaxNumber();
    }

    @Test
    void nextOrderNumber_noExistingOrders_returnsOne() {
        when(orderRepository.findMaxNumber()).thenReturn(null);

        Long nextNumber = orderService.nextOrderNumber();

        assertEquals(1L, nextNumber);
        verify(orderRepository, times(1)).findMaxNumber();
    }

    @Test
    void findHistory_returnsOrders() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        Customer customer = new Customer("Test Customer", "123");
        List<Order> orders = Arrays.asList(new Order(), new Order());
        when(orderRepository.findHistory(start, end, customer)).thenReturn(orders);

        List<Order> foundOrders = orderService.findHistory(start, end, customer);

        assertNotNull(foundOrders);
        assertEquals(2, foundOrders.size());
        verify(orderRepository, times(1)).findHistory(start, end, customer);
    }

    @Test
    void exportCsv_success() throws IOException {
        List<Order> orders = new ArrayList<>();
        Order order1 = new Order();
        order1.setNumber(1L);
        order1.setCreatedAt(LocalDateTime.now());
        order1.setPaymentMethod(PaymentMethod.CASH);
        order1.setTotal(new BigDecimal("10.00"));
        orders.add(order1);

        Order order2 = new Order();
        order2.setNumber(2L);
        order2.setCreatedAt(LocalDateTime.now());
        order2.setPaymentMethod(PaymentMethod.CREDITSALE);
        order2.setCustomer(new Customer("Test Customer", "123"));
        order2.setTotal(new BigDecimal("20.00"));
        orders.add(order2);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
            mockedFiles.when(() -> Files.writeString(any(Path.class), anyString(), any(java.nio.charset.Charset.class))).thenReturn(null);

            String filePath = orderService.exportCsv(orders);

            assertNotNull(filePath);
            assertTrue(filePath.contains("export_"));
            mockedFiles.verify(() -> Files.createDirectories(any(Path.class)), times(1));
            mockedFiles.verify(() -> Files.writeString(any(Path.class), anyString(), any(java.nio.charset.Charset.class)), times(1));
        }
    }

    @Test
    void exportCsv_ioException_throwsRuntimeException() throws IOException {
        List<Order> orders = new ArrayList<>();

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenThrow(new IOException("Test IO Exception"));

            assertThrows(RuntimeException.class, () -> {
                orderService.exportCsv(orders);
            });
            mockedFiles.verify(() -> Files.createDirectories(any(Path.class)), times(1));
            mockedFiles.verify(() -> Files.writeString(any(Path.class), anyString(), any(java.nio.charset.Charset.class)), never());
        }
    }
}