package org.example.tema2.structure;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import javafx.beans.property.*;
import org.example.tema2.structure.utils.ProductDeserializer;

@JsonDeserialize(using = ProductDeserializer.class)
public abstract sealed class Product permits Food, Drink {

    public enum Type {
        APPETIZER,
        MAIN_COURSE,
        DESSERT,
        COOLING_DRINK,
        ALCOHOL_DRINK,
    }

    public String toString(Type type) {
        switch (type) {
            case APPETIZER:
                return "Aperitiv";
            case MAIN_COURSE:
                return "Fel principal";
            case DESSERT:
                return "Desert";
            case COOLING_DRINK:
                return "Bautura racoritoare";
            case ALCOHOL_DRINK:
                return "Bautura alcoolica";
        }
        return "";
    }

    private final StringProperty name = new SimpleStringProperty(this, "name", "");
    private final DoubleProperty price = new SimpleDoubleProperty(this, "price", 0.0);
    private final BooleanProperty vegetarian = new SimpleBooleanProperty(this, "vegetarian", false);
    private Type type;

    // No-arg constructor (useful for frameworks/deserializers)
    public Product() {
    }

    public Product(String name, double price, Type type) {
        this.name.set(name);
        this.price.set(price);
        this.type = type;
    }

    public Product(String name, double price, Type type, boolean vegetarian) {
        this.name.set(name);
        this.price.set(price);
        this.type = type;
        this.vegetarian.set(vegetarian);
    }

    @Override
    public String toString() {
        return "> " + name.get() + " – " + price.get() + " RON";
    }

    public String getName() {
        return name.get();
    }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public double getPrice() {
        return price.get();
    }
    public void setPrice(double price){
        this.price.set(price);
    }
    public DoubleProperty priceProperty() {
        return this.price;
    }

    public boolean isVegetarian() {
        return vegetarian.get();
    }
    public void setVegetarian(boolean vegetarian) {
        this.vegetarian.set(vegetarian);
    }
    public BooleanProperty vegetarianProperty() { return vegetarian; }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}