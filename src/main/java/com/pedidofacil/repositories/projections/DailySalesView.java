package com.pedidofacil.repositories.projections;

import java.math.BigDecimal;

public interface DailySalesView {
    String getDay();
    BigDecimal getTotal();
}
