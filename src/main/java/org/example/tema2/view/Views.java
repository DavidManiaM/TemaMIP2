package org.example.tema2.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.tema2.RestaurantApplication;
import org.example.tema2.model.Drink;
import org.example.tema2.model.Food;
import org.example.tema2.model.Product;
import org.example.tema2.structure.Restaurant;

public class Views {
    Stage stage;
    Restaurant restaurant;
    HBox welcomeView;
    ListView<Product> productListView;
    VBox productDetailsView;
    VBox clientView;

    private Scene welcomeScene;
    private Scene clientScene;

    public Views(Stage stage, Restaurant restaurant) {
        this.stage = stage;
        this.restaurant = restaurant;
        initProductListView();
        initProductDetailsView();
        initClientView(); // Creates clientView and clientScene
        initWelcomeView(); // Creates welcomeView and welcomeScene
    }

    public void initClientView() {
        HBox root = new HBox(20, productListView, productDetailsView);
        root.setPadding(new Insets(20));

        ObservableList<Product> observableProducts = FXCollections.observableArrayList(restaurant.getProducts());
        RestaurantApplication.filtered = new FilteredList<>(observableProducts, t -> true);
        productListView.setItems(RestaurantApplication.filtered);

        MenuButton filterDropdown = new MenuButton("Filtre");

        CheckBox vegetarianCheckBox = new CheckBox();
        HBox vegetarianHBox = new HBox(10, new Label("Doar vegetariene"), vegetarianCheckBox);

        CheckBox foodCheckBox = new CheckBox("Mancare");
        foodCheckBox.setSelected(true);
        CheckBox drinkCheckBox = new CheckBox("Bautura");
        drinkCheckBox.setSelected(true);
        HBox foodTypeHBox = new HBox(10, new Label("Tip"), foodCheckBox, drinkCheckBox);

        Spinner<Integer> minPriceSpinner = new Spinner<>(0, 500, 0);
        minPriceSpinner.setEditable(true);
        Spinner<Integer> maxPriceSpinner = new Spinner<>(0, 500, 500);
        maxPriceSpinner.setEditable(true);

        HBox priceIntervalHBox = new HBox(10, new Label("Pret"), minPriceSpinner, maxPriceSpinner);

        Button applyFiltersButton = new Button("Aplica");

        CustomMenuItem vegetarianHBoxDropdownItem = new CustomMenuItem(vegetarianHBox);
        vegetarianHBoxDropdownItem.setHideOnClick(false);

        CustomMenuItem foodTypeHBoxDropdownItem = new CustomMenuItem(foodTypeHBox);
        foodTypeHBoxDropdownItem.setHideOnClick(false);

        CustomMenuItem priceIntervalHBoxDropdownItem = new CustomMenuItem(priceIntervalHBox);
        priceIntervalHBoxDropdownItem.setHideOnClick(false);

        CustomMenuItem applyFiltersButtonDropdownItem = new CustomMenuItem(applyFiltersButton);

        filterDropdown.getItems().addAll(vegetarianHBoxDropdownItem, foodTypeHBoxDropdownItem,
                priceIntervalHBoxDropdownItem, applyFiltersButtonDropdownItem);

        TextField searchField = new TextField();
        searchField.setPromptText("Nume...");
        searchField.setMaxWidth(300);
        Button searchButton = new Button("Cauta");
        searchButton.setOnAction(e -> RestaurantApplication.applyFiltersAndSearch(observableProducts, productListView, vegetarianCheckBox, foodCheckBox,
                drinkCheckBox, minPriceSpinner, maxPriceSpinner, searchField));
        applyFiltersButton.setOnAction(e -> RestaurantApplication.applyFiltersAndSearch(observableProducts, productListView, vegetarianCheckBox, foodCheckBox,
                drinkCheckBox, minPriceSpinner, maxPriceSpinner, searchField));

        HBox topLeftBox = new HBox(10, filterDropdown, searchField, searchButton);

        Button goBackButton = new Button("Înapoi");
        goBackButton.setOnAction(e -> RestaurantApplication.welcomeView());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBox = new HBox(10, topLeftBox, spacer, goBackButton);
        topBox.setAlignment(Pos.TOP_LEFT);
        topBox.setPadding(new Insets(10));

        clientView = new VBox(10, topBox, root);
        clientScene = new Scene(clientView, 720, 450);
    }

    public void initProductDetailsView() {
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
            if (newP != null) {
                nameLabel.setText(newP.getName());
                priceLabel.setText(String.valueOf(newP.getPrice()));
                if (newP instanceof Food) {
                    volumeWeightLabelText.setText("Gramaj: ");
                    volumeWeightLabelValue.setText(String.valueOf(((Food) newP).getWeight()));
                }
                if (newP instanceof Drink) {
                    volumeWeightLabelText.setText("Volum: ");
                    volumeWeightLabelValue.setText(String.valueOf(((Drink) newP).getVolume()));
                }
            } else {
                nameLabel.setText("");
                priceLabel.setText("");
                volumeWeightLabelValue.setText("");
            }
        });
    }

    public void initProductListView() {
        productListView = new ListView<>();
        productListView.setItems(FXCollections.observableArrayList(restaurant.getProducts()));
        productListView.setPrefWidth(400);
    }

    public void initWelcomeView() {
        Button buttonGuest = new Button("Client");
        buttonGuest.setPrefSize(150, 60);
        Button buttonWaiterManager = new Button("Ospatar sau Manager");
        buttonWaiterManager.setPrefSize(150, 60);
        welcomeView = new HBox(buttonGuest, buttonWaiterManager);
        welcomeView.setAlignment(Pos.CENTER);
        welcomeView.setPadding(new Insets(10, 10, 10, 10));
        welcomeView.setSpacing(10);

        buttonGuest.setOnAction(e -> RestaurantApplication.buttonGuestAction());
        buttonWaiterManager.setOnAction(e -> RestaurantApplication.loginView(stage));

        welcomeScene = new Scene(welcomeView, 720, 450);
    }

    public Scene getWelcomeScene() {
        return welcomeScene;
    }

    public Scene getClientScene() {
        return clientScene;
    }

    public Stage getStage() {
        return stage;
    }
}
