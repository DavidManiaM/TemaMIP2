package org.example.tema2.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.example.tema2.service.ProductService;
import org.example.tema2.model.Product;

import java.util.List;

public class ProductController {
    private final ProductService service;
    private final ObservableList<Product> products = FXCollections.observableArrayList();

    @FXML
    private ListView<Product> productListView;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        productListView.setItems(products);
    }

    @FXML
    public void onImportFromDb() {
        List<Product> loaded = service.getAllProducts();
        // Replace contents atomically to avoid showing null/empty entries
        products.setAll(loaded);
    }
}