module org.example.tema2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires javafx.base;

    opens org.example.tema2.structure to com.fasterxml.jackson.databind, org.hibernate.orm.core;
    exports org.example.tema2.structure;
    exports org.example.tema2;
    exports org.example.tema2.structure.utils;
    opens org.example.tema2 to javafx.fxml;
    exports org.example.tema2.model;
    opens org.example.tema2.model to com.fasterxml.jackson.databind, org.hibernate.orm.core;
}
