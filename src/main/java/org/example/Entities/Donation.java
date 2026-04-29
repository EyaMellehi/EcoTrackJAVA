package org.example.Entities;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Donation {

    private Integer id;
    private String type;
    private Double montant;
    private String descriptionMateriel;
    private LocalDateTime dateDon;
    private String statut;
    private String messageDon;

    private Association association;
    private User donateur;

    public Donation() {
        this.type = "argent";
        this.statut = "EN_ATTENTE";
        this.dateDon = LocalDateTime.now(ZoneId.of("Africa/Tunis"));
    }

    // ✅ GETTERS & SETTERS

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public String getDescriptionMateriel() {
        return descriptionMateriel;
    }

    public void setDescriptionMateriel(String descriptionMateriel) {
        this.descriptionMateriel = descriptionMateriel;
    }

    public LocalDateTime getDateDon() {
        return dateDon;
    }

    public void setDateDon(LocalDateTime dateDon) {
        this.dateDon = dateDon;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMessageDon() {
        return messageDon;
    }

    public void setMessageDon(String messageDon) {
        this.messageDon = messageDon;
    }

    public Association getAssociation() {
        return association;
    }

    public void setAssociation(Association association) {
        this.association = association;
    }

    public User getDonateur() {
        return donateur;
    }

    public void setDonateur(User donateur) {
        this.donateur = donateur;
    }
}