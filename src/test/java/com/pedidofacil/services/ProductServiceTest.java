package com.pedidofacil.services;

import com.pedidofacil.models.Product;
import com.pedidofacil.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save() {
        Product product = new Product("Test Product", "Brand A", "Unit A", new BigDecimal("10.00"));
        when(productRepository.save(product)).thenReturn(product);

        Product savedProduct = productService.save(product);

        assertNotNull(savedProduct);
        assertEquals("Test Product", savedProduct.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void findAll() {
        Product product1 = new Product("Product 1", "Brand B", "Unit B", new BigDecimal("20.00"));
        Product product2 = new Product("Product 2", "Brand C", "Unit C", new BigDecimal("30.00"));
        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> foundProducts = productService.findAll();

        assertNotNull(foundProducts);
        assertEquals(2, foundProducts.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void delete() {
        Product product = new Product("Product to Delete", "Brand D", "Unit D", new BigDecimal("40.00"));
        doNothing().when(productRepository).delete(product);

        productService.delete(product);

        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void saveAsync() throws Exception {
        Product product = new Product("Async Product", "Brand E", "Unit E", new BigDecimal("50.00"));
        when(productRepository.save(product)).thenReturn(product);

        CompletableFuture<Product> future = productService.saveAsync(product);
        Product savedProduct = future.get();

        assertNotNull(savedProduct);
        assertEquals("Async Product", savedProduct.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void findAllAsync() throws Exception {
        Product product1 = new Product("Async Product 1", "Brand F", "Unit F", new BigDecimal("60.00"));
        Product product2 = new Product("Async Product 2", "Brand G", "Unit G", new BigDecimal("70.00"));
        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);

        CompletableFuture<List<Product>> future = productService.findAllAsync();
        List<Product> foundProducts = future.get();

        assertNotNull(foundProducts);
        assertEquals(2, foundProducts.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void deleteAsync() throws Exception {
        Product product = new Product("Async Product to Delete", "Brand H", "Unit H", new BigDecimal("80.00"));
        doNothing().when(productRepository).delete(product);

        CompletableFuture<Void> future = productService.deleteAsync(product);
        future.get();

        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void save_withNullProduct_throwsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            productService.save(null);
        });
    }

    @Test
    void delete_withNullProduct_throwsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            productService.delete(null);
        });
    }

    @Test
    void saveAsync_withException_handlesCorrectly() throws Exception {
        // Arrange
        Product product = new Product("Async Product", "Brand E", "Unit E", new BigDecimal("50.00"));
        when(productRepository.save(product)).thenThrow(new RuntimeException("Database error"));

        // Act
        CompletableFuture<Product> future = productService.saveAsync(product);

        // Assert
        assertThrows(Exception.class, () -> {
            future.get();
        });
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void findAllAsync_withException_handlesCorrectly() throws Exception {
        // Arrange
        when(productRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        CompletableFuture<List<Product>> future = productService.findAllAsync();

        // Assert
        assertThrows(Exception.class, () -> {
            future.get();
        });
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void deleteAsync_withException_handlesCorrectly() throws Exception {
        // Arrange
        Product product = new Product("Async Product to Delete", "Brand H", "Unit H", new BigDecimal("80.00"));
        doThrow(new RuntimeException("Database error")).when(productRepository).delete(product);

        // Act
        CompletableFuture<Void> future = productService.deleteAsync(product);

        // Assert
        assertThrows(Exception.class, () -> {
            future.get();
        });
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void save_withSpecialCharacters_handlesCorrectly() {
        Product product = new Product("Produto & Cia Ltda.", "Marca® 2023", "UN", new BigDecimal("99.99"));
        when(productRepository.save(product)).thenReturn(product);

        Product savedProduct = productService.save(product);

        assertNotNull(savedProduct);
        assertEquals("Produto & Cia Ltda.", savedProduct.getName());
        assertEquals("Marca® 2023", savedProduct.getBrand());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void findAll_withEmptyList_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        List<Product> foundProducts = productService.findAll();

        assertNotNull(foundProducts);
        assertTrue(foundProducts.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void save_withZeroPrice_handlesCorrectly() {
        Product product = new Product("Produto Grátis", "Marca A", "UN", BigDecimal.ZERO);
        when(productRepository.save(product)).thenReturn(product);

        Product savedProduct = productService.save(product);

        assertNotNull(savedProduct);
        assertEquals(BigDecimal.ZERO, savedProduct.getPrice());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void save_withNegativePrice_handlesCorrectly() {
        Product product = new Product("Produto com Desconto", "Marca B", "UN", new BigDecimal("-10.00"));
        when(productRepository.save(product)).thenReturn(product);

        Product savedProduct = productService.save(product);

        assertNotNull(savedProduct);
        assertEquals(new BigDecimal("-10.00"), savedProduct.getPrice());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void save_withHighPrecisionPrice_handlesCorrectly() {
        Product product = new Product("Produto Preciso", "Marca C", "UN", new BigDecimal("123.456789"));
        when(productRepository.save(product)).thenReturn(product);

        Product savedProduct = productService.save(product);

        assertNotNull(savedProduct);
        assertEquals(new BigDecimal("123.456789"), savedProduct.getPrice());
        verify(productRepository, times(1)).save(product);
    }
}