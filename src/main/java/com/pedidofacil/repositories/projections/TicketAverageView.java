package com.pedidofacil.repositories.projections;

import com.pedidofacil.models.PaymentMethod;

public interface TicketAverageView {
    PaymentMethod getPaymentMethod();
    Long getOrders();
    Double getAverage();
}
