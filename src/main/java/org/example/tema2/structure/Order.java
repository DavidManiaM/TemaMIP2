package org.example.tema2.structure;

import jakarta.persistence.*;
import org.example.tema2.model.Product;
import org.example.tema2.model.Waiter;
import org.example.tema2.structure.utils.OrderElement;
import org.example.tema2.structure.utils.SpecialOffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public static final int TVA = Restaurant.TVA;

    @ManyToOne
    @JoinColumn(name = "waiter_id")
    private Waiter waiter;

    @Column(name = "table_number")
    private int tableNumber;

    @Column(name = "total_price")
    private double totalPrice;

    @Transient
    List<OrderElement> elements = new ArrayList<>();
    public int discount = 0;

    @Transient
    public Optional<SpecialOffer> activeSpecialOffer;
    private static final String PIZZA_OFFER_NAME = "Pizza Margherita - Oferta";

    @Transient
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
    @Transient
    SpecialOffer pizza1Plus1Free = new SpecialOffer() {
        @Override
        public String getName() {
            return "Pizza 1 + 1 gratis";
        }

        @Override
        public boolean isApplicable() {
            return elements.stream().anyMatch(el -> el.getProduct().getName().contains("Pizza") && !el.getProduct().getName().equals(PIZZA_OFFER_NAME));
        }

        private int nrOfPizzas(){
            int nrPizzas = 0;
            for (OrderElement element : elements) {
                if(element.getProduct().getName().contains("Pizza") && !element.getProduct().getName().equals(PIZZA_OFFER_NAME)) {
                    nrPizzas += element.quantity;
                }
            }
            return nrPizzas;
        }

        @Override
        public void applyOffer() {
            // This logic is now handled in the main applyOffer method
        }
    };
    @Transient
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


    @Transient
    private double discountAmount = 0.0;

    public Order(){
        // activeSpecialOffer = Optional.ofNullable(pizza1Plus1Free);
    }

    public void applyOffer() {
        discountAmount = 0.0;
        // Reset custom prices if any were set (though we use discountAmount now)
        for(OrderElement el : elements) el.setCustomPrice(null);

        org.example.tema2.structure.utils.OfferManager offerManager = org.example.tema2.structure.utils.OfferManager.getInstance();

        if (offerManager.isOfferActive(org.example.tema2.structure.utils.OfferManager.HAPPY_HOUR_DRINKS)) {
            applyHappyHour();
        }
        if (offerManager.isOfferActive(org.example.tema2.structure.utils.OfferManager.MEAL_DEAL)) {
            applyMealDeal();
        }
        if (offerManager.isOfferActive(org.example.tema2.structure.utils.OfferManager.PARTY_PACK)) {
            applyPartyPack();
        }
    }

    private void applyHappyHour() {
        int drinkCount = 0;
        for (OrderElement el : elements) {
            if (el.getProduct() instanceof org.example.tema2.model.Drink) {
                for(int i=0; i<el.quantity; i++) {
                    drinkCount++;
                    if (drinkCount % 2 == 0) {
                        discountAmount += el.getProduct().getPrice() * 0.5;
                    }
                }
            }
        }
    }

    private void applyMealDeal() {
        boolean hasPizza = elements.stream().anyMatch(el -> el.getProduct() instanceof org.example.tema2.model.Pizza);
        if (hasPizza) {
            OrderElement cheapestDessertEl = null;
            double minPrice = Double.MAX_VALUE;

            for (OrderElement el : elements) {
                if (el.getProduct().getType() == Product.Type.DESSERT) {
                    if (el.getProduct().getPrice() < minPrice) {
                        minPrice = el.getProduct().getPrice();
                        cheapestDessertEl = el;
                    }
                }
            }

            if (cheapestDessertEl != null) {
                discountAmount += cheapestDessertEl.getProduct().getPrice() * 0.25;
            }
        }
    }

    private void applyPartyPack() {
        int pizzaCount = 0;
        for (OrderElement el : elements) {
            if (el.getProduct() instanceof org.example.tema2.model.Pizza) {
                pizzaCount += el.quantity;
            }
        }

        if (pizzaCount >= 4) {
            double minPrice = Double.MAX_VALUE;
            for (OrderElement el : elements) {
                if (el.getProduct() instanceof org.example.tema2.model.Pizza) {
                    if (el.getProduct().getPrice() < minPrice) {
                        minPrice = el.getProduct().getPrice();
                    }
                }
            }

            if (minPrice != Double.MAX_VALUE) {
                discountAmount += minPrice;
            }
        }
    }

    public double getTotalPrice(){
        double total = 0;
        for (OrderElement element : elements){
            total += element.getPrice();
        }
        double priceAfterDiscount = total - discountAmount;
        if (priceAfterDiscount < 0) priceAfterDiscount = 0;

        return priceAfterDiscount * ((100 - discount) / 100.0);
    }

//    @Override
//    public String toString() {
//
//        applyOffer();
//
//        StringBuilder str = new StringBuilder();
//        str.append("Order {\n");
//        for (OrderElement element : elements){
//            str.append("\t").append(element.toString()).append("\n");
//        }
//
//        if(activeSpecialOffer.isPresent() && activeSpecialOffer.get().isApplicable()){
//            str.append("------------------------------\n\tOferta activa: ").append(activeSpecialOffer.get().getName()).append("\n");
//        }
//
//        double totalWithVat = getTotalPrice();
//        str.append("------------------------------\n\tTotal cu TVA: ").append(totalWithVat).append("\n");
//
//        String rawPriceString = String.format("%.2f", totalWithVat * 100 / (100 + TVA));
//        str.append("\tTotal fara TVA: ").append(rawPriceString).append("\n");
//
//        return str.append("}").toString();
//    }

    @Override
    public String toString() {

        applyOffer();

        return "Order [id = " + id + ", pret = " + totalPrice + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<OrderElement> getOrderElements() {
        return elements;
    }

    public void removeProduct(Product productToRemove) {
        elements.removeIf(element -> element.getProduct() == productToRemove);
    }

    public void setWaiter(Waiter assignedWaiter) {
        this.waiter = assignedWaiter;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setProducts(List<OrderElement> orderElements) {
        this.elements = orderElements;
    }
}
