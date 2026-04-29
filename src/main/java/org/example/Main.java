package org.example;

import org.example.Utils.MyConnection;
import org.example.Entities.PointRecyclage;
import org.example.Services.PointRecyclageService;
import java.util.List;
public class Main {
    public static void main(String[] args) {
        System.out.println(MyConnection.getInstance());
        try {
            PointRecyclageService service = new PointRecyclageService();
            List<PointRecyclage> points = service.getAllPoints();

            for (PointRecyclage p : points) {
                System.out.println("ID : " + p.getId());
                System.out.println("Catégorie : " + (p.getCategorie() != null ? p.getCategorie().getNom() : "null"));
                System.out.println("Quantité : " + p.getQuantite());
                System.out.println("Adresse : " + p.getAddress());
                System.out.println("Statut : " + p.getStatut());
                System.out.println("--------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}