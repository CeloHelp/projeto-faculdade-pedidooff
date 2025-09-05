package com.pedidofacil.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("Produto Teste", "Marca A", "UN", new BigDecimal("10.50"));
    }

    @Test
    void constructor_createsProductWithValues() {
        Product newProduct = new Product("Novo Produto", "Marca B", "KG", new BigDecimal("25.75"));
        
        assertEquals("Novo Produto", newProduct.getName());
        assertEquals("Marca B", newProduct.getBrand());
        assertEquals("KG", newProduct.getUnit());
        assertEquals(new BigDecimal("25.75"), newProduct.getPrice());
    }

    @Test
    void constructor_withNullValues_handlesCorrectly() {
        Product newProduct = new Product(null, null, null, null);
        
        assertNull(newProduct.getName());
        assertNull(newProduct.getBrand());
        assertNull(newProduct.getUnit());
        assertNull(newProduct.getPrice());
    }

    @Test
    void constructor_emptyConstructor_createsEmptyProduct() {
        Product emptyProduct = new Product();
        
        assertNull(emptyProduct.getId());
        assertNull(emptyProduct.getName());
        assertNull(emptyProduct.getBrand());
        assertNull(emptyProduct.getUnit());
        assertNull(emptyProduct.getPrice());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        // Test ID
        product.setId(1L);
        assertEquals(1L, product.getId());

        // Test Name
        product.setName("Produto Atualizado");
        assertEquals("Produto Atualizado", product.getName());

        // Test Brand
        product.setBrand("Marca Atualizada");
        assertEquals("Marca Atualizada", product.getBrand());

        // Test Unit
        product.setUnit("L");
        assertEquals("L", product.getUnit());

        // Test Price
        product.setPrice(new BigDecimal("15.99"));
        assertEquals(new BigDecimal("15.99"), product.getPrice());
    }

    @Test
    void toString_withBrand_returnsNameAndBrand() {
        // Act
        String result = product.toString();

        // Assert
        assertEquals("Produto Teste (Marca A)", result);
    }

    @Test
    void toString_withNullBrand_returnsOnlyName() {
        // Arrange
        product.setBrand(null);

        // Act
        String result = product.toString();

        // Assert
        assertEquals("Produto Teste", result);
    }

    @Test
    void toString_withEmptyBrand_returnsOnlyName() {
        // Arrange
        product.setBrand("");

        // Act
        String result = product.toString();

        // Assert
        assertEquals("Produto Teste", result);
    }

    @Test
    void toString_withBlankBrand_returnsOnlyName() {
        // Arrange
        product.setBrand("   ");

        // Act
        String result = product.toString();

        // Assert
        assertEquals("Produto Teste", result);
    }

    @Test
    void toString_withNullName_returnsEmptyString() {
        // Arrange
        product.setName(null);

        // Act
        String result = product.toString();

        // Assert
        assertEquals("null", result);
    }

    @Test
    void pricePrecision_handlesCorrectly() {
        // Test with high precision
        BigDecimal precisePrice = new BigDecimal("123.456789");
        product.setPrice(precisePrice);
        assertEquals(precisePrice, product.getPrice());

        // Test with zero
        product.setPrice(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, product.getPrice());

        // Test with negative value
        product.setPrice(new BigDecimal("-10.50"));
        assertEquals(new BigDecimal("-10.50"), product.getPrice());
    }

    @Test
    void nameAndBrand_withSpecialCharacters_handlesCorrectly() {
        // Test with special characters
        product.setName("Produto & Cia Ltda.");
        product.setBrand("Marca® 2023");

        assertEquals("Produto & Cia Ltda.", product.getName());
        assertEquals("Marca® 2023", product.getBrand());
        assertEquals("Produto & Cia Ltda. (Marca® 2023)", product.toString());
    }

    @Test
    void unit_withVariousUnits_handlesCorrectly() {
        String[] units = {"UN", "KG", "L", "M", "M²", "M³", "CX", "PC"};
        
        for (String unit : units) {
            product.setUnit(unit);
            assertEquals(unit, product.getUnit());
        }
    }

    @Test
    void brand_withVariousBrands_handlesCorrectly() {
        String[] brands = {"Marca A", "Marca B", "Genérico", "Sem Marca", "Marca 123"};
        
        for (String brand : brands) {
            product.setBrand(brand);
            assertEquals(brand, product.getBrand());
            assertTrue(product.toString().contains(brand));
        }
    }

    @Test
    void name_withVariousNames_handlesCorrectly() {
        String[] names = {"Produto A", "Produto B", "Produto Genérico", "Produto 123", "Produto & Cia"};
        
        for (String name : names) {
            product.setName(name);
            assertEquals(name, product.getName());
            assertTrue(product.toString().startsWith(name));
        }
    }

    @Test
    void allFields_withNullValues_handlesCorrectly() {
        // Set all fields to null
        product.setId(null);
        product.setName(null);
        product.setBrand(null);
        product.setUnit(null);
        product.setPrice(null);

        // Assert all are null
        assertNull(product.getId());
        assertNull(product.getName());
        assertNull(product.getBrand());
        assertNull(product.getUnit());
        assertNull(product.getPrice());
    }

    @Test
    void allFields_withEmptyStrings_handlesCorrectly() {
        // Set all string fields to empty
        product.setName("");
        product.setBrand("");
        product.setUnit("");

        // Assert all are empty strings
        assertEquals("", product.getName());
        assertEquals("", product.getBrand());
        assertEquals("", product.getUnit());
    }
}