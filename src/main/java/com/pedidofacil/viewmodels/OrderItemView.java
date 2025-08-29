package com.pedidofacil.viewmodels;

import com.pedidofacil.models.Product;

import java.math.BigDecimal;

public class OrderItemView {
    private Product product;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public OrderItemView() {}

    public OrderItemView(Product product, BigDecimal quantity, BigDecimal unitPrice) {
        this.product = product;
        this.productName = product != null ? product.getName() : null;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        recalc();
    }

    public void recalc() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(quantity);
        }
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; this.productName = product != null ? product.getName() : null; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; recalc(); }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; recalc(); }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
