package org.example.tema2.structure;

import java.util.*;

public class Menu {



    private List<Product> products;


    Menu() {

        products = List.of(
                new Food("Batoane de mozzarella", 15, 200, Product.Type.APPETIZER, true),
                new Food("Cartofi prajiti cu usturoi si parmezan", 18.5, 250, Product.Type.APPETIZER, true),
                new Food("Pizza Margherita", 45, 450, Product.Type.MAIN_COURSE, true),
                new Food("Paste Carbonara", 52.5, 400, Product.Type.MAIN_COURSE, false),
                new Food("Lava Cake", 20, 300, Product.Type.DESSERT, true),
                new Food("Tiramisu", 22, 350, Product.Type.DESSERT, true),
                new Food("Cheesecake", 30, 300, Product.Type.DESSERT, true),
                new Drink("Limonada", 15, 400, Product.Type.COOLING_DRINK, true),
                new Drink("Apa plata", 8, 500, Product.Type.COOLING_DRINK, true),
                new Drink("Bere", 8, 500, Product.Type.ALCOHOL_DRINK, true),
                new Drink("Vin alb sec", 8, 500, Product.Type.ALCOHOL_DRINK, true),
                new Drink("Coniac", 8, 500, Product.Type.ALCOHOL_DRINK, true)

        );

    }


    public List<Product> getProducts() {
        return products;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Meniu:\n");

        products.forEach(product -> sb.append(product.toString()).append("\n"));

        return sb.toString();
    }

    public Product.Type chooseProductType(){
        System.out.println("Alege ce produse doresti sa vezi:");
        System.out.println("\t1. Aperitive:");
        System.out.println("\t2. Feluri principale:");
        System.out.println("\t3. Deserturi:");
        System.out.println("\t4. Bauturi racoritoare:");
        System.out.println("\t5. Bauturi alcoolice:");

        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            try {
                choice = sc.nextInt();
                if(choice < 1 || choice > 5){
                    System.out.println("Alege un numar valid!");
                }
            } catch (InputMismatchException e) {
                sc.nextLine();
                System.out.println("Alege un numar valid!");
            }

        } while(choice < 1 || choice > 5);

        return switch (choice) {
            case 1 -> Product.Type.APPETIZER;
            case 2 -> Product.Type.MAIN_COURSE;
            case 3 -> Product.Type.DESSERT;
            case 4 -> Product.Type.COOLING_DRINK;
            case 5 -> Product.Type.ALCOHOL_DRINK;
            default -> null;
        };

    }

    public void printProductType(Product.Type type) {
        products.forEach(product -> {
            if(product.getType() == type)
                System.out.println(product.toString());
        });
    }

    public List<Product> getProductsOfType(Product.Type type) {
        List<Product> list = new ArrayList<>();
        products.forEach(product -> {
            if(product.getType() == type)
                list.add(product);
        });
        return list;
    }

    public Optional<List<Product>> searchProduct(String search){
        List<Product> result = new ArrayList<>();
        for(Product p : products){
            if(p.getName().toLowerCase().contains(search.toLowerCase()))
                result.add(p);
        }

        if(result.isEmpty())
            return Optional.empty();
        return Optional.of(result);
    }

    public void printSearchProduct(String search) {
        Optional<List<Product>> result = searchProduct(search);

        if(result.isEmpty()){
            System.out.println("No product with name [" + search + "] found!");
            return;
        }

        for(Product p : result.get())
            System.out.println(p.toString());
    }

}