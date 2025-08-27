package com.pedidooff;

import com.pedidooff.model.Produto;
import com.pedidooff.service.ProdutoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    CommandLineRunner ensureDbDirectory() {
        return args -> {
            String userHome = System.getProperty("user.home");
            java.nio.file.Path dir = java.nio.file.Path.of(userHome, "PedidoFacil");
            if (java.nio.file.Files.notExists(dir)) {
                java.nio.file.Files.createDirectories(dir);
            }
        };
    }

    @Bean
    CommandLineRunner seedSampleData(ProdutoService service) {
        return args -> {
            if (service.listar().isEmpty()) {
                service.salvar(new Produto("Produto Exemplo", 9.99));
            }
        };
    }
}
