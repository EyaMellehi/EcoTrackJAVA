package main;

import entities.Annonce;
import gui.AfficherDetailsAnnonce;
import javafx.fxml.FXMLLoader;
import services.AnnonceService;

import java.sql.SQLException;

/**
 * Test simple pour vérifier que la page de détails charge correctement
 */
public class TestDetailsPage {
    public static void main(String[] args) {
        try {
            System.out.println("=== TEST DE CHARGEMENT FXML ===");
            
            // Charger une annonce
            AnnonceService service = new AnnonceService();
            java.util.List<Annonce> annonces = service.readAll();
            
            if (annonces.isEmpty()) {
                System.err.println("❌ Aucune annonce en base!");
                return;
            }
            
            Annonce annonce = annonces.get(0);
            System.out.println("✅ Annonce chargée: " + annonce.getTitre());
            
            // Charger le FXML
            System.out.println("📄 Chargement du FXML AfficherDetailsAnnonce.fxml...");
            FXMLLoader loader = new FXMLLoader(TestDetailsPage.class.getResource("/AfficherDetailsAnnonce.fxml"));
            Object root = loader.load();
            System.out.println("✅ FXML chargé avec succès!");
            
            // Obtenir le contrôleur
            AfficherDetailsAnnonce controller = loader.getController();
            System.out.println("✅ Contrôleur obtenu: " + controller.getClass().getName());
            
            // Définir l'annonce
            controller.setAnnonce(annonce);
            System.out.println("✅ Annonce définie dans le contrôleur");
            
            System.out.println("\n✅ TEST RÉUSSI - Le FXML charge correctement!");
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

