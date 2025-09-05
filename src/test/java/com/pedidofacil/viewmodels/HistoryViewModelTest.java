package com.pedidofacil.viewmodels;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Order;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.services.IOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryViewModelTest {

    @Mock
    private IOrderService orderService;

    @InjectMocks
    private HistoryViewModel historyViewModel;

    private LocalDate startDate;
    private LocalDate endDate;
    private Customer testCustomer;
    private Order testOrder1;
    private Order testOrder2;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2023, 1, 1);
        endDate = LocalDate.of(2023, 1, 31);
        
        testCustomer = new Customer("Cliente Teste", "123456789");
        
        testOrder1 = new Order();
        testOrder1.setNumber(1L);
        testOrder1.setCreatedAt(LocalDateTime.of(2023, 1, 15, 10, 0));
        testOrder1.setPaymentMethod(PaymentMethod.CASH);
        testOrder1.setTotal(new BigDecimal("100.00"));
        testOrder1.setCustomer(testCustomer);

        testOrder2 = new Order();
        testOrder2.setNumber(2L);
        testOrder2.setCreatedAt(LocalDateTime.of(2023, 1, 20, 14, 30));
        testOrder2.setPaymentMethod(PaymentMethod.CREDITSALE);
        testOrder2.setTotal(new BigDecimal("250.00"));
        testOrder2.setCustomer(testCustomer);
    }

    @Test
    void search_withValidDates_returnsOrders() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(testOrder1, testOrder2);
        when(orderService.findHistory(any(LocalDateTime.class), any(LocalDateTime.class), eq(testCustomer)))
                .thenReturn(expectedOrders);

        historyViewModel.setStartDate(startDate);
        historyViewModel.setEndDate(endDate);
        historyViewModel.setCustomer(testCustomer);

        // Act
        historyViewModel.search();

        // Assert
        assertEquals(2, historyViewModel.getOrders().size());
        assertEquals("Encontrados: 2", historyViewModel.getStatus());
        verify(orderService, times(1)).findHistory(any(LocalDateTime.class), any(LocalDateTime.class), eq(testCustomer));
    }

    @Test
    void search_withNullDates_handlesCorrectly() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(testOrder1);
        when(orderService.findHistory(isNull(), isNull(), isNull()))
                .thenReturn(expectedOrders);

        historyViewModel.setStartDate(null);
        historyViewModel.setEndDate(null);
        historyViewModel.setCustomer(null);

        // Act
        historyViewModel.search();

        // Assert
        assertEquals(1, historyViewModel.getOrders().size());
        assertEquals("Encontrados: 1", historyViewModel.getStatus());
        verify(orderService, times(1)).findHistory(isNull(), isNull(), isNull());
    }

    @Test
    void search_withEmptyResults_handlesCorrectly() {
        // Arrange
        when(orderService.findHistory(any(LocalDateTime.class), any(LocalDateTime.class), any(Customer.class)))
                .thenReturn(Collections.emptyList());

        historyViewModel.setStartDate(startDate);
        historyViewModel.setEndDate(endDate);

        // Act
        historyViewModel.search();

        // Assert
        assertEquals(0, historyViewModel.getOrders().size());
        assertEquals("Encontrados: 0", historyViewModel.getStatus());
        verify(orderService, times(1)).findHistory(any(LocalDateTime.class), any(LocalDateTime.class), any(Customer.class));
    }

    @Test
    void exportCsv_withOrders_returnsPath() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder1, testOrder2);
        historyViewModel.setOrders(orders);
        String expectedPath = "/tmp/export_20230101_20230131.csv";
        when(orderService.exportCsv(orders)).thenReturn(expectedPath);

        // Act
        String result = historyViewModel.exportCsv();

        // Assert
        assertEquals(expectedPath, result);
        assertEquals("Exportado para: " + expectedPath, historyViewModel.getStatus());
        verify(orderService, times(1)).exportCsv(orders);
    }

    @Test
    void exportCsv_withEmptyOrders_returnsNull() {
        // Arrange
        historyViewModel.setOrders(Collections.emptyList());

        // Act
        String result = historyViewModel.exportCsv();

        // Assert
        assertNull(result);
        assertEquals("Nada para exportar", historyViewModel.getStatus());
        verify(orderService, never()).exportCsv(anyList());
    }

    @Test
    void exportCsv_withNullOrders_returnsNull() {
        // Arrange
        historyViewModel.setOrders(null);

        // Act
        String result = historyViewModel.exportCsv();

        // Assert
        assertNull(result);
        assertEquals("Nada para exportar", historyViewModel.getStatus());
        verify(orderService, never()).exportCsv(anyList());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        // Test setters and getters
        historyViewModel.setStartDate(startDate);
        assertEquals(startDate, historyViewModel.getStartDate());

        historyViewModel.setEndDate(endDate);
        assertEquals(endDate, historyViewModel.getEndDate());

        historyViewModel.setCustomer(testCustomer);
        assertEquals(testCustomer, historyViewModel.getCustomer());

        List<Order> orders = Arrays.asList(testOrder1, testOrder2);
        historyViewModel.setOrders(orders);
        assertEquals(2, historyViewModel.getOrders().size());

        historyViewModel.setStatus("Test Status");
        assertEquals("Test Status", historyViewModel.getStatus());
    }

    @Test
    void search_withOnlyStartDate_handlesCorrectly() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(testOrder1);
        when(orderService.findHistory(any(LocalDateTime.class), isNull(), any(Customer.class)))
                .thenReturn(expectedOrders);

        historyViewModel.setStartDate(startDate);
        historyViewModel.setEndDate(null);

        // Act
        historyViewModel.search();

        // Assert
        assertEquals(1, historyViewModel.getOrders().size());
        assertEquals("Encontrados: 1", historyViewModel.getStatus());
        verify(orderService, times(1)).findHistory(any(LocalDateTime.class), isNull(), any(Customer.class));
    }

    @Test
    void search_withOnlyEndDate_handlesCorrectly() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(testOrder2);
        when(orderService.findHistory(isNull(), any(LocalDateTime.class), any(Customer.class)))
                .thenReturn(expectedOrders);

        historyViewModel.setStartDate(null);
        historyViewModel.setEndDate(endDate);

        // Act
        historyViewModel.search();

        // Assert
        assertEquals(1, historyViewModel.getOrders().size());
        assertEquals("Encontrados: 1", historyViewModel.getStatus());
        verify(orderService, times(1)).findHistory(isNull(), any(LocalDateTime.class), any(Customer.class));
    }
}