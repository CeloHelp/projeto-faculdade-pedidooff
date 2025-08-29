package com.pedidofacil.services;

import com.pedidofacil.models.Product;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IProductService {
    Product save(Product p);
    List<Product> findAll();
    void delete(Product p);

    // Async versions
    @Async
    CompletableFuture<Product> saveAsync(Product p);

    @Async
    CompletableFuture<List<Product>> findAllAsync();

    @Async
    CompletableFuture<Void> deleteAsync(Product p);
}
