package org.example.Entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PointRecyclage {
    private int id;
    private double quantite;
    private LocalDate dateDec;
    private String description;
    private String address;
    private double latitude;
    private double longitude;
    private String statut;

    private Categorie categorie;
    private RapportRecyc rapportRecyc;
    private User citoyen;
    private User agentTerrain;

    private LocalDateTime assignedAt;
    private Integer aiScore;
    private String aiPriority;
    private String aiExplanation;
    private LocalDateTime aiEstimatedAt;

    public PointRecyclage() {
    }

    public PointRecyclage(double quantite, LocalDate dateDec, String description, String address,
                          double latitude, double longitude, String statut,
                          Categorie categorie, User citoyen, User agentTerrain) {
        this.quantite = quantite;
        this.dateDec = dateDec;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.statut = statut;
        this.categorie = categorie;
        this.citoyen = citoyen;
        this.agentTerrain = agentTerrain;
    }

    public PointRecyclage(int id, double quantite, LocalDate dateDec, String description, String address,
                          double latitude, double longitude, String statut,
                          Categorie categorie, User citoyen, User agentTerrain,
                          LocalDateTime assignedAt, Integer aiScore, String aiPriority,
                          String aiExplanation, LocalDateTime aiEstimatedAt) {
        this.id = id;
        this.quantite = quantite;
        this.dateDec = dateDec;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.statut = statut;
        this.categorie = categorie;
        this.citoyen = citoyen;
        this.agentTerrain = agentTerrain;
        this.assignedAt = assignedAt;
        this.aiScore = aiScore;
        this.aiPriority = aiPriority;
        this.aiExplanation = aiExplanation;
        this.aiEstimatedAt = aiEstimatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getQuantite() { return quantite; }
    public void setQuantite(double quantite) { this.quantite = quantite; }

    public LocalDate getDateDec() { return dateDec; }
    public void setDateDec(LocalDate dateDec) { this.dateDec = dateDec; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public RapportRecyc getRapportRecyc() { return rapportRecyc; }
    public void setRapportRecyc(RapportRecyc rapportRecyc) { this.rapportRecyc = rapportRecyc; }

    public User getCitoyen() { return citoyen; }
    public void setCitoyen(User citoyen) { this.citoyen = citoyen; }

    public User getAgentTerrain() { return agentTerrain; }
    public void setAgentTerrain(User agentTerrain) { this.agentTerrain = agentTerrain; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public Integer getAiScore() { return aiScore; }
    public void setAiScore(Integer aiScore) { this.aiScore = aiScore; }

    public String getAiPriority() { return aiPriority; }
    public void setAiPriority(String aiPriority) { this.aiPriority = aiPriority; }

    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }

    public LocalDateTime getAiEstimatedAt() { return aiEstimatedAt; }
    public void setAiEstimatedAt(LocalDateTime aiEstimatedAt) { this.aiEstimatedAt = aiEstimatedAt; }

    @Override
    public String toString() {
        return "PointRecyclage{" +
                "id=" + id +
                ", quantite=" + quantite +
                ", dateDec=" + dateDec +
                ", statut='" + statut + '\'' +
                ", categorie=" + (categorie != null ? categorie.getNom() : "null") +
                '}';
    }
}