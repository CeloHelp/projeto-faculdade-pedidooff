module com.pedidooff {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;

    opens com.pedidooff to javafx.fxml, spring.core, spring.beans, spring.context;
    exports com.pedidooff;
}
