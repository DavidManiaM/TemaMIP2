package org.example.tema2.structure;

import com.fasterxml.jackson.annotation.*;
import org.example.tema2.model.Product;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({ "name", "TVA", "menu", "orders" })
public class Restaurant {

    public static final int TVA = 9;
    private final String name;
    @JsonIgnore
    private List<Product> products = new ArrayList<>();
    private Menu menu = new Menu();
    @JsonIgnore
    private List<Order> orders = new ArrayList<>();

    public Restaurant(String name) {
        this.name = name;
        this.menu = new Menu();
        menu.initializeDefaultProducts();
        this.products = new ArrayList<>(menu.getProducts());
        orders =  new ArrayList<>();
        orders.add(new Order());
    }


    @JsonProperty("TVA")
    public int getTvaValue() {
        return TVA;
    }

    @JsonSetter("TVA")
    public void setTvaValue(int tva) {}

    @JsonCreator
    public Restaurant(@JsonProperty("name") String name,
                      @JsonProperty("menu") Menu menu) {
        this.name = name;
        this.menu = menu != null ? menu : new Menu();
        this.products = new ArrayList<>(this.menu.getProducts());
        orders =  new ArrayList<>();
        orders.add(new Order());
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder("--- Meniul Restaurantului \"" + name + "\" ---\n");

        for (Product p : products) {
            toReturn.append(p.toString()).append("\n");
        }

        toReturn.append("−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−\n");

        return toReturn.toString();
    }

    public void addProduct(Product p) {
        products.add(p);
    }

    public Menu getMenu() {
        return menu;
    }

    public String getName() {
        return name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

}