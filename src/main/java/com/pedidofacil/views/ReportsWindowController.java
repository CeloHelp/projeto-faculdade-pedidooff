package com.pedidofacil.views;

import com.pedidofacil.repositories.projections.DailySalesView;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ReportsWindowController implements Initializable {

    private final ReportsViewModel vm;

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
