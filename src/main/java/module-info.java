module org.example.tema2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires javafx.base;
    requires javafx.graphics;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;

    opens org.example.tema2 to org.hibernate.orm.core;
    opens org.example.tema2.structure to org.hibernate.orm.core;

    exports org.example.tema2;
    uses jakarta.persistence.spi.PersistenceProvider;
}
