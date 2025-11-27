package org.example.tema2;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Box;
import javafx.stage.Stage;
import org.example.tema2.structure.Product;
import org.example.tema2.structure.Restaurant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestaurantApplication extends Application {
    @Override
    public void start(Stage stage) {
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
//        stage.setTitle("Hello!");
//        stage.setScene(scene);
//        stage.show();
//        StringProperty name = new SimpleStringProperty("Andei");
//        Label nameLabel = new Label();
//        nameLabel.textProperty().bind(name);
//        System.out.println(name.getValue());
//
//        name.setValue("Maria");
//        System.out.println(name.getValue());
//
//        Scene scene = new Scene(nameLabel, 300, 250);
//        stage.setTitle("Restaurant");
//        stage.setScene(scene);
//        Box box = new Box();
//        box.setLayoutX(300);
//        box.setLayoutY(300);
//        box.setStyle("-fx-background-color: black");
//        stage.show();



//        Restaurant restaurant = new Restaurant("La Ardei");
//        List<Product> products = restaurant.getProducts();
//
//
//        GridPane formGrid = new GridPane();
//        formGrid.setHgap(10);
//        formGrid.setVgap(10);
//        formGrid.add(new Label("Name:"), 0, 0);
//        formGrid.add(nameField, 1, 0);
//
//        VBox root = new VBox(15, titleLabel, formGrid, saveButton, summaryArea);
//        root.setAlignment(Pos.TOP_CENTER);
//        root.setPadding(new Insets(20));
//
//        Scene scene = new Scene(root, 400, 450);
//        stage.setTitle("Profile App - Reactive");
//        stage.setScene(scene);
//        stage.show();



        Restaurant restaurant = new Restaurant("La Ardei");
        List<Product> products = restaurant.getProducts();

        // Left: list of products
        ListView<Product> productListView = new ListView<>();
        productListView.setItems(FXCollections.observableArrayList(products));
        productListView.setPrefWidth(180);
        productListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                } else {
                    // show a human-friendly label; replace with p.getName() if available
                    setText(p.toString());
                }
            }
        });

        // Right: form and summary (define missing controls)
        Label titleLabel = new Label("Product Editor");
        TextField nameField = new TextField();
        Button saveButton = new Button("Save");
        TextArea summaryArea = new TextArea();
        summaryArea.setEditable(false);
        summaryArea.setPrefRowCount(10);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.add(new Label("Name:"), 0, 0);
        formGrid.add(nameField, 1, 0);

        VBox rightBox = new VBox(10, titleLabel, formGrid, saveButton, summaryArea);
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.setPadding(new Insets(10));
        rightBox.setPrefWidth(300);

        // Main layout: left and right sections
        HBox root = new HBox(20, productListView, rightBox);
        root.setPadding(new Insets(20));

        // Optional: select product to populate form/summary
        productListView.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            if (newP != null) {
                // adjust to actual Product API (e.g., getName/getDescription)
                nameField.setText(newP.toString());
                summaryArea.setText(newP.toString());
            }
        });

        Scene scene = new Scene(root, 520, 450);
        stage.setTitle("Restaurant - Products");
        stage.setScene(scene);
        stage.show();

    }
}
