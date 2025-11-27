package org.example.tema2;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.shape.Box;
import javafx.stage.Stage;

import java.io.IOException;

public class RestaurantApplication extends Application {
    @Override
    public void start(Stage stage) {
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
//        stage.setTitle("Hello!");
//        stage.setScene(scene);
//        stage.show();
        StringProperty name = new SimpleStringProperty("Andei");
        Label nameLabel = new Label();
        nameLabel.textProperty().bind(name);
        System.out.println(name.getValue());

        name.setValue("Maria");
        System.out.println(name.getValue());

        Scene scene = new Scene(nameLabel, 300, 250);
        stage.setTitle("Restaurant");
        stage.setScene(scene);
        Box box = new Box();
        box.setLayoutX(300);
        box.setLayoutY(300);
        box.setStyle("-fx-background-color: black");
        stage.show();

    }
}
