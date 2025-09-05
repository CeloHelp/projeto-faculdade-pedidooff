package com.pedidofacil.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer("Cliente Teste", "123456789");
    }

    @Test
    void constructor_createsCustomerWithValues() {
        Customer newCustomer = new Customer("Novo Cliente", "987654321");
        
        assertEquals("Novo Cliente", newCustomer.getName());
        assertEquals("987654321", newCustomer.getPhone());
    }

    @Test
    void constructor_withNullValues_handlesCorrectly() {
        Customer newCustomer = new Customer(null, null);
        
        assertNull(newCustomer.getName());
        assertNull(newCustomer.getPhone());
    }

    @Test
    void constructor_emptyConstructor_createsEmptyCustomer() {
        Customer emptyCustomer = new Customer();
        
        assertNull(emptyCustomer.getId());
        assertNull(emptyCustomer.getName());
        assertNull(emptyCustomer.getPhone());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        // Test ID
        customer.setId(1L);
        assertEquals(1L, customer.getId());

        // Test Name
        customer.setName("Cliente Atualizado");
        assertEquals("Cliente Atualizado", customer.getName());

        // Test Phone
        customer.setPhone("987654321");
        assertEquals("987654321", customer.getPhone());
    }

    @Test
    void name_withSpecialCharacters_handlesCorrectly() {
        String[] names = {
            "João da Silva",
            "Maria José & Cia",
            "Empresa Ltda.",
            "Cliente 123",
            "José da Silva Santos"
        };
        
        for (String name : names) {
            customer.setName(name);
            assertEquals(name, customer.getName());
        }
    }

    @Test
    void phone_withVariousFormats_handlesCorrectly() {
        String[] phones = {
            "123456789",
            "(11) 99999-9999",
            "+55 11 99999-9999",
            "11 99999-9999",
            "11999999999",
            "123-456-7890"
        };
        
        for (String phone : phones) {
            customer.setPhone(phone);
            assertEquals(phone, customer.getPhone());
        }
    }

    @Test
    void name_withEmptyString_handlesCorrectly() {
        customer.setName("");
        assertEquals("", customer.getName());
    }

    @Test
    void phone_withEmptyString_handlesCorrectly() {
        customer.setPhone("");
        assertEquals("", customer.getPhone());
    }

    @Test
    void name_withWhitespace_handlesCorrectly() {
        customer.setName("   Cliente com Espaços   ");
        assertEquals("   Cliente com Espaços   ", customer.getName());
    }

    @Test
    void phone_withWhitespace_handlesCorrectly() {
        customer.setPhone("   123 456 789   ");
        assertEquals("   123 456 789   ", customer.getPhone());
    }

    @Test
    void allFields_withNullValues_handlesCorrectly() {
        // Set all fields to null
        customer.setId(null);
        customer.setName(null);
        customer.setPhone(null);

        // Assert all are null
        assertNull(customer.getId());
        assertNull(customer.getName());
        assertNull(customer.getPhone());
    }

    @Test
    void constructor_withEmptyStrings_handlesCorrectly() {
        Customer emptyStringCustomer = new Customer("", "");
        
        assertEquals("", emptyStringCustomer.getName());
        assertEquals("", emptyStringCustomer.getPhone());
    }

    @Test
    void name_withUnicodeCharacters_handlesCorrectly() {
        String[] unicodeNames = {
            "José da Silva",
            "María González",
            "François Dupont",
            "李小明",
            "Александр Петров"
        };
        
        for (String name : unicodeNames) {
            customer.setName(name);
            assertEquals(name, customer.getName());
        }
    }

    @Test
    void phone_withInternationalFormats_handlesCorrectly() {
        String[] internationalPhones = {
            "+1-555-123-4567",
            "+44 20 7946 0958",
            "+33 1 42 86 83 26",
            "+86 138 0013 8000",
            "+55 11 99999-9999"
        };
        
        for (String phone : internationalPhones) {
            customer.setPhone(phone);
            assertEquals(phone, customer.getPhone());
        }
    }

    @Test
    void name_withVeryLongString_handlesCorrectly() {
        String longName = "Cliente com um nome muito longo que pode ser usado para testar o comportamento com strings extensas";
        customer.setName(longName);
        assertEquals(longName, customer.getName());
    }

    @Test
    void phone_withVeryLongString_handlesCorrectly() {
        String longPhone = "123456789012345678901234567890";
        customer.setPhone(longPhone);
        assertEquals(longPhone, customer.getPhone());
    }

    @Test
    void name_withNumbers_handlesCorrectly() {
        String[] namesWithNumbers = {
            "Cliente 123",
            "Empresa 2023",
            "Loja 1",
            "Cliente 2.0",
            "Test 123ABC"
        };
        
        for (String name : namesWithNumbers) {
            customer.setName(name);
            assertEquals(name, customer.getName());
        }
    }

    @Test
    void phone_withLetters_handlesCorrectly() {
        String[] phonesWithLetters = {
            "123-ABC-4567",
            "1-800-CALL-NOW",
            "555-GET-HELP"
        };
        
        for (String phone : phonesWithLetters) {
            customer.setPhone(phone);
            assertEquals(phone, customer.getPhone());
        }
    }
}