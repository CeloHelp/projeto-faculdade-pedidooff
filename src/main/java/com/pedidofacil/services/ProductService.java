package com.pedidofacil.services;

import com.pedidofacil.models.Product;
import com.pedidofacil.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Product save(Product p) {
        return repository.save(p);
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Transactional
    public void delete(Product p) {
        repository.delete(p);
    }
}
