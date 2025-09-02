package com.pedidofacil.services;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.repositories.projections.DailySalesView;
import com.pedidofacil.repositories.projections.PaymentDistributionView;
import com.pedidofacil.repositories.projections.ProductSalesView;
import com.pedidofacil.repositories.projections.TicketAverageView;
import com.pedidofacil.repositories.projections.TopCustomerView;

import java.time.LocalDate;
import java.util.List;

public interface IReportService {
    List<ProductSalesView> topProducts(LocalDate start, LocalDate end, int limit);
    List<PaymentDistributionView> paymentDistribution(LocalDate start, LocalDate end);
    List<TicketAverageView> ticketAverage(LocalDate start, LocalDate end);
    List<TopCustomerView> topCustomers(LocalDate start, LocalDate end, PaymentMethod method, int limit);
    List<DailySalesView> dailySales(LocalDate start, LocalDate end);
}
