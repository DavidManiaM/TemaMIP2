package org.example.tema2.model;

public class Table {
    static private int counter = 1;
    private int number;

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
}
