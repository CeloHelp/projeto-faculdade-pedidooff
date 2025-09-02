package com.pedidofacil.repositories.projections;

import com.pedidofacil.models.PaymentMethod;
import java.math.BigDecimal;

public interface PaymentDistributionView {
    PaymentMethod getPaymentMethod();
    BigDecimal getTotal();
}
