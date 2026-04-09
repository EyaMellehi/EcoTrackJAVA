package org.example.Controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Entities.Categorie;
import org.example.Services.CategorieService;

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
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom est obligatoire.");
            return;
        }

        if (nom.length() < 2) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom doit contenir au moins 2 caractères.");
            return;
        }

        if (description.length() > 255) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La description ne doit pas dépasser 255 caractères.");
            return;
        }

        double coefPoints;
        try {
            coefPoints = Double.parseDouble(coefText);
            if (coefPoints <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le coefficient doit être supérieur à 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Coefficient invalide.");
            return;
        }

        try {
            Categorie categorie = new Categorie(nom, description.isEmpty() ? null : description, coefPoints);
            categorieService.addCategorie(categorie);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée avec succès.");
            navigate("/admin/categories.fxml", "Catégories");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter la catégorie.");
            e.printStackTrace();
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

    private void navigate(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) tfNom.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}