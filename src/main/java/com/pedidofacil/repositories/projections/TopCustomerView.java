package com.pedidofacil.repositories.projections;

import java.math.BigDecimal;

public interface TopCustomerView {
    Long getCustomerId();
    String getCustomerName();
    BigDecimal getTotal();
}
