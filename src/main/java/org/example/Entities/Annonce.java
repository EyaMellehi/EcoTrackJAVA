package org.example.Entities;

import java.time.LocalDateTime;

public class Annonce {
    private int id;
    private String titre;
    private LocalDateTime datePub;
    private String region;
    private String contenu;
    private String categorie;
    private String mediaPath;
    private Integer auteurId;
    private String auteurNom;
    private LocalDateTime dateModification;
    private boolean active;

    public Annonce() {}

    public Annonce(String titre, LocalDateTime datePub, String region, String contenu, 
                   String categorie, String mediaPath, Integer auteurId) {
        this.titre = titre;
        this.datePub = datePub;
        this.region = region;
        this.contenu = contenu;
        this.categorie = categorie;
        this.mediaPath = mediaPath;
        this.auteurId = auteurId;
        this.active = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public LocalDateTime getDatePub() { return datePub; }
    public void setDatePub(LocalDateTime datePub) { this.datePub = datePub; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getMediaPath() { return mediaPath; }
    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }

    public Integer getAuteurId() { return auteurId; }
    public void setAuteurId(Integer auteurId) { this.auteurId = auteurId; }

    public String getAuteurNom() { return auteurNom; }
    public void setAuteurNom(String auteurNom) { this.auteurNom = auteurNom; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "Annonce{" + "id=" + id + ", titre='" + titre + '\'' + '}';
    }
}

