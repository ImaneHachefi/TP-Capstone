package com.example.service;

import com.example.model.Creneau;
import com.example.model.Espace;
import com.example.model.Membre;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logique métier des réservations (transactions RESOURCE_LOCAL).
 */
public class GestionReservations {

    private static final Logger LOGGER = Logger.getLogger(GestionReservations.class.getName());

    private final EntityManagerFactory emf;

    public GestionReservations(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Creneau reserver(Long membreId, Long espaceId, LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null || !debut.isBefore(fin)) {
            throw new IllegalArgumentException("La date de début doit précéder la date de fin.");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Membre membre = em.find(Membre.class, membreId);
            // Verrou sur l'espace : deux réservations simultanées du même espace sont sérialisées
            Espace espace = em.find(Espace.class, espaceId, LockModeType.PESSIMISTIC_WRITE);
            if (membre == null || espace == null) {
                throw new IllegalArgumentException("Membre ou espace introuvable.");
            }

            Long conflits = em.createQuery(
                            "SELECT COUNT(c) FROM Creneau c "
                                    + "WHERE c.espace.id = :espaceId AND c.dateDebut < :fin AND c.dateFin > :debut",
                            Long.class)
                    .setParameter("espaceId", espaceId)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getSingleResult();

            if (conflits > 0) {
                throw new ConflitReservationException("L'espace '" + espace.getNom()
                        + "' est déjà réservé sur cette plage horaire.");
            }

            Creneau creneau = new Creneau(membre, espace, debut, fin);
            em.persist(creneau);
            tx.commit();

            LOGGER.info("Réservation créée pour " + membre.getEmail() + " dans " + espace.getNom());
            return creneau;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.log(Level.WARNING, "Échec de la réservation : " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    public void annuler(Long creneauId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Creneau creneau = em.find(Creneau.class, creneauId);
            if (creneau != null) {
                em.remove(creneau);
            }
            tx.commit();
            LOGGER.info("Réservation " + creneauId + " annulée.");
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.log(Level.SEVERE, "Échec de l'annulation : " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /** Recherche en JPQL (JOIN FETCH pour éviter le N+1). */
    public List<Creneau> rechercherJpql(String nomEspace, LocalDateTime apres) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Creneau c JOIN FETCH c.espace e JOIN FETCH c.membre "
                                    + "WHERE e.nom = :nom AND c.dateDebut > :apres ORDER BY c.dateDebut",
                            Creneau.class)
                    .setParameter("nom", nomEspace)
                    .setParameter("apres", apres)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Même recherche avec l'API Criteria. */
    @SuppressWarnings("unchecked")
    public List<Creneau> rechercherCriteria(String nomEspace, LocalDateTime apres) {
        EntityManager em = emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Creneau> cq = cb.createQuery(Creneau.class);
            Root<Creneau> root = cq.from(Creneau.class);

            Join<Creneau, Espace> espace = (Join<Creneau, Espace>) root.<Creneau, Espace>fetch("espace");            root.fetch("membre");

            cq.select(root)
                    .where(cb.and(
                            cb.equal(espace.get("nom"), nomEspace),
                            cb.greaterThan(root.<LocalDateTime>get("dateDebut"), apres)))
                    .orderBy(cb.asc(root.get("dateDebut")));

            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }
}
