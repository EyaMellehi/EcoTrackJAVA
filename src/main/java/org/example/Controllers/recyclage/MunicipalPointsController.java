package org.example.Controllers.recyclage;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarMunicipalController;
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MunicipalPointsController {

    @FXML private NavbarMunicipalController navbarIncludeController;

    @FXML private Label lblDelegation;
    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblInProgress;
    @FXML private Label lblCollected;
    @FXML private Label lblAssigned;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<String> cbPriority;

    @FXML private TableView<PointRecyclage> tablePoints;
    @FXML private TableColumn<PointRecyclage, Integer> colId;
    @FXML private TableColumn<PointRecyclage, String> colCitizen;
    @FXML private TableColumn<PointRecyclage, String> colCategory;
    @FXML private TableColumn<PointRecyclage, String> colQuantity;
    @FXML private TableColumn<PointRecyclage, String> colAddress;
    @FXML private TableColumn<PointRecyclage, String> colDate;
    @FXML private TableColumn<PointRecyclage, String> colStatus;
    @FXML private TableColumn<PointRecyclage, String> colPriority;
    @FXML private TableColumn<PointRecyclage, String> colAssignment;
    @FXML private TableColumn<PointRecyclage, Void> colActions;

    private User loggedUser;

    private final PointRecyclageService pointService = new PointRecyclageService();

    private final ObservableList<PointRecyclage> masterList = FXCollections.observableArrayList();
    private final ObservableList<PointRecyclage> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "All statuses", "PENDING", "IN_PROGRESS", "COLLECTE", "VALIDE", "REFUSE"
        ));
        cbStatus.setValue("All statuses");

        cbPriority.setItems(FXCollections.observableArrayList(
                "All priorities", "LOW", "MEDIUM", "HIGH", "URGENT", "None"
        ));
        cbPriority.setValue("All priorities");

        initTable();

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbPriority.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
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

        loadPoints();
    }

    private void initTable() {
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));

        colCitizen.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCitoyen() != null ? safe(data.getValue().getCitoyen().getName()) : "-"
        ));

        colCategory.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCategorie() != null ? safe(data.getValue().getCategorie().getNom()) : "-"
        ));

        colQuantity.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getQuantite() + " kg"
        ));

        colAddress.setCellValueFactory(data -> new SimpleStringProperty(
                safe(data.getValue().getAddress())
        ));

        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDateDec() != null ? data.getValue().getDateDec().toString() : ""
        ));

        colStatus.setCellValueFactory(data -> new SimpleStringProperty(
                safe(data.getValue().getStatut())
        ));

        colPriority.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAiPriority() != null ? data.getValue().getAiPriority() : "None"
        ));

        colAssignment.setCellValueFactory(data -> {
            if (data.getValue().getAgentTerrain() != null) {
                return new SimpleStringProperty("Assigned: " + safe(data.getValue().getAgentTerrain().getName()));
            }
            return new SimpleStringProperty("Not assigned");
        });

        addActionsColumn();
        tablePoints.setItems(filteredList);
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnOpen = new Button("Open");
            private final Button btnRefuse = new Button("Refuse");
            private final HBox pane = new HBox(8, btnOpen, btnRefuse);

            {
                btnOpen.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;");
                btnRefuse.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");

                btnOpen.setOnAction(event -> {
                    PointRecyclage point = getTableView().getItems().get(getIndex());
                    handleOpen(point);
                });

                btnRefuse.setOnAction(event -> {
                    PointRecyclage point = getTableView().getItems().get(getIndex());
                    handleRefuse(point);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    PointRecyclage point = getTableView().getItems().get(getIndex());

                    boolean canRefuse = point != null &&
                            !"COLLECTE".equalsIgnoreCase(safe(point.getStatut())) &&
                            !"VALIDE".equalsIgnoreCase(safe(point.getStatut())) &&
                            !"REFUSE".equalsIgnoreCase(safe(point.getStatut()));

                    btnRefuse.setVisible(canRefuse);
                    btnRefuse.setManaged(canRefuse);

                    setGraphic(pane);
                }
            }
        });
    }

    private void loadPoints() {
        if (loggedUser == null) {
            return;
        }

        try {
            List<PointRecyclage> points = pointService.getPointsForMunicipal(loggedUser);
            masterList.setAll(points);
            applyFilters();
            updateStats();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les points.");
        }
    }

    private void applyFilters() {
        String search = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        String selectedStatus = cbStatus.getValue();
        String selectedPriority = cbPriority.getValue();

        List<PointRecyclage> result = new ArrayList<>();

        for (PointRecyclage p : masterList) {
            if (matchesSearch(p, search) && matchesStatus(p, selectedStatus) && matchesPriority(p, selectedPriority)) {
                result.add(p);
            }
        }

        filteredList.setAll(result);
        updateAssignedLabel();
    }

    private boolean matchesSearch(PointRecyclage p, String search) {
        if (search.isEmpty()) return true;

        String citizen = p.getCitoyen() != null ? safe(p.getCitoyen().getName()) : "";
        String category = p.getCategorie() != null ? safe(p.getCategorie().getNom()) : "";
        String address = safe(p.getAddress());
        String status = safe(p.getStatut());
        String priority = p.getAiPriority() != null ? p.getAiPriority() : "None";

        String all = (citizen + " " + category + " " + address + " " + status + " " + priority).toLowerCase();
        return all.contains(search);
    }

    private boolean matchesStatus(PointRecyclage p, String selectedStatus) {
        if (selectedStatus == null || selectedStatus.equals("All statuses")) return true;
        return safe(p.getStatut()).equalsIgnoreCase(selectedStatus);
    }

    private boolean matchesPriority(PointRecyclage p, String selectedPriority) {
        if (selectedPriority == null || selectedPriority.equals("All priorities")) return true;

        String priority = p.getAiPriority() != null ? p.getAiPriority() : "None";
        return priority.equalsIgnoreCase(selectedPriority);
    }

    private void updateStats() {
        int total = masterList.size();
        int pending = 0;
        int inProgress = 0;
        int collected = 0;

        for (PointRecyclage p : masterList) {
            String s = safe(p.getStatut()).toUpperCase();

            if (s.equals("PENDING") || s.equals("DECLARE") || s.equals("EN_ATTENTE")) {
                pending++;
            } else if (s.equals("IN_PROGRESS")) {
                inProgress++;
            } else if (s.equals("COLLECTE")) {
                collected++;
            }
        }

        lblTotal.setText(String.valueOf(total));
        lblPending.setText(String.valueOf(pending));
        lblInProgress.setText(String.valueOf(inProgress));
        lblCollected.setText(String.valueOf(collected));

        updateAssignedLabel();
    }

    private void updateAssignedLabel() {
        long assigned = filteredList.stream()
                .filter(p -> p.getAgentTerrain() != null)
                .count();

        lblAssigned.setText("Assigned: " + assigned + "/" + filteredList.size());
    }

    private void handleOpen(PointRecyclage point) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/municipal_point_details.fxml"));
            Parent root = loader.load();

            MunicipalPointDetailsController controller = loader.getController();
            controller.setData(loggedUser, point);

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Point details");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails du point.");
        }
    }

    private void handleRefuse(PointRecyclage point) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Refuser le point");
        confirm.setContentText("Voulez-vous vraiment refuser ce point ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                pointService.refusePointByMunicipal(point.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Point refusé avec succès.");
                loadPoints();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de refuser ce point.");
            }
        }
    }

    @FXML
    private void refreshTable() {
        loadPoints();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}