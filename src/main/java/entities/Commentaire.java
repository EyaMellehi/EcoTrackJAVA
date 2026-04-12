package entities;

import java.sql.Timestamp;

public class Commentaire {
    private int id;
    private int annonceId;
    private String texte;
    private Timestamp dateCreation;
    
    // Constructor without id (for INSERT)
    public Commentaire(int annonceId, String texte) {
        this.annonceId = annonceId;
        this.texte = texte;
        this.dateCreation = new Timestamp(System.currentTimeMillis());
    }
    
    // Constructor with id (for SELECT)
    public Commentaire(int id, int annonceId, String texte, Timestamp dateCreation) {
        this.id = id;
        this.annonceId = annonceId;
        this.texte = texte;
        this.dateCreation = dateCreation;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public int getAnnonceId() {
        return annonceId;
    }
    
    public String getTexte() {
        return texte;
    }
    
    public Timestamp getDateCreation() {
        return dateCreation;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setAnnonceId(int annonceId) {
        this.annonceId = annonceId;
    }
    
    public void setTexte(String texte) {
        this.texte = texte;
    }
    
    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", annonceId=" + annonceId +
                ", texte='" + texte + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}

