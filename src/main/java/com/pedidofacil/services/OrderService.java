package com.pedidofacil.services;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Order;
import com.pedidofacil.models.OrderItem;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OrderService implements IOrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    @Value("${user.home}")
    private String userHome;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public Order createOrder(List<OrderItem> items, PaymentMethod paymentMethod, Customer customer) {
        if (paymentMethod == PaymentMethod.CREDITSALE && customer == null) {
            throw new IllegalArgumentException("Cliente é obrigatório para venda a prazo (Fiado)");
        }
        Order order = new Order();
        order.setNumber(nextOrderNumber());
        order.setPaymentMethod(paymentMethod);
        order.setCustomer(customer);
        if (items != null) {
            for (OrderItem it : items) {
                order.addItem(it);
            }
        }
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Long nextOrderNumber() {
        Long max = orderRepository.findMaxNumber();
        return (max == null ? 0L : max) + 1L;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findHistory(LocalDateTime start, LocalDateTime end, Customer customer) {
        return orderRepository.findHistory(start, end, customer);
    }

    @Override
    public String exportCsv(List<Order> orders) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path dir = Path.of(userHome, "Documents", "PedidoFacil");
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve("export_" + date + ".csv");
            StringBuilder sb = new StringBuilder();
            sb.append("Number;Date;Payment;Customer;Total\n");
            if (orders != null) {
                for (Order o : orders) {
                    String cust = o.getCustomer() != null ? o.getCustomer().getName() : "";
                    sb.append(o.getNumber()).append(';')
                      .append(o.getCreatedAt()).append(';')
                      .append(o.getPaymentMethod()).append(';')
                      .append(cust).append(';')
                      .append(o.getTotal()).append('\n');
                }
            }
            Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Erro exportando CSV", e);
            throw new RuntimeException("Falha ao exportar CSV", e);
        }
    }

    // Async wrappers
    @Override
    @Async
    public CompletableFuture<Order> createOrderAsync(List<OrderItem> items, PaymentMethod paymentMethod, Customer customer) {
        return CompletableFuture.supplyAsync(() -> createOrder(items, paymentMethod, customer));
    }

    @Override
    @Async
    public CompletableFuture<Long> nextOrderNumberAsync() {
        return CompletableFuture.supplyAsync(this::nextOrderNumber);
    }

    @Override
    @Async
    public CompletableFuture<List<Order>> findHistoryAsync(LocalDateTime start, LocalDateTime end, Customer customer) {
        return CompletableFuture.supplyAsync(() -> findHistory(start, end, customer));
    }

    @Override
    @Async
    public CompletableFuture<String> exportCsvAsync(List<Order> orders) {
        return CompletableFuture.supplyAsync(() -> exportCsv(orders));
    }
}
