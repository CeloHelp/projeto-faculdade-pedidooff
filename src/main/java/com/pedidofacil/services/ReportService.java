package com.pedidofacil.services;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Order;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.repositories.OrderRepository;
import com.pedidofacil.repositories.projections.DailySalesView;
import com.pedidofacil.repositories.projections.PaymentDistributionView;
import com.pedidofacil.repositories.projections.ProductSalesView;
import com.pedidofacil.repositories.projections.TicketAverageView;
import com.pedidofacil.repositories.projections.TopCustomerView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService implements IReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private final OrderRepository orderRepository;

    public ReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private LocalDateTime startOf(LocalDate d) {
        return d == null ? null : d.atStartOfDay();
    }
    
    private LocalDateTime endOf(LocalDate d) {
        return d == null ? null : d.atTime(LocalTime.MAX);
    }

    @Override
    public List<ProductSalesView> topProducts(LocalDate start, LocalDate end, int limit) {
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
        log.info("Buscando vendas diárias de {} até {}", start, end);
        try {
            // Tenta primeiro a query nativa
            List<DailySalesView> nativeResult = orderRepository.dailySales(startOf(start), endOf(end));
            log.info("Query nativa retornou {} resultados", nativeResult.size());
            if (!nativeResult.isEmpty()) {
                log.debug("Usando resultado da query nativa para vendas diárias");
                return nativeResult;
            }
        } catch (Exception e) {
            log.warn("Query nativa falhou, usando processamento manual: {}", e.getMessage());
        }

        // Fallback: processa manualmente os pedidos
        List<Order> orders = orderRepository.findOrdersInPeriod(startOf(start), endOf(end));
        log.info("Fallback encontrou {} pedidos no período", orders.size());
        
        if (orders.isEmpty()) {
            log.info("Nenhum pedido encontrado no período");
            return Collections.emptyList();
        }

        // Agrupa por data
        Map<LocalDate, BigDecimal> salesByDay = new TreeMap<>();
        
        for (Order order : orders) {
            if (order.getCreatedAt() != null && order.getTotal() != null) {
                LocalDate day = order.getCreatedAt().toLocalDate();
                salesByDay.merge(day, order.getTotal(), BigDecimal::add);
            }
        }

        // Converte para o formato esperado
        List<DailySalesView> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        
        for (Map.Entry<LocalDate, BigDecimal> entry : salesByDay.entrySet()) {
            result.add(new DailySalesViewImpl(
                entry.getKey().format(formatter),
                entry.getValue()
            ));
        }

        log.info("Processamento manual retornou {} dias de vendas", result.size());
        return result;
    }

    // Implementação interna da interface DailySalesView
    private static class DailySalesViewImpl implements DailySalesView {
        private final String day;
        private final BigDecimal total;

        public DailySalesViewImpl(String day, BigDecimal total) {
            this.day = day;
            this.total = total;
        }

        @Override
        public String getDay() {
            return day;
        }

        @Override
        public BigDecimal getTotal() {
            return total;
        }
    }
}
