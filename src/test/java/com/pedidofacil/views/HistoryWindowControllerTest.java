package com.pedidofacil.views;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Order;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.viewmodels.HistoryViewModel;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryWindowControllerTest {

    @Mock
    private HistoryViewModel mockViewModel;

    private HistoryWindowController controller;

    private Order testOrder1;
    private Order testOrder2;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        controller = new HistoryWindowController(mockViewModel);
        
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
    void constructor_initializesCorrectly() {
        // Assert
        assertNotNull(controller);
    }

    @Test
    void onSearch_withValidDates_callsViewModelSearch() {
        // Arrange
        DatePicker dpStart = new DatePicker(LocalDate.of(2023, 1, 1));
        DatePicker dpEnd = new DatePicker(LocalDate.of(2023, 1, 31));
        TableView<Order> tblOrders = new TableView<>();
        
        setPrivateField(controller, "dpStart", dpStart);
        setPrivateField(controller, "dpEnd", dpEnd);
        setPrivateField(controller, "tblOrders", tblOrders);
        
        List<Order> orders = Arrays.asList(testOrder1, testOrder2);
        when(mockViewModel.getOrders()).thenReturn(orders);
        when(mockViewModel.getStatus()).thenReturn("Encontrados: 2");

        // Act
        controller.onSearch();

        // Assert
        verify(mockViewModel, times(1)).setStartDate(LocalDate.of(2023, 1, 1));
        verify(mockViewModel, times(1)).setEndDate(LocalDate.of(2023, 1, 31));
        verify(mockViewModel, times(1)).search();
        verify(mockViewModel, times(1)).getOrders();
        verify(mockViewModel, times(1)).getStatus();
    }

    @Test
    void onSearch_withNullDates_callsViewModelSearch() {
        // Arrange
        DatePicker dpStart = new DatePicker();
        DatePicker dpEnd = new DatePicker();
        TableView<Order> tblOrders = new TableView<>();
        
        setPrivateField(controller, "dpStart", dpStart);
        setPrivateField(controller, "dpEnd", dpEnd);
        setPrivateField(controller, "tblOrders", tblOrders);
        
        when(mockViewModel.getOrders()).thenReturn(Arrays.asList(testOrder1));
        when(mockViewModel.getStatus()).thenReturn("Encontrados: 1");

        // Act
        controller.onSearch();

        // Assert
        verify(mockViewModel, times(1)).setStartDate(null);
        verify(mockViewModel, times(1)).setEndDate(null);
        verify(mockViewModel, times(1)).search();
    }

    @Test
    void onSearch_withEmptyResults_handlesCorrectly() {
        // Arrange
        DatePicker dpStart = new DatePicker(LocalDate.of(2023, 1, 1));
        DatePicker dpEnd = new DatePicker(LocalDate.of(2023, 1, 31));
        TableView<Order> tblOrders = new TableView<>();
        
        setPrivateField(controller, "dpStart", dpStart);
        setPrivateField(controller, "dpEnd", dpEnd);
        setPrivateField(controller, "tblOrders", tblOrders);
        
        when(mockViewModel.getOrders()).thenReturn(Arrays.asList());
        when(mockViewModel.getStatus()).thenReturn("Encontrados: 0");

        // Act
        controller.onSearch();

        // Assert
        verify(mockViewModel, times(1)).search();
        verify(mockViewModel, times(1)).getOrders();
        verify(mockViewModel, times(1)).getStatus();
    }

    @Test
    void onExport_withOrders_callsViewModelExport() {
        // Arrange
        when(mockViewModel.exportCsv()).thenReturn("/tmp/export.csv");
        when(mockViewModel.getStatus()).thenReturn("Exportado para: /tmp/export.csv");

        // Act
        controller.onExport();

        // Assert
        verify(mockViewModel, times(1)).exportCsv();
        verify(mockViewModel, times(1)).getStatus();
    }

    @Test
    void onExport_withNoOrders_handlesCorrectly() {
        // Arrange
        when(mockViewModel.exportCsv()).thenReturn(null);
        when(mockViewModel.getStatus()).thenReturn("Nada para exportar");

        // Act
        controller.onExport();

        // Assert
        verify(mockViewModel, times(1)).exportCsv();
        verify(mockViewModel, times(1)).getStatus();
    }

    @Test
    void onExport_withEmptyPath_handlesCorrectly() {
        // Arrange
        when(mockViewModel.exportCsv()).thenReturn("");
        when(mockViewModel.getStatus()).thenReturn("Nada para exportar");

        // Act
        controller.onExport();

        // Assert
        verify(mockViewModel, times(1)).exportCsv();
        verify(mockViewModel, times(1)).getStatus();
    }

    @Test
    void onSearch_withOnlyStartDate_handlesCorrectly() {
        // Arrange
        DatePicker dpStart = new DatePicker(LocalDate.of(2023, 1, 1));
        DatePicker dpEnd = new DatePicker();
        TableView<Order> tblOrders = new TableView<>();
        
        setPrivateField(controller, "dpStart", dpStart);
        setPrivateField(controller, "dpEnd", dpEnd);
        setPrivateField(controller, "tblOrders", tblOrders);
        
        when(mockViewModel.getOrders()).thenReturn(Arrays.asList(testOrder1));
        when(mockViewModel.getStatus()).thenReturn("Encontrados: 1");

        // Act
        controller.onSearch();

        // Assert
        verify(mockViewModel, times(1)).setStartDate(LocalDate.of(2023, 1, 1));
        verify(mockViewModel, times(1)).setEndDate(null);
        verify(mockViewModel, times(1)).search();
    }

    @Test
    void onSearch_withOnlyEndDate_handlesCorrectly() {
        // Arrange
        DatePicker dpStart = new DatePicker();
        DatePicker dpEnd = new DatePicker(LocalDate.of(2023, 1, 31));
        TableView<Order> tblOrders = new TableView<>();
        
        setPrivateField(controller, "dpStart", dpStart);
        setPrivateField(controller, "dpEnd", dpEnd);
        setPrivateField(controller, "tblOrders", tblOrders);
        
        when(mockViewModel.getOrders()).thenReturn(Arrays.asList(testOrder2));
        when(mockViewModel.getStatus()).thenReturn("Encontrados: 1");

        // Act
        controller.onSearch();

        // Assert
        verify(mockViewModel, times(1)).setStartDate(null);
        verify(mockViewModel, times(1)).setEndDate(LocalDate.of(2023, 1, 31));
        verify(mockViewModel, times(1)).search();
    }

    @Test
    void onSearch_withMultipleOrders_handlesCorrectly() {
        // Arrange
        DatePicker dpStart = new DatePicker(LocalDate.of(2023, 1, 1));
        DatePicker dpEnd = new DatePicker(LocalDate.of(2023, 1, 31));
        TableView<Order> tblOrders = new TableView<>();
        
        setPrivateField(controller, "dpStart", dpStart);
        setPrivateField(controller, "dpEnd", dpEnd);
        setPrivateField(controller, "tblOrders", tblOrders);
        
        List<Order> orders = Arrays.asList(testOrder1, testOrder2);
        when(mockViewModel.getOrders()).thenReturn(orders);
        when(mockViewModel.getStatus()).thenReturn("Encontrados: 2");

        // Act
        controller.onSearch();

        // Assert
        verify(mockViewModel, times(1)).search();
        verify(mockViewModel, times(1)).getOrders();
        verify(mockViewModel, times(1)).getStatus();
    }

    @Test
    void onExport_withValidPath_handlesCorrectly() {
        // Arrange
        String exportPath = "/tmp/export_20230101_20230131.csv";
        when(mockViewModel.exportCsv()).thenReturn(exportPath);
        when(mockViewModel.getStatus()).thenReturn("Exportado para: " + exportPath);

        // Act
        controller.onExport();

        // Assert
        verify(mockViewModel, times(1)).exportCsv();
        verify(mockViewModel, times(1)).getStatus();
    }

    @Test
    void onExport_withException_handlesCorrectly() {
        // Arrange
        when(mockViewModel.exportCsv()).thenThrow(new RuntimeException("Erro de exportação"));
        when(mockViewModel.getStatus()).thenReturn("Erro ao exportar");

        // Act & Assert
        assertDoesNotThrow(() -> {
            controller.onExport();
        });
        
        verify(mockViewModel, times(1)).exportCsv();
        verify(mockViewModel, times(1)).getStatus();
    }

    @Test
    void onSearch_withException_handlesCorrectly() {
        // Arrange
        DatePicker dpStart = new DatePicker(LocalDate.of(2023, 1, 1));
        DatePicker dpEnd = new DatePicker(LocalDate.of(2023, 1, 31));
        TableView<Order> tblOrders = new TableView<>();
        
        setPrivateField(controller, "dpStart", dpStart);
        setPrivateField(controller, "dpEnd", dpEnd);
        setPrivateField(controller, "tblOrders", tblOrders);
        
        doThrow(new RuntimeException("Erro de busca")).when(mockViewModel).search();
        when(mockViewModel.getStatus()).thenReturn("Erro na busca");

        // Act & Assert
        assertDoesNotThrow(() -> {
            controller.onSearch();
        });
        
        verify(mockViewModel, times(1)).search();
        verify(mockViewModel, times(1)).getStatus();
    }

    // Helper method to set private fields using reflection
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            // In a real test environment, you might want to handle this differently
            // For now, we'll just ignore reflection errors
        }
    }

    @Test
    void constructor_withNullViewModel_handlesCorrectly() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            new HistoryWindowController(null);
        });
    }
}