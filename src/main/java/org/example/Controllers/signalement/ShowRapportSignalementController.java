package org.example.Controllers.signalement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Entities.Media;
import org.example.Entities.RapportSignalement;
import org.example.Entities.User;
import org.example.Services.MediaService;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShowRapportSignalementController {

    @FXML private Label lblSignalementId;
    @FXML private Label lblAgentId;
    @FXML private Label lblDateIntervention;
    @FXML private Label lblCommentaire;
    @FXML private FlowPane mediaContainer;


    private final MediaService mediaService = new MediaService();

    private User loggedUser;
    private RapportSignalement rapport;

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;

    }


    public void setRapport(RapportSignalement rapport) {
        this.rapport = rapport;
        loadRapportData();
        loadPhotos();
    }

    private void loadRapportData() {
        if (rapport == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        lblSignalementId.setText(String.valueOf(rapport.getSignalementId()));
        lblAgentId.setText(String.valueOf(rapport.getAgentTerrainId()));
        lblDateIntervention.setText(
                rapport.getDateIntervention() != null
                        ? rapport.getDateIntervention().format(formatter)
                        : ""
        );
        lblCommentaire.setText(rapport.getCommentaire() != null ? rapport.getCommentaire() : "");
    }

    private void loadPhotos() {
        mediaContainer.getChildren().clear();

        if (rapport == null) return;

        try {
            List<Media> medias = mediaService.getByRapportSignalementId(rapport.getId());

            if (medias.isEmpty()) {
                Label empty = new Label("No photos available.");
                empty.setStyle("-fx-text-fill: #7a8087; -fx-font-size: 14px;");
                mediaContainer.getChildren().add(empty);
                return;
            }

            for (Media media : medias) {
                File file = new File(media.getUrl());
                if (!file.exists()) continue;

                ImageView imageView = new ImageView(new Image(file.toURI().toString()));
                imageView.setFitWidth(170);
                imageView.setFitHeight(120);
                imageView.setPreserveRatio(false);

                Label filename = new Label(media.getFilename());
                filename.setMaxWidth(170);
                filename.setWrapText(true);
                filename.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

                VBox box = new VBox(6, imageView, filename);
                mediaContainer.getChildren().add(box);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() {
        try {
            FXMLLoader loader;

            if (loggedUser != null && loggedUser.getRoles() != null) {
                if (loggedUser.getRoles().contains("ROLE_AGENT_TERRAIN")) {
                    loader = new FXMLLoader(getClass().getResource("/signalement/list_assigned_signalements.fxml"));
                } else if (loggedUser.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {
                    loader = new FXMLLoader(getClass().getResource("/signalement/list_municipal_signalements.fxml"));
                } else {
                    loader = new FXMLLoader(getClass().getResource("/signalement/list_signalement.fxml"));
                }
            } else {
                loader = new FXMLLoader(getClass().getResource("/signalement/list_signalement.fxml"));
            }

            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof ListAssignedSignalementController) {
                ((ListAssignedSignalementController) controller).setLoggedUser(loggedUser);
            } else if (controller instanceof ListMunicipalSignalementController) {
                ((ListMunicipalSignalementController) controller).setLoggedUser(loggedUser);
            } else if (controller instanceof ListSignalementController) {
                ((ListSignalementController) controller).setLoggedUser(loggedUser);
            }

            Stage stage = (Stage) lblSignalementId.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reports");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}