package org.example.tema2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.binding.Bindings;
import org.example.tema2.structure.Product;
import org.example.tema2.structure.Restaurant;
import org.example.tema2.structure.Menu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RestaurantApplication extends Application {

    private Restaurant restaurant = new Restaurant("La Ardei");
    private ObjectMapper mapper = new ObjectMapper();
    private Path restaurantConfigFilePath = Path.of("configRestaurant.json");
    private Path menuConfigFilePath = Path.of("configMenu.json");
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("restaurantPU");

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


        deserializeRestaurant();


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

        ObservableList<Product> observableProducts = FXCollections.observableArrayList(restaurant.getProducts());
        productListView.setItems(observableProducts);

        MenuButton menuButton = new MenuButton("File");
        MenuItem importItem = new MenuItem("Importa din BD");
        MenuItem exportItem = new MenuItem("Exporta in BD");
        MenuItem saveItem = new MenuItem("Salveaza local");

        importItem.setOnAction(e -> {
            List<Product> importedProducts = importFromDB();

            // Update both the observable list AND the restaurant
            observableProducts.setAll(importedProducts);
            restaurant.getProducts().clear();
            restaurant.getProducts().addAll(importedProducts);
            syncMenuWithProducts(observableProducts);

            exportToJson(restaurant);
            System.out.println("Import selected");
        });

        exportItem.setOnAction(e -> {
            restaurant.getProducts().clear();
            restaurant.getProducts().addAll(observableProducts);
            syncMenuWithProducts(observableProducts);
            exportToDB(restaurant.getProducts());
        });

        saveItem.setOnAction(e -> {
            restaurant.getProducts().clear();
            restaurant.getProducts().addAll(observableProducts);
            syncMenuWithProducts(observableProducts);
            serializeRestaurant();
        });



        menuButton.getItems().addAll(importItem, exportItem, saveItem);

        VBox topBox = new VBox(10, menuButton, root);
        topBox.setAlignment(Pos.TOP_LEFT);
        topBox.setPadding(new Insets(10));

        Scene scene = new Scene(topBox, 720, 450);
        stage.setTitle("Restaurant \"La Ardei\"");
        stage.setScene(scene);
        stage.show();

    }

    private void syncMenuWithProducts(List<Product> source) {
        List<Product> target = restaurant.getMenu().getProducts();
        target.clear();
        target.addAll(source);
    }

    private void serializeRestaurant() {
        String jsonString = null;
        try {
            jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(restaurant);
            Files.writeString(restaurantConfigFilePath, jsonString);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        System.out.println("Successfully wrote to '" + restaurantConfigFilePath + "'");
    }

    private void deserializeRestaurant() {
        try {
            // Deserialize the Restaurant object from configRestaurant.json
            restaurant = mapper.readValue(restaurantConfigFilePath.toFile(), Restaurant.class);
            System.out.println("Successfully read from '" + restaurantConfigFilePath + "'");
            System.out.println(restaurant);

        } catch (JsonProcessingException e) {
            System.err.println("JSON syntax or mapping error in '" + restaurantConfigFilePath + "'. Please check the file content.");
        } catch (IOException e) {
            System.err.println("An error occurred while processing '" + restaurantConfigFilePath + "'.");
        }
    }


    private List<Product> importFromDB() {
        EntityManager em = emf.createEntityManager();

        List<Product> products = new ArrayList<>();
        try {
            Menu menu = em.createQuery("SELECT m FROM Menu m", Menu.class)
                    .setMaxResults(1)
                    .getSingleResult();
            products.addAll(menu.getProducts());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return products;
    }


    private void exportToDB(List<Product> products) {
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory("restaurantPU");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // Clear existing data
            em.createQuery("DELETE FROM Product p").executeUpdate();
            em.createQuery("DELETE FROM Menu m").executeUpdate();

            // Create a new Menu and persist it
            Menu newMenu = new Menu();
            em.persist(newMenu);

            // Persist each product and add to menu
            for (Product p : products) {
                // Reset ID to null so JPA treats it as a new entity
                p.setId(null);
                em.persist(p);
                newMenu.getProducts().add(p);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }





    private void exportToJson(Restaurant restaurant) {
//        Path restaurantConfigFilePath = Path.of("configRestaurant.json");
//        ObjectMapper mapper = new ObjectMapper();

        try {
            // Create parent directories if they don't exist
            if (restaurantConfigFilePath.getParent() != null) {
                Files.createDirectories(restaurantConfigFilePath.getParent());
            }

            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(restaurant);
            Files.writeString(restaurantConfigFilePath, jsonString);
            System.out.println("Successfully wrote to '" + restaurantConfigFilePath + "'");
        } catch (JsonProcessingException e) {
            System.err.println("JSON syntax or mapping error: " + e.getMessage());
            e.printStackTrace();  // Add this to see the actual error
        } catch (IOException e) {
            System.err.println("An error occurred while writing to '" + restaurantConfigFilePath + "'.");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }



}
