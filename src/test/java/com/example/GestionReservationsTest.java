package com.example;

import com.example.model.Creneau;
import com.example.model.Espace;
import com.example.model.Membre;
import com.example.service.ConflitReservationException;
import com.example.service.GestionReservations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GestionReservationsTest extends BaseTestPersistance {

    private GestionReservations service;
    private Membre membre;
    private Espace espace;
    private LocalDateTime debut;

    @BeforeEach
    void preparer() {
        service = new GestionReservations(emf);
        membre = creerMembre("Alice", "alice@example.com");
        espace = creerEspace("Salle A", 20);
        debut = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    @DisplayName("Un créneau qui chevauche une réservation existante est refusé")
    void chevauchement_leveUnConflit() {
        service.reserver(membre.getId(), espace.getId(), debut, debut.plusHours(2));

        assertThrows(ConflitReservationException.class, () ->
                service.reserver(membre.getId(), espace.getId(), debut.plusHours(1), debut.plusHours(3)));
    }

    @Test
    @DisplayName("Des créneaux consécutifs (fin = début suivant) sont autorisés")
    void creneauxConsecutifs_sontAutorises() {
        service.reserver(membre.getId(), espace.getId(), debut, debut.plusHours(1));

        Creneau suivant = service.reserver(membre.getId(), espace.getId(),
                debut.plusHours(1), debut.plusHours(2));

        assertNotNull(suivant.getId());
    }

    @Test
    @DisplayName("Une date de fin antérieure à la date de début est rejetée")
    void datesInvalides_sontRejetees() {
        assertThrows(IllegalArgumentException.class, () ->
                service.reserver(membre.getId(), espace.getId(), debut.plusHours(2), debut));
    }

    @Test
    @DisplayName("JPQL et Criteria renvoient les mêmes créneaux")
    void jpqlEtCriteria_donnentLeMemeResultat() {
        service.reserver(membre.getId(), espace.getId(), debut, debut.plusHours(1));
        service.reserver(membre.getId(), espace.getId(), debut.plusHours(2), debut.plusHours(3));

        List<Long> idsJpql = service.rechercherJpql("Salle A", LocalDateTime.now())
                .stream().map(Creneau::getId).collect(Collectors.toList());
        List<Long> idsCriteria = service.rechercherCriteria("Salle A", LocalDateTime.now())
                .stream().map(Creneau::getId).collect(Collectors.toList());

        assertEquals(2, idsJpql.size());
        assertEquals(idsJpql, idsCriteria);
    }
}
