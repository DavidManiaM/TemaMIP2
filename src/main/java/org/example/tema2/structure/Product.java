package org.example.tema2.structure;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
        };
        return  "";
    }

    private final String name;
    private double price;
    private boolean vegetarian = false;
    private Type type;

    public Product(String name, double price, Type type) {
        this.name = name;
        this.price = price;
        this.type = type;
    }

    public Product(String name, double price, Type type, boolean vegetarian) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.vegetarian = vegetarian;
    }

    @Override
    public String toString() {
        return "> " + name + " – " + price + " RON";
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}