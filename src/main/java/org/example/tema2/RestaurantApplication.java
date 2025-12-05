package org.example.tema2;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
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
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Stage;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import org.example.tema2.structure.Product;
import org.example.tema2.structure.Restaurant;
import org.example.tema2.structure.Menu;

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
        productListView.setPrefWidth(400);
//        productListView.setCellFactory(lv -> new ListCell<>() {
//            @Override
//            protected void updateItem(Product p, boolean empty) {
//                super.updateItem(p, empty);
//                if (empty || p == null) {
//                    setText(null);
//                } else {
//                    setText(p.toString());
//                }
//            }
//        });

        // Right
        Label titleLabel = new Label("Editor de produse");
        TextField nameField = new TextField();
        nameField.setPromptText("Nume produs");
        TextField priceField = new TextField();
        priceField.setPromptText("Pret");
        TextArea summaryArea = new TextArea();
        summaryArea.setEditable(false);
        summaryArea.setPrefRowCount(10);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.add(new Label("Nume:"), 0, 0);
        formGrid.add(nameField, 1, 0);
        formGrid.add(new Label("Pret:"), 0, 1);
        formGrid.add(priceField, 1, 1);

        VBox rightBox = new VBox(10, titleLabel, formGrid, summaryArea);
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.setPadding(new Insets(10));
        rightBox.setPrefWidth(300);

        // Main layout: left and right sections
        HBox root = new HBox(20, productListView, rightBox);
        root.setPadding(new Insets(20));

        productListView.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            // Unbind from the old product first
            if (oldP != null) {
                nameField.textProperty().unbindBidirectional(oldP.nameProperty());
                Bindings.unbindBidirectional(priceField.textProperty(), oldP.priceProperty());
            }

            if (newP != null) {
                nameField.setText(newP.getName());
                priceField.setText(String.valueOf(newP.getPrice()));
                summaryArea.setText(newP.getName());

                nameField.textProperty().bindBidirectional(newP.nameProperty());
                Bindings.bindBidirectional(priceField.textProperty(), newP.priceProperty(), new javafx.util.converter.NumberStringConverter());
            } else {
                nameField.clear();
                priceField.clear();
                summaryArea.clear();
            }
        });


        Scene scene = new Scene(root, 720, 450);
        stage.setTitle("Restaurant \"La Ardei\"");
        stage.setScene(scene);
        stage.show();











        resetAndPopulateDatabase();


    }

    private void resetAndPopulateDatabase() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("restaurantPU");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // Delete all data
            em.createQuery("DELETE FROM Food").executeUpdate();
            em.createQuery("DELETE FROM Drink").executeUpdate();
            em.createQuery("DELETE FROM Product").executeUpdate();
            em.createQuery("DELETE FROM Menu").executeUpdate();

            // Insert new data
            Menu menu = new Menu();
            menu.initializeDefaultProducts();
            em.persist(menu);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }

}
