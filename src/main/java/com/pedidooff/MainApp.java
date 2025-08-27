package com.pedidooff;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("PedidoOff");
        // Janela vazia por enquanto
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
