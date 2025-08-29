package com.pedidofacil.services;

import com.pedidofacil.models.Customer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ICustomerService {
    Customer save(Customer c);
    List<Customer> findAll();
    void delete(Customer c);

    Optional<Customer> findByName(String name);

    CompletableFuture<Customer> saveAsync(Customer c);
    CompletableFuture<List<Customer>> findAllAsync();
    CompletableFuture<Void> deleteAsync(Customer c);
}
