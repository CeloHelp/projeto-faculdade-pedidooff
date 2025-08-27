package com.pedidooff;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class AppConfig {

    @Bean
    CommandLineRunner ensureDbDirectory() {
        return args -> {
            String userHome = System.getProperty("user.home");
            Path dir = Path.of(userHome, "PedidoFacil");
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
            }
        };
    }
}
