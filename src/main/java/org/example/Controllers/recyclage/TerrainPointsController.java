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
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TerrainPointsController {

    @FXML private Label lblAgentName;
    @FXML private Label lblTotal;
    @FXML private Label lblInProgress;
    @FXML private Label lblCollected;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatus;

    @FXML private TableView<PointRecyclage> tablePoints;
    @FXML private TableColumn<PointRecyclage, Integer> colId;
    @FXML private TableColumn<PointRecyclage, String> colCitizen;
    @FXML private TableColumn<PointRecyclage, String> colCategory;
    @FXML private TableColumn<PointRecyclage, String> colQuantity;
    @FXML private TableColumn<PointRecyclage, String> colAddress;
    @FXML private TableColumn<PointRecyclage, String> colDate;
    @FXML private TableColumn<PointRecyclage, String> colStatus;
    @FXML private TableColumn<PointRecyclage, Void> colActions;

    private User loggedUser;
    private final PointRecyclageService pointService = new PointRecyclageService();

    private final ObservableList<PointRecyclage> masterList = FXCollections.observableArrayList();
    private final ObservableList<PointRecyclage> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "All statuses", "IN_PROGRESS", "COLLECTE", "VALIDE"
        ));
        cbStatus.setValue("All statuses");

        initTable();

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user != null) {
            lblAgentName.setText("Field Agent: " + safe(user.getName()));
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

        addActionsColumn();

        tablePoints.setItems(filteredList);
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnOpen = new Button("Open");
            private final HBox pane = new HBox(8, btnOpen);

            {
                btnOpen.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;");

                btnOpen.setOnAction(event -> {
                    PointRecyclage point = getTableView().getItems().get(getIndex());
                    handleOpen(point);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadPoints() {
        if (loggedUser == null) return;

        try {
            List<PointRecyclage> points = pointService.getPointsForFieldAgent(loggedUser);
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

        List<PointRecyclage> result = new ArrayList<>();

        for (PointRecyclage p : masterList) {
            boolean matchesSearch = matchesSearch(p, search);
            boolean matchesStatus = matchesStatus(p, selectedStatus);

            if (matchesSearch && matchesStatus) {
                result.add(p);
            }
        }

        filteredList.setAll(result);
    }

    private boolean matchesSearch(PointRecyclage p, String search) {
        if (search.isEmpty()) return true;

        String citizen = p.getCitoyen() != null ? safe(p.getCitoyen().getName()) : "";
        String category = p.getCategorie() != null ? safe(p.getCategorie().getNom()) : "";
        String address = safe(p.getAddress());
        String status = safe(p.getStatut());

        String all = (citizen + " " + category + " " + address + " " + status).toLowerCase();
        return all.contains(search);
    }

    private boolean matchesStatus(PointRecyclage p, String selectedStatus) {
        if (selectedStatus == null || selectedStatus.equals("All statuses")) return true;
        return safe(p.getStatut()).equalsIgnoreCase(selectedStatus);
    }

    private void updateStats() {
        int total = masterList.size();
        int inProgress = 0;
        int collected = 0;

        for (PointRecyclage p : masterList) {
            String s = safe(p.getStatut()).toUpperCase();
            if (s.equals("IN_PROGRESS")) inProgress++;
            if (s.equals("COLLECTE")) collected++;
        }

        lblTotal.setText(String.valueOf(total));
        lblInProgress.setText(String.valueOf(inProgress));
        lblCollected.setText(String.valueOf(collected));
    }

    private void handleOpen(PointRecyclage point) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/terrain_point_details.fxml"));
            Parent root = loader.load();

            TerrainPointDetailsController controller = loader.getController();
            controller.setData(loggedUser, point);

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Point details");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails du point.");
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