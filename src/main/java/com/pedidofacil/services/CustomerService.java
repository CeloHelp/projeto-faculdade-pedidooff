package com.pedidofacil.services;

import com.pedidofacil.models.Customer;
import com.pedidofacil.repositories.CustomerRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class CustomerService implements ICustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Customer save(Customer c) {
        return repository.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public void delete(Customer c) {
        repository.delete(c);
    }

    @Override
    public Optional<Customer> findByName(String name) {
        return repository.findByName(name);
    }

    @Override
    @Async
    public CompletableFuture<Customer> saveAsync(Customer c) {
        return CompletableFuture.supplyAsync(() -> save(c));
    }

    @Override
    @Async
    public CompletableFuture<List<Customer>> findAllAsync() {
        return CompletableFuture.supplyAsync(this::findAll);
    }

    @Override
    @Async
    public CompletableFuture<Void> deleteAsync(Customer c) {
        return CompletableFuture.runAsync(() -> delete(c));
    }
}
