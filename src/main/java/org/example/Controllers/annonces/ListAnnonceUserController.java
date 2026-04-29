package org.example.Controllers.annonces;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.FlowPane;
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
import java.util.ArrayList;
import java.util.List;

public class ListAnnonceUserController {

    @FXML private FlowPane annonceCardsContainer;
    @FXML private TextField tfSearchAnnonce;
    @FXML private ComboBox<String> cbCategorieFilter;
    @FXML private Label lblTotalAnnonces;
    @FXML private Label lblRecommendationInfo;
    @FXML private HBox navbar;
    @FXML private NavbarCitoyenController navbarController;

    private AnnonceService annonceService = new AnnonceService();
    private CommentaireAnnonceService commentaireService = new CommentaireAnnonceService();
    private RecommendationService recommendationService = new RecommendationService();
    private User loggedUser;
    private ObservableList<Annonce> annonceList = FXCollections.observableArrayList();

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (navbarController != null) {
            navbarController.setLoggedUser(user);
        }

        loadAnnonces();
    }

    @FXML
    public void initialize() {
        cbCategorieFilter.getItems().addAll(
            "Agriculture",
            "Associations et collectifs citoyens",
            "Collectes de déchets",
            "Environnement"
        );

        tfSearchAnnonce.setOnKeyReleased(e -> applyFiltersAndSort());
        cbCategorieFilter.setOnAction(e -> applyFiltersAndSort());
    }

    @FXML
    private void goToRecommendedAnnonces() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/annonces/recommended_annonces_user.fxml"));
            Parent root = loader.load();

            RecommendedAnnonceUserController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) annonceCardsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Annonces recommandées");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir les recommandations: " + e.getMessage());
        }
    }

    private void applyFiltersAndSort() {
        try {
            List<Annonce> filtered = new ArrayList<>(annonceList);

            String keyword = tfSearchAnnonce.getText().trim();
            if (!keyword.isEmpty()) {
                filtered = filtered.stream()
                    .filter(a -> a.getTitre().toLowerCase().contains(keyword.toLowerCase()) ||
                               a.getContenu().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
            }

            String category = cbCategorieFilter.getValue();
            if (category != null && !category.isEmpty()) {
                filtered = filtered.stream()
                    .filter(a -> a.getCategorie().equals(category))
                    .toList();
            }

            renderAnnonces(filtered);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du filtrage: " + e.getMessage());
        }
    }

    @FXML
    private void loadAnnonces() {
        try {
            List<Annonce> list = annonceService.getAll();
            list = recommendationService.rankForUser(list, loggedUser);
            annonceList = FXCollections.observableArrayList(list);
            applyFiltersAndSort();

            if (lblRecommendationInfo != null) {
                boolean personalized = loggedUser != null && loggedUser.getId() > 0;
                lblRecommendationInfo.setText(personalized
                        ? "Tri personalise selon vos likes/dislikes et votre region."
                        : "Connectez-vous pour voir un tri personnalise.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderAnnonces(List<Annonce> list) {
        annonceCardsContainer.getChildren().clear();
        if (lblTotalAnnonces != null) {
            lblTotalAnnonces.setText(String.valueOf(list.size()));
        }

        if (list.isEmpty()) {
            Label noData = new Label("Aucune annonce trouvée");
            noData.setStyle("-fx-text-fill: #7a8087; -fx-font-size: 15px; -fx-padding: 24;");
            annonceCardsContainer.getChildren().add(noData);
            return;
        }

        for (Annonce a : list) {
            VBox item = createAnnonceItem(a);
            annonceCardsContainer.getChildren().add(item);
        }
    }

    private VBox createAnnonceItem(Annonce a) {
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

        if (a.getMediaPath() != null && !a.getMediaPath().isBlank()) {
            try {
                File imgFile = new File(a.getMediaPath());
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

        Label titleLabel = new Label(a.getTitre());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #104b2c;");
        titleLabel.setWrapText(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label metaLabel = new Label("Publié le " + a.getDatePub().format(formatter) + " • " + a.getRegion());
        metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        HBox badges = new HBox(8);
        Label categoryBadge = new Label(a.getCategorie());
        categoryBadge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 999;");
        Label regionBadge = new Label(a.getRegion());
        regionBadge.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 999;");
        badges.getChildren().addAll(categoryBadge, regionBadge);

        String content = a.getContenu() != null ? a.getContenu() : "";
        String preview = content.length() > 180 ? content.substring(0, 180) + "..." : content;
        Label contentLabel = new Label(preview);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-line-spacing: 4;");
        contentLabel.setWrapText(true);

        int commentCount = 0;
        try {
            Integer viewerId = loggedUser != null ? loggedUser.getId() : null;
            commentCount = commentaireService.countTotalVisibleComments(a.getId(), viewerId);
        } catch (SQLException e) {
            commentCount = 0;
        }

        Label commentBadge = new Label(commentCount + " commentaire(s)");
        commentBadge.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #334155; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 999;");

        Button viewBtn = new Button("👁️ Voir Détails & Commentaires");
        viewBtn.setStyle("-fx-background-color: linear-gradient(to right, #2e7d32, #43a047); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 10 18; -fx-font-size: 13px;");
        viewBtn.setOnAction(e -> {
            try {
                showAnnonceDetails(a);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().addAll(commentBadge, viewBtn);

        item.getChildren().addAll(mediaPane, badges, titleLabel, metaLabel, contentLabel, footer);
        return item;
    }

    private Label createNoMediaLabel() {
        Label label = new Label("Aperçu de l'annonce");
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
        stage.setTitle("Détails de l'Annonce");
        stage.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
