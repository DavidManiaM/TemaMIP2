package org.example.tema2.structure;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DRINK")
public final class Drink extends Product {

    @Column(name = "volume")
    private int volume;

    public Drink() {
        super();
    }

    public Drink(String name, double price, int volume, Product.Type type) {
        super(name, price, type);
        this.volume = volume;
    }

    public Drink(String name, double price, int volume, Product.Type type, boolean isVegetarian) {
        super(name, price, type, isVegetarian);
        this.volume = volume;
    }

    @Override
    public String toString() {
        return super.toString() + " – Volum: " + volume + "ml";
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

}


