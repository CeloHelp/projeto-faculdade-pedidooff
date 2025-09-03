package com.pedidofacil.views;

import com.pedidofacil.models.PaymentMethod;
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
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;
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
import java.util.*;
import java.util.prefs.Preferences;

@Component
public class ReportsWindowController implements Initializable {

    private static final String PREF_EXPORT_DIR = "export_dir";

    private final ReportsViewModel vm;
    private Path lastExportDir;
    private final Preferences prefs = Preferences.userNodeForPackage(ReportsWindowController.class);

    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private PieChart piePayments;
    @FXML private BarChart<String, Number> barProducts;
    @FXML private BarChart<String, Number> barDaily;

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
        colPayMethod.setCellValueFactory(cd -> new SimpleStringProperty(pmLabel(cd.getValue().getPaymentMethod())));
        colOrders.setCellValueFactory(new PropertyValueFactory<>("orders"));
        colAverage.setCellValueFactory(new PropertyValueFactory<>("average"));
        colAverage.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); }
                else { setText(fmtDec(BigDecimal.valueOf(item.doubleValue())) + " R$"); }
            }
        });

        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCustomerTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colCustomerTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(""); }
                else { setText(fmtDec(BigDecimal.valueOf(item.doubleValue())) + " R$"); }
            }
        });

        // Formatação dos eixos (R$) nos gráficos
        if (barProducts.getYAxis() instanceof NumberAxis by) {
            by.setTickLabelFormatter(new StringConverter<>() {
                @Override public String toString(Number object) { return fmtDec(BigDecimal.valueOf(object.doubleValue())) + " R$"; }
                @Override public Number fromString(String string) { return 0; }
            });
        }
        if (barDaily.getYAxis() instanceof NumberAxis ly) {
            ly.setTickLabelFormatter(new StringConverter<>() {
                @Override public String toString(Number object) { return fmtDec(BigDecimal.valueOf(object.doubleValue())) + " R$"; }
                @Override public Number fromString(String string) { return 0; }
            });
        }
        if (barDaily.getXAxis() instanceof CategoryAxis cx) {
            cx.setTickLabelRotation(45);
        }

        dpStart.setValue(vm.getStartDate());
        dpEnd.setValue(vm.getEndDate());

        onToday(); // Set default to today's date
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
        var ym = java.time.YearMonth.now();
        dpStart.setValue(ym.atDay(1));
        dpEnd.setValue(ym.atEndOfMonth());
        onRefresh();
    }

    @FXML
    public void onLastMonth() {
        var ym = java.time.YearMonth.now().minusMonths(1);
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
        String stamp = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        Path dir = base.resolve("reports_" + stamp);
        try {
            Files.createDirectories(dir);

            // daily_sales.csv
            List<String> daily = new ArrayList<>();
            daily.add("dia;total");
            for (DailySalesView d : vm.getDailySales()) {
                if (d == null || d.getDay() == null || d.getTotal() == null) continue;
                daily.add(d.getDay() + ";" + fmtDec(d.getTotal()) + " R$");
            }
            writeCsv(dir.resolve("daily_sales.csv"), daily);

            // top_products.csv
            List<String> prods = new ArrayList<>();
            prods.add("produto;quantidade;total");
            for (ProductSalesView p : vm.getProductSales()) {
                if (p == null || p.getProductName() == null) continue;
                prods.add(p.getProductName() + ";" + fmtDec(p.getQuantity()) + ";" + fmtDec(p.getTotal()) + " R$");
            }
            writeCsv(dir.resolve("top_products.csv"), prods);

            // payment_distribution.csv
            List<String> pays = new ArrayList<>();
            pays.add("pagamento;total");
            for (PaymentDistributionView pd : vm.getPaymentDistribution()) {
                if (pd == null || pd.getPaymentMethod() == null || pd.getTotal() == null) continue;
                pays.add(pmLabel(pd.getPaymentMethod()) + ";" + fmtDec(pd.getTotal()) + " R$");
            }
            writeCsv(dir.resolve("payment_distribution.csv"), pays);

            // ticket_average.csv
            List<String> tickets = new ArrayList<>();
            tickets.add("pagamento;pedidos;ticket_medio");
            for (TicketAverageView t : vm.getTicketAverages()) {
                if (t == null || t.getPaymentMethod() == null || t.getOrders() == null || t.getAverage() == null) continue;
                tickets.add(pmLabel(t.getPaymentMethod()) + ";" + t.getOrders() + ";" + fmtDec(BigDecimal.valueOf(t.getAverage())) + " R$");
            }
            writeCsv(dir.resolve("ticket_average.csv"), tickets);

            // top_customers.csv
            List<String> customers = new ArrayList<>();
            customers.add("cliente;total");
            for (TopCustomerView c : vm.getTopCustomers()) {
                if (c == null || c.getCustomerName() == null || c.getTotal() == null) continue;
                customers.add(c.getCustomerName() + ";" + fmtDec(c.getTotal()) + " R$");
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

            boolean opened = openWithDesktop(dir);
            if (!opened) {
                opened = openWithShell(dir);
            }

            if (!opened) {
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

    @FXML
    public void onChooseExportFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        String current = prefs.get(PREF_EXPORT_DIR, null);
        if (current != null) {
            File f = new File(current);
            if (f.exists() && f.isDirectory()) {
                chooser.setInitialDirectory(f);
            }
        } else {
            File home = new File(System.getProperty("user.home"));
            if (home.exists()) chooser.setInitialDirectory(home);
        }
        chooser.setTitle("Escolher Pasta de Exportação");
        Stage stage = (Stage) piePayments.getScene().getWindow();
        File selected = chooser.showDialog(stage);
        if (selected != null) {
            prefs.put(PREF_EXPORT_DIR, selected.getAbsolutePath());
            lastExportDir = selected.toPath();
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setHeaderText("Pasta de exportação atualizada");
            ok.setContentText(selected.getAbsolutePath());
            ok.showAndWait();
        }
    }

    private boolean openWithDesktop(Path dir) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(dir.toFile());
                    return true;
                }
            }
        } catch (Exception ignored) {
            // tenta fallback via shell
        }
        return false;
    }

    private boolean openWithShell(Path dir) {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", dir.toString()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", dir.toString()).start();
            } else {
                new ProcessBuilder("xdg-open", dir.toString()).start();
            }
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private Path getExportBaseDir() {
        String configured = prefs.get(PREF_EXPORT_DIR, null);
        Path base;
        if (configured != null && !configured.isBlank()) {
            base = Paths.get(configured);
        } else {
            String userHome = System.getProperty("user.home");
            base = Paths.get(userHome, "PedidoFacil", "exports");
        }
        try {
            Files.createDirectories(base);
        } catch (IOException ignored) {
        }
        return base;
    }

    private String fmtDec(BigDecimal v) {
        if (v == null) return "";
        var sy = new DecimalFormatSymbols(new java.util.Locale("pt", "BR"));
        sy.setDecimalSeparator(',');
        sy.setGroupingSeparator('.');
        var df = new DecimalFormat("#0.00", sy);
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
                    .map(p -> new PieChart.Data(pmLabel(p.getPaymentMethod()), p.getTotal().doubleValue()))
                    .toList();
            piePayments.setData(FXCollections.observableArrayList(pieData));

            var prodSeries = new XYChart.Series<String, Number>();
            prodSeries.setName("Top Produtos");
            for (ProductSalesView p : vm.getProductSales()) {
                if (p == null || p.getProductName() == null || p.getTotal() == null) continue;
                prodSeries.getData().add(new XYChart.Data<>(p.getProductName(), p.getTotal().doubleValue()));
            }
            barProducts.getData().setAll(prodSeries);
            addBarTooltips(barProducts);

            var dailySeries = new XYChart.Series<String, Number>();
            dailySeries.setName("Gráfico de Vendas");
            var points = new LinkedHashMap<String, BigDecimal>();
            for (DailySalesView d : vm.getDailySales()) {
                if (d == null || d.getDay() == null) continue;
                points.put(d.getDay(), d.getTotal() == null ? BigDecimal.ZERO : d.getTotal());
            }
            var df = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate s = vm.getStartDate();
            LocalDate e = vm.getEndDate();
            if (s == null || e == null) {
                var min = points.keySet().stream().map(k -> LocalDate.parse(k, df)).min(LocalDate::compareTo);
                var max = points.keySet().stream().map(k -> LocalDate.parse(k, df)).max(LocalDate::compareTo);
                if (min.isPresent() && max.isPresent()) {
                    s = min.get();
                    e = max.get();
                }
            }
            if (s != null && e != null && !e.isBefore(s)) {
                var cur = s;
                while (!cur.isAfter(e)) {
                    String key = cur.format(df);
                    points.putIfAbsent(key, BigDecimal.ZERO);
                    cur = cur.plusDays(1);
                }
            }
            var ordered = new ArrayList<>(points.keySet());
            ordered.sort(Comparator.naturalOrder());
            for (String day : ordered) {
                dailySeries.getData().add(new XYChart.Data<>(day, points.get(day).doubleValue()));
            }
            barDaily.getData().setAll(dailySeries);
            addBarTooltips(barDaily);

            tblTicket.setItems(FXCollections.observableArrayList(vm.getTicketAverages()));
            tblTopCustomers.setItems(FXCollections.observableArrayList(vm.getTopCustomers()));
        } catch (Exception ex) {
            piePayments.setData(FXCollections.observableArrayList());
            barProducts.getData().clear();
            barDaily.getData().clear();
        }
    }

    private String pmLabel(PaymentMethod m) {
        if (m == null) return "";
        return switch (m) {
            case CASH -> "Dinheiro";
            case PIX -> "PIX";
            case DEBIT -> "Débito";
            case CREDIT -> "Crédito";
            case CREDITSALE -> "Crediário";
        };
    }

    private void addBarTooltips(BarChart<String, Number> chart) {
        for (XYChart.Series<String, Number> s : chart.getData()) {
            for (XYChart.Data<String, Number> d : s.getData()) {
                String text = d.getXValue() + ": " + fmtDec(BigDecimal.valueOf(d.getYValue().doubleValue())) + " R$";
                Tooltip tp = new Tooltip(text);
                Tooltip.install(d.getNode(), tp);
            }
        }
    }
}
