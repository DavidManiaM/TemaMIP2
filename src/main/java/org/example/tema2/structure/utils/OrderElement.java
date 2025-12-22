package org.example.tema2.structure.utils;


import org.example.tema2.model.Product;

public class OrderElement {
    public int quantity;
    public Product product;
    private Double customPrice;

    public OrderElement(int quantity, Product product) {
        this.quantity = quantity;
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    @Override
    public String toString() {
        return quantity + " x " + product.getName() + ": " + product.getPrice();
    }

    public double getPrice(){
        if (customPrice != null) return quantity * customPrice;
        return quantity * product.getPrice();
    }

    public void setCustomPrice(Double price) {
        this.customPrice = price;
    }
}