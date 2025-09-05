package com.pedidofacil.viewmodels;

import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.repositories.projections.DailySalesView;
import com.pedidofacil.repositories.projections.PaymentDistributionView;
import com.pedidofacil.repositories.projections.ProductSalesView;
import com.pedidofacil.repositories.projections.TicketAverageView;
import com.pedidofacil.repositories.projections.TopCustomerView;
import com.pedidofacil.services.IReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportsViewModelTest {

    @Mock
    private IReportService reportService;

    @InjectMocks
    private ReportsViewModel reportsViewModel;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2023, 1, 1);
        endDate = LocalDate.of(2023, 1, 31);
    }

    @Test
    void refreshAll_loadsAllReportData() {
        // Arrange
        ProductSalesView mockProductSales = mock(ProductSalesView.class);
        PaymentDistributionView mockPaymentDist = mock(PaymentDistributionView.class);
        TicketAverageView mockTicketAvg = mock(TicketAverageView.class);
        TopCustomerView mockTopCustomer = mock(TopCustomerView.class);
        DailySalesView mockDailySales = mock(DailySalesView.class);

        when(reportService.topProducts(startDate, endDate, 10))
                .thenReturn(Arrays.asList(mockProductSales));
        when(reportService.paymentDistribution(startDate, endDate))
                .thenReturn(Arrays.asList(mockPaymentDist));
        when(reportService.ticketAverage(startDate, endDate))
                .thenReturn(Arrays.asList(mockTicketAvg));
        when(reportService.topCustomers(startDate, endDate, null, 10))
                .thenReturn(Arrays.asList(mockTopCustomer));
        when(reportService.dailySales(startDate, endDate))
                .thenReturn(Arrays.asList(mockDailySales));

        reportsViewModel.setStartDate(startDate);
        reportsViewModel.setEndDate(endDate);

        // Act
        reportsViewModel.refreshAll();

        // Assert
        assertEquals(1, reportsViewModel.getProductSales().size());
        assertEquals(1, reportsViewModel.getPaymentDistribution().size());
        assertEquals(1, reportsViewModel.getTicketAverages().size());
        assertEquals(1, reportsViewModel.getTopCustomers().size());
        assertEquals(1, reportsViewModel.getDailySales().size());

        verify(reportService, times(1)).topProducts(startDate, endDate, 10);
        verify(reportService, times(1)).paymentDistribution(startDate, endDate);
        verify(reportService, times(1)).ticketAverage(startDate, endDate);
        verify(reportService, times(1)).topCustomers(startDate, endDate, null, 10);
        verify(reportService, times(1)).dailySales(startDate, endDate);
    }

    @Test
    void refreshAll_withNullDates_handlesCorrectly() {
        // Arrange
        when(reportService.topProducts(eq(null), eq(null), eq(10)))
                .thenReturn(Collections.emptyList());
        when(reportService.paymentDistribution(eq(null), eq(null)))
                .thenReturn(Collections.emptyList());
        when(reportService.ticketAverage(eq(null), eq(null)))
                .thenReturn(Collections.emptyList());
        when(reportService.topCustomers(eq(null), eq(null), eq(null), eq(10)))
                .thenReturn(Collections.emptyList());
        when(reportService.dailySales(eq(null), eq(null)))
                .thenReturn(Collections.emptyList());

        reportsViewModel.setStartDate(null);
        reportsViewModel.setEndDate(null);

        // Act
        reportsViewModel.refreshAll();

        // Assert
        assertTrue(reportsViewModel.getProductSales().isEmpty());
        assertTrue(reportsViewModel.getPaymentDistribution().isEmpty());
        assertTrue(reportsViewModel.getTicketAverages().isEmpty());
        assertTrue(reportsViewModel.getTopCustomers().isEmpty());
        assertTrue(reportsViewModel.getDailySales().isEmpty());

        verify(reportService, times(1)).topProducts(eq(null), eq(null), eq(10));
        verify(reportService, times(1)).paymentDistribution(eq(null), eq(null));
        verify(reportService, times(1)).ticketAverage(eq(null), eq(null));
        verify(reportService, times(1)).topCustomers(eq(null), eq(null), eq(null), eq(10));
        verify(reportService, times(1)).dailySales(eq(null), eq(null));
    }

    @Test
    void refreshTopCustomersFiado_loadsCreditSaleCustomers() {
        // Arrange
        TopCustomerView mockTopCustomer = mock(TopCustomerView.class);
        when(reportService.topCustomers(startDate, endDate, PaymentMethod.CREDITSALE, 10))
                .thenReturn(Arrays.asList(mockTopCustomer));

        reportsViewModel.setStartDate(startDate);
        reportsViewModel.setEndDate(endDate);

        // Act
        reportsViewModel.refreshTopCustomersFiado();

        // Assert
        assertEquals(1, reportsViewModel.getTopCustomers().size());
        verify(reportService, times(1)).topCustomers(startDate, endDate, PaymentMethod.CREDITSALE, 10);
    }

    @Test
    void refreshTopCustomersFiado_withNullDates_handlesCorrectly() {
        // Arrange
        when(reportService.topCustomers(eq(null), eq(null), eq(PaymentMethod.CREDITSALE), eq(10)))
                .thenReturn(Collections.emptyList());

        reportsViewModel.setStartDate(null);
        reportsViewModel.setEndDate(null);

        // Act
        reportsViewModel.refreshTopCustomersFiado();

        // Assert
        assertTrue(reportsViewModel.getTopCustomers().isEmpty());
        verify(reportService, times(1)).topCustomers(eq(null), eq(null), eq(PaymentMethod.CREDITSALE), eq(10));
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        // Test setters and getters
        reportsViewModel.setStartDate(startDate);
        assertEquals(startDate, reportsViewModel.getStartDate());

        reportsViewModel.setEndDate(endDate);
        assertEquals(endDate, reportsViewModel.getEndDate());

        // Test initial empty state
        assertTrue(reportsViewModel.getProductSales().isEmpty());
        assertTrue(reportsViewModel.getPaymentDistribution().isEmpty());
        assertTrue(reportsViewModel.getTicketAverages().isEmpty());
        assertTrue(reportsViewModel.getTopCustomers().isEmpty());
        assertTrue(reportsViewModel.getDailySales().isEmpty());
    }

    @Test
    void refreshAll_withEmptyResults_handlesCorrectly() {
        // Arrange
        when(reportService.topProducts(startDate, endDate, 10))
                .thenReturn(Collections.emptyList());
        when(reportService.paymentDistribution(startDate, endDate))
                .thenReturn(Collections.emptyList());
        when(reportService.ticketAverage(startDate, endDate))
                .thenReturn(Collections.emptyList());
        when(reportService.topCustomers(startDate, endDate, null, 10))
                .thenReturn(Collections.emptyList());
        when(reportService.dailySales(startDate, endDate))
                .thenReturn(Collections.emptyList());

        reportsViewModel.setStartDate(startDate);
        reportsViewModel.setEndDate(endDate);

        // Act
        reportsViewModel.refreshAll();

        // Assert
        assertTrue(reportsViewModel.getProductSales().isEmpty());
        assertTrue(reportsViewModel.getPaymentDistribution().isEmpty());
        assertTrue(reportsViewModel.getTicketAverages().isEmpty());
        assertTrue(reportsViewModel.getTopCustomers().isEmpty());
        assertTrue(reportsViewModel.getDailySales().isEmpty());
    }

    @Test
    void refreshAll_withMixedResults_handlesCorrectly() {
        // Arrange
        ProductSalesView mockProductSales = mock(ProductSalesView.class);
        DailySalesView mockDailySales = mock(DailySalesView.class);

        when(reportService.topProducts(startDate, endDate, 10))
                .thenReturn(Arrays.asList(mockProductSales));
        when(reportService.paymentDistribution(startDate, endDate))
                .thenReturn(Collections.emptyList());
        when(reportService.ticketAverage(startDate, endDate))
                .thenReturn(Collections.emptyList());
        when(reportService.topCustomers(startDate, endDate, null, 10))
                .thenReturn(Collections.emptyList());
        when(reportService.dailySales(startDate, endDate))
                .thenReturn(Arrays.asList(mockDailySales));

        reportsViewModel.setStartDate(startDate);
        reportsViewModel.setEndDate(endDate);

        // Act
        reportsViewModel.refreshAll();

        // Assert
        assertEquals(1, reportsViewModel.getProductSales().size());
        assertTrue(reportsViewModel.getPaymentDistribution().isEmpty());
        assertTrue(reportsViewModel.getTicketAverages().isEmpty());
        assertTrue(reportsViewModel.getTopCustomers().isEmpty());
        assertEquals(1, reportsViewModel.getDailySales().size());
    }
}