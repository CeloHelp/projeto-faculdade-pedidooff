module com.pedidooff {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.pedidooff to javafx.fxml;
    exports com.pedidooff;
}
