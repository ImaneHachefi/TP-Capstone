package com.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Une réservation d'espace par un membre sur une plage horaire.
 */
@Entity
@Table(name = "creneaux")
public class Creneau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "espace_id", nullable = false)
    private Espace espace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Version
    private int version;

    public Creneau() {
    }

    public Creneau(Membre membre, Espace espace, LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.membre = membre;
        this.espace = espace;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Espace getEspace() { return espace; }
    public void setEspace(Espace espace) { this.espace = espace; }

    public Membre getMembre() { return membre; }
    public void setMembre(Membre membre) { this.membre = membre; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public int getVersion() { return version; }
}
