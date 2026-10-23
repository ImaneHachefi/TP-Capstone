package com.example.util;

import com.example.model.Creneau;
import com.example.model.Espace;
import com.example.model.Membre;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Insère un jeu de données réaliste. Les dates sont calculées à partir de "demain 9h".
 */
public class AmorceDonnees {

    private final EntityManagerFactory emf;

    public AmorceDonnees(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void peupler() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Membre alice = new Membre("Alice Dupont", "alice.dupont@example.com");
            Membre karim = new Membre("Karim Benali", "karim.benali@example.com");
            Membre sara = new Membre("Sara Lemaire", "sara.lemaire@example.com");
            em.persist(alice);
            em.persist(karim);
            em.persist(sara);

            Espace salleA = new Espace("Salle A", 20);
            Espace salleB = new Espace("Salle B", 8);
            Espace amphi = new Espace("Amphithéâtre", 120);
            em.persist(salleA);
            em.persist(salleB);
            em.persist(amphi);

            LocalDateTime base = LocalDate.now().plusDays(1).atTime(9, 0);

            em.persist(new Creneau(alice, salleA, base, base.plusHours(2)));
            em.persist(new Creneau(karim, salleA, base.plusHours(3), base.plusHours(4)));
            em.persist(new Creneau(sara, salleB, base, base.plusMinutes(90)));
            em.persist(new Creneau(alice, amphi, base.plusDays(1), base.plusDays(1).plusHours(3)));

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
        System.out.println(">>> Jeu de données inséré (3 membres, 3 espaces, 4 créneaux).");
    }
}
