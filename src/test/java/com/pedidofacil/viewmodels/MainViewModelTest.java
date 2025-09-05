package com.pedidofacil.viewmodels;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Order;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.models.Product;
import com.pedidofacil.services.ICustomerService;
import com.pedidofacil.services.IOrderService;
import com.pedidofacil.services.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainViewModelTest {

    @Mock
    private IProductService productService;

    @Mock
    private IOrderService orderService;

    @Mock
    private ICustomerService customerService;

    @InjectMocks
    private MainViewModel mainViewModel;

    private Product testProduct;
    private Customer testCustomer;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testProduct = new Product("Produto Teste", "Marca A", "UN", new BigDecimal("10.00"));
        testCustomer = new Customer("Cliente Teste", "123456789");
        testOrder = new Order();
        testOrder.setNumber(1L);
    }

    @Test
    void onLoaded_loadsProductsAndCustomers() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        List<Customer> customers = Arrays.asList(testCustomer);
        when(productService.findAll()).thenReturn(products);
        when(customerService.findAll()).thenReturn(customers);
        when(orderService.nextOrderNumber()).thenReturn(1L);

        // Act
        mainViewModel.onLoaded();

        // Assert
        assertEquals(1, mainViewModel.getProducts().size());
        assertEquals(1, mainViewModel.getCustomers().size());
        assertEquals(1L, mainViewModel.getNextOrderNumber());
        assertEquals("Produtos carregados: 1", mainViewModel.getStatusMessage());
        verify(productService, times(1)).findAll();
        verify(customerService, times(1)).findAll();
        verify(orderService, times(1)).nextOrderNumber();
    }

    @Test
    void createOrFindCustomer_withExistingCustomer_returnsExisting() {
        // Arrange
        String customerName = "Cliente Existente";
        when(customerService.findByName(customerName)).thenReturn(Optional.of(testCustomer));

        // Act
        Customer result = mainViewModel.createOrFindCustomer(customerName, "123");

        // Assert
        assertEquals(testCustomer, result);
        verify(customerService, times(1)).findByName(customerName);
        verify(customerService, never()).save(any(Customer.class));
    }

    @Test
    void createOrFindCustomer_withNewCustomer_createsAndReturnsNew() {
        // Arrange
        String customerName = "Novo Cliente";
        when(customerService.findByName(customerName)).thenReturn(Optional.empty());
        when(customerService.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        Customer result = mainViewModel.createOrFindCustomer(customerName, "123");

        // Assert
        assertEquals(testCustomer, result);
        verify(customerService, times(1)).findByName(customerName);
        verify(customerService, times(1)).save(any(Customer.class));
    }

    @Test
    void createOrFindCustomer_withBlankName_returnsNull() {
        // Act
        Customer result = mainViewModel.createOrFindCustomer("", "123");

        // Assert
        assertNull(result);
        verify(customerService, never()).findByName(anyString());
        verify(customerService, never()).save(any(Customer.class));
    }

    @Test
    void addItem_withValidData_addsItem() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));

        // Act
        mainViewModel.addItem();

        // Assert
        assertEquals(1, mainViewModel.getItems().size());
        assertEquals("Item adicionado.", mainViewModel.getStatusMessage());
        assertEquals(new BigDecimal("20.00"), mainViewModel.getTotal());
    }

    @Test
    void addItem_withNoSelectedProduct_showsError() {
        // Arrange
        mainViewModel.setSelectedProduct(null);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));

        // Act
        mainViewModel.addItem();

        // Assert
        assertEquals(0, mainViewModel.getItems().size());
        assertEquals("Selecione um produto.", mainViewModel.getStatusMessage());
    }

    @Test
    void addItem_withInvalidQuantity_showsError() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("-1"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));

        // Act
        mainViewModel.addItem();

        // Assert
        assertEquals(0, mainViewModel.getItems().size());
        assertEquals("Quantidade inválida.", mainViewModel.getStatusMessage());
    }

    @Test
    void addItem_withInvalidUnitPrice_showsError() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("-5.00"));

        // Act
        mainViewModel.addItem();

        // Assert
        assertEquals(0, mainViewModel.getItems().size());
        assertEquals("Preço unitário inválido.", mainViewModel.getStatusMessage());
    }

    @Test
    void removeItem_withValidIndex_removesItem() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));
        mainViewModel.addItem();

        // Act
        mainViewModel.removeItem(0);

        // Assert
        assertEquals(0, mainViewModel.getItems().size());
        assertEquals(BigDecimal.ZERO, mainViewModel.getTotal());
    }

    @Test
    void removeItem_withInvalidIndex_doesNothing() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));
        mainViewModel.addItem();

        // Act
        mainViewModel.removeItem(5); // Índice inválido

        // Assert
        assertEquals(1, mainViewModel.getItems().size());
    }

    @Test
    void newOrder_clearsAllData() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));
        mainViewModel.addItem();
        mainViewModel.setPaymentMethod(PaymentMethod.CREDITSALE);
        mainViewModel.setCustomerName("Cliente Teste");
        when(orderService.nextOrderNumber()).thenReturn(2L);

        // Act
        mainViewModel.newOrder();

        // Assert
        assertEquals(0, mainViewModel.getItems().size());
        assertEquals(BigDecimal.ZERO, mainViewModel.getTotal());
        assertEquals(PaymentMethod.CASH, mainViewModel.getPaymentMethod());
        assertEquals("", mainViewModel.getCustomerName());
        assertEquals("Novo pedido iniciado.", mainViewModel.getStatusMessage());
        verify(orderService, times(1)).nextOrderNumber();
    }

    @Test
    void finalizeOrder_withCashPayment_savesOrder() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));
        mainViewModel.addItem();
        mainViewModel.setPaymentMethod(PaymentMethod.CASH);
        mainViewModel.setCustomerName("Cliente Teste");

        when(customerService.findByName("Cliente Teste")).thenReturn(Optional.of(testCustomer));
        when(orderService.createOrder(anyList(), eq(PaymentMethod.CASH), eq(testCustomer))).thenReturn(testOrder);
        when(orderService.nextOrderNumber()).thenReturn(2L);

        // Act
        mainViewModel.finalizeOrder();

        // Assert
        assertTrue(mainViewModel.getStatusMessage().contains("Pedido 1 salvo"));
        verify(orderService, times(1)).createOrder(anyList(), eq(PaymentMethod.CASH), eq(testCustomer));
    }

    @Test
    void finalizeOrder_withCreditSaleAndNoCustomer_showsError() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));
        mainViewModel.addItem();
        mainViewModel.setPaymentMethod(PaymentMethod.CREDITSALE);
        mainViewModel.setCustomerName("");

        // Act
        mainViewModel.finalizeOrder();

        // Assert
        assertEquals("Informe o cliente para venda fiado.", mainViewModel.getStatusMessage());
        verify(orderService, never()).createOrder(anyList(), any(PaymentMethod.class), any(Customer.class));
    }

    @Test
    void finalizeOrder_withCreditSaleAndCustomer_savesOrder() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));
        mainViewModel.addItem();
        mainViewModel.setPaymentMethod(PaymentMethod.CREDITSALE);
        mainViewModel.setCustomerName("Cliente Teste");

        when(customerService.findByName("Cliente Teste")).thenReturn(Optional.of(testCustomer));
        when(orderService.createOrder(anyList(), eq(PaymentMethod.CREDITSALE), eq(testCustomer))).thenReturn(testOrder);
        when(orderService.nextOrderNumber()).thenReturn(2L);

        // Act
        mainViewModel.finalizeOrder();

        // Assert
        assertTrue(mainViewModel.getStatusMessage().contains("Pedido 1 salvo"));
        verify(orderService, times(1)).createOrder(anyList(), eq(PaymentMethod.CREDITSALE), eq(testCustomer));
    }

    @Test
    void finalizeOrder_withException_showsError() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));
        mainViewModel.addItem();
        mainViewModel.setPaymentMethod(PaymentMethod.CASH);

        when(orderService.createOrder(anyList(), any(PaymentMethod.class), any(Customer.class)))
                .thenThrow(new RuntimeException("Erro de banco"));

        // Act
        mainViewModel.finalizeOrder();

        // Assert
        assertTrue(mainViewModel.getStatusMessage().contains("Falha ao salvar pedido"));
        verify(orderService, times(1)).createOrder(anyList(), any(PaymentMethod.class), isNull());
    }

    @Test
    void printOrSavePdf_showsNotImplementedMessage() {
        // Act
        mainViewModel.printOrSavePdf();

        // Assert
        assertEquals("Imprimir/Salvar PDF (não implementado).", mainViewModel.getStatusMessage());
    }

    @Test
    void setSelectedProduct_updatesUnitPrice() {
        // Act
        mainViewModel.setSelectedProduct(testProduct);

        // Assert
        assertEquals(testProduct.getPrice(), mainViewModel.getUnitPrice());
    }

    @Test
    void recalcTotal_calculatesCorrectTotal() {
        // Arrange
        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("2"));
        mainViewModel.setUnitPrice(new BigDecimal("10.00"));
        mainViewModel.addItem();

        mainViewModel.setSelectedProduct(testProduct);
        mainViewModel.setQuantity(new BigDecimal("3"));
        mainViewModel.setUnitPrice(new BigDecimal("5.00"));
        mainViewModel.addItem();

        // Act
        mainViewModel.recalcTotal();

        // Assert
        assertEquals(new BigDecimal("35.00"), mainViewModel.getTotal());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        // Test setters and getters
        mainViewModel.setSearchQuery("test query");
        assertEquals("test query", mainViewModel.getSearchQuery());

        mainViewModel.setProducts(Arrays.asList(testProduct));
        assertEquals(1, mainViewModel.getProducts().size());

        mainViewModel.setItems(Arrays.asList(new OrderItemView(testProduct, BigDecimal.ONE, BigDecimal.TEN)));
        assertEquals(1, mainViewModel.getItems().size());

        mainViewModel.setPaymentMethod(PaymentMethod.PIX);
        assertEquals(PaymentMethod.PIX, mainViewModel.getPaymentMethod());

        mainViewModel.setCustomerName("Test Customer");
        assertEquals("Test Customer", mainViewModel.getCustomerName());

        mainViewModel.setTotal(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), mainViewModel.getTotal());

        mainViewModel.setCustomers(Arrays.asList(testCustomer));
        assertEquals(1, mainViewModel.getCustomers().size());
    }
}