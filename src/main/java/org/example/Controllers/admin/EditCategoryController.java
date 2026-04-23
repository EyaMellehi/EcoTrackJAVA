package org.example.Controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.Entities.Categorie;
import org.example.Services.CategorieService;
import org.example.Utils.ModernNotification;

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

        tfNom.setText(categorie.getNom() != null ? categorie.getNom() : "");
        tfCoefPoints.setText(String.valueOf(categorie.getCoefPoints()));
        taDescription.setText(categorie.getDescription() != null ? categorie.getDescription() : "");

        lblTitle.setText("Modifier : " + (categorie.getNom() != null ? categorie.getNom() : ""));
        lblSubTitle.setText("Mets à jour le nom / coef / description.");
        lblId.setText("#" + categorie.getId());
        lblCoef.setText(String.valueOf(categorie.getCoefPoints()));
        lblNbPoints.setText(String.valueOf(categorie.getNbPoints()));
    }

    @FXML
    void updateCategory() {
        if (categorie == null) return;

        String nom = tfNom.getText() != null ? tfNom.getText().trim() : "";
        String coefText = tfCoefPoints.getText() != null ? tfCoefPoints.getText().trim() : "";
        String description = taDescription.getText() != null ? taDescription.getText().trim() : "";

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
            categorie.setNom(nom);
            categorie.setDescription(description.isEmpty() ? null : description);
            categorie.setCoefPoints(coefPoints);

            categorieService.updateCategorie(categorie);

            ModernNotification.showSuccess(getCurrentStage(), "Succès", "Catégorie modifiée avec succès.");
            navigate("/admin/categories.fxml", "Catégories");

        } catch (SQLException e) {
            e.printStackTrace();
            ModernNotification.showError(getCurrentStage(), "Erreur", "Impossible de modifier la catégorie.");
        }
    }

    @FXML
    void deleteCategory() {
        if (categorie == null) return;

        boolean confirmed = ModernNotification.showConfirmation(
                "Confirmation",
                "Supprimer cette catégorie ?"
        );

        if (confirmed) {
            try {
                categorieService.deleteCategorie(categorie.getId());
                ModernNotification.showSuccess(getCurrentStage(), "Succès", "Catégorie supprimée.");
                navigate("/admin/categories.fxml", "Catégories");
            } catch (SQLException e) {
                e.printStackTrace();
                ModernNotification.showError(getCurrentStage(), "Erreur", "Impossible de supprimer la catégorie.");
            }
        }
    }

    @FXML
    void backToCategories() {
        navigate("/admin/categories.fxml", "Catégories");
    }

    @FXML
    void goToDashboard() {
        navigate("/admin/admin_dashboard.fxml", "Dashboard");
    }

    @FXML
    void goToSubscribers() {
        navigate("/admin/subscribers.fxml", "Subscribers");
    }

    @FXML
    void goToMunicipalAgents() {
        navigate("/admin/municipal_agents.fxml", "Municipal Agents");
    }

    @FXML
    void goToFieldAgents() {
        navigate("/admin/field_agents.fxml", "Field Agents");
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

    @FXML
    void logout() {
        navigate("/home.fxml", "EcoTrack - Home");
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