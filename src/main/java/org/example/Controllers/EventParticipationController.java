package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.Event;
import org.example.Entities.User;
import org.example.Services.EmailService;
import org.example.Services.ParticipationService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class EventParticipationController {

    @FXML
    private Label lblEventTitle;
    @FXML
    private Label lblEventLieu;
    @FXML
    private Label lblEventDateDeb;
    @FXML
    private Label lblEventDateFin;
    @FXML
    private Label lblEventPoints;
    @FXML
    private Label lblUserName;
    @FXML
    private Label lblAlert;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnBackList;

    private final ParticipationService participationService = new ParticipationService();
    private final EmailService emailService = new EmailService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private User currentUser;
    private Event event;

    public void setLoggedUser(User user) {
        this.currentUser = user;
        refreshUserLabel();
    }

    public void setEvent(Event event) {
        this.event = event;
        refreshEventInfo();
    }

    @FXML
    public void initialize() {
        if (btnHome != null) {
            btnHome.setOnAction(e -> goHome());
        }
        if (btnCancel != null) {
            btnCancel.setOnAction(e -> goBack());
        }
        if (btnBackList != null) {
            btnBackList.setOnAction(e -> goBack());
        }
        if (btnConfirm != null) {
            btnConfirm.setOnAction(e -> confirmParticipation());
        }
        if (lblAlert != null) {
            lblAlert.getStyleClass().add("event-alert");
            lblAlert.setVisible(false);
            lblAlert.setManaged(false);
        }
    }

    private void refreshUserLabel() {
        if (lblUserName != null) {
            lblUserName.setText(currentUser != null ? currentUser.getName() : "Utilisateur inconnu");
        }
    }

    private void refreshEventInfo() {
        if (event == null) {
            return;
        }
        lblEventTitle.setText(event.getTitre());
        lblEventLieu.setText(event.getLieu());
        lblEventDateDeb.setText(event.getDateDeb() != null ? event.getDateDeb().format(formatter) : "—");
        lblEventDateFin.setText(event.getDateFin() != null ? event.getDateFin().format(formatter) : "—");
        lblEventPoints.setText(event.getPointGain() > 0 ? event.getPointGain() + " points" : "0 point");

        if (currentUser != null && participationService.hasParticipation(currentUser.getId(), event.getId())) {
            showAlert("Vous êtes déjà inscrit à cet événement.", true);
            if (btnConfirm != null) {
                btnConfirm.setDisable(true);
            }
        }
    }

    @FXML
    private void confirmParticipation() {
        if (currentUser == null || event == null) {
            showAlert("Données manquantes pour confirmer l'inscription.", true);
            return;
        }

        if (!askUserConfirmation()) {
            showAlert("Inscription annulée.", false);
            return;
        }

        boolean ok = participationService.registerParticipation(currentUser, event);
        if (ok) {
            sendCheckinEmail();
            showAlert("Inscription enregistree. Les points seront attribues apres validation de presence.", false);
            showSuccessDialog();
            btnConfirm.setDisable(true);
            btnConfirm.setText("Inscrit");
        } else {
            showAlert(participationService.getLastError() != null ? participationService.getLastError() : "Inscription impossible.", true);
        }
    }

    private boolean askUserConfirmation() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Confirmer votre inscription");
        confirm.setContentText("Voulez-vous participer a l'evenement : " + event.getTitre() + " ?");

        ButtonType yesButton = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    private void showSuccessDialog() {
        Alert success = new Alert(Alert.AlertType.INFORMATION, "Inscription enregistree avec succes.", ButtonType.OK);
        success.setTitle("Inscription validee");
        success.setHeaderText(null);
        success.showAndWait();
    }

    private void sendCheckinEmail() {
        if (currentUser == null || currentUser.getEmail() == null || currentUser.getEmail().isBlank() || event == null) {
            return;
        }

        try {
            String qrPayload = participationService.buildAttendanceQrPayload(event, currentUser.getId());
            emailService.sendEventCheckinQr(
                    currentUser.getEmail(),
                    currentUser.getName(),
                    event.getTitre(),
                    qrPayload
            );
        } catch (Exception ignored) {
            // L'inscription reste valide meme si l'email n'a pas pu etre envoye.
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/events.fxml"));
            Parent root = loader.load();

            EventsController controller = loader.getController();
            controller.setLoggedUser(currentUser);

            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Events");
            stage.show();
        } catch (IOException e) {
            showAlert("Impossible de revenir à la liste des événements.", true);
        }
    }

    @FXML
    private void goHome() {
        if (currentUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
                Parent root = loader.load();

                HomeConnectedController controller = loader.getController();
                controller.setLoggedUser(currentUser);

                Stage stage = (Stage) btnHome.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("EcoTrack - Home");
                stage.show();
            } catch (IOException e) {
                showAlert("Impossible d'ouvrir la page d'accueil.", true);
            }
        } else {
            goBack();
        }
    }

    private void showAlert(String message, boolean error) {
        lblAlert.setText(message);
        lblAlert.setVisible(true);
        lblAlert.setManaged(true);
        lblAlert.getStyleClass().removeAll("event-alert-error", "event-alert-success");
        lblAlert.getStyleClass().add(error ? "event-alert-error" : "event-alert-success");
    }
}

