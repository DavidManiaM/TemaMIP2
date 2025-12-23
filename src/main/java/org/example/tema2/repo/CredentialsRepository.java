package org.example.tema2.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import org.example.tema2.model.Credentials;

import java.util.Optional;

public class CredentialsRepository {

    private final EntityManagerFactory emf;

    public CredentialsRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Optional<Credentials.Role> getRoleByUsernameAndPassword(String username, String password) {
        EntityManager em = emf.createEntityManager();
        try {
            Credentials.Role role = em.createQuery(
                            "SELECT c.role FROM Credentials c " +
                                    "WHERE c.userName = :username AND c.password = :password",
                            Credentials.Role.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();

            return Optional.of(role);

        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}
