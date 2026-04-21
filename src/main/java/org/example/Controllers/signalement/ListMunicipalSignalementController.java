package org.example.Controllers.signalement;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarMunicipalController;
import org.example.Entities.RapportSignalement;
import org.example.Entities.Signalement;
import org.example.Entities.User;
import org.example.Services.RapportSignalementService;
import org.example.Services.SignalementService;
import org.example.Services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListMunicipalSignalementController {

    @FXML private NavbarMunicipalController navbarIncludeController;

    @FXML private Label lblDelegation;
    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblInProgress;
    @FXML private Label lblResolved;
    @FXML private Label lblAssigned;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatus;

    @FXML private TableView<Signalement> tableSignalements;
    @FXML private TableColumn<Signalement, String> colId;
    @FXML private TableColumn<Signalement, String> colCitizen;
    @FXML private TableColumn<Signalement, String> colType;
    @FXML private TableColumn<Signalement, String> colTitle;
    @FXML private TableColumn<Signalement, String> colAddress;
    @FXML private TableColumn<Signalement, String> colDate;
    @FXML private TableColumn<Signalement, String> colStatus;
    @FXML private TableColumn<Signalement, String> colAssignment;
    @FXML private TableColumn<Signalement, Void> colActions;

    private User loggedUser;

    private final SignalementService signalementService = new SignalementService();
    private final UserService userService = new UserService();
    private final RapportSignalementService rapportService = new RapportSignalementService();

    private final ObservableList<Signalement> masterList = FXCollections.observableArrayList();
    private final ObservableList<Signalement> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "All", "EN_ATTENTE", "EN_COURS", "TRAITE"
        ));
        cbStatus.setValue("All");

        initTable();

        tableSignalements.setPlaceholder(new Label("No signalement found."));
        tableSignalements.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user != null) {
            lblDelegation.setText("Delegation: " + safe(user.getDelegation()));
        } else {
            lblDelegation.setText("Delegation: -");
        }

        if (navbarIncludeController != null) {
            navbarIncludeController.setLoggedUser(user);
        }

        loadSignalements();
    }

    private void initTable() {
        colId.setCellValueFactory(data ->
                new SimpleStringProperty("#" + data.getValue().getId())
        );

        colCitizen.setCellValueFactory(data -> {
            try {
                Integer citoyenId = data.getValue().getCitoyenId();
                if (citoyenId != null) {
                    User citizen = userService.getUserById(citoyenId);
                    return new SimpleStringProperty(citizen != null ? safe(citizen.getName()) : "-");
                }
            } catch (Exception ignored) {
            }
            return new SimpleStringProperty("-");
        });

        colType.setCellValueFactory(data ->
                new SimpleStringProperty(safe(data.getValue().getType()))
        );

        colTitle.setCellValueFactory(data ->
                new SimpleStringProperty(safe(data.getValue().getTitre()))
        );

        colAddress.setCellValueFactory(data ->
                new SimpleStringProperty(safe(data.getValue().getAddresse()))
        );

        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDateCreation() != null
                                ? data.getValue().getDateCreation().toString()
                                : "-"
                )
        );

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(safe(data.getValue().getStatut()))
        );

        colAssignment.setCellValueFactory(data -> {
            try {
                Integer agentId = data.getValue().getAgentAssigneId();
                if (agentId != null) {
                    User agent = userService.getUserById(agentId);
                    return new SimpleStringProperty("Assigned: " + (agent != null ? safe(agent.getName()) : "Agent"));
                }
            } catch (Exception ignored) {
            }
            return new SimpleStringProperty("Not assigned");
        });

        styleStatusColumn();
        addActionsColumn();

        tableSignalements.setItems(filteredList);
    }

    private void styleStatusColumn() {
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null || statut.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(statut.toUpperCase());
                badge.setStyle(getStatusBadgeStyle(statut));
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnOpen = new Button("Open");
            private final Button btnDelete = new Button("Delete");
            private final HBox box = new HBox(8, btnOpen, btnDelete);

            {
                btnOpen.setStyle("-fx-background-color: #eef7ee; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                btnDelete.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");

                btnOpen.setOnAction(e -> {
                    Signalement signalement = getTableView().getItems().get(getIndex());
                    handleOpen(signalement);
                });

                btnDelete.setOnAction(e -> {
                    Signalement signalement = getTableView().getItems().get(getIndex());
                    handleDelete(signalement);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Signalement signalement = getTableView().getItems().get(getIndex());

                if (signalement == null) {
                    setGraphic(null);
                    return;
                }

                String statut = safe(signalement.getStatut()).toUpperCase();
                boolean canDelete = !statut.equals("TRAITE");

                btnDelete.setVisible(canDelete);
                btnDelete.setManaged(canDelete);

                setGraphic(box);
            }
        });
    }

    private void loadSignalements() {
        if (loggedUser == null) return;

        try {
            List<Signalement> signalements = signalementService.getByDelegation(loggedUser.getDelegation());
            masterList.setAll(signalements);
            applyFilters();
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load signalements.");
        }
    }

    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String selectedStatus = cbStatus.getValue();

        List<Signalement> result = new ArrayList<>();

        for (Signalement s : masterList) {
            if (matchesSearch(s, keyword) && matchesStatus(s, selectedStatus)) {
                result.add(s);
            }
        }

        filteredList.setAll(result);
        updateAssignedLabel();
    }

    private boolean matchesSearch(Signalement s, String search) {
        if (search.isEmpty()) return true;

        String titre = safe(s.getTitre());
        String type = safe(s.getType());
        String address = safe(s.getAddresse());
        String status = safe(s.getStatut());
        String delegation = safe(s.getDelegation());

        String citizen = "";
        try {
            if (s.getCitoyenId() != null) {
                User user = userService.getUserById(s.getCitoyenId());
                citizen = user != null ? safe(user.getName()) : "";
            }
        } catch (Exception ignored) {
        }

        String all = (titre + " " + type + " " + address + " " + status + " " + delegation + " " + citizen).toLowerCase();
        return all.contains(search);
    }

    private boolean matchesStatus(Signalement s, String selectedStatus) {
        return selectedStatus == null
                || selectedStatus.equals("All")
                || safe(s.getStatut()).equalsIgnoreCase(selectedStatus);
    }

    private void updateStats() {
        lblTotal.setText(String.valueOf(masterList.size()));
        lblPending.setText(String.valueOf(masterList.stream().filter(s -> "EN_ATTENTE".equalsIgnoreCase(safe(s.getStatut()))).count()));
        lblInProgress.setText(String.valueOf(masterList.stream().filter(s -> "EN_COURS".equalsIgnoreCase(safe(s.getStatut()))).count()));
        lblResolved.setText(String.valueOf(masterList.stream().filter(s -> "TRAITE".equalsIgnoreCase(safe(s.getStatut()))).count()));
        updateAssignedLabel();
    }

    private void updateAssignedLabel() {
        long assigned = filteredList.stream()
                .filter(s -> s.getAgentAssigneId() != null)
                .count();

        lblAssigned.setText("Assigned: " + assigned + "/" + filteredList.size());
    }

    private void handleOpen(Signalement signalement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/signalement/municipal_signalement_details.fxml"));
            Parent root = loader.load();

            MunicipalSignalementDetailsController controller = loader.getController();
            controller.setData(loggedUser, signalement);

            Stage stage = (Stage) tableSignalements.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Signalement Details");
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to open signalement details.");
        }
    }

    private void handleDelete(Signalement signalement) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Delete signalement");
        confirm.setContentText("Do you really want to delete this signalement?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                signalementService.delete(signalement.getId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Signalement deleted successfully.");
                loadSignalements();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to delete signalement.");
            }
        }
    }

    @FXML
    private void refreshTable() {
        loadSignalements();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String getStatusBadgeStyle(String statut) {
        String s = safe(statut).toUpperCase();

        String base = "-fx-padding: 6 12; "
                + "-fx-background-radius: 14; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 12px;";

        return switch (s) {
            case "EN_ATTENTE" -> base + "-fx-background-color: #facc15; -fx-text-fill: #111827;";
            case "EN_COURS" -> base + "-fx-background-color: #2563eb; -fx-text-fill: white;";
            case "TRAITE" -> base + "-fx-background-color: #16a34a; -fx-text-fill: white;";
            default -> base + "-fx-background-color: #9ca3af; -fx-text-fill: white;";
        };
    }
}