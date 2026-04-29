package org.example.Entities;

import java.time.LocalDateTime;

public class Event {
    private int id;
    private String titre;
    private String description;
    private String lieu;
    private LocalDateTime dateDeb;
    private LocalDateTime dateFin;
    private int capaciteMax;
    private int pointGain;
    private String statut; // "publie", "termine"
    private int createurId;
    private Integer coverMediaId;
    private int participationCount;

    // Constructeurs
    public Event() {
    }

    public Event(String titre, String description, String lieu, LocalDateTime dateDeb, 
                 LocalDateTime dateFin, int capaciteMax, int pointGain, String statut) {
        this.titre = titre;
        this.description = description;
        this.lieu = lieu;
        this.dateDeb = dateDeb;
        this.dateFin = dateFin;
        this.capaciteMax = capaciteMax;
        this.pointGain = pointGain;
        this.statut = statut;
    }

    // Getters et Setters
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

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public LocalDateTime getDateDeb() {
        return dateDeb;
    }

    public void setDateDeb(LocalDateTime dateDeb) {
        this.dateDeb = dateDeb;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public int getPointGain() {
        return pointGain;
    }

    public void setPointGain(int pointGain) {
        this.pointGain = pointGain;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCoverMediaPath() {
        return coverMediaId != null ? String.valueOf(coverMediaId) : null;
    }

    public void setCoverMediaPath(String coverMediaPath) {
        if (coverMediaPath != null && !coverMediaPath.isEmpty()) {
            try {
                this.coverMediaId = Integer.parseInt(coverMediaPath);
            } catch (NumberFormatException e) {
                this.coverMediaId = null;
            }
        }
    }

    public int getCreateurId() {
        return createurId;
    }

    public void setCreateurId(int createurId) {
        this.createurId = createurId;
    }

    public Integer getCoverMediaId() {
        return coverMediaId;
    }

    public void setCoverMediaId(Integer coverMediaId) {
        this.coverMediaId = coverMediaId;
    }

    public int getParticipationCount() {
        return participationCount;
    }

    public void setParticipationCount(int participationCount) {
        this.participationCount = participationCount;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", lieu='" + lieu + '\'' +
                ", dateDeb=" + dateDeb +
                ", statut='" + statut + '\'' +
                '}';
    }
}

