package org.example.Controllers.signalement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Entities.Media;
import org.example.Entities.RapportSignalement;
import org.example.Entities.Signalement;
import org.example.Entities.User;
import org.example.Services.MediaService;
import org.example.Services.RapportSignalementService;
import org.example.Services.UserService;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShowSignalementController {

    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblType;
    @FXML private Label lblStatut;
    @FXML private Label lblAddresse;
    @FXML private Label lblLatitude;
    @FXML private Label lblLongitude;
    @FXML private Label lblDateCreation;
    @FXML private Label lblDelegation;
    @FXML private Label lblAgentAssigneId;
    @FXML private Label lblAssignedAt;
    @FXML private FlowPane mediaContainer;


    private final RapportSignalementService rapportService = new RapportSignalementService();
    private final UserService userService = new UserService();
    private final MediaService mediaService = new MediaService();

    private User loggedUser;
    private Signalement signalement;

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;

    }

    public void setSignalement(Signalement signalement) {
        this.signalement = signalement;
        loadSignalementData();
        loadPhotos();
    }


    private void loadSignalementData() {
        if (signalement == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        lblTitre.setText(signalement.getTitre() != null ? signalement.getTitre() : "");
        lblDescription.setText(signalement.getDescription() != null ? signalement.getDescription() : "");
        lblType.setText(signalement.getType() != null ? signalement.getType() : "");
        lblStatut.setText(signalement.getStatut() != null ? signalement.getStatut() : "");
        lblAddresse.setText(signalement.getAddresse() != null ? signalement.getAddresse() : "");
        lblLatitude.setText(String.valueOf(signalement.getLatitude()));
        lblLongitude.setText(String.valueOf(signalement.getLongitude()));
        lblDateCreation.setText(signalement.getDateCreation() != null ? signalement.getDateCreation().format(formatter) : "");
        lblDelegation.setText(signalement.getDelegation() != null ? signalement.getDelegation() : "");

        if (signalement.getAgentAssigneId() != null) {
            try {
                User assignedAgent = userService.getUserById(signalement.getAgentAssigneId());
                lblAgentAssigneId.setText(assignedAgent != null ? assignedAgent.getName() : "Unknown agent");
            } catch (Exception e) {
                lblAgentAssigneId.setText("Unknown agent");
            }
        } else {
            lblAgentAssigneId.setText("Not assigned");
        }

        lblAssignedAt.setText(signalement.getAssignedAt() != null ? signalement.getAssignedAt().format(formatter) : "Not assigned");
    }

    private void loadPhotos() {
        mediaContainer.getChildren().clear();

        if (signalement == null) return;

        try {
            List<Media> medias = mediaService.getBySignalementId(signalement.getId());

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
                imageView.setFitWidth(240);
                imageView.setFitHeight(170);
                imageView.setPreserveRatio(false);
                imageView.setStyle("-fx-cursor: hand;");
                imageView.setOnMouseClicked(e -> openImagePreview(file));

                Label filename = new Label(media.getFilename());
                filename.setMaxWidth(240);
                filename.setWrapText(true);
                filename.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

                VBox box = new VBox(6, imageView, filename);
                mediaContainer.getChildren().add(box);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openImagePreview(File file) {
        Stage previewStage = new Stage();
        ImageView imageView = new ImageView(new Image(file.toURI().toString()));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(900);
        imageView.setFitHeight(650);

        StackPane root = new StackPane(imageView);
        root.setStyle("-fx-background-color: black; -fx-padding: 20;");

        Scene scene = new Scene(root, 950, 700);
        previewStage.setScene(scene);
        previewStage.setTitle("Photo Preview");
        previewStage.show();
    }

    @FXML
    public void viewRapport() {
        if (signalement == null) return;

        try {
            RapportSignalement rapport = rapportService.getBySignalementId(signalement.getId());

            if (rapport == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Report");
                alert.setHeaderText(null);
                alert.setContentText("No intervention report exists for this signalement yet.");
                alert.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signalement/show_rapport_signalement.fxml"));
            Parent root = loader.load();

            ShowRapportSignalementController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setRapport(rapport);

            Stage stage = (Stage) lblTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Intervention Report");
            stage.show();

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

            Stage stage = (Stage) lblTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reports");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}