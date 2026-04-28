package org.example.Controllers.signalement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.Controllers.HomeConnectedController;
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Entities.Media;
import org.example.Entities.RapportSignalement;
import org.example.Entities.Signalement;
import org.example.Entities.User;
import org.example.Services.MediaService;
import org.example.Services.RapportSignalementService;
import org.example.Services.SignalementService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListAssignedSignalementController {

    @FXML private TextField tfSearch;
    @FXML private FlowPane reportsContainer;
    @FXML private ComboBox<String> cbSort;

    @FXML private HBox navbarCitoyen;
    @FXML private NavbarCitoyenController navbarCitoyenController;

    @FXML private Label lblTotalReports;
    @FXML private Label lblInProgress;
    @FXML private Label lblResolved;

    private final SignalementService signalementService = new SignalementService();
    private final RapportSignalementService rapportService = new RapportSignalementService();
    private final MediaService mediaService = new MediaService();

    private User loggedUser;
    private ObservableList<Signalement> signalementList = FXCollections.observableArrayList();

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;

        if (navbarCitoyenController != null) {
            navbarCitoyenController.setLoggedUser(loggedUser);
        }

        loadAssignedSignalements();
    }

    @FXML
    public void initialize() {
        cbSort.getItems().addAll(
                "Newest first",
                "Oldest first",
                "Status"
        );
    }

    @FXML
    public void loadAssignedSignalements() {
        if (loggedUser == null) return;

        try {
            List<Signalement> list = signalementService.getByAgentAssigneId(loggedUser.getId());
            signalementList = FXCollections.observableArrayList(list);
            applyFiltersAndSort();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStats(List<Signalement> list) {
        long total = list.size();
        long inProgress = list.stream()
                .filter(s -> "EN_COURS".equalsIgnoreCase(safe(s.getStatut())))
                .count();
        long resolved = list.stream()
                .filter(s -> "TRAITE".equalsIgnoreCase(safe(s.getStatut())))
                .count();

        if (lblTotalReports != null) lblTotalReports.setText(String.valueOf(total));
        if (lblInProgress != null) lblInProgress.setText(String.valueOf(inProgress));
        if (lblResolved != null) lblResolved.setText(String.valueOf(resolved));
    }

    private void renderCards(List<Signalement> list) {
        reportsContainer.getChildren().clear();

        if (list.isEmpty()) {
            Label empty = new Label("No assigned signalements.");
            empty.setStyle("-fx-text-fill: #7a8087; -fx-font-size: 16px;");
            reportsContainer.getChildren().add(empty);
            return;
        }

        for (Signalement s : list) {
            VBox card = new VBox(10);
            card.setPrefWidth(330);
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-padding: 0 0 20 0; " +
                            "-fx-background-radius: 10; " +
                            "-fx-border-color: #eeeeee; " +
                            "-fx-border-radius: 10;"
            );

            VBox content = new VBox(10);
            content.setPadding(new Insets(0, 20, 0, 20));

            try {
                Media firstMedia = mediaService.getFirstBySignalementId(s.getId());
                if (firstMedia != null) {
                    File file = new File(firstMedia.getUrl());
                    if (file.exists()) {
                        ImageView imageView = new ImageView(new Image(file.toURI().toString()));
                        imageView.setFitWidth(330);
                        imageView.setFitHeight(170);
                        imageView.setPreserveRatio(false);
                        card.getChildren().add(imageView);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            HBox top = new HBox();
            Label type = new Label(s.getType() != null ? s.getType() : "");
            type.setStyle("-fx-text-fill: #104b2c; -fx-font-size: 18px; -fx-font-weight: bold;");

            Label badge = new Label(formatStatus(s.getStatut()));
            badge.setStyle(statusStyle(s.getStatut()));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            top.getChildren().addAll(type, spacer, badge);

            Label addr = new Label(s.getAddresse() != null ? s.getAddresse() : "");
            addr.setWrapText(true);
            addr.setStyle("-fx-text-fill: #7a8087; -fx-font-size: 14px;");

            Label titre = new Label(s.getTitre() != null ? s.getTitre() : "");
            titre.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: bold;");

            String descText = s.getDescription() != null ? s.getDescription() : "";
            if (descText.length() > 80) {
                descText = descText.substring(0, 80) + "...";
            }
            Label desc = new Label(descText);
            desc.setWrapText(true);
            desc.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");

            HBox actions = new HBox(8);

            Button showBtn = new Button("Show");
            showBtn.setStyle("-fx-background-color: white; -fx-border-color: #2563eb; -fx-text-fill: #2563eb;");
            showBtn.setOnAction(e -> openShowSignalement(s));

            Button addRapportBtn = new Button("Add Report");
            addRapportBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
            addRapportBtn.setOnAction(e -> addRapport(s));

            Button viewRapportBtn = new Button("View Report");
            viewRapportBtn.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white;");
            viewRapportBtn.setOnAction(e -> viewRapport(s));

            boolean reportExists = false;
            try {
                reportExists = rapportService.getBySignalementId(s.getId()) != null;
            } catch (Exception ignored) {
            }

            addRapportBtn.setDisable(reportExists);
            viewRapportBtn.setDisable(!reportExists);

            actions.getChildren().addAll(showBtn, addRapportBtn, viewRapportBtn);

            content.getChildren().addAll(top, addr, titre, desc, actions);
            card.getChildren().add(content);
            reportsContainer.getChildren().add(card);
        }
    }

    private String formatStatus(String statut) {
        if (statut == null) return "";
        return switch (statut.toUpperCase()) {
            case "EN_ATTENTE", "PENDING" -> "Pending";
            case "EN_COURS", "IN_PROGRESS" -> "In progress";
            case "TRAITE", "RESOLVED" -> "Resolved";
            default -> statut;
        };
    }

    private String statusStyle(String statut) {
        if (statut == null) {
            return "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 14;";
        }

        return switch (statut.toUpperCase()) {
            case "EN_ATTENTE", "PENDING" ->
                    "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 14;";
            case "EN_COURS", "IN_PROGRESS" ->
                    "-fx-background-color: #facc15; -fx-text-fill: #111827; -fx-padding: 6 12; -fx-background-radius: 14;";
            case "TRAITE", "RESOLVED" ->
                    "-fx-background-color: #16a34a; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 14;";
            default ->
                    "-fx-background-color: #111827; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 14;";
        };
    }

    @FXML
    public void searchSignalements() {
        applyFiltersAndSort();
    }

    @FXML
    public void sortSignalements() {
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        List<Signalement> result = new ArrayList<>(signalementList);

        String keyword = tfSearch.getText() != null ? tfSearch.getText().trim().toLowerCase() : "";
        if (!keyword.isEmpty()) {
            result = result.stream()
                    .filter(s ->
                            (s.getTitre() != null && s.getTitre().toLowerCase().contains(keyword)) ||
                                    (s.getType() != null && s.getType().toLowerCase().contains(keyword)) ||
                                    (s.getStatut() != null && s.getStatut().toLowerCase().contains(keyword)) ||
                                    (s.getAddresse() != null && s.getAddresse().toLowerCase().contains(keyword)) ||
                                    (s.getDelegation() != null && s.getDelegation().toLowerCase().contains(keyword))
                    )
                    .collect(Collectors.toList());
        }

        String sort = cbSort.getValue();
        if (sort != null) {
            switch (sort) {
                case "Newest first" ->
                        result.sort(Comparator.comparing(
                                Signalement::getDateCreation,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        ));
                case "Oldest first" ->
                        result.sort(Comparator.comparing(
                                Signalement::getDateCreation,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ));
                case "Status" ->
                        result.sort(Comparator.comparing(s -> s.getStatut() == null ? "" : s.getStatut()));
            }
        }

        loadStats(result);
        renderCards(result);
    }

    private void openShowSignalement(Signalement selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signalement/show_signalement.fxml"));
            Parent root = loader.load();

            ShowSignalementController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setSignalement(selected);

            Stage stage = (Stage) reportsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Signalement Details");
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void addRapport(Signalement selected) {
        try {
            RapportSignalement existing = rapportService.getBySignalementId(selected.getId());
            if (existing != null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "This signalement already has a report.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signalement/add_rapport_signalement.fxml"));
            Parent root = loader.load();

            AddRapportSignalementController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setSignalement(selected);

            Stage stage = (Stage) reportsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Intervention Report");
            stage.show();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewRapport(Signalement selected) {
        try {
            RapportSignalement rapport = rapportService.getBySignalementId(selected.getId());

            if (rapport == null) {
                showAlert(Alert.AlertType.INFORMATION, "No Report", "No report exists for this signalement yet.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signalement/show_rapport_signalement.fxml"));
            Parent root = loader.load();

            ShowRapportSignalementController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setRapport(rapport);

            Stage stage = (Stage) reportsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Intervention Report");
            stage.show();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home_connected.fxml"));
            Parent root = loader.load();

            HomeConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) reportsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}