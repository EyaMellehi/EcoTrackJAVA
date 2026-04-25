package org.example.Entities;

import java.time.LocalDateTime;

public class SignalementCommentaire {
    private int id;
    private int commentaireId;
    private int citoyenId;
    private String raison;
    private String statut;
    private LocalDateTime dateSignalement;
    /** Rempli via JOIN pour l'interface admin (non colonne persistée seule). */
    private String commentaireDescription;

    public SignalementCommentaire() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommentaireId() {
        return commentaireId;
    }

    public void setCommentaireId(int commentaireId) {
        this.commentaireId = commentaireId;
    }

    public int getCitoyenId() {
        return citoyenId;
    }

    public void setCitoyenId(int citoyenId) {
        this.citoyenId = citoyenId;
    }

    public String getRaison() {
        return raison;
    }

    public void setRaison(String raison) {
        this.raison = raison;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateSignalement() {
        return dateSignalement;
    }

    public void setDateSignalement(LocalDateTime dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public String getCommentaireDescription() {
        return commentaireDescription;
    }

    public void setCommentaireDescription(String commentaireDescription) {
        this.commentaireDescription = commentaireDescription;
    }
}
