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
import org.example.tema2.model.Credentials;
import org.example.tema2.model.Drink;
import org.example.tema2.model.Food;
import org.example.tema2.model.Product;
import org.example.tema2.repo.CredentialsRepository;
import org.example.tema2.structure.Restaurant;
import org.example.tema2.structure.Menu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RestaurantApplication extends Application {

    private Restaurant restaurant = new Restaurant("La Ardei");
    private ObjectMapper mapper = new ObjectMapper();
    private Path restaurantConfigFilePath = Path.of("configRestaurant.json");
    private Path menuConfigFilePath = Path.of("configMenu.json");
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("restaurantPU");
    FilteredList<Product> filtered;

    ListView<Product> productListView;
    VBox productDetailsView;

    @Override
    public void start(Stage stage) {

        deserializeRestaurant();

        // WelcomePage

        // insertInitialCredentials(); --> Don't call again
        welcomeView(stage);


        // Iteratia6(stage);

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


    private void welcomeView(Stage stage) {
        Button buttonGuest = new Button("Client");
        buttonGuest.setPrefSize(150, 60);
        Button buttonWaiterManager = new Button("Ospatar sau Manager");
        buttonWaiterManager.setPrefSize(150, 60);
        HBox root = new HBox(buttonGuest, buttonWaiterManager);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setSpacing(10);
        Scene scene = new Scene(root, 720, 450);
        stage.setTitle("Restaurant \"La Ardei\"");
        stage.setScene(scene);
        stage.show();

        initViews();

        buttonGuest.setOnAction(e -> {
            buttonGuestAction(stage);
        });

        buttonWaiterManager.setOnAction(e -> {
            buttonLoginAction(stage); // To Login
        });
    }

    private void initViews() {
        List<Product> products = restaurant.getProducts();

        // Left: list of products
        productListView = new ListView<>();
        productListView.setItems(FXCollections.observableArrayList(products));
        productListView.setPrefWidth(400);

        // Right
        Label titleLabel = new Label("Detalii despre produs");
        Label nameLabel = new Label();
        Label priceLabel = new Label();
        Label volumeWeightLabelText = new Label("Volum / gramaj: ");
        Label volumeWeightLabelValue = new Label();

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.add(new Label("Nume: "), 0, 0);
        formGrid.add(nameLabel, 1, 0);
        formGrid.add(new Label("Pret: "), 0, 1);
        formGrid.add(priceLabel, 1, 1);
        formGrid.add(volumeWeightLabelText, 0, 2);
        formGrid.add(volumeWeightLabelValue, 1, 2);

        productDetailsView = new VBox(10, titleLabel, formGrid);
        productDetailsView.setAlignment(Pos.TOP_CENTER);
        productDetailsView.setPadding(new Insets(10));
        productDetailsView.setPrefWidth(300);

        productListView.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            // Unbind from the old product first
            if (oldP != null) {
                nameLabel.textProperty().unbindBidirectional(oldP.nameProperty());
                Bindings.unbindBidirectional(priceLabel.textProperty(), oldP.priceProperty());

                if(oldP instanceof Food)
                    Bindings.unbindBidirectional(volumeWeightLabelValue.textProperty(), ((Food)oldP).weightProperty());
                if(oldP instanceof Drink)
                    Bindings.unbindBidirectional(volumeWeightLabelValue.textProperty(), ((Drink)oldP).volumeProperty());

            }

            if (newP != null) {
                nameLabel.setText(newP.getName());
                priceLabel.setText(String.valueOf(newP.getPrice()));
                if(newP instanceof Food) {
                    volumeWeightLabelText.setText("Gramaj: ");
                    volumeWeightLabelValue.setText(String.valueOf(((Food) newP).getWeight()));
                }
                if (newP instanceof Drink) {
                    volumeWeightLabelText.setText("Volum: ");
                    volumeWeightLabelValue.setText(String.valueOf(((Drink) newP).getVolume()));
                }

                nameLabel.textProperty().bindBidirectional(newP.nameProperty());
                Bindings.bindBidirectional(priceLabel.textProperty(), newP.priceProperty(), new javafx.util.converter.NumberStringConverter());

                if(oldP instanceof Food)
                    Bindings.bindBidirectional(volumeWeightLabelValue.textProperty(), ((Food)oldP).weightProperty(), new javafx.util.converter.NumberStringConverter());
                if(oldP instanceof Drink)
                    Bindings.bindBidirectional(volumeWeightLabelValue.textProperty(), ((Drink)oldP).volumeProperty(), new javafx.util.converter.NumberStringConverter());
            } else {
                nameLabel.setText("");
                priceLabel.setText("");
                volumeWeightLabelValue.setText("");
            }
        });
    }

    private void buttonGuestAction(Stage stage) {
        List<Product> products = restaurant.getProducts();

        // Main layout: left and right sections
        HBox root = new HBox(20, productListView, productDetailsView);
        root.setPadding(new Insets(20));

        ObservableList<Product> observableProducts = FXCollections.observableArrayList(restaurant.getProducts());
        filtered = new FilteredList<>(observableProducts, t -> true);
        productListView.setItems(filtered);

        MenuButton filterDropdown = new MenuButton("Filtre");

        CheckBox vegetarianCheckBox = new CheckBox();
        HBox vegetarianHBox = new HBox(10);
        vegetarianHBox.getChildren().addAll(
                new Label("Doar vegetariene"),
                vegetarianCheckBox
        );

        CheckBox foodCheckBox = new CheckBox("Mancare");
        foodCheckBox.setSelected(true);
        CheckBox drinkCheckBox = new CheckBox("Bautura");
        drinkCheckBox.setSelected(true);
        HBox foodTypeHBox = new HBox(10);
        foodTypeHBox.getChildren().addAll(
                new Label("Tip"),
                foodCheckBox,
                drinkCheckBox
        );


        Spinner<Integer> minPriceSpinner = new Spinner<>(0, 500, 0);
        minPriceSpinner.setEditable(true); // allows typing
        Spinner<Integer> maxPriceSpinner = new Spinner<>(0, 500, 500);
        maxPriceSpinner.setEditable(true); // allows typing

        HBox priceIntervalHBox = new HBox(10);
        priceIntervalHBox.getChildren().addAll(
                new Label("Tip"),
                minPriceSpinner,
                maxPriceSpinner
        );

        Button applyFiltersButton = new Button("Aplica");

        CustomMenuItem vegetarianHBoxDropdownItem = new CustomMenuItem(vegetarianHBox);
        vegetarianHBoxDropdownItem.setHideOnClick(false); // optional

        CustomMenuItem foodTypeHBoxDropdownItem = new CustomMenuItem(foodTypeHBox);
        foodTypeHBoxDropdownItem.setHideOnClick(false); // optional

        CustomMenuItem priceIntervalHBoxDropdownItem = new CustomMenuItem(priceIntervalHBox);
        priceIntervalHBoxDropdownItem.setHideOnClick(false); // optional

        CustomMenuItem applyFiltersButtonDropdownItem = new CustomMenuItem(applyFiltersButton);

        filterDropdown.getItems().addAll(vegetarianHBoxDropdownItem, foodTypeHBoxDropdownItem,
                priceIntervalHBoxDropdownItem, applyFiltersButtonDropdownItem);

        TextField searchField = new TextField();
        searchField.setPromptText("Nume...");
        searchField.setMaxWidth(300);
        Button searchButton = new Button("Cauta");
        searchButton.setOnAction(e -> {
            applyFiltersAndSearch(observableProducts, productListView, vegetarianCheckBox, foodCheckBox,
                    drinkCheckBox, minPriceSpinner, maxPriceSpinner, searchField);
        });
        applyFiltersButton.setOnAction(e -> {
            applyFiltersAndSearch(observableProducts, productListView, vegetarianCheckBox, foodCheckBox,
                    drinkCheckBox, minPriceSpinner, maxPriceSpinner, searchField);
        });

        HBox topLeftBox = new HBox(10, filterDropdown, searchField, searchButton);

        Button goBackButton = new Button("Înapoi");
        goBackButton.setOnAction(e -> {welcomeView(stage);});

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBox = new HBox(10, topLeftBox, spacer, goBackButton);
        topBox.setAlignment(Pos.TOP_LEFT);
        topBox.setPadding(new Insets(10));

        VBox rootBox = new VBox(10, topBox, root);

        Scene scene = new Scene(rootBox, 720, 450);
        stage.setTitle("Restaurant \"La Ardei\"");
        stage.setScene(scene);
        // stage.show();
    }

    private void applyFiltersAndSearch(ObservableList<Product> observableProducts, ListView<Product> productListView,
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

    private void buttonLoginAction(Stage stage) {

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

            Credentials credentials = new Credentials(name, password);

            EntityManagerFactory credentialsEmf;
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

    private void waiterView(Stage stage) {
    }

    private void managerView(Stage stage) {
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
        } catch (IOException e) {
            System.err.println("An error occurred while processing '" + restaurantConfigFilePath + "'.");
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



}
