package com.pedidofacil;

import com.pedidofacil.models.Customer;
import com.pedidofacil.models.Product;
import com.pedidofacil.repositories.CustomerRepository;
import com.pedidofacil.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class DbInitializer {
    private static final Logger log = LoggerFactory.getLogger(DbInitializer.class);

    @Bean
    CommandLineRunner ensureDbDirectoryPf() {
        return args -> {
            String userHome = System.getProperty("user.home");
            Path dir = Paths.get(userHome, "PedidoFacil");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            log.info("[PF] Diretório do banco de dados: {}", dir.toAbsolutePath());
        };
    }

    @Bean
    CommandLineRunner seedProductsPf(ProductRepository productRepository) {
        return args -> {
            // Se houver menos de 7 produtos, completamos a lista base
            if (productRepository.count() < 7) {
                Product cimento = new Product(
                        "Cimento Itaú",
                        "Itaú",
                        "saco 50kg",
                        new BigDecimal("38.00")
                );
                Product sifao = new Product(
                        "Sifão Branco",
                        "",
                        "un",
                        new BigDecimal("10.00")
                );
                Product areia = new Product(
                        "Areia Média",
                        "",
                        "m³",
                        new BigDecimal("120.00")
                );
                Product brita = new Product(
                        "Brita 1",
                        "",
                        "m³",
                        new BigDecimal("140.00")
                );
                Product tijolo = new Product(
                        "Tijolo 6 furos",
                        "",
                        "milheiro",
                        new BigDecimal("850.00")
                );
                Product cimentoVotoran = new Product(
                        "Cimento Votoran",
                        "Votoran",
                        "saco 50kg",
                        new BigDecimal("39.50")
                );
                Product argamassa = new Product(
                        "Argamassa AC1",
                        "Quartzolit",
                        "saco 20kg",
                        new BigDecimal("24.90")
                );

                productRepository.save(cimento);
                productRepository.save(sifao);
                productRepository.save(areia);
                productRepository.save(brita);
                productRepository.save(tijolo);
                productRepository.save(cimentoVotoran);
                productRepository.save(argamassa);
                log.info("[PF] Produtos base garantidos (count: {}).", productRepository.count());
            }
        };
    }

    @Bean
    CommandLineRunner seedCustomersPf(CustomerRepository customerRepository) {
        return args -> {
            // Se houver menos de 4 clientes, completamos a lista base
            if (customerRepository.count() < 4) {
                customerRepository.save(new Customer("João Silva", "(11) 90000-0001"));
                customerRepository.save(new Customer("Maria Souza", "(11) 90000-0002"));
                customerRepository.save(new Customer("Carlos Pereira", "(11) 90000-0003"));
                customerRepository.save(new Customer("Ana Oliveira", "(11) 90000-0004"));
                log.info("[PF] Clientes base garantidos (count: {}).", customerRepository.count());
            }
        };
    }
}
