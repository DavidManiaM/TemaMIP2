package org.example.tema2.repo;

import jakarta.persistence.*;
import org.example.tema2.model.Product;

import java.util.List;
import java.util.Optional;

public class ProductRepository {
    private final EntityManagerFactory emf;

    public ProductRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<Product> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Product> q = em.createQuery("SELECT p FROM Product p", Product.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Product> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Product.class, id));
        } finally {
            em.close();
        }
    }

    public Product save(Product product) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product merged = em.merge(product);
            tx.commit();
            return merged;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product p = em.find(Product.class, id);
            if (p != null) em.remove(p);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
