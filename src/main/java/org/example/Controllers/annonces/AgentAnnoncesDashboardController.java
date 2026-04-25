package org.example.Controllers.annonces;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarMunicipalController;
import org.example.Entities.CommentaireAnnonce;
import org.example.Entities.SignalementCommentaire;
import org.example.Entities.User;
import org.example.Services.AnnonceService;
import org.example.Services.CommentaireAnnonceService;
import org.example.Services.SignalementCommentaireService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AgentAnnoncesDashboardController {

    private static final String CARD_SHADOW =
            "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 16, 0.12, 0, 6);";

    @FXML private HBox navbar;
    @FXML private NavbarMunicipalController navbarController;
    @FXML private Label lblAnnouncementsCount;
    @FXML private Label lblPendingCommentsCount;
    @FXML private Label lblApprovedCommentsCount;
    @FXML private Label lblPendingSignalementsCount;
    @FXML private VBox pendingCommentsContainer;
    @FXML private VBox signalementsListContainer;
    @FXML private Label lblDashboardMessage;
    @FXML private Label lblSignalementsMessage;

    private final AnnonceService annonceService = new AnnonceService();
    private final CommentaireAnnonceService commentaireService = new CommentaireAnnonceService();
    private final SignalementCommentaireService signalementService = new SignalementCommentaireService();
    private User loggedUser;

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        if (navbarController != null) {
            navbarController.setLoggedUser(user);
        }
        refreshDashboard();
    }

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    @FXML
    private void refreshDashboard() {
        try {
            lblAnnouncementsCount.setText(String.valueOf(annonceService.getAll().size()));
            lblPendingCommentsCount.setText(String.valueOf(commentaireService.countPendingComments()));
            lblApprovedCommentsCount.setText(String.valueOf(commentaireService.countApprovedComments()));
            lblPendingSignalementsCount.setText(String.valueOf(signalementService.countEnAttente()));

            List<CommentaireAnnonce> pending = commentaireService.getPending();
            renderPendingComments(pending);
            setDashboardMessage(pending.isEmpty()
                ? "Aucun commentaire en attente pour le moment."
                : pending.size() + " commentaire(s) en attente de modération.", true);

            List<SignalementCommentaire> signalements = signalementService.getEnAttente();
            renderSignalements(signalements);
            setSignalementsMessage(signalements.isEmpty()
                ? "Aucun signalement en attente pour le moment."
                : signalements.size() + " signalement(s) à traiter.", true);
        } catch (SQLException e) {
            setDashboardMessage("Impossible de charger le dashboard: " + e.getMessage(), false);
            setSignalementsMessage("Chargement impossible.", false);
        }
    }

    private void renderPendingComments(List<CommentaireAnnonce> comments) {
        pendingCommentsContainer.getChildren().clear();

        if (comments.isEmpty()) {
            Label empty = new Label("Aucun commentaire à modérer.");
            empty.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-padding: 10 0;");
            pendingCommentsContainer.getChildren().add(empty);
            return;
        }

        for (CommentaireAnnonce comment : comments) {
            pendingCommentsContainer.getChildren().add(createPendingCommentCard(comment));
        }
    }

    private VBox createPendingCommentCard(CommentaireAnnonce comment) {
        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #e5e7eb; " + CARD_SHADOW);

        HBox header = new HBox(10);
        Label author = new Label(comment.getAuteurNom() != null ? comment.getAuteurNom() : "Anonyme");
        author.setStyle("-fx-font-weight: bold; -fx-text-fill: #104b2c; -fx-font-size: 14px;");

        Label badge = new Label("EN ATTENTE");
        badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 999;");

        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label date = new Label(comment.getDateComm() != null ? comment.getDateComm().format(formatter) : "");
        date.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

        header.getChildren().addAll(author, badge, spacer, date);

        Label context = new Label("Annonce #" + comment.getAnnonceId() + " • Commentaire #" + comment.getId());
        context.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

        Label description = new Label(comment.getDescription() != null ? comment.getDescription() : "");
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");

        HBox actions = new HBox(10);
        Button approveBtn = new Button("Approuver");
        approveBtn.setStyle("-fx-background-color: linear-gradient(to right, #16a34a, #22c55e); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 8 16;");
        approveBtn.setOnAction(e -> approveComment(comment));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: linear-gradient(to right, #dc2626, #ef4444); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 8 16;");
        deleteBtn.setOnAction(e -> deleteComment(comment));

        actions.getChildren().addAll(approveBtn, deleteBtn);

        box.getChildren().addAll(header, context, description, actions);
        return box;
    }

    private void renderSignalements(List<SignalementCommentaire> signalements) {
        signalementsListContainer.getChildren().clear();

        if (signalements.isEmpty()) {
            Label empty = new Label("Aucun signalement à afficher pour le moment.");
            empty.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-padding: 14 4 8 4;");
            signalementsListContainer.getChildren().add(empty);
            return;
        }

        for (SignalementCommentaire s : signalements) {
            signalementsListContainer.getChildren().add(createSignalementCard(s));
        }
    }

    private static String truncatePlain(String text, int max) {
        if (text == null) {
            return "";
        }
        String t = text.replace("\n", " ").trim();
        if (t.length() <= max) {
            return t;
        }
        return t.substring(0, max) + "…";
    }

    private VBox createSignalementCard(SignalementCommentaire s) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #fafafa; -fx-padding: 18; -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #e5e7eb; " + CARD_SHADOW);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Commentaire n° " + s.getCommentaireId());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #104b2c; -fx-font-size: 15px;");

        Label badge = new Label("SIGNALÉ");
        badge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 999;");

        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dateStr = s.getDateSignalement() != null ? s.getDateSignalement().format(formatter) : "";
        Label date = new Label("Signalé le " + dateStr);
        date.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        header.getChildren().addAll(title, badge, spacer, date);

        Label meta = new Label("Signalement n° " + s.getId() + " • Citoyen (id " + s.getCitoyenId() + ")");
        meta.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

        Label raisonCaption = new Label("Raison du signalement");
        raisonCaption.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label raison = new Label(s.getRaison() != null ? s.getRaison() : "—");
        raison.setWrapText(true);
        raison.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1d4ed8;");

        Label excerptCaption = new Label("Extrait du commentaire signalé");
        excerptCaption.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label excerpt = new Label(truncatePlain(s.getCommentaireDescription(), 220));
        excerpt.setWrapText(true);
        excerpt.setMaxWidth(Double.MAX_VALUE);
        excerpt.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px; -fx-line-spacing: 2px;");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setStyle("-fx-padding: 6 0 0 0;");

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: linear-gradient(to right, #dc2626, #ef4444); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 10 18; -fx-font-size: 13px;");
        deleteBtn.setOnAction(e -> deleteCommentForSignalement(s));

        Button rejectBtn = new Button("✅ Rejeter");
        rejectBtn.setStyle("-fx-background-color: linear-gradient(to right, #1d4ed8, #2563eb); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 10 18; -fx-font-size: 13px;");
        rejectBtn.setOnAction(e -> rejeterSignalement(s));

        actions.getChildren().addAll(deleteBtn, rejectBtn);

        box.getChildren().addAll(header, meta, raisonCaption, raison, excerptCaption, excerpt, actions);
        return box;
    }

    private void deleteCommentForSignalement(SignalementCommentaire s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce commentaire ?");
        confirm.setContentText("Le commentaire sera supprimé et tous les signalements associés disparaîtront (cascade).");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            commentaireService.deleteByIdForModeration(s.getCommentaireId());
            refreshDashboard();
            setSignalementsMessage("Commentaire supprimé. Liste actualisée.", true);
        } catch (SQLException e) {
            setSignalementsMessage("Impossible de supprimer : " + e.getMessage(), false);
        }
    }

    private void rejeterSignalement(SignalementCommentaire s) {
        try {
            signalementService.updateStatut(s.getId(), "rejete");
            refreshDashboard();
            setSignalementsMessage("Signalement rejeté. Liste actualisée.", true);
        } catch (SQLException e) {
            setSignalementsMessage("Impossible de rejeter : " + e.getMessage(), false);
        }
    }

    private void approveComment(CommentaireAnnonce comment) {
        try {
            commentaireService.approve(comment.getId());
            refreshDashboard();
            setDashboardMessage("Commentaire approuvé avec succès.", true);
        } catch (SQLException e) {
            setDashboardMessage("Impossible d'approuver le commentaire: " + e.getMessage(), false);
        }
    }

    private void deleteComment(CommentaireAnnonce comment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce commentaire ?");
        confirm.setContentText("Cette action est irréversible.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            commentaireService.delete(comment.getId(), comment.getAuteurId());
            refreshDashboard();
            setDashboardMessage("Commentaire supprimé.", true);
        } catch (SQLException e) {
            setDashboardMessage("Impossible de supprimer le commentaire: " + e.getMessage(), false);
        }
    }

    @FXML
    private void openAnnouncementsManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/annonces/list_annonces_admin.fxml"));
            Parent root = loader.load();

            ListAnnonceAdminController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) (pendingCommentsContainer.getScene() != null
                    ? pendingCommentsContainer.getScene().getWindow()
                    : signalementsListContainer.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des annonces");
            stage.show();
        } catch (Exception e) {
            setDashboardMessage("Impossible d'ouvrir la gestion des annonces: " + e.getMessage(), false);
        }
    }

    private void setDashboardMessage(String message, boolean success) {
        applyStatusPill(lblDashboardMessage, message, success);
    }

    private void setSignalementsMessage(String message, boolean success) {
        applyStatusPill(lblSignalementsMessage, message, success);
    }

    private static void applyStatusPill(Label label, String message, boolean success) {
        if (label == null) {
            return;
        }
        label.setText(message);
        if (success) {
            label.setStyle("-fx-text-fill: #166534; -fx-background-color: #dcfce7; -fx-padding: 10 14; -fx-background-radius: 12; -fx-font-weight: bold;");
        } else {
            label.setStyle("-fx-text-fill: #b91c1c; -fx-background-color: #fee2e2; -fx-padding: 10 14; -fx-background-radius: 12; -fx-font-weight: bold;");
        }
    }
}


