package org.example.Controllers.recyclage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.PointRecyclage;
import org.example.Entities.RapportRecyc;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;
import org.example.Services.RapportRecycService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CreateRapportRecycController {

    @FXML private Label lblPointId;
    @FXML private Label lblPointAddress;
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

        lblPointId.setText("Créer un rapport - Point #" + point.getId());
        lblPointAddress.setText(point.getAddress());
        dpDateCollect.setValue(LocalDate.now());
    }

    @FXML
    private void saveRapport() {
        try {
            if (dpDateCollect.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Choisis une date de collecte.");
                return;
            }

            if (txtQuantiteCollecte.getText() == null || txtQuantiteCollecte.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Saisis la quantité collectée.");
                return;
            }

            double qte = Double.parseDouble(txtQuantiteCollecte.getText().trim());

            RapportRecyc rapport = new RapportRecyc();
            rapport.setDateCollect(dpDateCollect.getValue().atStartOfDay());
            rapport.setQuantiteCollecte(qte);
            rapport.setCommentaire(txtCommentaire.getText());
            rapport.setPointAttribue(currentPoint.getId());
            rapport.setPointRecy(currentPoint);
            rapport.setAgentTerrain(loggedUser);

            rapportService.createRapport(rapport);
            pointService.markPointCollected(currentPoint.getId());

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport enregistré avec succès.");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/show_rapport_recyc_terrain.fxml"));
            Parent root = loader.load();

            ShowRapportRecycTerrainController controller = loader.getController();
            controller.setData(loggedUser, currentPoint);

            Stage stage = (Stage) lblPointId.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Rapport");
            stage.show();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être un nombre.");
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