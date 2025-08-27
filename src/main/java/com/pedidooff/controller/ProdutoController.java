package com.pedidooff.controller;

import com.pedidooff.model.Produto;
import com.pedidooff.service.ProdutoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class ProdutoController {

    private final ProdutoService service;

    @FXML private TextField txtNome;
    @FXML private TextField txtPreco;
    @FXML private TableView<Produto> tableProdutos;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, Double> colPreco;

    private final ObservableList<Produto> dados = FXCollections.observableArrayList();

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nome"));
        colPreco.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("preco"));
        tableProdutos.setItems(dados);
        recarregarTabela();
    }

    @FXML
    public void onAdicionar(ActionEvent event) {
        String nome = txtNome.getText();
        String precoStr = txtPreco.getText();
        if (nome == null || nome.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Informe o nome do produto.");
            return;
        }
        Double preco;
        try {
            preco = Double.parseDouble(precoStr.replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Preço inválido.");
            return;
        }
        Produto salvo = service.salvar(new Produto(nome.trim(), preco));
        txtNome.clear();
        txtPreco.clear();
        dados.add(salvo);
    }

    @FXML
    public void onRemoverSelecionado(ActionEvent event) {
        Produto selecionado = tableProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Remover", "Selecione um produto para remover.");
            return;
        }
        service.remover(selecionado);
        dados.remove(selecionado);
    }

    private void recarregarTabela() {
        dados.setAll(service.listar());
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
