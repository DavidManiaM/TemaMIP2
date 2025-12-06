module org.example.tema2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    opens org.example.tema2.structure to com.fasterxml.jackson.databind, org.hibernate.orm.core;
    exports org.example.tema2.structure;
    exports org.example.tema2;
    opens org.example.tema2 to javafx.fxml;
}
