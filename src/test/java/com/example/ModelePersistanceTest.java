package com.example;

import com.example.model.Espace;
import com.example.model.Membre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.*;

class ModelePersistanceTest extends BaseTestPersistance {

    @Test
    @DisplayName("Un membre persisté reçoit un identifiant et une version initiale")
    void persisterMembre_attribueUnId() {
        Membre membre = creerMembre("Test User", "test.user@example.com");

        assertNotNull(membre.getId());
        assertEquals(0, membre.getVersion());
    }

    @Test
    @DisplayName("Deux membres avec le même email violent la contrainte d'unicité")
    void emailDuplique_leveUneException() {
        creerMembre("Premier", "doublon@example.com");

        EntityManager em = emf.createEntityManager();
        try {
            assertThrows(PersistenceException.class, () -> {
                em.getTransaction().begin();
                em.persist(new Membre("Second", "doublon@example.com"));
                em.getTransaction().commit();
            });
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    @Test
    @DisplayName("@Version : une modification concurrente est détectée")
    void verrouillageOptimiste_detecteLeConflit() {
        Espace espace = creerEspace("Salle A", 20);

        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();
        try {
            Espace copie1 = em1.find(Espace.class, espace.getId());
            Espace copie2 = em2.find(Espace.class, espace.getId());

            em1.getTransaction().begin();
            copie1.setCapacite(30);
            em1.getTransaction().commit();

            em2.getTransaction().begin();
            copie2.setCapacite(40);
            assertThrows(OptimisticLockException.class, em2::flush);
        } finally {
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
            em1.close();
            em2.close();
        }
    }
}
