package org.example.Controllers.annonces;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import org.example.Entities.User;
import org.example.Services.AnnonceService;
import org.example.Services.CommentaireAnnonceService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AgentAnnoncesDashboardController {

    @FXML private HBox navbar;
    @FXML private NavbarMunicipalController navbarController;
    @FXML private Label lblAnnouncementsCount;
    @FXML private Label lblPendingCommentsCount;
    @FXML private Label lblApprovedCommentsCount;
    @FXML private VBox pendingCommentsContainer;
    @FXML private Label lblDashboardMessage;

    private final AnnonceService annonceService = new AnnonceService();
    private final CommentaireAnnonceService commentaireService = new CommentaireAnnonceService();
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

            List<CommentaireAnnonce> pending = commentaireService.getPending();
            renderPendingComments(pending);
            setDashboardMessage(pending.isEmpty()
                ? "Aucun commentaire en attente pour le moment."
                : pending.size() + " commentaire(s) en attente de modération.", true);
        } catch (SQLException e) {
            setDashboardMessage("Impossible de charger le dashboard: " + e.getMessage(), false);
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
        box.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #e5e7eb; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 16, 0.12, 0, 6);");

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

            Stage stage = (Stage) pendingCommentsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des annonces");
            stage.show();
        } catch (Exception e) {
            setDashboardMessage("Impossible d'ouvrir la gestion des annonces: " + e.getMessage(), false);
        }
    }

    private void setDashboardMessage(String message, boolean success) {
        if (lblDashboardMessage == null) {
            return;
        }

        lblDashboardMessage.setText(message);
        if (success) {
            lblDashboardMessage.setStyle("-fx-text-fill: #166534; -fx-background-color: #dcfce7; -fx-padding: 10 14; -fx-background-radius: 12; -fx-font-weight: bold;");
        } else {
            lblDashboardMessage.setStyle("-fx-text-fill: #b91c1c; -fx-background-color: #fee2e2; -fx-padding: 10 14; -fx-background-radius: 12; -fx-font-weight: bold;");
        }
    }
}


