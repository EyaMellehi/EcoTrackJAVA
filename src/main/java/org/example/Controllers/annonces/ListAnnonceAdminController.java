package org.example.Controllers.annonces;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import org.example.Services.SmsNotificationService;
import org.example.Services.UserService;

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
    private UserService userService = new UserService();
    private SmsNotificationService smsNotificationService = new SmsNotificationService();
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

        Button notifyBtn = new Button("📲 Informer les citoyens");
        notifyBtn.setStyle("-fx-background-color: linear-gradient(to right, #0ea5e9, #2563eb); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 8 15; -fx-font-size: 13px;");
        notifyBtn.setOnAction(e -> notifyCitizensForAnnonce(a, notifyBtn));

        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn, notifyBtn);

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
                annonceService.deleteById(a.getId());
                loadAnnonces();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Annonce supprimée avec succès");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    private void notifyCitizensForAnnonce(Annonce annonce, Button triggerButton) {
        if (annonce == null) {
            showAlert(Alert.AlertType.WARNING, "Annonce", "Annonce introuvable.");
            return;
        }

        String region = annonce.getRegion();
        if (region == null || region.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Région manquante", "Cette annonce n'a pas de région valide.");
            return;
        }

        List<User> recipients;
        try {
            recipients = userService.getCitoyensByRegionWithPhone(region);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les citoyens: " + e.getMessage());
            return;
        }

        if (recipients.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "SMS", "Aucun citoyen actif avec téléphone pour la région " + region + ".");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Informer les citoyens de " + region + " ?");
        confirm.setContentText("Cette action enverra des SMS à " + recipients.size() + " citoyen(s).");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        triggerButton.setDisable(true);
        triggerButton.setText("Envoi...");

        Task<SmsNotificationService.SmsDispatchReport> task = new Task<>() {
            @Override
            protected SmsNotificationService.SmsDispatchReport call() {
                return smsNotificationService.notifyCitizensForAnnonce(annonce, recipients);
            }
        };

        task.setOnSucceeded(event -> {
            SmsNotificationService.SmsDispatchReport report = task.getValue();
            triggerButton.setDisable(false);
            triggerButton.setText("📲 Informer les citoyens");

            StringBuilder message = new StringBuilder()
                    .append("SMS envoyés: ").append(report.sent).append("\n")
                    .append("Déjà envoyés (ignorés): ").append(report.skipped).append("\n")
                    .append("Échecs: ").append(report.failed);

            if (!report.errors.isEmpty()) {
                message.append("\n\nExemple d'erreur: ").append(report.errors.get(0));
            }

            showAlert(Alert.AlertType.INFORMATION, "Résultat envoi SMS", message.toString());
        });

        task.setOnFailed(event -> {
            triggerButton.setDisable(false);
            triggerButton.setText("📲 Informer les citoyens");
            Throwable ex = task.getException();
            showAlert(Alert.AlertType.ERROR, "Erreur SMS", ex != null ? ex.getMessage() : "Erreur d'envoi inconnue.");
        });

        Thread worker = new Thread(task, "annonce-sms-dispatch-task");
        worker.setDaemon(true);
        worker.start();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

