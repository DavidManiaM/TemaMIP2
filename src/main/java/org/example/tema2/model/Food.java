package org.example.tema2.model;

import jakarta.persistence.*;
import javafx.beans.property.*;

@Entity
@DiscriminatorValue("FOOD")
public /*sealed*/ class Food extends Product /*permits Pizza*/ {

    @Transient
    private IntegerProperty weight = new SimpleIntegerProperty();

    public Food() {
        super();
    }

    public Food(String name, double price, int weight, Product.Type type) {
        super(name, price,  type);
        this.weight.set(weight);
    }

    public Food(String name, double price, int weight, Product.Type type, boolean isVegetarian) {
        super(name, price,  type, isVegetarian);
        this.weight.set(weight);
    }

    @Override
    public String toString() {
        return super.toString() + " – Gramaj: " + weight + "g";
    }

    @Column(name = "weight")
    @Access(AccessType.PROPERTY)
    public int getWeight() {
        return weight.get();
    }
    public void setWeight(int weight) {
        this.weight.set(weight);
    }

    public IntegerProperty weightProperty() { return this.weight; }
}
