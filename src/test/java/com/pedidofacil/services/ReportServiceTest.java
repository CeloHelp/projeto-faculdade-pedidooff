package com.pedidofacil.services;

import com.pedidofacil.models.Order;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.repositories.OrderRepository;
import com.pedidofacil.repositories.projections.DailySalesView;
import com.pedidofacil.repositories.projections.PaymentDistributionView;
import com.pedidofacil.repositories.projections.ProductSalesView;
import com.pedidofacil.repositories.projections.TicketAverageView;
import com.pedidofacil.repositories.projections.TopCustomerView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReportService reportService;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2023, 1, 1);
        endDate = LocalDate.of(2023, 1, 31);
    }

    @Test
    void topProducts_withValidDates_returnsProductSales() {
        // Arrange
        ProductSalesView mockView = mock(ProductSalesView.class);
        when(mockView.getProductName()).thenReturn("Produto Teste");
        when(mockView.getQuantity()).thenReturn(new BigDecimal("10"));
        when(mockView.getTotal()).thenReturn(new BigDecimal("100.00"));
        
        List<ProductSalesView> expectedViews = Arrays.asList(mockView);
        when(orderRepository.productSales(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(expectedViews);

        // Act
        List<ProductSalesView> result = reportService.topProducts(startDate, endDate, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Produto Teste", result.get(0).getProductName());
        verify(orderRepository, times(1)).productSales(any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    @Test
    void topProducts_withNullDates_handlesCorrectly() {
        // Arrange
        when(orderRepository.productSales(isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<ProductSalesView> result = reportService.topProducts(null, null, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).productSales(isNull(), isNull(), any(PageRequest.class));
    }

    @Test
    void paymentDistribution_withValidDates_returnsDistribution() {
        // Arrange
        PaymentDistributionView mockView = mock(PaymentDistributionView.class);
        when(mockView.getPaymentMethod()).thenReturn(PaymentMethod.CASH);
        when(mockView.getTotal()).thenReturn(new BigDecimal("500.00"));
        
        List<PaymentDistributionView> expectedViews = Arrays.asList(mockView);
        when(orderRepository.sumByPaymentMethod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedViews);

        // Act
        List<PaymentDistributionView> result = reportService.paymentDistribution(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(PaymentMethod.CASH, result.get(0).getPaymentMethod());
        verify(orderRepository, times(1)).sumByPaymentMethod(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void ticketAverage_withValidDates_returnsAverages() {
        // Arrange
        TicketAverageView mockView = mock(TicketAverageView.class);
        when(mockView.getPaymentMethod()).thenReturn(PaymentMethod.CASH);
        when(mockView.getAverage()).thenReturn(25.50);
        
        List<TicketAverageView> expectedViews = Arrays.asList(mockView);
        when(orderRepository.ticketAverageByPayment(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedViews);

        // Act
        List<TicketAverageView> result = reportService.ticketAverage(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(PaymentMethod.CASH, result.get(0).getPaymentMethod());
        verify(orderRepository, times(1)).ticketAverageByPayment(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void topCustomers_withValidDates_returnsTopCustomers() {
        // Arrange
        TopCustomerView mockView = mock(TopCustomerView.class);
        when(mockView.getCustomerName()).thenReturn("Cliente Teste");
        when(mockView.getTotal()).thenReturn(new BigDecimal("1000.00"));
        
        List<TopCustomerView> expectedViews = Arrays.asList(mockView);
        when(orderRepository.topCustomers(any(LocalDateTime.class), any(LocalDateTime.class), eq(PaymentMethod.CASH), any(PageRequest.class)))
                .thenReturn(expectedViews);

        // Act
        List<TopCustomerView> result = reportService.topCustomers(startDate, endDate, PaymentMethod.CASH, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cliente Teste", result.get(0).getCustomerName());
        verify(orderRepository, times(1)).topCustomers(any(LocalDateTime.class), any(LocalDateTime.class), eq(PaymentMethod.CASH), any(PageRequest.class));
    }

    @Test
    void dailySales_withNativeQuerySuccess_returnsNativeResult() {
        // Arrange
        DailySalesView mockView = mock(DailySalesView.class);
        when(mockView.getDay()).thenReturn("2023-01-15");
        when(mockView.getTotal()).thenReturn(new BigDecimal("150.00"));
        
        List<DailySalesView> expectedViews = Arrays.asList(mockView);
        when(orderRepository.dailySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedViews);

        // Act
        List<DailySalesView> result = reportService.dailySales(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2023-01-15", result.get(0).getDay());
        verify(orderRepository, times(1)).dailySales(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(orderRepository, never()).findOrdersInPeriod(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void dailySales_withNativeQueryFailure_fallsBackToManualProcessing() {
        // Arrange
        when(orderRepository.dailySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Query nativa falhou"));

        Order order1 = new Order();
        order1.setCreatedAt(LocalDateTime.of(2023, 1, 15, 10, 0));
        order1.setTotal(new BigDecimal("100.00"));

        Order order2 = new Order();
        order2.setCreatedAt(LocalDateTime.of(2023, 1, 15, 14, 0));
        order2.setTotal(new BigDecimal("50.00"));

        List<Order> orders = Arrays.asList(order1, order2);
        when(orderRepository.findOrdersInPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act
        List<DailySalesView> result = reportService.dailySales(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2023-01-15", result.get(0).getDay());
        assertEquals(new BigDecimal("150.00"), result.get(0).getTotal());
        verify(orderRepository, times(1)).dailySales(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(orderRepository, times(1)).findOrdersInPeriod(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void dailySales_withEmptyNativeResult_fallsBackToManualProcessing() {
        // Arrange
        when(orderRepository.dailySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.of(2023, 1, 15, 10, 0));
        order.setTotal(new BigDecimal("100.00"));

        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findOrdersInPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act
        List<DailySalesView> result = reportService.dailySales(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2023-01-15", result.get(0).getDay());
        assertEquals(new BigDecimal("100.00"), result.get(0).getTotal());
        verify(orderRepository, times(1)).dailySales(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(orderRepository, times(1)).findOrdersInPeriod(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void dailySales_withNoOrders_returnsEmptyList() {
        // Arrange
        when(orderRepository.dailySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Query nativa falhou"));
        when(orderRepository.findOrdersInPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<DailySalesView> result = reportService.dailySales(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).dailySales(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(orderRepository, times(1)).findOrdersInPeriod(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void dailySales_withOrdersHavingNullValues_handlesCorrectly() {
        // Arrange
        when(orderRepository.dailySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Query nativa falhou"));

        Order order1 = new Order();
        order1.setCreatedAt(LocalDateTime.of(2023, 1, 15, 10, 0));
        order1.setTotal(new BigDecimal("100.00"));

        Order order2 = new Order();
        order2.setCreatedAt(null); // Data nula
        order2.setTotal(new BigDecimal("50.00"));

        Order order3 = new Order();
        order3.setCreatedAt(LocalDateTime.of(2023, 1, 15, 14, 0));
        order3.setTotal(null); // Total nulo

        List<Order> orders = Arrays.asList(order1, order2, order3);
        when(orderRepository.findOrdersInPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act
        List<DailySalesView> result = reportService.dailySales(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2023-01-15", result.get(0).getDay());
        assertEquals(new BigDecimal("100.00"), result.get(0).getTotal());
    }
}