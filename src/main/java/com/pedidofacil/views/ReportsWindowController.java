package com.pedidofacil.views;

import com.pedidofacil.repositories.projections.DailySalesView;
import com.pedidofacil.repositories.projections.PaymentDistributionView;
import com.pedidofacil.repositories.projections.ProductSalesView;
import com.pedidofacil.repositories.projections.TicketAverageView;
import com.pedidofacil.repositories.projections.TopCustomerView;
import com.pedidofacil.viewmodels.ReportsViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class ReportsWindowController implements Initializable {

    private final ReportsViewModel vm;
    private Path lastExportDir;

    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private PieChart piePayments;
    @FXML private BarChart<String, Number> barProducts;
    @FXML private LineChart<String, Number> lineDaily;

    @FXML private TableView<TicketAverageView> tblTicket;
    @FXML private TableColumn<TicketAverageView, String> colPayMethod;
    @FXML private TableColumn<TicketAverageView, Number> colOrders;
    @FXML private TableColumn<TicketAverageView, Number> colAverage;

    @FXML private TableView<TopCustomerView> tblTopCustomers;
    @FXML private TableColumn<TopCustomerView, String> colCustomer;
    @FXML private TableColumn<TopCustomerView, Number> colCustomerTotal;

    public ReportsWindowController(ReportsViewModel vm) {
        this.vm = vm;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colPayMethod.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getPaymentMethod().name()));
        colOrders.setCellValueFactory(new PropertyValueFactory<>("orders"));
        colAverage.setCellValueFactory(new PropertyValueFactory<>("average"));

        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomerTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        dpStart.setValue(vm.getStartDate());
        dpEnd.setValue(vm.getEndDate());

        refresh();
    }

    @FXML
    public void onRefresh() {
        vm.setStartDate(dpStart.getValue());
        vm.setEndDate(dpEnd.getValue());
        refresh();
    }

    @FXML
    public void onClose() {
        Stage stage = (Stage) piePayments.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onToday() {
        LocalDate d = LocalDate.now();
        dpStart.setValue(d);
        dpEnd.setValue(d);
        onRefresh();
    }

    @FXML
    public void onLast7() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        dpStart.setValue(start);
        dpEnd.setValue(end);
        onRefresh();
    }

    @FXML
    public void onThisMonth() {
        YearMonth ym = YearMonth.now();
        dpStart.setValue(ym.atDay(1));
        dpEnd.setValue(ym.atEndOfMonth());
        onRefresh();
    }

    @FXML
    public void onLastMonth() {
        YearMonth ym = YearMonth.now().minusMonths(1);
        dpStart.setValue(ym.atDay(1));
        dpEnd.setValue(ym.atEndOfMonth());
        onRefresh();
    }

    @FXML
    public void onClearPeriod() {
        dpStart.setValue(null);
        dpEnd.setValue(null);
        onRefresh();
    }

    @FXML
    public void onExport() {
        // Atualiza dados do período atual
        vm.setStartDate(dpStart.getValue());
        vm.setEndDate(dpEnd.getValue());
        vm.refreshAll();

        Path base = getExportBaseDir();
        String stamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        Path dir = base.resolve("reports_" + stamp);
        try {
            Files.createDirectories(dir);

            // daily_sales.csv
            List<String> daily = new ArrayList<>();
            daily.add("dia;total");
            for (DailySalesView d : vm.getDailySales()) {
                if (d == null || d.getDay() == null || d.getTotal() == null) continue;
                daily.add(d.getDay() + ";" + fmtDec(d.getTotal()));
            }
            writeCsv(dir.resolve("daily_sales.csv"), daily);

            // top_products.csv
            List<String> prods = new ArrayList<>();
            prods.add("produto;quantidade;total");
            for (ProductSalesView p : vm.getProductSales()) {
                if (p == null || p.getProductName() == null) continue;
                prods.add(p.getProductName() + ";" + fmtDec(p.getQuantity()) + ";" + fmtDec(p.getTotal()));
            }
            writeCsv(dir.resolve("top_products.csv"), prods);

            // payment_distribution.csv
            List<String> pays = new ArrayList<>();
            pays.add("pagamento;total");
            for (PaymentDistributionView pd : vm.getPaymentDistribution()) {
                if (pd == null || pd.getPaymentMethod() == null || pd.getTotal() == null) continue;
                pays.add(pd.getPaymentMethod().name() + ";" + fmtDec(pd.getTotal()));
            }
            writeCsv(dir.resolve("payment_distribution.csv"), pays);

            // ticket_average.csv
            List<String> tickets = new ArrayList<>();
            tickets.add("pagamento;pedidos;ticket_medio");
            for (TicketAverageView t : vm.getTicketAverages()) {
                if (t == null || t.getPaymentMethod() == null || t.getOrders() == null || t.getAverage() == null) continue;
                tickets.add(t.getPaymentMethod().name() + ";" + t.getOrders() + ";" + fmtDec(BigDecimal.valueOf(t.getAverage())));
            }
            writeCsv(dir.resolve("ticket_average.csv"), tickets);

            // top_customers.csv
            List<String> customers = new ArrayList<>();
            customers.add("cliente;total");
            for (TopCustomerView c : vm.getTopCustomers()) {
                if (c == null || c.getCustomerName() == null || c.getTotal() == null) continue;
                customers.add(c.getCustomerName() + ";" + fmtDec(c.getTotal()));
            }
            writeCsv(dir.resolve("top_customers.csv"), customers);

            lastExportDir = dir;

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setHeaderText("Exportação concluída");
            ok.setContentText("Arquivos salvos em:\n" + dir.toString());
            ok.showAndWait();
        } catch (Exception ex) {
            Alert er = new Alert(Alert.AlertType.ERROR);
            er.setHeaderText("Falha ao exportar CSV");
            er.setContentText(String.valueOf(ex.getMessage()));
            er.showAndWait();
        }
    }

    @FXML
    public void onOpenFolder() {
        try {
            Path dir = (lastExportDir != null) ? lastExportDir : getExportBaseDir();
            Files.createDirectories(dir); // garante que existe
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(dir.toFile());
            } else {
                Alert inf = new Alert(Alert.AlertType.INFORMATION);
                inf.setHeaderText("Abra a pasta manualmente");
                inf.setContentText(dir.toString());
                inf.showAndWait();
            }
        } catch (IOException e) {
            Alert er = new Alert(Alert.AlertType.ERROR);
            er.setHeaderText("Não foi possível abrir a pasta");
            er.setContentText(String.valueOf(e.getMessage()));
            er.showAndWait();
        }
    }

    private Path getExportBaseDir() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "PedidoFacil", "exports");
    }

    private String fmtDec(BigDecimal v) {
        if (v == null) return "";
        DecimalFormatSymbols sy = new DecimalFormatSymbols(new Locale("pt", "BR"));
        sy.setDecimalSeparator(',');
        sy.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#0.00", sy);
        df.setGroupingUsed(false);
        return df.format(v);
    }

    private void writeCsv(Path file, List<String> lines) throws IOException {
        Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void refresh() {
        try {
            vm.refreshAll();

            var pieData = vm.getPaymentDistribution().stream()
                    .filter(p -> p != null && p.getPaymentMethod() != null && p.getTotal() != null)
                    .map(p -> new PieChart.Data(p.getPaymentMethod().name(), p.getTotal().doubleValue()))
                    .toList();
            piePayments.setData(FXCollections.observableArrayList(pieData));

            var prodSeries = new XYChart.Series<String, Number>();
            prodSeries.setName("Top Produtos");
            for (ProductSalesView p : vm.getProductSales()) {
                if (p == null || p.getProductName() == null || p.getTotal() == null) continue;
                prodSeries.getData().add(new XYChart.Data<>(p.getProductName(), p.getTotal().doubleValue()));
            }
            barProducts.getData().setAll(prodSeries);

            var lineSeries = new XYChart.Series<String, Number>();
            lineSeries.setName("Vendas");
            for (DailySalesView d : vm.getDailySales()) {
                if (d == null || d.getDay() == null || d.getTotal() == null) continue;
                lineSeries.getData().add(new XYChart.Data<>(d.getDay(), d.getTotal().doubleValue()));
            }
            lineDaily.getData().setAll(lineSeries);

            tblTicket.setItems(FXCollections.observableArrayList(vm.getTicketAverages()));
            tblTopCustomers.setItems(FXCollections.observableArrayList(vm.getTopCustomers()));
        } catch (Exception ex) {
            piePayments.setData(FXCollections.observableArrayList());
            barProducts.getData().clear();
            lineDaily.getData().clear();
        }
    }
}
