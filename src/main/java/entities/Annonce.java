package entities;

import java.sql.Timestamp;

public class Annonce {
    private int id;
    private String titre;
    private Timestamp datePub;
    private String region;
    private String contenu;
    private String categorie;
    private String mediaPath;
    
    // Constructor without id (for INSERT)
    public Annonce(String titre, String region, String contenu, String categorie, String mediaPath) {
        this.titre = titre;
        this.region = region;
        this.contenu = contenu;
        this.categorie = categorie;
        this.mediaPath = mediaPath;
    }
    
    // Constructor with id (for SELECT)
    public Annonce(int id, String titre, Timestamp datePub, String region, String contenu, String categorie, String mediaPath) {
        this.id = id;
        this.titre = titre;
        this.datePub = datePub;
        this.region = region;
        this.contenu = contenu;
        this.categorie = categorie;
        this.mediaPath = mediaPath;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getTitre() {
        return titre;
    }
    
    public Timestamp getDatePub() {
        return datePub;
    }
    
    public String getRegion() {
        return region;
    }
    
    public String getContenu() {
        return contenu;
    }
    
    public String getCategorie() {
        return categorie;
    }
    
    public String getMediaPath() {
        return mediaPath;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setTitre(String titre) {
        this.titre = titre;
    }
    
    public void setDatePub(Timestamp datePub) {
        this.datePub = datePub;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
    
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }
    
    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }
    
    @Override
    public String toString() {
        return "Annonce{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", datePub=" + datePub +
                ", region='" + region + '\'' +
                ", contenu='" + contenu + '\'' +
                ", categorie='" + categorie + '\'' +
                ", mediaPath='" + mediaPath + '\'' +
                '}';
    }
}
