package org.example.Controllers.annonces;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarMunicipalController;
import org.example.Entities.Annonce;
import org.example.Entities.User;
import org.example.Services.AnnonceService;
import org.example.Services.CommentaireAnnonceService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ListAnnonceAdminController {

    @FXML private VBox annonceListContainer;
    @FXML private TextField tfSearchAnnonce;
    @FXML private ComboBox<String> cbCategorieFilter;
    @FXML private ComboBox<String> cbRegionFilter;
    @FXML private Label lblTotalAnnonces;
    @FXML private HBox navbar;
    @FXML private NavbarMunicipalController navbarController;

    private AnnonceService annonceService = new AnnonceService();
    private CommentaireAnnonceService commentaireService = new CommentaireAnnonceService();
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

        cbRegionFilter.getItems().addAll(
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Sfax", "Sousse", 
            "Kairouan", "Kasserine", "Sidi Bouzid", "Médenine", "Tataouine",
            "Gabès", "Gafsa", "Tozeur", "Kébili", "Jendouba", "Kef", "Siliana",
            "Nabeul", "Bizerte", "Zaghouan", "Monastir", "Mahdia", "Oued Slemane"
        );

        tfSearchAnnonce.setOnKeyReleased(e -> applyFiltersAndSort());
        cbCategorieFilter.setOnAction(e -> applyFiltersAndSort());
        cbRegionFilter.setOnAction(e -> applyFiltersAndSort());
    }

    @FXML
    public void addNewAnnonce() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/annonces/add_annonce.fxml"));
        Parent root = loader.load();

        AddAnnonceController controller = loader.getController();
        controller.setLoggedUser(loggedUser);

        Stage stage = new Stage();
        stage.setScene(new Scene(root, 900, 700));
        stage.setTitle("Ajouter une Annonce");
        stage.showAndWait();

        loadAnnonces();
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

            String region = cbRegionFilter.getValue();
            if (region != null && !region.isEmpty()) {
                filtered = filtered.stream()
                    .filter(a -> a.getRegion().equals(region))
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
            annonceList = FXCollections.observableArrayList(list);
            applyFiltersAndSort();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderAnnonces(List<Annonce> list) {
        annonceListContainer.getChildren().clear();
        lblTotalAnnonces.setText("Total: " + list.size());

        if (list.isEmpty()) {
            Label noData = new Label("Aucune annonce trouvée");
            noData.setStyle("-fx-text-fill: #7a8087; -fx-font-size: 14px;");
            annonceListContainer.getChildren().add(noData);
            return;
        }

        for (Annonce a : list) {
            HBox item = createAnnonceItem(a);
            annonceListContainer.getChildren().add(item);
        }
    }

    private HBox createAnnonceItem(Annonce a) {
        HBox item = new HBox(15);
        item.setStyle("-fx-padding: 15; -fx-border-color: #e5e7eb; -fx-border-radius: 8; " +
                      "-fx-background-color: white; -fx-background-radius: 8;");
        item.setPrefHeight(100);
        item.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(5);
        Label titleLabel = new Label(a.getTitre());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #104b2c;");

        int commentCount = 0;
        try {
            Integer viewerId = loggedUser != null ? loggedUser.getId() : null;
            commentCount = commentaireService.countTotalVisibleComments(a.getId(), viewerId);
        } catch (SQLException e) {
            commentCount = 0;
        }

        Label commentBadge = new Label(commentCount + " commentaire(s)");
        commentBadge.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 999;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label metaLabel = new Label(a.getCategorie() + " • " + a.getRegion() + " • " + a.getDatePub().format(formatter));
        metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7a8087;");

        content.getChildren().addAll(titleLabel, metaLabel, commentBadge);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPrefWidth(250);

        Button viewBtn = new Button("👁️ Voir");
        viewBtn.setStyle("-fx-background-color: linear-gradient(to right, #0066cc, #1d4ed8); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 8 15; -fx-font-size: 13px;");
        viewBtn.setOnAction(e -> {
            try {
                viewAnnonce(a);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Button editBtn = new Button("✏️ Modifier");
        editBtn.setStyle("-fx-background-color: linear-gradient(to right, #2e7d32, #43a047); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 8 15; -fx-font-size: 13px;");
        editBtn.setOnAction(e -> {
            try {
                editAnnonce(a);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: linear-gradient(to right, #dc3545, #ef4444); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 8 15; -fx-font-size: 13px;");
        deleteBtn.setOnAction(e -> deleteAnnonce(a));

        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);

        item.getChildren().addAll(content, actions);
        HBox.setHgrow(content, Priority.ALWAYS);

        return item;
    }

    private void viewAnnonce(Annonce a) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/annonces/show_annonce.fxml"));
        Parent root = loader.load();

        ShowAnnonceController controller = loader.getController();
        controller.setLoggedUser(loggedUser);
        controller.setAnnonce(a);

        Stage stage = new Stage();
        stage.setScene(new Scene(root, 1000, 800));
        stage.setTitle("Détails de l'Annonce");
        stage.showAndWait();
    }

    private void editAnnonce(Annonce a) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/annonces/edit_annonce.fxml"));
        Parent root = loader.load();

        EditAnnonceController controller = loader.getController();
        controller.setLoggedUser(loggedUser);
        controller.setAnnonce(a);

        Stage stage = new Stage();
        stage.setScene(new Scene(root, 900, 700));
        stage.setTitle("Modifier l'Annonce");
        stage.showAndWait();

        loadAnnonces();
    }

    private void deleteAnnonce(Annonce a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Êtes-vous sûr?");
        confirm.setContentText("Voulez-vous supprimer cette annonce?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                annonceService.delete(a.getId(), loggedUser.getId());
                loadAnnonces();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Annonce supprimée avec succès");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

