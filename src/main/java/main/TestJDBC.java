package main;

import entities.Annonce;
import services.AnnonceService;

import java.sql.SQLException;

public class TestJDBC {
    public static void main(String[] args) {
        try {
            // Create an Annonce with sample data
            Annonce annonce = new Annonce(
                    "Test Annonce",
                    "Tunis",
                    "Ceci est une annonce de test",
                    "Agriculture",
                    null  // No media for test
            );
            
            // Create the annonce
            AnnonceService service = new AnnonceService();
            service.create(annonce);
            
            // Read all annonces
            System.out.println("Annonces dans la base de données:");
            service.readAll().forEach(System.out::println);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
