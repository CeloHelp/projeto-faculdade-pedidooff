package com.pedidofacil.services;

import com.pedidofacil.models.Customer;
import com.pedidofacil.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save() {
        Customer customer = new Customer("Test Customer", "123456789");
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer savedCustomer = customerService.save(customer);

        assertNotNull(savedCustomer);
        assertEquals("Test Customer", savedCustomer.getName());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void findAll() {
        Customer customer1 = new Customer("Customer 1", "111");
        Customer customer2 = new Customer("Customer 2", "222");
        List<Customer> customers = Arrays.asList(customer1, customer2);
        when(customerRepository.findAll()).thenReturn(customers);

        List<Customer> foundCustomers = customerService.findAll();

        assertNotNull(foundCustomers);
        assertEquals(2, foundCustomers.size());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void delete() {
        Customer customer = new Customer("Test Customer", "123456789");
        doNothing().when(customerRepository).delete(customer);

        customerService.delete(customer);

        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    void findByNameFound() {
        Customer customer = new Customer("Test Customer", "123456789");
        when(customerRepository.findByName("Test Customer")).thenReturn(Optional.of(customer));

        Optional<Customer> foundCustomer = customerService.findByName("Test Customer");

        assertTrue(foundCustomer.isPresent());
        assertEquals("Test Customer", foundCustomer.get().getName());
        verify(customerRepository, times(1)).findByName("Test Customer");
    }

    @Test
    void findByNameNotFound() {
        when(customerRepository.findByName("NonExistent Customer")).thenReturn(Optional.empty());

        Optional<Customer> foundCustomer = customerService.findByName("NonExistent Customer");

        assertFalse(foundCustomer.isPresent());
        verify(customerRepository, times(1)).findByName("NonExistent Customer");
    }

    @Test
    void saveAsync() throws Exception {
        Customer customer = new Customer("Async Customer", "987654321");
        when(customerRepository.save(customer)).thenReturn(customer);

        CompletableFuture<Customer> future = customerService.saveAsync(customer);
        Customer savedCustomer = future.get(); // Wait for the async operation to complete

        assertNotNull(savedCustomer);
        assertEquals("Async Customer", savedCustomer.getName());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void findAllAsync() throws Exception {
        Customer customer1 = new Customer("Async Customer 1", "111");
        Customer customer2 = new Customer("Async Customer 2", "222");
        List<Customer> customers = Arrays.asList(customer1, customer2);
        when(customerRepository.findAll()).thenReturn(customers);

        CompletableFuture<List<Customer>> future = customerService.findAllAsync();
        List<Customer> foundCustomers = future.get(); // Wait for the async operation to complete

        assertNotNull(foundCustomers);
        assertEquals(2, foundCustomers.size());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void deleteAsync() throws Exception {
        Customer customer = new Customer("Async Customer to Delete", "123");
        doNothing().when(customerRepository).delete(customer);

        CompletableFuture<Void> future = customerService.deleteAsync(customer);
        future.get(); // Wait for the async operation to complete

        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    void save_withNullCustomer_throwsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            customerService.save(null);
        });
    }

    @Test
    void delete_withNullCustomer_throwsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            customerService.delete(null);
        });
    }

    @Test
    void findByName_withNullName_returnsEmpty() {
        // Act
        Optional<Customer> result = customerService.findByName(null);

        // Assert
        assertFalse(result.isPresent());
        verify(customerRepository, never()).findByName(anyString());
    }

    @Test
    void findByName_withEmptyName_returnsEmpty() {
        // Act
        Optional<Customer> result = customerService.findByName("");

        // Assert
        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findByName("");
    }

    @Test
    void saveAsync_withException_handlesCorrectly() throws Exception {
        // Arrange
        Customer customer = new Customer("Async Customer", "987654321");
        when(customerRepository.save(customer)).thenThrow(new RuntimeException("Database error"));

        // Act
        CompletableFuture<Customer> future = customerService.saveAsync(customer);

        // Assert
        assertThrows(Exception.class, () -> {
            future.get();
        });
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void findAllAsync_withException_handlesCorrectly() throws Exception {
        // Arrange
        when(customerRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        CompletableFuture<List<Customer>> future = customerService.findAllAsync();

        // Assert
        assertThrows(Exception.class, () -> {
            future.get();
        });
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void deleteAsync_withException_handlesCorrectly() throws Exception {
        // Arrange
        Customer customer = new Customer("Async Customer to Delete", "123");
        doThrow(new RuntimeException("Database error")).when(customerRepository).delete(customer);

        // Act
        CompletableFuture<Void> future = customerService.deleteAsync(customer);

        // Assert
        assertThrows(Exception.class, () -> {
            future.get();
        });
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    void save_withSpecialCharacters_handlesCorrectly() {
        Customer customer = new Customer("Cliente & Cia Ltda.", "123-456-789");
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer savedCustomer = customerService.save(customer);

        assertNotNull(savedCustomer);
        assertEquals("Cliente & Cia Ltda.", savedCustomer.getName());
        assertEquals("123-456-789", savedCustomer.getPhone());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void findAll_withEmptyList_returnsEmptyList() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList());

        List<Customer> foundCustomers = customerService.findAll();

        assertNotNull(foundCustomers);
        assertTrue(foundCustomers.isEmpty());
        verify(customerRepository, times(1)).findAll();
    }
}