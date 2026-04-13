package org.example.Entities;

import java.time.LocalDateTime;

public class RapportSignalement {
    private int id;
    private LocalDateTime dateIntervention;
    private String commentaire;
    private int signalementId;
    private int agentTerrainId;

    public RapportSignalement() {
    }

    public RapportSignalement(LocalDateTime dateIntervention, String commentaire, int signalementId, int agentTerrainId) {
        this.dateIntervention = dateIntervention;
        this.commentaire = commentaire;
        this.signalementId = signalementId;
        this.agentTerrainId = agentTerrainId;
    }

    public RapportSignalement(int id, LocalDateTime dateIntervention, String commentaire, int signalementId, int agentTerrainId) {
        this.id = id;
        this.dateIntervention = dateIntervention;
        this.commentaire = commentaire;
        this.signalementId = signalementId;
        this.agentTerrainId = agentTerrainId;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDateIntervention() {
        return dateIntervention;
    }

    public void setDateIntervention(LocalDateTime dateIntervention) {
        this.dateIntervention = dateIntervention;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public int getSignalementId() {
        return signalementId;
    }

    public void setSignalementId(int signalementId) {
        this.signalementId = signalementId;
    }

    public int getAgentTerrainId() {
        return agentTerrainId;
    }

    public void setAgentTerrainId(int agentTerrainId) {
        this.agentTerrainId = agentTerrainId;
    }

    @Override
    public String toString() {
        return "RapportSignalement{" +
                "id=" + id +
                ", signalementId=" + signalementId +
                ", agentTerrainId=" + agentTerrainId +
                '}';
    }
}