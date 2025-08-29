package com.pedidooff.controller;

import com.pedidofacil.models.Product;
import com.pedidofacil.services.IProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class ProdutoController {

    private final IProductService service;

    @FXML private TextField txtNome;
    @FXML private TextField txtMarca;
    @FXML private TextField txtUnidade;
    @FXML private TextField txtPreco;
    @FXML private Button btnRemover;
    @FXML private TableView<Product> tableProdutos;
    @FXML private TableColumn<Product, String> colNome;
    @FXML private TableColumn<Product, String> colMarca;
    @FXML private TableColumn<Product, String> colUnidade;
    @FXML private TableColumn<Product, BigDecimal> colPreco;

    private final ObservableList<Product> dados = FXCollections.observableArrayList();

    public ProdutoController(IProductService service) {
        this.service = service;
    }

    // Máscara de moeda
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private boolean updatingPreco = false;

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("brand"));
        colUnidade.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Formatação de moeda (pt-BR) na tabela
        colPreco.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(currency.format(value));
                }
            }
        });

        tableProdutos.setItems(dados);
        tableProdutos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Botão Remover desabilitado quando sem seleção
        btnRemover.disableProperty().bind(tableProdutos.getSelectionModel().selectedItemProperty().isNull());

        // Máscara de moeda no campo de preço: digite números e formata como R$ 0,00
        txtPreco.textProperty().addListener((obs, oldText, newText) -> {
            if (updatingPreco) return;
            updatingPreco = true;
            try {
                String digits = newText == null ? "" : newText.replaceAll("[^0-9]", "");
                if (digits.isEmpty()) {
                    txtPreco.setText(currency.format(BigDecimal.ZERO));
                } else {
                    BigDecimal val = new BigDecimal(digits).divide(new BigDecimal(100));
                    txtPreco.setText(currency.format(val));
                }
                txtPreco.positionCaret(txtPreco.getText().length());
            } finally {
                updatingPreco = false;
            }
        });
        // Valor inicial
        txtPreco.setText(currency.format(BigDecimal.ZERO));

        // Enter no campo preço aciona adicionar
        txtPreco.setOnAction(e -> onAdicionar(null));

        recarregarTabela();
    }

    @FXML
    public void onAdicionar(ActionEvent event) {
        String nome = txtNome.getText();
        String marca = txtMarca.getText();
        String unidade = txtUnidade.getText();
        String precoStr = txtPreco.getText();

        if (nome == null || nome.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Informe o nome do produto.");
            return;
        }
        if (unidade == null || unidade.isBlank()) {
            unidade = "un";
        }

        BigDecimal preco;
        try {
            if (precoStr == null) throw new IllegalArgumentException("Preço nulo");
            // Remove espaços comuns e não separáveis (NBSP), símbolos e mantém apenas dígitos e separadores
            String normalized = precoStr
                    .replace('\u00A0', ' ')
                    .trim()
                    .replaceAll("[^0-9,.-]", "");
            if (normalized.isBlank()) throw new IllegalArgumentException("Preço vazio");
            // Remove separadores de milhar e converte vírgula para ponto
            normalized = normalized.replace(".", "").replace(",", ".");
            preco = new BigDecimal(normalized);
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Informe um preço válido.");
            return;
        }
        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "O preço deve ser maior que zero.");
            return;
        }

        Product novo = new Product(nome.trim(), marca == null ? "" : marca.trim(), unidade.trim(), preco);
        Product salvo = service.save(novo);

        // Limpar campos e reiniciar preço formatado
        txtNome.clear();
        txtMarca.clear();
        txtUnidade.clear();
        txtPreco.setText(currency.format(BigDecimal.ZERO));

        dados.add(salvo);
    }

    @FXML
    public void onRemoverSelecionado(ActionEvent event) {
        Product selecionado = tableProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Remover", "Selecione um produto para remover.");
            return;
        }
        service.delete(selecionado);
        dados.remove(selecionado);
    }

    private void recarregarTabela() {
        dados.setAll(service.findAll());
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
