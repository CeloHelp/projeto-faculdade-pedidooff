module com.pedidooff {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.data.jpa;
    requires spring.tx;
    requires jakarta.persistence;

    opens com.pedidooff to javafx.fxml, spring.core, spring.beans, spring.context;
    opens com.pedidooff.service to spring.core, spring.beans, spring.context;
    opens com.pedidooff.repository to spring.core, spring.beans, spring.context;
    opens com.pedidooff.model to org.hibernate.orm.core, spring.core, spring.beans, spring.context;

    exports com.pedidooff;
    exports com.pedidooff.model;
}
