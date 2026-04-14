package org.example.Entities;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Association {

    private Integer id;
    private String nom;
    private String type;
    private String description;
    private String region;
    private int tel;
    private String email;
    private String logo;
    private boolean isActive;
    private LocalDateTime dateCreation;
    private String addresse;

    private List<Donation> donations;

    // ✅ CONSTRUCTEUR
    public Association() {
        this.donations = new ArrayList<>();
        this.dateCreation = LocalDateTime.now(ZoneId.of("Africa/Tunis"));
        this.isActive = true;
    }

    // ✅ GETTERS & SETTERS

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getTel() {
        return tel;
    }

    public void setTel(int tel) {
        this.tel = tel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getAddresse() {
        return addresse;
    }

    public void setAddresse(String addresse) {
        this.addresse = addresse;
    }

    public List<Donation> getDonations() {
        return donations;
    }

    public void setDonations(List<Donation> donations) {
        this.donations = donations;
    }

    // ✅ Méthodes équivalentes Symfony

    public void addDonation(Donation donation) {
        if (!this.donations.contains(donation)) {
            this.donations.add(donation);
        }
    }

    public void removeDonation(Donation donation) {
        this.donations.remove(donation);
    }
}