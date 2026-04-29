package org.example.Controllers.annonces;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Entities.Annonce;
import org.example.Entities.User;
import org.example.Services.AnnonceService;
import org.example.Services.CommentaireAnnonceService;
import org.example.Services.RecommendationService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecommendedAnnonceUserController {

    @FXML private FlowPane annonceCardsContainer;
    @FXML private Label lblTotalRecommendations;
    @FXML private HBox navbar;
    @FXML private NavbarCitoyenController navbarController;

    private final AnnonceService annonceService = new AnnonceService();
    private final CommentaireAnnonceService commentaireService = new CommentaireAnnonceService();
    private final RecommendationService recommendationService = new RecommendationService();
    private User loggedUser;

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        if (navbarController != null) {
            navbarController.setLoggedUser(user);
        }
        loadRecommendedAnnonces();
    }

    @FXML
    public void initialize() {
        // Le chargement démarre via setLoggedUser pour personnaliser correctement.
    }

    private void loadRecommendedAnnonces() {
        try {
            List<Annonce> all = annonceService.getAll();
            List<Annonce> recommended = recommendationService.rankForUser(all, loggedUser);
            renderAnnonces(recommended);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les recommandations: " + e.getMessage());
        }
    }

    private void renderAnnonces(List<Annonce> list) {
        annonceCardsContainer.getChildren().clear();
        lblTotalRecommendations.setText(String.valueOf(list.size()));

        if (list.isEmpty()) {
            Label noData = new Label("Aucune recommandation disponible pour le moment.");
            noData.setStyle("-fx-text-fill: #7a8087; -fx-font-size: 15px; -fx-padding: 24;");
            annonceCardsContainer.getChildren().add(noData);
            return;
        }

        for (Annonce annonce : list) {
            annonceCardsContainer.getChildren().add(createAnnonceItem(annonce));
        }
    }

    private VBox createAnnonceItem(Annonce annonce) {
        VBox item = new VBox(12);
        item.setPrefWidth(420);
        item.setMaxWidth(420);
        item.setStyle("-fx-padding: 16; -fx-border-color: #e5e7eb; -fx-border-radius: 18; " +
                "-fx-background-color: white; -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 18, 0.12, 0, 6);");

        StackPane mediaPane = new StackPane();
        mediaPane.setPrefHeight(220);
        mediaPane.setStyle("-fx-background-color: #edf7ee; -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #d1fae5;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(388);
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(true);

        if (annonce.getMediaPath() != null && !annonce.getMediaPath().isBlank()) {
            try {
                File imgFile = new File(annonce.getMediaPath());
                if (imgFile.exists()) {
                    imageView.setImage(new Image(imgFile.toURI().toString(), 0, 220, true, true));
                    mediaPane.getChildren().add(imageView);
                } else {
                    mediaPane.getChildren().add(createNoMediaLabel());
                }
            } catch (Exception ex) {
                mediaPane.getChildren().add(createNoMediaLabel());
            }
        } else {
            mediaPane.getChildren().add(createNoMediaLabel());
        }

        Label recommendedBadge = new Label("⭐ Recommandée pour vous");
        recommendedBadge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 999;");

        Label titleLabel = new Label(annonce.getTitre());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #104b2c;");
        titleLabel.setWrapText(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label metaLabel = new Label("Publiee le " + annonce.getDatePub().format(formatter) + " - " + annonce.getRegion());
        metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        HBox badges = new HBox(8);
        Label categoryBadge = new Label(annonce.getCategorie());
        categoryBadge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 999;");
        Label regionBadge = new Label(annonce.getRegion());
        regionBadge.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 999;");
        badges.getChildren().addAll(categoryBadge, regionBadge);

        String content = annonce.getContenu() != null ? annonce.getContenu() : "";
        String preview = content.length() > 180 ? content.substring(0, 180) + "..." : content;
        Label contentLabel = new Label(preview);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-line-spacing: 4;");
        contentLabel.setWrapText(true);

        int commentCount = 0;
        try {
            Integer viewerId = loggedUser != null ? loggedUser.getId() : null;
            commentCount = commentaireService.countTotalVisibleComments(annonce.getId(), viewerId);
        } catch (SQLException ignored) {
            commentCount = 0;
        }

        Label commentBadge = new Label(commentCount + " commentaire(s)");
        commentBadge.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #334155; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 999;");

        Button viewBtn = new Button("Voir details & commentaires");
        viewBtn.setStyle("-fx-background-color: linear-gradient(to right, #2e7d32, #43a047); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 10 18; -fx-font-size: 13px;");
        viewBtn.setOnAction(e -> {
            try {
                showAnnonceDetails(annonce);
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir l'annonce: " + ex.getMessage());
            }
        });

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().addAll(commentBadge, viewBtn);

        item.getChildren().addAll(mediaPane, recommendedBadge, badges, titleLabel, metaLabel, contentLabel, footer);
        return item;
    }

    private Label createNoMediaLabel() {
        Label label = new Label("Apercu de l'annonce");
        label.setStyle("-fx-text-fill: #166534; -fx-font-size: 16px; -fx-font-weight: bold;");
        return label;
    }

    private void showAnnonceDetails(Annonce annonce) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/annonces/show_annonce.fxml"));
        Parent root = loader.load();

        ShowAnnonceController controller = loader.getController();
        controller.setLoggedUser(loggedUser);
        controller.setAnnonce(annonce);

        Stage stage = new Stage();
        stage.setScene(new Scene(root, 1000, 800));
        stage.setTitle("Details de l'annonce");
        stage.showAndWait();

        // Recharger ensuite: l'utilisateur a peut-etre like/dislike depuis le detail.
        loadRecommendedAnnonces();
    }

    @FXML
    private void goBackToAllAnnonces() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/annonces/list_annonces_user.fxml"));
            Parent root = loader.load();

            ListAnnonceUserController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) annonceCardsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Annonces");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible de revenir aux annonces: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

