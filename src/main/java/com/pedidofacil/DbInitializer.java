package com.pedidofacil;

import com.pedidofacil.models.Product;
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
            if (productRepository.count() == 0) {
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
                productRepository.save(cimento);
                productRepository.save(sifao);
                log.info("[PF] Produtos iniciais inseridos (2 itens).");
            }
        };
    }
}
