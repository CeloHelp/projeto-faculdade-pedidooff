package com.pedidofacil.services;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Order;
import com.pedidofacil.models.OrderItem;
import com.pedidofacil.models.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IOrderService {

    Order createOrder(List<OrderItem> items, PaymentMethod paymentMethod, Customer customer);

    Long nextOrderNumber();

    List<Order> findHistory(LocalDateTime start, LocalDateTime end, Customer customer);

    String exportCsv(List<Order> orders);

    // Async versions
    CompletableFuture<Order> createOrderAsync(List<OrderItem> items, PaymentMethod paymentMethod, Customer customer);
    CompletableFuture<Long> nextOrderNumberAsync();
    CompletableFuture<List<Order>> findHistoryAsync(LocalDateTime start, LocalDateTime end, Customer customer);
    CompletableFuture<String> exportCsvAsync(List<Order> orders);
}
