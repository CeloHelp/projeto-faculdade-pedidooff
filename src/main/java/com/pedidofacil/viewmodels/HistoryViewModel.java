package com.pedidofacil.viewmodels;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Order;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.services.IOrderService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class HistoryViewModel {

    private final IOrderService orderService;

    public HistoryViewModel(IOrderService orderService) {
        this.orderService = orderService;
    }

    private LocalDate startDate;
    private LocalDate endDate;
    private Customer customer;

    private List<Order> orders = new ArrayList<>();
    private String status = "";

    public void search() {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        orders = orderService.findHistory(start, end, customer);
        status = "Encontrados: " + orders.size();
    }

    public String exportCsv() {
        if (orders == null || orders.isEmpty()) {
            status = "Nada para exportar";
            return null;
        }
        String path = orderService.exportCsv(orders);
        status = "Exportado para: " + path;
        return path;
    }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
