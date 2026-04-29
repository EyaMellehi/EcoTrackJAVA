package org.example.Controllers.annonces;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.CommentaireAnnonce;
import org.example.Entities.SignalementCommentaire;
import org.example.Entities.User;
import org.example.Services.SignalementCommentaireService;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class SignalerCommentaireController {

    private static final List<String> RAISONS = Arrays.asList(
            "Harcèlement ou intimidation",
            "Contenu violent ou haineux",
            "Arnaque ou fausses informations",
            "Spam ou contenu indésirable",
            "Contenu inapproprié",
            "Autre"
    );

    @FXML private VBox reasonsContainer;
    @FXML private Label lblFeedback;

    private final SignalementCommentaireService signalementService = new SignalementCommentaireService();
    private CommentaireAnnonce commentaire;
    private User citoyen;
    private PauseTransition closeDelay;

    public void setCommentaire(CommentaireAnnonce c) {
        this.commentaire = c;
    }

    public void setCitoyen(User u) {
        this.citoyen = u;
    }

    @FXML
    public void initialize() {
        lblFeedback.setText("");
        reasonsContainer.getChildren().clear();
        for (String raison : RAISONS) {
            HBox row = buildReasonRow(raison);
            reasonsContainer.getChildren().add(row);
        }
    }

    private HBox buildReasonRow(String raison) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(Cursor.HAND);
        row.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10; -fx-padding: 14 16; -fx-border-color: #e5e7eb; -fx-border-radius: 10;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 10; -fx-padding: 14 16; -fx-border-color: #bbf7d0; -fx-border-radius: 10;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10; -fx-padding: 14 16; -fx-border-color: #e5e7eb; -fx-border-radius: 10;"));
        Label text = new Label(raison);
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #1f2937;");
        text.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label chevron = new Label(">");
        chevron.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        row.getChildren().addAll(text, chevron);
        row.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> onReasonChosen(raison));
        return row;
    }

    private void onReasonChosen(String raison) {
        if (commentaire == null || citoyen == null) {
            setFeedback("Session invalide. Fermez et réessayez.", FeedbackKind.ERROR);
            return;
        }

        if (closeDelay != null) {
            closeDelay.stop();
        }

        try {
            if (signalementService.dejaSignale(commentaire.getId(), citoyen.getId())) {
                setFeedback("Vous avez déjà signalé ce commentaire", FeedbackKind.WARN);
                scheduleClose();
                return;
            }

            SignalementCommentaire s = new SignalementCommentaire();
            s.setCommentaireId(commentaire.getId());
            s.setCitoyenId(citoyen.getId());
            s.setRaison(raison);
            s.setStatut("en_attente");
            signalementService.create(s);
            setFeedback("Merci, votre signalement a été envoyé.", FeedbackKind.SUCCESS);
            scheduleClose();
        } catch (SQLException ex) {
            setFeedback("Erreur: " + ex.getMessage(), FeedbackKind.ERROR);
        }
    }

    private enum FeedbackKind {
        SUCCESS,
        ERROR,
        WARN
    }

    private void setFeedback(String message, FeedbackKind kind) {
        lblFeedback.setText(message);
        if (message == null || message.isBlank()) {
            lblFeedback.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
            return;
        }
        switch (kind) {
            case SUCCESS -> lblFeedback.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #166534; -fx-background-color: #dcfce7; -fx-padding: 10 14; -fx-background-radius: 8;");
            case WARN -> lblFeedback.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #92400e; -fx-background-color: #fef3c7; -fx-padding: 10 14; -fx-background-radius: 8;");
            case ERROR -> lblFeedback.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #b91c1c; -fx-background-color: #fee2e2; -fx-padding: 10 14; -fx-background-radius: 8;");
        }
    }

    private void scheduleClose() {
        closeDelay = new PauseTransition(Duration.seconds(2));
        closeDelay.setOnFinished(e -> Platform.runLater(this::closeStage));
        closeDelay.play();
    }

    @FXML
    private void onClose() {
        if (closeDelay != null) {
            closeDelay.stop();
        }
        closeStage();
    }

    private void closeStage() {
        Stage st = (Stage) reasonsContainer.getScene().getWindow();
        if (st != null) {
            st.close();
        }
    }
}
