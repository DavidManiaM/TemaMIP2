package org.example.tema2.service;

import org.example.tema2.repo.ProductRepository;
import org.example.tema2.model.Product;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> getAllProducts() {
        // fetch all and filter out null / invalid rows to avoid "deleting" UI items
        return repo.findAll().stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName() != null && !p.getName().trim().isEmpty())
                .collect(Collectors.toList());
    }

    public Product saveProduct(Product p) {
        // add business validations here
        return repo.save(p);
    }

    public void deleteProduct(Long id) {
        repo.delete(id);
    }
}