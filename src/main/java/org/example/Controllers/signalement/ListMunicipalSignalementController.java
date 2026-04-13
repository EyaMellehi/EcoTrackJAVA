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
import org.example.Entities.Media;
import org.example.Entities.Signalement;
import org.example.Entities.User;
import org.example.Services.MediaService;
import org.example.Services.SignalementService;
import org.example.Services.UserService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListMunicipalSignalementController {

    @FXML private TextField tfSearch;
    @FXML private ComboBox<User> cbAgents;
    @FXML private FlowPane reportsContainer;
    @FXML private ComboBox<String> cbSort;
    @FXML private javafx.scene.layout.HBox navbarCitoyen;
    @FXML private javafx.scene.layout.HBox navbarMunicipal;

    @FXML private org.example.Controllers.components.NavbarCitoyenController navbarCitoyenController;
    @FXML private org.example.Controllers.components.NavbarMunicipalController navbarMunicipalController;

    private User user;
    private final SignalementService signalementService = new SignalementService();
    private final UserService userService = new UserService();
    private final MediaService mediaService = new MediaService();

    private User loggedUser;
    private ObservableList<Signalement> signalementList = FXCollections.observableArrayList();

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
        this.user = loggedUser;

        loadMunicipalSignalements();
        loadFieldAgents();
        configureNavbar();
    }
    private void configureNavbar() {
        if (navbarCitoyen == null || navbarMunicipal == null) {
            return;
        }

        if (loggedUser == null || loggedUser.getRoles() == null) {
            showCitoyenNavbar();
            return;
        }

        String roles = loggedUser.getRoles();

        if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
            showMunicipalNavbar();

            if (navbarMunicipalController != null) {
                navbarMunicipalController.setLoggedUser(loggedUser);
            }
        } else {
            showCitoyenNavbar();

            if (navbarCitoyenController != null) {
                navbarCitoyenController.setLoggedUser(loggedUser);
            }
        }
    }

    private void showCitoyenNavbar() {
        navbarCitoyen.setVisible(true);
        navbarCitoyen.setManaged(true);

        navbarMunicipal.setVisible(false);
        navbarMunicipal.setManaged(false);
    }

    private void showMunicipalNavbar() {
        navbarMunicipal.setVisible(true);
        navbarMunicipal.setManaged(true);

        navbarCitoyen.setVisible(false);
        navbarCitoyen.setManaged(false);
    }

    @FXML
    public void initialize() {
        if (cbSort != null) {
            cbSort.getItems().addAll(
                    "Newest first",
                    "Oldest first",
                    "Status"
            );
        }

        cbAgents.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " - " + item.getDelegation());
            }
        });

        cbAgents.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " - " + item.getDelegation());
            }
        });
    }

    @FXML
    public void loadMunicipalSignalements() {
        if (loggedUser == null || loggedUser.getDelegation() == null) return;

        try {
            List<Signalement> list = signalementService.getByDelegation(loggedUser.getDelegation());
            signalementList = FXCollections.observableArrayList(list);
            applyFiltersAndSort();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFieldAgents() {
        if (loggedUser == null || loggedUser.getDelegation() == null) return;

        try {
            List<User> agents = userService.getFieldAgentsByDelegation(loggedUser.getDelegation());
            cbAgents.setItems(FXCollections.observableArrayList(agents));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderCards(List<Signalement> list) {
        reportsContainer.getChildren().clear();

        if (list.isEmpty()) {
            Label empty = new Label("No signalements found for your delegation.");
            empty.setStyle("-fx-text-fill: #7a8087; -fx-font-size: 16px;");
            reportsContainer.getChildren().add(empty);
            return;
        }

        for (Signalement s : list) {
            VBox card = new VBox(10);
            card.setPrefWidth(340);
            card.setStyle("-fx-background-color: white; -fx-padding: 0 0 20 0; -fx-background-radius: 10; "
                    + "-fx-border-color: #eeeeee; -fx-border-radius: 10;");

            VBox content = new VBox(10);
            content.setPadding(new Insets(0, 20, 0, 20));

            try {
                Media firstMedia = mediaService.getFirstBySignalementId(s.getId());
                if (firstMedia != null) {
                    File file = new File(firstMedia.getUrl());
                    if (file.exists()) {
                        ImageView imageView = new ImageView(new Image(file.toURI().toString()));
                        imageView.setFitWidth(340);
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
            if (descText.length() > 80) descText = descText.substring(0, 80) + "...";
            Label desc = new Label(descText);
            desc.setWrapText(true);
            desc.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");

            Label assignedInfo = new Label(
                    s.getAgentAssigneId() != null ? "Already assigned" : "Not assigned"
            );
            assignedInfo.setStyle(s.getAgentAssigneId() != null
                    ? "-fx-text-fill: #d97706; -fx-font-size: 13px; -fx-font-weight: bold;"
                    : "-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: bold;");

            HBox actions = new HBox(8);

            Button showBtn = new Button("Show");
            showBtn.setStyle("-fx-background-color: white; -fx-border-color: #2563eb; -fx-text-fill: #2563eb;");
            showBtn.setOnAction(e -> openShowSignalement(s));

            Button assignBtn = new Button("Assign");
            assignBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
            assignBtn.setOnAction(e -> assignSignalement(s));

            boolean assignable = s.getAgentAssigneId() == null && !"TRAITE".equalsIgnoreCase(s.getStatut());
            assignBtn.setDisable(!assignable);

            actions.getChildren().addAll(showBtn, assignBtn);

            content.getChildren().addAll(top, addr, titre, desc, assignedInfo, actions);
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
        if (statut == null) return "-fx-background-color: #6b7280; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 14;";
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
                        result.sort(Comparator.comparing(Signalement::getDateCreation,
                                Comparator.nullsLast(Comparator.reverseOrder())));
                case "Oldest first" ->
                        result.sort(Comparator.comparing(Signalement::getDateCreation,
                                Comparator.nullsLast(Comparator.naturalOrder())));
                case "Status" ->
                        result.sort(Comparator.comparing(s -> s.getStatut() == null ? "" : s.getStatut()));
            }
        }

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

    private void assignSignalement(Signalement selected) {
        User selectedAgent = cbAgents.getValue();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a signalement.");
            return;
        }

        if (selectedAgent == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a field agent.");
            return;
        }

        if ("TRAITE".equalsIgnoreCase(selected.getStatut())) {
            showAlert(Alert.AlertType.WARNING, "Warning", "This signalement is already treated.");
            return;
        }

        if (selected.getAgentAssigneId() != null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "This signalement is already assigned to an agent.");
            return;
        }

        try {
            signalementService.assignAgent(selected.getId(), selectedAgent.getId());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Signalement assigned successfully.");
            loadMunicipalSignalements();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}