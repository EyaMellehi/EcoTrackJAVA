package org.example.Entities;

import java.time.LocalDateTime;

public class CommentaireAnnonce {
    private int id;
    private String description;
    private String etat;
    private LocalDateTime dateComm;
    private Integer annonceId;
    private Integer auteurId;
    private String auteurNom;
    private Integer parentId;
    private String moderationStatus;
    private LocalDateTime dateModification;

    public CommentaireAnnonce() {}

    public CommentaireAnnonce(String description, Integer annonceId, Integer auteurId) {
        this.description = description;
        this.annonceId = annonceId;
        this.auteurId = auteurId;
        this.etat = "En attente";
        this.moderationStatus = "En attente";
        this.dateComm = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public LocalDateTime getDateComm() { return dateComm; }
    public void setDateComm(LocalDateTime dateComm) { this.dateComm = dateComm; }

    public Integer getAnnonceId() { return annonceId; }
    public void setAnnonceId(Integer annonceId) { this.annonceId = annonceId; }

    public Integer getAuteurId() { return auteurId; }
    public void setAuteurId(Integer auteurId) { this.auteurId = auteurId; }

    public String getAuteurNom() { return auteurNom; }
    public void setAuteurNom(String auteurNom) { this.auteurNom = auteurNom; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public String getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(String moderationStatus) { this.moderationStatus = moderationStatus; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    @Override
    public String toString() {
        return "CommentaireAnnonce{" + "id=" + id + ", annonceId=" + annonceId + '}';
    }
}

