package com.example;

import com.example.model.Espace;
import com.example.model.Membre;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Socle commun : H2 en mémoire + nettoyage des tables avant chaque test.
 */
public abstract class BaseTestPersistance {

    protected static EntityManagerFactory emf;

    @BeforeAll
    static void demarrer() {
        emf = Persistence.createEntityManagerFactory("salles-test-pu");
    }

    @AfterAll
    static void arreter() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void nettoyer() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Creneau").executeUpdate();
            em.createQuery("DELETE FROM Espace").executeUpdate();
            em.createQuery("DELETE FROM Membre").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    protected Membre creerMembre(String nom, String email) {
        Membre membre = new Membre(nom, email);
        persister(membre);
        return membre;
    }

    protected Espace creerEspace(String nom, int capacite) {
        Espace espace = new Espace(nom, capacite);
        persister(espace);
        return espace;
    }

    private void persister(Object entite) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entite);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
