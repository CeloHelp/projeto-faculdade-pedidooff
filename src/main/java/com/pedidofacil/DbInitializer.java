package com.pedidofacil;

import com.pedidofacil.models.*;
import com.pedidofacil.repositories.CustomerRepository;
import com.pedidofacil.repositories.OrderRepository;
import com.pedidofacil.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
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

    @Bean
    CommandLineRunner seedOrdersPf(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        return args -> {
            long existing = orderRepository.count();
            if (existing >= 50) {
                log.info("[PF] Pedidos já suficientes ({}), não será feito mock.", existing);
                return;
            }

            List<Product> products = productRepository.findAll();
            List<Customer> customers = customerRepository.findAll();
            if (products.isEmpty()) {
                log.warn("[PF] Sem produtos cadastrados, não é possível mockar pedidos.");
                return;
            }

            int toCreate = (int) Math.max(0, 60 - existing); // tenta chegar a ~60 pedidos
            long maxNumber = 0L;
            try {
                Long max = orderRepository.findMaxNumber();
                maxNumber = (max == null ? 0L : max);
            } catch (Exception e) {
                // em bancos vazios, a consulta pode falhar dependendo do dialeto; seguimos de 0
                maxNumber = 0L;
            }

            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            LocalDate today = LocalDate.now();

            for (int i = 0; i < toCreate; i++) {
                Order o = new Order();
                o.setNumber(++maxNumber);

                // Datas nos últimos 60 dias
                int minusDays = rnd.nextInt(0, 60);
                int hour = rnd.nextInt(8, 20); // horário comercial
                int minute = rnd.nextInt(0, 60);
                o.setCreatedAt(today.minusDays(minusDays).atTime(hour, minute));

                // Método de pagamento (distribuição simples)
                PaymentMethod[] methods = PaymentMethod.values();
                o.setPaymentMethod(methods[rnd.nextInt(methods.length)]);

                // Cliente: 70% dos pedidos com cliente, 30% sem (à vista/balcão)
                if (!customers.isEmpty() && rnd.nextDouble() < 0.7) {
                    Customer c = customers.get(rnd.nextInt(customers.size()));
                    o.setCustomer(c);
                }

                // Itens: 1..4 itens por pedido
                int items = rnd.nextInt(1, 5);
                for (int j = 0; j < items; j++) {
                    Product p = products.get(rnd.nextInt(products.size()));
                    // Quantidade: 1..5 (inteiro) com chance de 0.5 extra para materiais a granel
                    BigDecimal qty = BigDecimal.valueOf(rnd.nextInt(1, 6));
                    if ("m³".equalsIgnoreCase(p.getUnit()) && rnd.nextDouble() < 0.3) {
                        qty = qty.add(new BigDecimal("0.5"));
                    }
                    BigDecimal unit = p.getPrice();
                    OrderItem item = new OrderItem(p, qty, unit);
                    o.addItem(item);
                }

                orderRepository.save(o);
            }

            log.info("[PF] Pedidos mockados adicionados: {} (total agora: {}).", toCreate, orderRepository.count());
        };
    }
}
