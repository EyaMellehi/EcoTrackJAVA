package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Entities.Event;
import org.example.Entities.User;
import org.example.Services.EventService;
import org.example.Services.ParticipationService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventHistoryController {

    @FXML
    private Label lblInfo;
    @FXML
    private FlowPane historyFlow;
    @FXML
    private VBox emptyBox;

    private final ParticipationService participationService = new ParticipationService();
    private final EventService eventService = new EventService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private User loggedUser;

    @FXML
    public void initialize() {
        // Chargement fait dans setLoggedUser
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        loadHistory();
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/events.fxml"));
            Parent root = loader.load();

            EventsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) historyFlow.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Events");
            stage.show();
        } catch (IOException e) {
            showError("Impossible de revenir à la liste des événements.");
        }
    }

    @FXML
    private void goHome() {
        if (loggedUser == null) {
            goBack();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
            Parent root = loader.load();

            HomeConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) historyFlow.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (IOException e) {
            showError("Impossible d'ouvrir la page d'accueil.");
        }
    }

    private void loadHistory() {
        historyFlow.getChildren().clear();
        if (loggedUser == null) {
            lblInfo.setText("Connectez-vous pour voir votre historique.");
            emptyBox.setVisible(true);
            emptyBox.setManaged(true);
            return;
        }

        List<ParticipationService.ParticipationHistoryItem> history = participationService.getParticipationHistory(loggedUser.getId());
        if (!history.isEmpty()) {
            emptyBox.setVisible(false);
            emptyBox.setManaged(false);
            lblInfo.setText("Total: " + history.size() + " participation(s)");
            for (ParticipationService.ParticipationHistoryItem item : history) {
                historyFlow.getChildren().add(createHistoryCard(item));
            }
        } else {
            emptyBox.setVisible(true);
            emptyBox.setManaged(true);
            String lastError = participationService.getLastError();
            if (lastError != null && !lastError.isBlank()) {
                lblInfo.setText("Aucune participation affichée. Détail: " + lastError);
            } else {
                lblInfo.setText("Aucune participation trouvée.");
            }
        }
    }

    private VBox createHistoryCard(ParticipationService.ParticipationHistoryItem item) {
        VBox card = new VBox(10);
        card.setPrefWidth(350);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        HBox titleRow = new HBox();
        Label lblTitle = new Label(item.getTitre() == null ? "(Sans titre)" : item.getTitre());
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label badge = new Label(item.getParticipationStatus() == null ? "inscrit" : item.getParticipationStatus());
        String status = badge.getText().toLowerCase();
        if ("present".equals(status)) {
            badge.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 999;");
        } else if ("absent".equals(status)) {
            badge.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 999;");
        } else {
            badge.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 999;");
        }

        HBox.setHgrow(lblTitle, Priority.ALWAYS);
        titleRow.getChildren().addAll(lblTitle, badge);

        Label lblLieu = new Label("Lieu: " + (item.getLieu() == null ? "—" : item.getLieu()));
        lblLieu.setStyle("-fx-text-fill: #4b5563;");
        Label lblEventDate = new Label("Date événement: " + (item.getEventDate() == null ? "—" : item.getEventDate().format(formatter)));
        lblEventDate.setStyle("-fx-text-fill: #4b5563;");
        Label lblInscriptionDate = new Label("Inscrit le: " + (item.getParticipationDate() == null ? "—" : item.getParticipationDate().format(formatter)));
        lblInscriptionDate.setStyle("-fx-text-fill: #4b5563;");
        Label lblPoints = new Label("Points: " + item.getPointGain());
        lblPoints.setStyle("-fx-text-fill: #92400e; -fx-font-weight: bold;");

        Button btnDetails = new Button("Détails");
        btnDetails.setStyle("-fx-background-color: transparent; -fx-border-color: #2563eb; -fx-text-fill: #2563eb;");
        btnDetails.setOnAction(e -> openParticipationDetails(item.getEventId()));

        card.getChildren().addAll(titleRow, lblLieu, lblEventDate, lblInscriptionDate, lblPoints, btnDetails);
        return card;
    }

    private void openParticipationDetails(int eventId) {
        if (loggedUser == null) {
            showError("Session expirée.");
            return;
        }
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            showError("Evenement introuvable.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event_participation.fxml"));
            Parent root = loader.load();

            EventParticipationController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setEvent(event);

            Stage stage = (Stage) historyFlow.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Participer - " + event.getTitre());
            stage.show();
        } catch (IOException e) {
            showError("Impossible d'ouvrir les détails.");
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText("Erreur");
        alert.showAndWait();
    }
}

