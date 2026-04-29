package org.example.Entities;

import java.time.LocalDateTime;

public class Signalement {
    private int id;
    private String titre;
    private String description;
    private String type;
    private String statut;
    private String addresse;
    private double latitude;
    private double longitude;
    private LocalDateTime dateCreation;
    private Integer citoyenId;
    private Integer agentAssigneId;
    private String delegation;
    private LocalDateTime assignedAt;

    public Signalement() {
    }

    public Signalement(String titre, String description, String type, String statut,
                       String addresse, double latitude, double longitude,
                       LocalDateTime dateCreation, Integer citoyenId,
                       Integer agentAssigneId, String delegation, LocalDateTime assignedAt) {
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.statut = statut;
        this.addresse = addresse;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateCreation = dateCreation;
        this.citoyenId = citoyenId;
        this.agentAssigneId = agentAssigneId;
        this.delegation = delegation;
        this.assignedAt = assignedAt;
    }

    public Signalement(int id, String titre, String description, String type, String statut,
                       String addresse, double latitude, double longitude,
                       LocalDateTime dateCreation, Integer citoyenId,
                       Integer agentAssigneId, String delegation, LocalDateTime assignedAt) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.statut = statut;
        this.addresse = addresse;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateCreation = dateCreation;
        this.citoyenId = citoyenId;
        this.agentAssigneId = agentAssigneId;
        this.delegation = delegation;
        this.assignedAt = assignedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getAddresse() {
        return addresse;
    }

    public void setAddresse(String addresse) {
        this.addresse = addresse;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Integer getCitoyenId() {
        return citoyenId;
    }

    public void setCitoyenId(Integer citoyenId) {
        this.citoyenId = citoyenId;
    }

    public Integer getAgentAssigneId() {
        return agentAssigneId;
    }

    public void setAgentAssigneId(Integer agentAssigneId) {
        this.agentAssigneId = agentAssigneId;
    }

    public String getDelegation() {
        return delegation;
    }

    public void setDelegation(String delegation) {
        this.delegation = delegation;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    @Override
    public String toString() {
        return "Signalement{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                ", statut='" + statut + '\'' +
                ", addresse='" + addresse + '\'' +
                ", citoyenId=" + citoyenId +
                '}';
    }
}