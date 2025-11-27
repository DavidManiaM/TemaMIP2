module org.example.tema2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires javafx.base;
    requires javafx.graphics;


    opens org.example.tema2 to javafx.fxml;
    exports org.example.tema2;
}