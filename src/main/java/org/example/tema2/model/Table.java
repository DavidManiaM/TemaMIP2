package org.example.tema2.model;

import org.example.tema2.structure.Order;

public class Table {
    static private int counter = 1;
    private int number;
    Order currentOrder;

    public Table() {
        number = counter;
        counter++;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Masa " + number;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(Order currentOrder) {
        this.currentOrder = currentOrder;
    }
}
