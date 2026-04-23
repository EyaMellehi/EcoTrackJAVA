package org.example.Controllers.recyclage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Entities.PointRecyclage;
import org.example.Entities.RapportRecyc;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;
import org.example.Services.RapportRecycService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class CreateRapportRecycController {

    @FXML private Label lblPointId;
    @FXML private Label lblPointAddress;
    @FXML private Label lblQuantiteDeclaree;
    @FXML private DatePicker dpDateCollect;
    @FXML private TextField txtQuantiteCollecte;
    @FXML private TextArea txtCommentaire;

    private User loggedUser;
    private PointRecyclage currentPoint;

    private final RapportRecycService rapportService = new RapportRecycService();
    private final PointRecyclageService pointService = new PointRecyclageService();

    public void setData(User loggedUser, PointRecyclage point) {
        this.loggedUser = loggedUser;
        this.currentPoint = point;

        if (point != null) {
            lblPointId.setText("Créer un rapport - Point #" + point.getId());
            lblPointAddress.setText(point.getAddress() != null ? point.getAddress() : "-");
            lblQuantiteDeclaree.setText(point.getQuantite() + " kg");
        }

        dpDateCollect.setValue(LocalDate.now());

        txtQuantiteCollecte.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            String cleaned = newVal.replace(",", ".");
            if (!cleaned.matches("\\d*(\\.\\d*)?")) {
                txtQuantiteCollecte.setText(oldVal);
            }
        });
    }

    @FXML
    private void openChatbotDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/chatbot_dialog.fxml"));
            Parent root = loader.load();

            ChatbotDialogController controller = loader.getController();
            controller.setTargetInput(txtCommentaire);
            controller.setMode("RAPPORT_COMMENT");

            Map<String, String> ctx = new HashMap<>();
            ctx.put("categorie",
                    currentPoint != null && currentPoint.getCategorie() != null && currentPoint.getCategorie().getNom() != null
                            ? currentPoint.getCategorie().getNom()
                            : "");
            ctx.put("quantiteDeclaree",
                    currentPoint != null ? String.valueOf(currentPoint.getQuantite()) : "");
            ctx.put("quantiteCollectee",
                    txtQuantiteCollecte.getText() != null ? txtQuantiteCollecte.getText().trim() : "");
            ctx.put("adresse",
                    currentPoint != null && currentPoint.getAddress() != null ? currentPoint.getAddress() : "");

            controller.setContext(ctx);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Assistant rédaction");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            txtCommentaire.requestFocus();
            txtCommentaire.positionCaret(txtCommentaire.getText().length());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d’ouvrir l’assistant IA.");
        }
    }

    @FXML
    private void saveRapport() {
        try {
            if (loggedUser == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun agent connecté.");
                return;
            }

            if (currentPoint == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun point sélectionné.");
                return;
            }

            if (dpDateCollect.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Choisis une date de collecte.");
                return;
            }

            if (dpDateCollect.getValue().isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.WARNING, "Attention", "La date de collecte ne peut pas être dans le futur.");
                return;
            }

            String quantiteText = txtQuantiteCollecte.getText() != null
                    ? txtQuantiteCollecte.getText().trim().replace(",", ".")
                    : "";

            if (quantiteText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Saisis la quantité collectée.");
                return;
            }

            double qte;
            try {
                qte = Double.parseDouble(quantiteText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être un nombre valide.");
                return;
            }

            if (qte <= 0) {
                showAlert(Alert.AlertType.WARNING, "Attention", "La quantité collectée doit être supérieure à 0.");
                return;
            }

            String commentaire = txtCommentaire.getText() != null ? txtCommentaire.getText().trim() : "";

            if (commentaire.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "La description est obligatoire.");
                return;
            }

            if (currentPoint.getCategorie() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Catégorie introuvable pour ce point.");
                return;
            }

            if (currentPoint.getCitoyen() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Citoyen introuvable pour ce point.");
                return;
            }

            int pointsGagnes = (int) Math.round(qte * currentPoint.getCategorie().getCoefPoints());

            LocalDateTime dateCollecte = LocalDateTime.of(dpDateCollect.getValue(), LocalTime.now());

            RapportRecyc rapport = new RapportRecyc();
            rapport.setDateCollect(dateCollecte);
            rapport.setQuantiteCollecte(qte);
            rapport.setCommentaire(commentaire);
            rapport.setPointAttribue(pointsGagnes);
            rapport.setPointRecy(currentPoint);
            rapport.setAgentTerrain(loggedUser);

            rapportService.createRapportAndRewardCitizen(rapport, currentPoint.getCitoyen().getId());
            pointService.markPointCollected(currentPoint.getId());

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Rapport enregistré avec succès. " + pointsGagnes + " points ajoutés au citoyen.");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/show_rapport_recyc_terrain.fxml"));
            Parent root = loader.load();

            ShowRapportRecycTerrainController controller = loader.getController();
            controller.setData(loggedUser, currentPoint);

            Stage stage = (Stage) lblPointId.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Rapport");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer le rapport.");
        }
    }

    @FXML
    private void cancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/terrain_point_details.fxml"));
            Parent root = loader.load();

            TerrainPointDetailsController controller = loader.getController();
            controller.setData(loggedUser, currentPoint);

            Stage stage = (Stage) lblPointId.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Point details");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();

        } catch (Exception e) {
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