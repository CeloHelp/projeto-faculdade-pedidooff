package com.pedidofacil.services;

import com.pedidofacil.models.Product;
import com.pedidofacil.repositories.ProductRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductService implements IProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Product save(Product p) {
        return repository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public void delete(Product p) {
        repository.delete(p);
    }

    @Override
    @Async
    public CompletableFuture<Product> saveAsync(Product p) {
        return CompletableFuture.completedFuture(save(p));
    }

    @Override
    @Async
    public CompletableFuture<List<Product>> findAllAsync() {
        return CompletableFuture.completedFuture(findAll());
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteAsync(Product p) {
        delete(p);
        return CompletableFuture.completedFuture(null);
    }
}
