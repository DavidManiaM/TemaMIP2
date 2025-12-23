package org.example.tema2.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
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
import javafx.scene.control.cell.PropertyValueFactory;
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
    private Scene managerScene;

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
                System.out.println("Vă rugăm să selectați o masă și un produs.");
                Alert alert =  new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Vă rugăm să selectați o masă și un produs.");
                alert.showAndWait();
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
                System.out.println("Vă rugăm să selectați o masă și un produs din comandă pentru a-l șterge.");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Vă rugăm să selectați o masă și un produs din comandă pentru a-l șterge.");
                alert.showAndWait();
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
                    Alert alert =  new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Comanda a fost adăugată cu succes pentru masa " + selectedTable.getNumber());
                    alert.showAndWait();

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
                    Alert alert =  new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Eroare la adăugarea comenzii.");
                    alert.showAndWait();
                } finally {
                    em.close();
                }
            } else {
                System.out.println("Nu există o comandă de adăugat pentru masa selectată.");
                Alert alert =  new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Nu există o comandă de adăugat pentru masa selectată.");
                alert.showAndWait();
            }
        });

        waiterNewOrderTabView = new HBox(10, tableListView, waiterProductListView, waiterRightContainer);
        waiterNewOrderTabView.setPadding(new Insets(10));
    }


    public void initClientView() {
        HBox root = new HBox(20, productListView, productDetailsView);
        root.setPadding(new Insets(20));

        ObservableList<Product> observableProducts = FXCollections.observableArrayList(restaurant.getProducts());
        RestaurantApplication.setFiltered(new FilteredList<>(observableProducts, t -> true));
        productListView.setItems(RestaurantApplication.getFiltered());

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

    public Scene getManagerScene() {
        if (managerScene == null) {
            initManagerView();
        }
        return managerScene;
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

    private void initManagerView() {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(createStaffTab(), createMenuTab(), createOffersTab(), createHistoryTab());
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Button goBackButton = new Button("Inapoi");
        goBackButton.setOnAction(e -> RestaurantApplication.welcomeView());

        VBox root = new VBox(10, goBackButton, tabPane);
        root.setPadding(new Insets(10));
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        managerScene = new Scene(root, 900, 600);
    }

    private Tab createStaffTab() {
        TableView<Waiter> waiterTable = new TableView<>();
        TableColumn<Waiter, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Waiter, String> nameCol = new TableColumn<>("Nume");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        waiterTable.getColumns().addAll(idCol, nameCol);

        EntityManager em = emf.createEntityManager();
        List<Waiter> waiters = em.createQuery("SELECT w FROM Waiter w", Waiter.class).getResultList();
        em.close();
        waiterTable.setItems(FXCollections.observableArrayList(waiters));

        TextField nameField = new TextField();
        nameField.setPromptText("Nume Ospatar");
        Button addButton = new Button("Adauga");
        Button deleteButton = new Button("Sterge");

        addButton.setOnAction(e -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                Waiter w = new Waiter(name);
                EntityManager em1 = emf.createEntityManager();
                em1.getTransaction().begin();
                em1.persist(w);
                em1.getTransaction().commit();
                em1.close();
                waiterTable.getItems().add(w);
                nameField.clear();
            }
        });

        deleteButton.setOnAction(e -> {
            Waiter selected = waiterTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sigur stergeti ospatarul " + selected.getName() + "? Toate comenzile vor fi sterse!", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        EntityManager em2 = emf.createEntityManager();
                        em2.getTransaction().begin();
                        Waiter managedWaiter = em2.find(Waiter.class, selected.getId());
                        if (managedWaiter != null) {
                            em2.remove(managedWaiter);
                        }
                        em2.getTransaction().commit();
                        em2.close();
                        waiterTable.getItems().remove(selected);
                    }
                });
            }
        });

        VBox controls = new VBox(10, nameField, addButton, deleteButton);
        HBox root = new HBox(10, waiterTable, controls);
        root.setPadding(new Insets(10));
        Tab tab = new Tab("Personal");
        tab.setContent(root);
        tab.setClosable(false);
        return tab;
    }

    private Tab createMenuTab() {
        TableView<Product> productTable = new TableView<>();
        TableColumn<Product, String> nameCol = new TableColumn<>("Nume");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        TableColumn<Product, Double> priceCol = new TableColumn<>("Pret");
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        productTable.getColumns().addAll(nameCol, priceCol);
        productTable.setItems(FXCollections.observableArrayList(restaurant.getProducts()));

        TextField nameField = new TextField();
        nameField.setPromptText("Nume Produs");
        TextField priceField = new TextField();
        priceField.setPromptText("Pret");
        Button addButton = new Button("Adauga");
        Button deleteButton = new Button("Sterge");
        Button editButton = new Button("Editeaza");

        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                Product p = new Food(name, price, 0, Product.Type.MAIN_COURSE); // Defaulting to Food/Main Course

                EntityManager em = emf.createEntityManager();
                try {
                    em.getTransaction().begin();
                    em.persist(p);
                    em.getTransaction().commit();

                    restaurant.addProduct(p);
                    productTable.getItems().add(p);
                    nameField.clear();
                    priceField.clear();
                } catch (Exception ex) {
                    if (em.getTransaction().isActive()) em.getTransaction().rollback();
                    ex.printStackTrace();
                } finally {
                    em.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        deleteButton.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                EntityManager em = emf.createEntityManager();
                try {
                    em.getTransaction().begin();
                    Product managedProduct = null;
                    if (selected.getId() != null) {
                        managedProduct = em.find(Product.class, selected.getId());
                    }
                    if (managedProduct == null) {
                        try {
                            List<Product> results = em.createQuery("SELECT p FROM Product p WHERE p.name = :name", Product.class)
                                    .setParameter("name", selected.getName())
                                    .getResultList();
                            if (!results.isEmpty()) {
                                managedProduct = results.get(0);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    if (managedProduct != null) {
                        em.remove(managedProduct);
                    }
                    em.getTransaction().commit();

                    restaurant.getProducts().remove(selected);
                    productTable.getItems().remove(selected);
                } catch (Exception ex) {
                    if (em.getTransaction().isActive()) em.getTransaction().rollback();
                    ex.printStackTrace();
                } finally {
                    em.close();
                }
            }
        });

        editButton.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    String newName = nameField.getText();
                    double newPrice = Double.parseDouble(priceField.getText());
                    String oldName = selected.getName();

                    EntityManager em = emf.createEntityManager();
                    try {
                        em.getTransaction().begin();
                        Product managedProduct = null;
                        if (selected.getId() != null) {
                            managedProduct = em.find(Product.class, selected.getId());
                        }

                        if (managedProduct == null) {
                            try {
                                List<Product> results = em.createQuery("SELECT p FROM Product p WHERE p.name = :name", Product.class)
                                        .setParameter("name", oldName)
                                        .getResultList();
                                if (!results.isEmpty()) {
                                    managedProduct = results.get(0);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        if (managedProduct != null) {
                            managedProduct.setName(newName);
                            managedProduct.setPrice(newPrice);
                            if (selected.getId() == null) {
                                selected.setId(managedProduct.getId());
                            }
                        } else {
                            // Product not in DB yet, persist it
                            selected.setName(newName);
                            selected.setPrice(newPrice);
                            em.persist(selected);
                        }
                        em.getTransaction().commit();

                        // Update local object
                        selected.setName(newName);
                        selected.setPrice(newPrice);
                        productTable.refresh();
                    } catch (Exception ex) {
                        if (em.getTransaction().isActive()) em.getTransaction().rollback();
                        ex.printStackTrace();
                    } finally {
                        em.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                priceField.setText(String.valueOf(newSelection.getPrice()));
            }
        });

        Button importButton = new Button("Import JSON");
        Button exportButton = new Button("Export JSON");

        importButton.setOnAction(e -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Restaurant loaded = mapper.readValue(new File("configRestaurant.json"), Restaurant.class);
                restaurant.getProducts().clear();
                restaurant.getProducts().addAll(loaded.getProducts());
                productTable.setItems(FXCollections.observableArrayList(restaurant.getProducts()));
                productTable.refresh();
                System.out.println("Imported from configRestaurant.json");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        exportButton.setOnAction(e -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(new File("configRestaurant.json"), restaurant);
                System.out.println("Exported to configRestaurant.json");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox controls = new VBox(10, nameField, priceField, addButton, editButton, deleteButton, importButton, exportButton);
        HBox root = new HBox(10, productTable, controls);
        root.setPadding(new Insets(10));
        Tab tab = new Tab("Meniu");
        tab.setContent(root);
        tab.setClosable(false);
        return tab;
    }

    private Tab createOffersTab() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        org.example.tema2.structure.utils.OfferManager om = org.example.tema2.structure.utils.OfferManager.getInstance();

        CheckBox cb1 = new CheckBox("Happy Hour Drinks (50% off every 2nd drink)");
        cb1.setSelected(om.isOfferActive(org.example.tema2.structure.utils.OfferManager.HAPPY_HOUR_DRINKS));
        cb1.setOnAction(e -> om.setOfferStatus(org.example.tema2.structure.utils.OfferManager.HAPPY_HOUR_DRINKS, cb1.isSelected()));

        CheckBox cb2 = new CheckBox("Meal Deal (Pizza -> Cheapest Dessert 25% off)");
        cb2.setSelected(om.isOfferActive(org.example.tema2.structure.utils.OfferManager.MEAL_DEAL));
        cb2.setOnAction(e -> om.setOfferStatus(org.example.tema2.structure.utils.OfferManager.MEAL_DEAL, cb2.isSelected()));

        CheckBox cb3 = new CheckBox("Party Pack (4 Pizzas -> 1 Free)");
        cb3.setSelected(om.isOfferActive(org.example.tema2.structure.utils.OfferManager.PARTY_PACK));
        cb3.setOnAction(e -> om.setOfferStatus(org.example.tema2.structure.utils.OfferManager.PARTY_PACK, cb3.isSelected()));

        root.getChildren().addAll(new Label("Activeaza Oferte:"), cb1, cb2, cb3);

        Tab tab = new Tab("Oferte");
        tab.setContent(root);
        tab.setClosable(false);
        return tab;
    }

    private Tab createHistoryTab() {
        ListView<Order> orderList = new ListView<>();
        EntityManager em = emf.createEntityManager();
        List<Order> orders = em.createQuery("SELECT o FROM Order o", Order.class).getResultList();
        em.close();
        orderList.setItems(FXCollections.observableArrayList(orders));

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> {
            EntityManager em2 = emf.createEntityManager();
            List<Order> newOrders = em2.createQuery("SELECT o FROM Order o", Order.class).getResultList();
            em2.close();
            orderList.setItems(FXCollections.observableArrayList(newOrders));
        });

        VBox root = new VBox(10, refreshBtn, orderList);
        root.setPadding(new Insets(10));
        Tab tab = new Tab("Istoric Global");
        tab.setContent(root);
        tab.setClosable(false);
        return tab;
    }

}
