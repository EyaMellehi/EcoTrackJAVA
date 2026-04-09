package org.example.Entities;

import java.time.LocalDateTime;

public class RapportRecyc {
    private int id;
    private LocalDateTime dateCollect;
    private double quantiteCollecte;
    private String commentaire;
    private int pointAttribue;
    private PointRecyclage pointRecy;
    private User agentTerrain;

    public RapportRecyc() {
    }

    public RapportRecyc(LocalDateTime dateCollect, double quantiteCollecte, String commentaire,
                        int pointAttribue, PointRecyclage pointRecy, User agentTerrain) {
        this.dateCollect = dateCollect;
        this.quantiteCollecte = quantiteCollecte;
        this.commentaire = commentaire;
        this.pointAttribue = pointAttribue;
        this.pointRecy = pointRecy;
        this.agentTerrain = agentTerrain;
    }

    public RapportRecyc(int id, LocalDateTime dateCollect, double quantiteCollecte, String commentaire,
                        int pointAttribue, PointRecyclage pointRecy, User agentTerrain) {
        this.id = id;
        this.dateCollect = dateCollect;
        this.quantiteCollecte = quantiteCollecte;
        this.commentaire = commentaire;
        this.pointAttribue = pointAttribue;
        this.pointRecy = pointRecy;
        this.agentTerrain = agentTerrain;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getDateCollect() { return dateCollect; }
    public void setDateCollect(LocalDateTime dateCollect) { this.dateCollect = dateCollect; }

    public double getQuantiteCollecte() { return quantiteCollecte; }
    public void setQuantiteCollecte(double quantiteCollecte) { this.quantiteCollecte = quantiteCollecte; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public int getPointAttribue() { return pointAttribue; }
    public void setPointAttribue(int pointAttribue) { this.pointAttribue = pointAttribue; }

    public PointRecyclage getPointRecy() { return pointRecy; }
    public void setPointRecy(PointRecyclage pointRecy) { this.pointRecy = pointRecy; }

    public User getAgentTerrain() { return agentTerrain; }
    public void setAgentTerrain(User agentTerrain) { this.agentTerrain = agentTerrain; }
}