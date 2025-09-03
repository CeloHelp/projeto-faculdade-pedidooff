package com.pedidofacil.viewmodels;

import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.repositories.projections.DailySalesView;
import com.pedidofacil.repositories.projections.PaymentDistributionView;
import com.pedidofacil.repositories.projections.ProductSalesView;
import com.pedidofacil.repositories.projections.TicketAverageView;
import com.pedidofacil.repositories.projections.TopCustomerView;
import com.pedidofacil.services.IReportService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Component
public class ReportsViewModel {

    private final IReportService reportService;

    private LocalDate startDate;
    private LocalDate endDate;

    private List<ProductSalesView> productSales = Collections.emptyList();
    private List<PaymentDistributionView> paymentDistribution = Collections.emptyList();
    private List<TicketAverageView> ticketAverages = Collections.emptyList();
    private List<TopCustomerView> topCustomers = Collections.emptyList();
    private List<DailySalesView> dailySales = Collections.emptyList();

    public ReportsViewModel(IReportService reportService) {
        this.reportService = reportService;
    }

    public void refreshAll() {
        this.productSales = reportService.topProducts(startDate, endDate, 10);
        this.paymentDistribution = reportService.paymentDistribution(startDate, endDate);
        this.ticketAverages = reportService.ticketAverage(startDate, endDate);
        this.topCustomers = reportService.topCustomers(startDate, endDate, null, 10);
        this.dailySales = reportService.dailySales(startDate, endDate);
    }

    public void refreshTopCustomersFiado() {
        this.topCustomers = reportService.topCustomers(startDate, endDate, PaymentMethod.CREDITSALE, 10);
    }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public List<ProductSalesView> getProductSales() { return productSales; }
    public List<PaymentDistributionView> getPaymentDistribution() { return paymentDistribution; }
    public List<TicketAverageView> getTicketAverages() { return ticketAverages; }
    public List<TopCustomerView> getTopCustomers() { return topCustomers; }
    public List<DailySalesView> getDailySales() { return dailySales; }
}
