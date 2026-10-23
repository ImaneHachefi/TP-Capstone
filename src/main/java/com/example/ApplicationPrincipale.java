package com.example;

import com.example.model.Espace;
import com.example.service.ConflitReservationException;
import com.example.service.GestionReservations;
import com.example.util.AmorceDonnees;
import com.example.util.ExecuteurMigration;
import com.example.util.RapportStatistiques;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApplicationPrincipale {

    // Doivent correspondre à persistence.xml / docker-compose.yml
    private static final String URL_JDBC = "jdbc:postgresql://localhost:5432/sallesdb";
    private static final String UTILISATEUR = "salles";
    private static final String MOT_DE_PASSE = "salles";

    public static void main(String[] args) throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("salles-pu");

        try {
            // 1. Migration (les tables existent déjà, créées par Hibernate au démarrage)
            new ExecuteurMigration(URL_JDBC, UTILISATEUR, MOT_DE_PASSE)
                    .executer("db/migration/v2_0_ajout_description.sql");

            // 2. Données de test
            new AmorceDonnees(emf).peupler();

            GestionReservations service = new GestionReservations(emf);
            LocalDateTime base = LocalDate.now().plusDays(1).atTime(9, 0);

            // 3. Réservation valide (11h-12h, entre les deux créneaux existants de la Salle A)
            service.reserver(2L, 1L, base.plusHours(2), base.plusHours(3));

            // 4. Réservation en conflit avec le créneau 9h-11h
            try {
                service.reserver(2L, 1L, base.plusMinutes(30), base.plusHours(1));
            } catch (ConflitReservationException e) {
                System.out.println(">>> Conflit détecté comme prévu : " + e.getMessage());
            }

            // 5. Recherches JPQL puis Criteria
            System.out.println(">>> JPQL     : " + service.rechercherJpql("Salle A", LocalDateTime.now()).size() + " créneaux");
            System.out.println(">>> Criteria : " + service.rechercherCriteria("Salle A", LocalDateTime.now()).size() + " créneaux");

            // 6. Cache L2 : deux lectures de la même entité dans deux EntityManager différents
            lireEspace(emf, "1ère lecture (miss + put)");
            lireEspace(emf, "2ème lecture (hit)");

            // 7. Rapport
            new RapportStatistiques(emf).afficher();
        } finally {
            emf.close();
        }
    }

    private static void lireEspace(EntityManagerFactory emf, String libelle) {
        EntityManager em = emf.createEntityManager();
        try {
            Espace espace = em.find(Espace.class, 1L);
            System.out.println(">>> " + libelle + " : " + espace.getNom() + " (" + espace.getDescription() + ")");
        } finally {
            em.close();
        }
    }
}
