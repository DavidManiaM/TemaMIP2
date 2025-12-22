package org.example.tema2.structure;

import org.example.tema2.model.Food;
import org.example.tema2.model.Product;
import org.example.tema2.structure.utils.OrderElement;
import org.example.tema2.structure.utils.SpecialOffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Order {

    public static final int TVA = Restaurant.TVA;
    List<OrderElement> elements = new ArrayList<>();
    public int discount = 0;
    public Optional<SpecialOffer> activeSpecialOffer;
    private boolean offerApplied = false;

    SpecialOffer _10PercentDiscountOver5Products = new SpecialOffer() {
        @Override
        public String getName() {
            return "10% reducere pentru 5 sau mai multe produse";
        }

        @Override
        public boolean isApplicable() {
            int nrOfProducts = 0;
            for (OrderElement element : elements) {
                nrOfProducts += element.quantity;
            }
            return nrOfProducts >= 5;
        }

        @Override
        public void applyOffer() {

            if(isApplicable()) {
                discount = 20;
            }
        }
    };
    SpecialOffer pizza1Plus1Free = new SpecialOffer() {
        @Override
        public String getName() {
            return "Pizza 1 + 1 gratis";
        }

        @Override
        public boolean isApplicable() {
            boolean hasPizza = false;
            for (OrderElement element : elements) {
                if(element.getProduct().getName().contains("Pizza")) {
                    hasPizza = true;
                    break;
                }
            }

            return hasPizza;
        }

        private int nrOfPizzas(){
            int nrPizzas = 0;
            for (OrderElement element : elements) {
                if(element.getProduct().getName().contains("Pizza")) {
                    nrPizzas += element.quantity;
                }
            }
            return nrPizzas;
        }

        @Override
        public void applyOffer() {
            elements.add(new OrderElement(nrOfPizzas(), new Food("Pizza Margherita - Oferta", 0, 450, Product.Type.MAIN_COURSE)));
        }
    };
    SpecialOffer _15PercentDiscountForLemonades = new SpecialOffer() {
        @Override
        public String getName() {
            return "20% reducere pentru limonade";
        }

        @Override
        public boolean isApplicable() {
            boolean hasLemonade = false;
            for (OrderElement element : elements) {
                if(element.getProduct().getName().contains("Limonada")) {
                    hasLemonade = true;
                    break;
                }
            }

            return hasLemonade;
        }

        @Override
        public void applyOffer() {
            if(isApplicable()) {
                for (OrderElement element : elements) {
                    if(element.getProduct().getName().contains("Limonada")) {
                        double discountedPrice = element.getProduct().getPrice() * (100 - 15) / 100;
                        element.getProduct().setPrice(discountedPrice);
                    }
                }
            }
        }
    };


    public Order(){
        activeSpecialOffer = Optional.ofNullable(pizza1Plus1Free);
    }

    public void applyOffer() {
        if (!offerApplied && activeSpecialOffer.isPresent() && activeSpecialOffer.get().isApplicable()) {
            activeSpecialOffer.get().applyOffer();
            offerApplied = true;
        }
    }

    public double getTotalPrice(){
        double total = 0;
        for (OrderElement element : elements){
            total += element.getPrice();
        }
        return total * ((100 - discount) / 100.0);
    }

    @Override
    public String toString() {

        applyOffer();

        StringBuilder str = new StringBuilder();
        str.append("Order {\n");
        for (OrderElement element : elements){
            str.append("\t").append(element.toString()).append("\n");
        }

        if(activeSpecialOffer.isPresent() && activeSpecialOffer.get().isApplicable()){
            str.append("------------------------------\n\tOferta activa: ").append(activeSpecialOffer.get().getName()).append("\n");
        }

        double totalWithVat = getTotalPrice();
        str.append("------------------------------\n\tTotal cu TVA: ").append(totalWithVat).append("\n");

        String rawPriceString = String.format("%.2f", totalWithVat * 100 / (100 + TVA));
        str.append("\tTotal fara TVA: ").append(rawPriceString).append("\n");

        return str.append("}").toString();
    }

    public void addElement(OrderElement element){
        elements.add(element);
    }

    public void addElement(int quantity, Product product){
        elements.add(new OrderElement(quantity, product));
    }

    public void addProduct(Product product){
        this.addElement(1, product);
    }

    public List<Product> getProducts(){
        List<Product> products = new ArrayList<>();
        for (OrderElement element : elements){
            products.add(element.getProduct());
        }
        return products;
    }

    public void removeProduct(Product productToRemove) {
        for (OrderElement element : elements){
            if(element.getProduct().getName().equals(productToRemove.getName())){
                elements.remove(element);
                break;
            }
        }
    }
}
