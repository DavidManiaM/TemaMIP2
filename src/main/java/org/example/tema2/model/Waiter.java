package org.example.tema2.model;

import jakarta.persistence.*;
import org.example.tema2.structure.Order;

import java.util.List;

@Entity
public class Waiter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "waiter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    public Waiter() {}

    public Waiter(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Waiter(String name) {
        this.name = name;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    // Other getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
