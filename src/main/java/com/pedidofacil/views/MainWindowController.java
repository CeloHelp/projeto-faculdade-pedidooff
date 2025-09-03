package com.pedidofacil.views;

import com.pedidofacil.config.AppSettings;
import com.pedidofacil.models.PaymentMethod;
import com.pedidofacil.models.Product;
import com.pedidofacil.viewmodels.MainViewModel;
import com.pedidofacil.viewmodels.OrderItemView;
import javafx.collections.FXCollections;
import com.pedidofacil.models.Customer;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import javafx.scene.control.ListCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
public class MainWindowController implements Initializable {

    private final MainViewModel vm;
    private final AppSettings settings;
    private final ApplicationContext context;

    public MainWindowController(MainViewModel vm, AppSettings settings, ApplicationContext context) {
        this.vm = vm;
        this.settings = settings;
        this.context = context;
    }

    @FXML private Label lblStore;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<Product> cmbProducts;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtUnitPrice;
    @FXML private TableView<OrderItemView> tblItems;
    @FXML private TableColumn<OrderItemView, String> colProduto;
    @FXML private TableColumn<OrderItemView, BigDecimal> colQtd;
    @FXML private TableColumn<OrderItemView, BigDecimal> colPreco;
    @FXML private TableColumn<OrderItemView, BigDecimal> colSubtotal;
    @FXML private TableColumn<OrderItemView, Void> colAcoes;
    @FXML private ComboBox<PaymentMethod> cmbPayment;
    @FXML private Label lblCustomerLabel;
    // Substitui TextField por ComboBox de clientes
    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private Button btnNewCustomer;
    @FXML private Button btnFinalize;
    @FXML private Button btnPrint;
    @FXML private Button btnNewOrder;
    @FXML private Button btnHistory;
    @FXML private Button btnReports;
    @FXML private Label lblTotal;
    @FXML private Label lblStatus;

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblStore.setText(settings.getStoreName() + " - CNPJ: " + settings.getCnpj() + " - Tel: " + settings.getPhone());

        vm.onLoaded();

