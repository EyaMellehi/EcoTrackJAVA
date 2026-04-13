package org.example.Controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Entities.Categorie;
import org.example.Services.CategorieService;

import java.io.IOException;
import java.sql.SQLException;

public class EditCategoryController {

    @FXML private TextField tfNom;
    @FXML private TextField tfCoefPoints;
    @FXML private TextArea taDescription;

    @FXML private Label lblTitle;
    @FXML private Label lblSubTitle;
    @FXML private Label lblId;
    @FXML private Label lblCoef;
    @FXML private Label lblNbPoints;

    private final CategorieService categorieService = new CategorieService();
    private Categorie categorie;

    public void setCategorieData(Categorie categorie) {
        this.categorie = categorie;

        tfNom.setText(categorie.getNom());
        tfCoefPoints.setText(String.valueOf(categorie.getCoefPoints()));
        taDescription.setText(categorie.getDescription());

        lblTitle.setText("Modifier : " + categorie.getNom());
        lblSubTitle.setText("Mets à jour le nom / coef / description.");
        lblId.setText("#" + categorie.getId());
        lblCoef.setText(String.valueOf(categorie.getCoefPoints()));
        lblNbPoints.setText(String.valueOf(categorie.getNbPoints()));
    }

    @FXML
    void updateCategory() {
        if (categorie == null) return;

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
            categorie.setNom(nom);
            categorie.setDescription(description.isEmpty() ? null : description);
            categorie.setCoefPoints(coefPoints);

            categorieService.updateCategorie(categorie);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie modifiée avec succès.");
            navigate("/admin/categories.fxml", "Catégories");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier la catégorie.");
            e.printStackTrace();
        }
    }

    @FXML
    void deleteCategory() {
        if (categorie == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer cette catégorie ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                categorieService.deleteCategorie(categorie.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie supprimée.");
                navigate("/admin/categories.fxml", "Catégories");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la catégorie.");
                e.printStackTrace();
            }
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