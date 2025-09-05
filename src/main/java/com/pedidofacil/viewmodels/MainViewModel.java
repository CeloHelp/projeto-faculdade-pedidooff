package com.pedidofacil.viewmodels;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.OrderItem;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.models.Product;
import com.pedidofacil.services.ICustomerService;
import com.pedidofacil.services.IOrderService;
import com.pedidofacil.services.IProductService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MainViewModel {

    private final IProductService productService;
    private final IOrderService orderService;
    private final ICustomerService customerService;

    // Produtos e busca
    private List<Product> products = new ArrayList<>();
    private String searchQuery = "";

    // Seleção e item atual
    private Product selectedProduct;
    private BigDecimal quantity = BigDecimal.ONE;
    private BigDecimal unitPrice = BigDecimal.ZERO;

    // Itens do pedido
    private List<OrderItemView> items = new ArrayList<>();

    // Pagamento e cliente
    private PaymentMethod paymentMethod = PaymentMethod.CASH;
    private String customerName = "";
    private List<Customer> customers = new ArrayList<>();

    // Totais e status
    private BigDecimal total = BigDecimal.ZERO;
    private String statusMessage = "";

    // Próximo número do pedido
    private Long nextOrderNumber;

    public MainViewModel(IProductService productService, IOrderService orderService, ICustomerService customerService) {
        this.productService = productService;
        this.orderService = orderService;
        this.customerService = customerService;
    }

    public void onLoaded() {
        this.products = productService.findAll();
        this.customers = customerService.findAll();
        refreshNextOrderNumber();
        setStatusMessage("Produtos carregados: " + products.size());
    }

    public Customer createOrFindCustomer(String name, String phone) {
        if (name == null || name.isBlank()) return null;
        Optional<Customer> existing = customerService.findByName(name.trim());
        Customer c = existing.orElseGet(() -> customerService.save(new Customer(name.trim(), phone)));
        // Atualiza cache local
        boolean already = customers.stream().anyMatch(cc -> cc.getId() != null && c.getId() != null && cc.getId().equals(c.getId()));
        if (!already) {
            customers.add(c);
        }
        return c;
    }

    public void updateUnitPriceFromSelected() {
        if (selectedProduct != null) {
            setUnitPrice(selectedProduct.getPrice());
        }
    }

    public void refreshNextOrderNumber() {
        this.nextOrderNumber = orderService.nextOrderNumber();
    }

    public Long getNextOrderNumber() { return nextOrderNumber; }

    public void addItem() {
        if (selectedProduct == null) {
            setStatusMessage("Selecione um produto.");
            return;
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            setStatusMessage("Quantidade inválida.");
            return;
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            setStatusMessage("Preço unitário inválido.");
            return;
        }
        OrderItemView view = new OrderItemView(selectedProduct, quantity, unitPrice);
        items.add(view);
        recalcTotal();
        setStatusMessage("Item adicionado.");
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            recalcTotal();
        }
    }

    public void newOrder() {
        items.clear();
        total = BigDecimal.ZERO;
        paymentMethod = PaymentMethod.CASH;
        customerName = "";
        refreshNextOrderNumber();
        statusMessage = "Novo pedido iniciado.";
    }

    public void finalizeOrder() {
        // Validação: para fiado, cliente é obrigatório
        if (paymentMethod == PaymentMethod.CREDITSALE && (customerName == null || customerName.isBlank())) {
            setStatusMessage("Informe o cliente para venda fiado.");
            return;
        }
        try {
            Customer customer = null;
            // Anexa cliente sempre que houver nome informado (para qualquer forma de pagamento)
            if (customerName != null && !customerName.isBlank()) {
                Optional<Customer> existing = customerService.findByName(customerName.trim());
                customer = existing.orElseGet(() -> customerService.save(new Customer(customerName.trim(), null)));
            }
            // Se fiado e ainda assim não há cliente, aborta
            if (paymentMethod == PaymentMethod.CREDITSALE && customer == null) {
                setStatusMessage("Informe o cliente para venda fiado.");
                return;
            }

            List<OrderItem> entityItems = new ArrayList<>();
            for (OrderItemView v : items) {
                entityItems.add(new OrderItem(v.getProduct(), v.getQuantity(), v.getUnitPrice()));
            }
            var order = orderService.createOrder(entityItems, paymentMethod, customer);
            refreshNextOrderNumber();
            setStatusMessage("Pedido " + order.getNumber() + " salvo. Total: R$ " + total + (customer != null ? ". Cliente: " + customer.getName() : ""));
        } catch (Exception e) {
            setStatusMessage("Falha ao salvar pedido: " + e.getMessage());
        }
    }

    public void printOrSavePdf() {
        // Placeholder de integração de impressão/salvamento PDF
        setStatusMessage("Imprimir/Salvar PDF (não implementado).");
    }

    public void recalcTotal() {
        total = items.stream()
                .map(OrderItemView::getSubtotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(OrderItemView::getSubtotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDiscount() {
        // Implemente a lógica de desconto aqui, se houver
        return BigDecimal.ZERO;
    }

    public BigDecimal getGrandTotal() {
        // Implemente a lógica de total geral (subtotal - desconto) aqui
        return getSubtotal().subtract(getDiscount());
    }

    // Getters/Setters para binding
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    public Product getSelectedProduct() { return selectedProduct; }
    public void setSelectedProduct(Product selectedProduct) {
        this.selectedProduct = selectedProduct;
        updateUnitPriceFromSelected();
    }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public List<OrderItemView> getItems() { return items; }
    public void setItems(List<OrderItemView> items) { this.items = items; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getStatusMessage() {
        return statusMessage != null ? statusMessage : "";
    }

    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public List<Customer> getCustomers() { return customers; }

    public void setCustomers(List<Customer> customers) { this.customers = customers; }
}
