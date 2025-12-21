package org.example.tema2.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import javafx.beans.property.*;
import org.example.tema2.structure.utils.ProductDeserializer;

@JsonDeserialize(using = ProductDeserializer.class)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "product_type")
public abstract /*sealed*/ class Product /*permits Food, Drink */{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Transient
    private StringProperty name = new SimpleStringProperty(this, "name", "");
    @Transient
    private DoubleProperty price = new SimpleDoubleProperty(this, "price", 0.0);
    @Transient
    private BooleanProperty vegetarian = new SimpleBooleanProperty(this, "vegetarian", false);

    @Enumerated(EnumType.STRING)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Override
    public String toString() {
        String nameValue = (name != null) ? name.get() : "";
        String priceValue;
        if (price != null) {
            // DoubleProperty#get() returns a primitive double, so convert to String
            priceValue = String.valueOf(price.get());
        } else {
            priceValue = "";
        }
        return "> " + nameValue + " – " + priceValue + " RON";
    }

    // JPA-compatible getters/setters that sync with properties
    @Column(name = "name")
    @Access(AccessType.PROPERTY)
    public String getName() { return name.get(); }
    public void setName(String n) { this.name.set(n); }

    @Column(name = "price")
    @Access(AccessType.PROPERTY)
    public double getPrice() { return price.get(); }
    public void setPrice(double p) { this.price.set(p); }

    @Column(name = "vegetarian")
    @Access(AccessType.PROPERTY)
    public boolean isVegetarian() { return vegetarian.get(); }
    public void setVegetarian(boolean v) { this.vegetarian.set(v); }

    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return this.price; }
    public BooleanProperty vegetarianProperty() { return vegetarian; }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}