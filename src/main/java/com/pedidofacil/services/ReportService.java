package com.pedidofacil.services;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.repositories.OrderRepository;
import com.pedidofacil.repositories.projections.DailySalesView;
import com.pedidofacil.repositories.projections.PaymentDistributionView;
import com.pedidofacil.repositories.projections.ProductSalesView;
import com.pedidofacil.repositories.projections.TicketAverageView;
import com.pedidofacil.repositories.projections.TopCustomerView;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReportService implements IReportService {

    private final OrderRepository orderRepository;

    public ReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private LocalDateTime startOf(LocalDate d) { return d == null ? null : d.atStartOfDay(); }
    private LocalDateTime endOf(LocalDate d) { return d == null ? null : d.atTime(LocalTime.MAX); }

    @Override
    public List<ProductSalesView> topProducts(LocalDate start, LocalDate end, int limit) {
        // Using native SQL via repository; fallback later if not implemented
        return orderRepository.productSales(startOf(start), endOf(end), PageRequest.of(0, limit));
    }

    @Override
    public List<PaymentDistributionView> paymentDistribution(LocalDate start, LocalDate end) {
        return orderRepository.sumByPaymentMethod(startOf(start), endOf(end));
    }

    @Override
    public List<TicketAverageView> ticketAverage(LocalDate start, LocalDate end) {
        return orderRepository.ticketAverageByPayment(startOf(start), endOf(end));
    }

    @Override
    public List<TopCustomerView> topCustomers(LocalDate start, LocalDate end, PaymentMethod method, int limit) {
        return orderRepository.topCustomers(startOf(start), endOf(end), method, PageRequest.of(0, limit));
    }

    @Override
    public List<DailySalesView> dailySales(LocalDate start, LocalDate end) {
        return orderRepository.dailySales(startOf(start), endOf(end));
    }
}
