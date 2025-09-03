package com.pedidofacil.repositories.projections;

import java.math.BigDecimal;

public interface ProductSalesView {
    Long getProductId();
    String getProductName();
    BigDecimal getQuantity();
    BigDecimal getTotal();
}