        cmbProducts.setItems(FXCollections.observableArrayList(vm.getProducts()));
        // Mostra o nome (e marca) do produto ao invés de toString()
        cmbProducts.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String brand = item.getBrand() != null && !item.getBrand().isBlank() ? " (" + item.getBrand() + ")" : "";
                    setText(item.getName() + brand);
                }
            }
        });
        cmbProducts.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String brand = item.getBrand() != null && !item.getBrand().isBlank() ? " (" + item.getBrand() + ")" : "";
                    setText(item.getName() + brand);
                }
            }
        });
        cmbProducts.setConverter(new StringConverter<>() {
            @Override
            public String toString(Product item) {
                if (item == null) return "";
                String brand = item.getBrand() != null && !item.getBrand().isBlank() ? " (" + item.getBrand() + ")" : "";
                return item.getName() + brand;
            }
            @Override
            public Product fromString(String string) { return null; }
        });

        cmbPayment.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        cmbPayment.getSelectionModel().select(vm.getPaymentMethod());

        // Preencher lista de clientes
        cmbCustomer.setItems(FXCollections.observableArrayList(vm.getCustomers()));
        cmbCustomer.setEditable(true);
        // Mostrar nome (e telefone) do cliente
        cmbCustomer.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getName() + (item.getPhone() != null && !item.getPhone().isBlank() ? " - " + item.getPhone() : ""));
            }
        });
        cmbCustomer.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getName() + (item.getPhone() != null && !item.getPhone().isBlank() ? " - " + item.getPhone() : ""));
            }
        });
        cmbCustomer.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer item) {
                if (item == null) return "";
                String phone = item.getPhone() != null && !item.getPhone().isBlank() ? " - " + item.getPhone() : "";
                return item.getName() + phone;
            }
            @Override
            public Customer fromString(String string) {
                if (string == null || string.isBlank()) return null;
                return new Customer(string.trim(), null);
            }
        });

        txtQuantity.setText("1");
        txtUnitPrice.setText(currency.format(BigDecimal.ZERO));

        colProduto.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Formatação de moeda (pt-BR) nas colunas de preço e subtotal
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
        colSubtotal.setCellFactory(col -> new TableCell<>() {
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

        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Remover");
            {
                btn.setOnAction(evt -> {
                    int index = getIndex();
                    vm.removeItem(index);
                    tblItems.getItems().remove(index);
                    updateTotalsAndStatus();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tblItems.setItems(FXCollections.observableArrayList());
        updateTotalsAndStatus();

        cmbProducts.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            vm.setSelectedProduct(n);
            if (vm.getUnitPrice() != null) {
                txtUnitPrice.setText(currency.format(vm.getUnitPrice()));
            }
        });

        cmbPayment.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            vm.setPaymentMethod(n);
            // Cliente sempre visível; apenas validação de obrigatório para fiado fica no ViewModel
        });
        // Cliente sempre visível: remove lógica de ocultar
        lblCustomerLabel.setVisible(true);
        lblCustomerLabel.setManaged(true);
        cmbCustomer.setVisible(true);
        cmbCustomer.setManaged(true);
        btnNewCustomer.setVisible(true);
        btnNewCustomer.setManaged(true);

        // Busca incremental de produtos
        txtSearch.textProperty().addListener((obs, old, val) -> filterProducts(val));
    }

    private void filterProducts(String query) {
        List<Product> base = vm.getProducts();
        if (query == null || query.isBlank()) {
            cmbProducts.setItems(FXCollections.observableArrayList(base));
            return;
        }
        String q = query.trim().toLowerCase();
        List<Product> filtered = base.stream()
                .filter(p -> {
                    String name = p.getName() != null ? p.getName().toLowerCase() : "";
                    String brand = p.getBrand() != null ? p.getBrand().toLowerCase() : "";
                    return name.contains(q) || brand.contains(q);
                })
                .collect(Collectors.toList());
        cmbProducts.setItems(FXCollections.observableArrayList(filtered));
        if (!filtered.isEmpty()) {
            cmbProducts.getSelectionModel().select(0);
        }
    }

    @FXML
    public void onAddItem(ActionEvent e) {
        try {
            BigDecimal qty = new BigDecimal(txtQuantity.getText().replace(",", "."));
            String priceStr = txtUnitPrice.getText().replaceAll("[^0-9,.-]", "").replace(".", "").replace(",", ".");
            BigDecimal price = new BigDecimal(priceStr);
            vm.setQuantity(qty);
            vm.setUnitPrice(price);
            vm.addItem();
            ObservableList<OrderItemView> tableItems = tblItems.getItems();
            tableItems.add(vm.getItems().get(vm.getItems().size() - 1));
            tblItems.refresh();
            updateTotalsAndStatus();
        } catch (Exception ex) {
            lblStatus.setText("Dados inválidos para item.");
        }
    }

    @FXML
    public void onFinalize(ActionEvent e) {
        Customer sel = cmbCustomer.getSelectionModel().getSelectedItem();
        if (sel != null) {
            vm.setCustomerName(sel.getName());
        } else if (cmbCustomer.isEditable() && cmbCustomer.getEditor() != null && !cmbCustomer.getEditor().getText().isBlank()) {
            vm.setCustomerName(cmbCustomer.getEditor().getText().trim());
        }
        vm.finalizeOrder();
        updateTotalsAndStatus();
    }

    @FXML
    public void onPrint(ActionEvent e) {
        vm.printOrSavePdf();
        updateTotalsAndStatus();
    }

    @FXML
    public void onNewOrder(ActionEvent e) {
        vm.newOrder();
        tblItems.getItems().clear();
        updateTotalsAndStatus();
    }

    @FXML
    public void onOpenHistory(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HistoryWindow.fxml"));
            loader.setControllerFactory(context::getBean);
            Stage stage = new Stage();
            stage.setTitle("Histórico de Pedidos");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException ex) {
            lblStatus.setText("Falha ao abrir histórico: " + ex.getMessage());
        }
    }

    @FXML
    public void onOpenReports(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportsWindow.fxml"));
            loader.setControllerFactory(context::getBean);
            Stage stage = new Stage();
            stage.setTitle("Relatórios");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException ex) {
            lblStatus.setText("Falha ao abrir relatórios: " + ex.getMessage());
        }
    }

    private void updateTotalsAndStatus() {
        lblTotal.setText(currency.format(vm.getTotal()));
        lblStatus.setText(vm.getStatusMessage());
    }

    @FXML
    public void onNewCustomer(ActionEvent e) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Novo Cliente");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome");
        TextField txtTelefone = new TextField();
        txtTelefone.setPromptText("Telefone");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Telefone:"), 0, 1);
        grid.add(txtTelefone, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String nome = txtNome.getText();
                String tel = txtTelefone.getText();
                if (nome != null && !nome.isBlank()) {
                    return vm.createOrFindCustomer(nome.trim(), tel);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            var current = cmbCustomer.getItems();
            if (current.stream().noneMatch(cc -> cc.getId() != null && c.getId() != null && cc.getId().equals(c.getId()))) {
                current.add(c);
                cmbCustomer.setItems(current);
            }
            cmbCustomer.getSelectionModel().select(c);
        });
    }
}
