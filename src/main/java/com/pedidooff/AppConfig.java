package com.pedidooff;

import com.pedidooff.model.Produto;
import com.pedidooff.service.ProdutoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    CommandLineRunner ensureDbDirectory() {
        return args -> {
            String userHome = System.getProperty("user.home");
            java.nio.file.Path dir = java.nio.file.Path.of(userHome, "PedidoFacil");
            if (java.nio.file.Files.notExists(dir)) {
                java.nio.file.Files.createDirectories(dir);
                log.info("Diretório do banco criado em: {}", dir);
            } else {
                log.info("Diretório do banco já existe: {}", dir);
            }
        };
    }

    @Bean
    CommandLineRunner seedSampleData(ProdutoService service) {
        return args -> {
            if (service.listar().isEmpty()) {
                Produto p = service.salvar(new Produto("Produto Exemplo", 9.99));
                log.info("Produto de exemplo inserido: id={}, nome={}, preco={}", p.getId(), p.getNome(), p.getPreco());
            }
        };
    }

    @Bean
    CommandLineRunner logCounts(ProdutoService service) {
        return args -> log.info("Quantidade de produtos no banco: {}", service.listar().size());
    }
}
