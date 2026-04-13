package org.example.Entities;

public class Categorie {
    private int id;
    private String nom;
    private String description;
    private double coefPoints;

    public Categorie() {
    }

    public Categorie(String nom, String description, double coefPoints) {
        this.nom = nom;
        this.description = description;
        this.coefPoints = coefPoints;
    }

    public Categorie(int id, String nom, String description, double coefPoints) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.coefPoints = coefPoints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCoefPoints() {
        return coefPoints;
    }

    public void setCoefPoints(double coefPoints) {
        this.coefPoints = coefPoints;
    }

    @Override
    public String toString() {
        return nom;
    }
    private int nbPoints;

    public int getNbPoints() {
        return nbPoints;
    }

    public void setNbPoints(int nbPoints) {
        this.nbPoints = nbPoints;
    }
}