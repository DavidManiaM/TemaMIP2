package org.example.tema2.structure;

import java.util.ArrayList;
import java.util.List;

public non-sealed class Pizza extends Food {

    public enum Topping {
        BASE_TOPPING,
        SAUCE,
        MOZZARELLA,
        PEPPERONI,
        MUSHROOMS,
        OLIVES,
        ONIONS,
        HAM,
        PINEAPPLE,
        SAUSAGE,
        SPINACH,
        FETA
    }

    public String toString(Topping topping) {
        return switch (topping) {
            case BASE_TOPPING -> "Blat";
            case SAUCE -> "Sos";
            case MOZZARELLA -> "Mozzarella";
            case PEPPERONI -> "Pepperoni";
            case MUSHROOMS -> "Ciuperci";
            case OLIVES -> "Masline";
            case ONIONS -> "Ceapa";
            case HAM -> "Sunca";
            case PINEAPPLE -> "Ananas";
            case SAUSAGE -> "Carnati";
            case SPINACH -> "Spanac";
            case FETA -> "Feta";
        };
    }

    private List<Topping> toppings;

    private Pizza(Builder builder){
        super("Custom Pizza", 60, 500, Type.MAIN_COURSE);
        this.toppings = builder.toppings;   // might need actual copying
    }

    public static class Builder {

        private List<Topping> toppings;

        public Builder(){
            toppings = new ArrayList<>(List.of(Topping.BASE_TOPPING, Topping.SAUCE));
        }

        public Builder addTopping(Topping topping){
            toppings.add(topping);
            return this;
        }

        public Pizza build(){
            return new Pizza(this);
        }

    }

    @Override
    public String toString() {
        System.out.println("Ingrediente pentru pizza custom:");
        StringBuilder sb = new StringBuilder();
        for (Topping topping : toppings) {
            sb.append("\t> ").append(toString(topping)).append("\n");
        }
        return sb.toString();
    }
}