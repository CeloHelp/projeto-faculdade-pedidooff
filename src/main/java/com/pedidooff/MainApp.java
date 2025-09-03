package com.pedidooff;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;

public class MainApp extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Garantir que o diretório do banco exista antes de iniciar o Spring/JPA
        try {
            String userHome = System.getProperty("user.home");
            Path dir = Paths.get(userHome, "PedidoFacil");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criar diretório do banco de dados", e);
        }

        springContext = new SpringApplicationBuilder(PedidoOffApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Handlers globais de exceção para capturar falhas não tratadas
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> handleException(e));
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> handleException(e));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();

        primaryStage.setTitle("PedidoOff - Principal");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }

    private void handleException(Throwable e) {
        // Log em stderr
        e.printStackTrace();
        // Log em arquivo
        try {
            String userHome = System.getProperty("user.home");
            Path dir = Paths.get(userHome, "PedidoFacil");
            Files.createDirectories(dir);
            Path log = dir.resolve("error.log");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String entry = "[" + ZonedDateTime.now() + "] " + e.getClass().getName() + ": " + (e.getMessage() == null ? "" : e.getMessage()) + System.lineSeparator() + sw + System.lineSeparator();
            Files.writeString(log, entry, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignored) { }

        // Exibir alerta amigável na UI
        Runnable show = () -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro inesperado");
                alert.setHeaderText(e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : ""));

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                TextArea textArea = new TextArea(sw.toString());
                textArea.setEditable(false);
                textArea.setWrapText(false);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);

                GridPane content = new GridPane();
                content.setVgap(6);
                content.add(new Label("Detalhes:"), 0, 0);
                content.add(textArea, 0, 1);

                alert.getDialogPane().setContent(content);
                alert.showAndWait();
            } catch (Exception ignored) { }
        };

        if (Platform.isFxApplicationThread()) {
            show.run();
        } else {
            Platform.runLater(show);
        }
    }

    @Override
    public void stop() {
        springContext.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
