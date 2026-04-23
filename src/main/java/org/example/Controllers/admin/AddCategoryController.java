package org.example.Controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Entities.Categorie;
import org.example.Services.CategorieService;
import org.example.Utils.ModernNotification;

import java.io.IOException;
import java.sql.SQLException;

public class AddCategoryController {

    @FXML private TextField tfNom;
    @FXML private TextField tfCoefPoints;
    @FXML private TextArea taDescription;

    private final CategorieService categorieService = new CategorieService();

    @FXML
    void saveCategory() {
        String nom = tfNom.getText().trim();
        String coefText = tfCoefPoints.getText().trim();
        String description = taDescription.getText().trim();

        if (nom.isEmpty()) {
            ModernNotification.showWarning(getCurrentStage(), "Validation", "Le nom est obligatoire.");
            return;
        }

        if (nom.length() < 2) {
            ModernNotification.showWarning(getCurrentStage(), "Validation", "Le nom doit contenir au moins 2 caractères.");
            return;
        }

        if (description.length() > 255) {
            ModernNotification.showWarning(getCurrentStage(), "Validation", "La description ne doit pas dépasser 255 caractères.");
            return;
        }

        double coefPoints;
        try {
            coefPoints = Double.parseDouble(coefText);
            if (coefPoints <= 0) {
                ModernNotification.showWarning(getCurrentStage(), "Validation", "Le coefficient doit être supérieur à 0.");
                return;
            }
        } catch (NumberFormatException e) {
            ModernNotification.showWarning(getCurrentStage(), "Validation", "Coefficient invalide.");
            return;
        }

        try {
            Categorie categorie = new Categorie(nom, description.isEmpty() ? null : description, coefPoints);
            categorieService.addCategorie(categorie);

            ModernNotification.showSuccess(getCurrentStage(), "Succès", "Catégorie ajoutée avec succès.");
            navigate("/admin/categories.fxml", "Catégories");

        } catch (SQLException e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Erreur", "Impossible d'ajouter la catégorie.");
        }
    }

    @FXML
    void backToCategories() {
        navigate("/admin/categories.fxml", "Catégories");
    }

    @FXML
    void goToDashboard() {
        navigate("/admin/dashboard.fxml", "Dashboard");
    }

    @FXML
    void goToCategories() {
        navigate("/admin/categories.fxml", "Catégories");
    }

    public void goToAssociation() {
        navigate("/admin_association/association.fxml", "Association");
    }

    public void goToDonation() {
        navigate("/donation/donationIndex.fxml", "Donation");
    }

    private void navigate(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = getCurrentStage();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Erreur", "Impossible d'ouvrir la page " + title + ".");
        }
    }

    private Stage getCurrentStage() {
        return tfNom != null && tfNom.getScene() != null
                ? (Stage) tfNom.getScene().getWindow()
                : null;
    }
}