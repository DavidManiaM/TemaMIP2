package org.example.tema2.view;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
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
import org.example.tema2.model.*;
import org.example.tema2.structure.Order;
import org.example.tema2.structure.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class Views {
    private Stage stage;
    private Restaurant restaurant;
    private HBox welcomeView;
    private ListView<Product> productListView;
    private VBox productDetailsView;
    private VBox clientView;
    private HBox waiterNewOrderTabView;
    private HBox waiterOfferHistoryTab;

    private Scene welcomeScene;
    private Scene clientScene;

    private EntityManagerFactory emf;

    public Views(EntityManagerFactory emf, Stage stage, Restaurant restaurant) {
        this.emf = emf;
        this.stage = stage;
        this.restaurant = restaurant;
        initProductListView();
        initProductDetailsView();
        initClientView();
        initWelcomeView();
        initWaiterNewOrderTabView();
        initWaiterOfferHistoryTab();
    }

    private void initWaiterOfferHistoryTab() {
        ListView<Order> orderListView = new ListView<>();
        EntityManager em = emf.createEntityManager();
        try {
            // Assuming the logged-in waiter has ID = 1 for this example.
            // In a real application, you would pass the actual logged-in waiter's ID.
            long waiterId = 1L;
            List<Order> orders = em.createQuery("SELECT o FROM Order o WHERE o.waiter.id = :waiterId", Order.class)
                    .setParameter("waiterId", waiterId)
                    .getResultList();
            orderListView.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not fetch order history.");
        } finally {
            em.close();
        }

        waiterOfferHistoryTab = new HBox(orderListView);
        waiterOfferHistoryTab.setPadding(new Insets(10));
    }

    private void initWaiterNewOrderTabView() {
        List<Table> tables = restaurant.getTables();

        // Left: List of tables
        ListView<Table> tableListView = new ListView<>();
        tableListView.setItems(FXCollections.observableArrayList(tables));
        tableListView.setPrefWidth(200);

        // Center: List of available products (Create a NEW instance for the waiter view)
        ListView<Product> waiterProductListView = new ListView<>();
        waiterProductListView.setItems(FXCollections.observableArrayList(restaurant.getProducts()));
        waiterProductListView.setPrefWidth(400);

        // Right: Order details
        Label currentOrderLabel = new Label("Comanda curentă:");
        ListView<Product> orderProductsListView = new ListView<>();
        orderProductsListView.setPrefHeight(150);
        Label totalLabel = new Label("Total: 0.0");
        Label specialOfferLabel = new Label(); // Label for special offers

        Button addProductButton = new Button("+");
        Button removeProductButton = new Button("-");
        Button addOrderButton = new Button("Adaugă Comanda");
        HBox waiterOrderAddRemoveButtonsContainer = new HBox(10, addProductButton, removeProductButton);

        // Create a NEW details view instance for the waiter view
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

        VBox waiterProductDetailsView = new VBox(10, titleLabel, formGrid);
        waiterProductDetailsView.setAlignment(Pos.TOP_CENTER);
        waiterProductDetailsView.setPadding(new Insets(10));
        waiterProductDetailsView.setPrefWidth(300);

        // Bind the new details view to the new list view
        waiterProductListView.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
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

        VBox waiterRightContainer = new VBox(10, currentOrderLabel, orderProductsListView, totalLabel, specialOfferLabel, waiterProductDetailsView, waiterOrderAddRemoveButtonsContainer, addOrderButton);
        waiterRightContainer.setPrefWidth(300);

        // Event handler for table selection
        tableListView.getSelectionModel().selectedItemProperty().addListener((obs, oldTable, newTable) -> {
            if (newTable != null) {
                Order currentOrder = newTable.getCurrentOrder();
                if (currentOrder != null) {
                    currentOrder.applyOffer();
                    orderProductsListView.setItems(FXCollections.observableArrayList(currentOrder.getProducts()));
                    totalLabel.setText("Total: " + currentOrder.getTotalPrice());
                    currentOrder.activeSpecialOffer.ifPresentOrElse(
                            offer -> {
                                if (offer.isApplicable()) {
                                    specialOfferLabel.setText("Ofertă activă: " + offer.getName());
                                } else {
                                    specialOfferLabel.setText("");
                                }
                            },
                            () -> specialOfferLabel.setText("")
                    );
                } else {
                    orderProductsListView.getItems().clear();
                    totalLabel.setText("Total: 0.0");
                    specialOfferLabel.setText("");
                }
            }
        });

        // Event handler for the '+' button (Use waiterProductListView)
        addProductButton.setOnAction(e -> {
            Table selectedTable = tableListView.getSelectionModel().getSelectedItem();
            Product selectedProduct = waiterProductListView.getSelectionModel().getSelectedItem();

            if (selectedTable != null && selectedProduct != null) {
                Order currentOrder = selectedTable.getCurrentOrder();
                if (currentOrder == null) {
                    currentOrder = new Order();
                    selectedTable.setCurrentOrder(currentOrder);
                }
                currentOrder.addProduct(selectedProduct);
                currentOrder.applyOffer();

                // Refresh the order view
                orderProductsListView.setItems(FXCollections.observableArrayList(currentOrder.getProducts()));
                totalLabel.setText("Total: " + currentOrder.getTotalPrice());
                currentOrder.activeSpecialOffer.ifPresentOrElse(
                        offer -> {
                            if (offer.isApplicable()) {
                                specialOfferLabel.setText("Ofertă activă: " + offer.getName());
                            } else {
                                specialOfferLabel.setText("");
                            }
                        },
                        () -> specialOfferLabel.setText("")
                );
            } else {
                System.out.println("Vă rugăm selectați o masă și un produs.");
            }
        });

        // Event handler for the '-' button
        removeProductButton.setOnAction(e -> {
            Table selectedTable = tableListView.getSelectionModel().getSelectedItem();
            Product productToRemove = orderProductsListView.getSelectionModel().getSelectedItem();

            if (selectedTable != null && productToRemove != null) {
                Order currentOrder = selectedTable.getCurrentOrder();
                if (currentOrder != null) {
                    currentOrder.removeProduct(productToRemove);
                    currentOrder.applyOffer();

                    // Refresh the order view
                    orderProductsListView.setItems(FXCollections.observableArrayList(currentOrder.getProducts()));
                    totalLabel.setText("Total: " + currentOrder.getTotalPrice());
                    currentOrder.activeSpecialOffer.ifPresentOrElse(
                            offer -> {
                                if (offer.isApplicable()) {
                                    specialOfferLabel.setText("Ofertă activă: " + offer.getName());
                                } else {
                                    specialOfferLabel.setText("");
                                }
                            },
                            () -> specialOfferLabel.setText("")
                    );
                }
            } else {
                System.out.println("Vă rugăm selectați o masă și un produs din comandă pentru a-l șterge.");
            }
        });

        addOrderButton.setOnAction(e -> {
            Table selectedTable = tableListView.getSelectionModel().getSelectedItem();
            if (selectedTable != null && selectedTable.getCurrentOrder() != null) {
                Order currentOrder = selectedTable.getCurrentOrder();

                if (currentOrder.getProducts() == null || currentOrder.getProducts().isEmpty()) {
                    System.out.println("Cannot add an empty order.");
                    return;
                }

                EntityManager em = emf.createEntityManager();
                try {
                    em.getTransaction().begin();

                    // Create a new Order entity for persistence
                    Order newOrderToPersist = new Order();

                    List<Waiter> waiters = em.createQuery("SELECT w FROM Waiter w", Waiter.class).getResultList();
                    if (waiters.isEmpty()) {
                        for (Waiter w : restaurant.getWaiters()) {
                            em.persist(w);
                            waiters.add(w);
                        }
                    }
                    Waiter assignedWaiter = waiters.get((int) (Math.random() * waiters.size()));
                    newOrderToPersist.setWaiter(assignedWaiter);

                    // Copy products from the temporary order to the new one
                    newOrderToPersist.setProducts(currentOrder.getOrderElements());
                    newOrderToPersist.setTableNumber(selectedTable.getNumber());
                    newOrderToPersist.setTotalPrice(currentOrder.getTotalPrice());

                    em.persist(newOrderToPersist);

                    if (assignedWaiter.getOrders() == null) {
                        assignedWaiter.setOrders(new ArrayList<>());
                    }
                    assignedWaiter.getOrders().add(newOrderToPersist);
                    em.merge(assignedWaiter);

                    if (restaurant.getOrders() == null) {
                        restaurant.setOrders(new ArrayList<>());
                    }
                    restaurant.getOrders().add(newOrderToPersist);

                    em.getTransaction().commit();

                    System.out.println("Comanda a fost adăugată cu succes pentru masa " + selectedTable.getNumber());

                    // Clear the temporary order from the table view
                    selectedTable.setCurrentOrder(null);
                    orderProductsListView.getItems().clear();
                    totalLabel.setText("Total: 0.0");
                    specialOfferLabel.setText("");

                } catch (Exception ex) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    ex.printStackTrace();
                    System.out.println("Eroare la adăugarea comenzii.");
                } finally {
                    em.close();
                }
            } else {
                System.out.println("Nu există o comandă de adăugat pentru masa selectată.");
            }
        });

        waiterNewOrderTabView = new HBox(10, tableListView, waiterProductListView, waiterRightContainer);
        waiterNewOrderTabView.setPadding(new Insets(10));
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

        clientView = new VBox(10, topBox, root); // Add 'root' here
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
        if (clientScene == null) {
            initClientView();
        }
        return clientScene;
    }

    public Stage getStage() {
        return stage;
    }

    public ListView<Product> getProductListView() {
        return productListView;
    }

    public void setProductListView(ListView<Product> productListView) {
        this.productListView = productListView;
    }

    public VBox getProductDetailsView() {
        return productDetailsView;
    }

    public void setProductDetailsView(VBox productDetailsView) {
        this.productDetailsView = productDetailsView;
    }

    public VBox getClientView() {
        return clientView;
    }

    public void setClientView(VBox clientView) {
        this.clientView = clientView;
    }

    public HBox getWaiterNewOrderTabView() {
        return waiterNewOrderTabView;
    }

    public void setWaiterNewOrderTabView(HBox waiterNewOrderTabView) {
        this.waiterNewOrderTabView = waiterNewOrderTabView;
    }

    public HBox getWaiterOfferHistoryTab() {
        return waiterOfferHistoryTab;
    }

    public void setWaiterOfferHistoryTab(HBox waiterOfferHistoryTab) {
        this.waiterOfferHistoryTab = waiterOfferHistoryTab;
    }
}
