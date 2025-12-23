package org.example.tema2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.binding.Bindings;
import org.example.tema2.model.*;
import org.example.tema2.repo.CredentialsRepository;
import org.example.tema2.structure.Order;
import org.example.tema2.structure.Restaurant;
import org.example.tema2.structure.Menu;
import org.example.tema2.view.Views;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestaurantApplication extends Application {

    private static Restaurant restaurant;
    private ObjectMapper mapper = new ObjectMapper();
    private final Path restaurantConfigFilePath = Path.of("configRestaurant.json");
    private final Path menuConfigFilePath = Path.of("configMenu.json");
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("restaurantPU");
    private static FilteredList<Product> filtered;

    static Views views;

    @Override
    public void start(Stage stage) {


        deserializeRestaurant();
        initWaiters();
        views = new Views(emf, stage, restaurant);

        // insertInitialCredentials(); --> Don't call again
        welcomeView();

    }

    private void initWaiters() {
        restaurant.setWaiters(List.of(
                new Waiter("Ionel"), new Waiter("Marcel"),
                new Waiter("Mirel"), new Waiter("Cornel"),
                new Waiter("Costel")
        ));
    }

    public void insertInitialCredentials() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Credentials credentialsWaiter =
                    new Credentials("Waiter123", "secretPassword", Credentials.Role.WAITER);

            Credentials credentialsManager =
                    new Credentials("MaestrulMania", "admin123", Credentials.Role.ADMIN);

            em.persist(credentialsWaiter);
            em.persist(credentialsManager);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }


    public static void welcomeView() {
        Stage stage = views.getStage();
        stage.setTitle("Restaurant \"La Ardei\"");
        stage.setScene(views.getWelcomeScene());
        stage.show();
    }

    public static void buttonGuestAction() {
        Stage stage = views.getStage();
        stage.setTitle("Restaurant \"La Ardei\"");
        stage.setScene(views.getClientScene());
    }

    public static void applyFiltersAndSearch(ObservableList<Product> observableProducts, ListView<Product> productListView,
                                             CheckBox vegetarianCheckBox, CheckBox foodCheckBox, CheckBox drinkCheckBox,
                                             Spinner<Integer> minPriceSpinner, Spinner<Integer> maxPriceSpinner,
                                             TextField searchField) {
        filtered.setPredicate(product -> {
            // 1. Text search
            String text = searchField.getText().toLowerCase();
            boolean matchesText = text.isEmpty() || product.getName().toLowerCase().contains(text);

            // 2. Vegetarian filter
            boolean matchesVegetarian = !vegetarianCheckBox.isSelected() || product.isVegetarian();

            // 3. Type filter
            boolean matchesType =
                    (foodCheckBox.isSelected() && product instanceof Food)
                            || (drinkCheckBox.isSelected() && product instanceof Drink)
                            || (!foodCheckBox.isSelected() && !drinkCheckBox.isSelected()); // optional: if none selected, show all

            // 4. Price filter
            double price = product.getPrice();
            boolean matchesPrice = price >= minPriceSpinner.getValue() && price <= maxPriceSpinner.getValue();

            // Combine all filters
            return matchesText && matchesVegetarian && matchesType && matchesPrice;
        });

    }

    public static void loginView(Stage stage) {

        // Login

        Label nameLabel = new Label("Nume: ");
        TextField nameField = new TextField();

        HBox nameContainer = new HBox(10, nameLabel, nameField);
        nameContainer.setAlignment(Pos.CENTER);
        nameContainer.setPadding(new Insets(10));
        nameContainer.setSpacing(10);

        Label passwordLabel = new Label("Parola: ");
        TextField passwordField = new TextField();

        HBox passwordContainer = new HBox(10, passwordLabel, passwordField);
        passwordContainer.setAlignment(Pos.CENTER);
        passwordContainer.setPadding(new Insets(10));
        passwordContainer.setSpacing(10);

        Button loginButton = new Button("Login");
        loginButton.setPrefSize(100, 40);
        loginButton.setOnAction(e -> {
            String name = nameField.getText();
            String password = passwordField.getText();

            CredentialsRepository credentialsRepo = new CredentialsRepository(emf);

            Optional<Credentials.Role> roleOptional = credentialsRepo.getRoleByUsernameAndPassword(name, password);

            if (roleOptional.isPresent()) {
                if (roleOptional.get() == Credentials.Role.ADMIN) {
                    System.out.println("LOGIN SUCCESSFUL: [ADMIN]");
                    managerView(stage);
                }
                else if (roleOptional.get() == Credentials.Role.WAITER) {
                    System.out.println("LOGIN SUCCESSFUL: [WAITER]");
                    waiterView(stage);
                }
            }
            else {
                System.out.println("LOGIN ERORR: Username or password is incorrect");
            }
        });

        VBox rootBox = new VBox(0, nameContainer, passwordContainer, loginButton);
        rootBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(rootBox, 720, 450);
        stage.setTitle("Restaurant \"La Ardei\"");
        stage.setScene(scene);
    }

    private static void waiterView(Stage stage) {

        HBox newOfferView = views.getWaiterNewOrderTabView();
        HBox offerHistoryView = views.getWaiterOfferHistoryTab();

        TabPane tabPane = new TabPane();
        Tab newOfferTab = new Tab("Oferta Noua");
        newOfferTab.setContent(newOfferView);
        Tab offerHistoryTab = new Tab("Istoric oferte");
        offerHistoryTab.setContent(offerHistoryView);

        tabPane.getTabs().addAll(newOfferTab, offerHistoryTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Button goBackButton = new Button("Inapoi");
        goBackButton.setOnAction(e -> welcomeView());

        VBox root = new VBox(10, goBackButton, tabPane);
        root.setPadding(new Insets(10));
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 900, 450);
        stage.setScene(scene);
    }

    private static void managerView(Stage stage) {
        stage.setScene(views.getManagerScene());
    }

    private void Iteratia6(Stage stage) {
        List<Product> products = restaurant.getProducts();

        // Left: list of products
        ListView<Product> productListView = new ListView<>();
        productListView.setItems(FXCollections.observableArrayList(products));
        productListView.setPrefWidth(400);

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
        // stage.show();
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
            restaurant = new Restaurant("La Ardei");
        } catch (IOException e) {
            System.err.println("An error occurred while processing '" + restaurantConfigFilePath + "'. Using default restaurant.");
            restaurant = new Restaurant("La Ardei");
        }
    }

    private List<Product> importFromDB() {
        EntityManager em = emf.createEntityManager();
        List<Product> products = new ArrayList<>();
        try {
            // Directly query for all products instead of through a Menu
            products = em.createQuery("SELECT p FROM Product p", Product.class)
                    .getResultList();
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
    public void stop() throws Exception {
        try {
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        } finally {
            super.stop();
        }
    }

    public static FilteredList<Product> getFiltered() {
        return filtered;
    }

    public static void setFiltered(FilteredList<Product> filtered) {
        RestaurantApplication.filtered = filtered;
    }
}