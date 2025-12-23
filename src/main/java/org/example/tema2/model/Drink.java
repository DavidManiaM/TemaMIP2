package org.example.tema2.model;

import jakarta.persistence.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

@Entity
@DiscriminatorValue("DRINK")
public final class Drink extends Product {

    private IntegerProperty volume = new SimpleIntegerProperty();

    public Drink() {
        super();
    }

    public Drink(String name, double price, int volume, Product.Type type) {
        super(name, price, type);
        this.volume.set(volume);
    }

    public Drink(String name, double price, int volume, Product.Type type, boolean isVegetarian) {
        super(name, price, type, isVegetarian);
        this.volume.set(volume);
    }

//    @Override
//    public String toString() {
//        return super.toString() + " – Volum: " + volume.get() + "ml";
//    }

    @Column(name = "volume")
    @Access(AccessType.PROPERTY)
    public int getVolume() {
        return volume.get();
    }
    public void setVolume(int volume) {
        this.volume.set(volume);
    }

    public IntegerProperty volumeProperty() { return this.volume; }

}


