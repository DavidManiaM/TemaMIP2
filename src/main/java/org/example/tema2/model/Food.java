package org.example.tema2.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FOOD")
public /*sealed*/ class Food extends Product /*permits Pizza*/ {

    @Column(name = "weight")
    private int weight;


    public Food() {
        super();
    }

    public Food(String name, double price, int weight, Product.Type type) {
        super(name, price,  type);
        this.weight = weight;
    }

    public Food(String name, double price, int weight, Product.Type type, boolean isVegetarian) {
        super(name, price,  type, isVegetarian);
        this.weight = weight;
    }

    @Override
    public String toString() {
        return super.toString() + " – Gramaj: " + weight + "g";
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
