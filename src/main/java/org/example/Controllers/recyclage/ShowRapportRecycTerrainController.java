package org.example.Controllers.recyclage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.Entities.PointRecyclage;
import org.example.Entities.RapportRecyc;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;
import org.example.Services.RapportRecycService;

import java.sql.SQLException;
import java.time.Duration;

public class ShowRapportRecycTerrainController {

    @FXML private Label lblTitle;
    @FXML private Label lblAddress;
    @FXML private Label lblCategorie;
    @FXML private Label lblQuantiteDeclaree;
    @FXML private Label lblDateCreation;
    @FXML private Label lblStatut;
    @FXML private Label lblDateCollecte;
    @FXML private Label lblQuantiteCollectee;
    @FXML private Label lblAgent;
    @FXML private Label lblPointAttribue;
    @FXML private Label lblTempsTraitement;
    @FXML private Label lblDescriptionPoint;
    @FXML private Label lblCommentaire;

    private User loggedUser;
    private PointRecyclage currentPoint;
    private RapportRecyc currentRapport;

    private final PointRecyclageService pointService = new PointRecyclageService();
    private final RapportRecycService rapportService = new RapportRecycService();

    public void setData(User loggedUser, PointRecyclage point) {
        this.loggedUser = loggedUser;
        this.currentPoint = point;
        loadData();
    }

    private void loadData() {
        try {
            currentPoint = pointService.getPointById(currentPoint.getId());
            currentRapport = rapportService.getRapportByPointId(currentPoint.getId());

            if (currentRapport == null) {
                lblTitle.setText("Aucun rapport trouvé");
                return;
            }

            lblTitle.setText("Rapport - Point #" + currentPoint.getId());
            lblAddress.setText(currentPoint.getAddress());
            lblCategorie.setText(currentPoint.getCategorie() != null ? currentPoint.getCategorie().getNom() : "-");
            lblQuantiteDeclaree.setText(currentPoint.getQuantite() + " kg");
            lblDateCreation.setText(currentPoint.getDateDec() != null ? currentPoint.getDateDec().toString() : "-");
            lblStatut.setText(currentPoint.getStatut());

            lblDateCollecte.setText(currentRapport.getDateCollect() != null ? currentRapport.getDateCollect().toString() : "-");
            lblQuantiteCollectee.setText(currentRapport.getQuantiteCollecte() + " kg");
            lblAgent.setText(currentRapport.getAgentTerrain() != null ? currentRapport.getAgentTerrain().getName() : "-");
            lblPointAttribue.setText("#" + currentRapport.getPointAttribue());

            if (currentPoint.getDateDec() != null && currentRapport.getDateCollect() != null) {
                Duration d = Duration.between(currentPoint.getDateDec().atStartOfDay(), currentRapport.getDateCollect());
                long days = d.toDays();
                long hours = d.toHours() % 24;
                long minutes = d.toMinutes() % 60;
                lblTempsTraitement.setText(days + "j : " + hours + "h : " + minutes + "min");
            } else {
                lblTempsTraitement.setText("-");
            }

            lblDescriptionPoint.setText(currentPoint.getDescription() != null && !currentPoint.getDescription().isEmpty() ? currentPoint.getDescription() : "-");
            lblCommentaire.setText(currentRapport.getCommentaire() != null && !currentRapport.getCommentaire().isEmpty() ? currentRapport.getCommentaire() : "-");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBackToPoint() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/terrain_point_details.fxml"));
            Parent root = loader.load();

            TerrainPointDetailsController controller = loader.getController();
            controller.setData(loggedUser, currentPoint);

            Stage stage = (Stage) lblTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Point details");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}