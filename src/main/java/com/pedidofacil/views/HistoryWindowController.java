package com.pedidofacil.views;

import com.pedidofacil.models.Order;
import com.pedidofacil.viewmodels.HistoryViewModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class HistoryWindowController implements Initializable {

    private final HistoryViewModel vm;

    public HistoryWindowController(HistoryViewModel vm) {
        this.vm = vm;
    }

    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private TableView<Order> tblOrders;
    @FXML private TableColumn<Order, Long> colNumber;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colPayment;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, BigDecimal> colTotal;
    @FXML private Label lblStatus;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colDate.setCellValueFactory(c -> {
            var dt = c.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleStringProperty(dt != null ? dt.format(fmt) : "");
        });
        colPayment.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getPaymentMethod())));
        colCustomer.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCustomer() != null ? c.getValue().getCustomer().getName() : ""));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        // Formatação monetária pt-BR com " R$"
        NumberFormat currency = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(currency.format(item) + " R$");
                }
            }
        });
    }

    @FXML
    public void onSearch() {
        vm.setStartDate(dpStart.getValue());
        vm.setEndDate(dpEnd.getValue());
        vm.search();
        tblOrders.setItems(FXCollections.observableArrayList(vm.getOrders()));
        lblStatus.setText(vm.getStatus());
    }

    @FXML
    public void onExport() {
        String path = vm.exportCsv();
        lblStatus.setText(vm.getStatus());
        if (path == null || path.isBlank()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Nada para exportar", ButtonType.OK);
            a.showAndWait();
        } else {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "CSV exportado para:\n" + path, ButtonType.OK);
            a.showAndWait();
        }
    }
}
