package com.pedidooff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EntityScan(basePackages = {"com.pedidooff.model", "com.pedidofacil.models"})
@EnableJpaRepositories(basePackages = {"com.pedidooff.repository", "com.pedidofacil.repositories"})
@ComponentScan(basePackages = {"com.pedidooff", "com.pedidofacil"})
@EnableAsync
public class PedidoOffApplication {
    public static void main(String[] args) {
        SpringApplication.run(PedidoOffApplication.class, args);
    }
}
