package org.example.Controllers.recyclage;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.Entities.PointRecyclage;
import org.example.Services.PointRecyclageService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PointsConnectedController {

    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblInProgress;
    @FXML private Label lblCollecte;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbStatus;

    @FXML private TableView<PointRecyclage> tablePoints;
    @FXML private TableColumn<PointRecyclage, Integer> colId;
    @FXML private TableColumn<PointRecyclage, String> colCategorie;
    @FXML private TableColumn<PointRecyclage, Double> colQuantite;
    @FXML private TableColumn<PointRecyclage, String> colAddress;
    @FXML private TableColumn<PointRecyclage, Object> colDate;
    @FXML private TableColumn<PointRecyclage, String> colStatut;
    @FXML private TableColumn<PointRecyclage, Void> colActions;

    private final PointRecyclageService pointService = new PointRecyclageService();
    private List<PointRecyclage> masterList;

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("", "PENDING", "IN_PROGRESS", "COLLECTE", "VALIDE"));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDec"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colCategorie.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategorie() != null
                        ? cellData.getValue().getCategorie().getNom()
                        : "-"));

        addActionsColumn();
        loadPoints();
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox pane = new HBox(8, btnVoir, btnModifier, btnSupprimer);

            {
                btnVoir.setStyle("-fx-background-color: white; -fx-border-color: #111827; -fx-text-fill: #111827; -fx-background-radius: 8; -fx-border-radius: 8;");
                btnModifier.setStyle("-fx-background-color: #2f9e44; -fx-text-fill: white; -fx-background-radius: 8;");
                btnSupprimer.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 8;");

                btnVoir.setOnAction(e -> {
                    PointRecyclage p = getTableView().getItems().get(getIndex());
                    openShow(p.getId());
                });

                btnModifier.setOnAction(e -> {
                    PointRecyclage p = getTableView().getItems().get(getIndex());
                    openEdit(p.getId());
                });

                btnSupprimer.setOnAction(e -> {
                    PointRecyclage p = getTableView().getItems().get(getIndex());
                    deletePoint(p.getId());
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
        try {
            masterList = pointService.getAllPoints();
            tablePoints.setItems(FXCollections.observableArrayList(masterList));
            updateStats(masterList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStats(List<PointRecyclage> points) {
        lblTotal.setText(String.valueOf(points.size()));
        lblPending.setText(String.valueOf(points.stream().filter(p -> "PENDING".equals(p.getStatut())).count()));
        lblInProgress.setText(String.valueOf(points.stream().filter(p -> "IN_PROGRESS".equals(p.getStatut())).count()));
        lblCollecte.setText(String.valueOf(points.stream().filter(p -> "COLLECTE".equals(p.getStatut())).count()));
    }

    @FXML
    void filterPoints() {
        String keyword = tfSearch.getText() == null ? "" : tfSearch.getText().toLowerCase().trim();
        String status = cbStatus.getValue();

        List<PointRecyclage> filtered = masterList.stream()
                .filter(p -> {
                    boolean matchesKeyword =
                            keyword.isEmpty()
                                    || (p.getAddress() != null && p.getAddress().toLowerCase().contains(keyword))
                                    || (p.getDescription() != null && p.getDescription().toLowerCase().contains(keyword))
                                    || (p.getStatut() != null && p.getStatut().toLowerCase().contains(keyword))
                                    || (p.getCategorie() != null && p.getCategorie().getNom() != null
                                    && p.getCategorie().getNom().toLowerCase().contains(keyword));

                    boolean matchesStatus =
                            status == null || status.isEmpty() || status.equals(p.getStatut());

                    return matchesKeyword && matchesStatus;
                })
                .collect(Collectors.toList());

        tablePoints.setItems(FXCollections.observableArrayList(filtered));
        updateStats(filtered);
    }

    @FXML
    void resetFilter() {
        tfSearch.clear();
        cbStatus.setValue("");
        tablePoints.setItems(FXCollections.observableArrayList(masterList));
        updateStats(masterList);
    }

    @FXML
    void goToAddPoint() {
        navigate("/recyclage/add_point_connected.fxml", "Créer un point");
    }

    @FXML
    void goToHomeConnected() {
        navigate("/Home_Connected.fxml", "Home");
    }

    private void openShow(int id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/show_point_connected.fxml"));
            Parent root = loader.load();
            ShowPointRecyclageController controller = loader.getController();
            controller.setPointId(id);

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du point");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEdit(int id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/edit_point_connected.fxml"));
            Parent root = loader.load();
            EditPointRecyclageController controller = loader.getController();
            controller.setPointId(id);

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier point");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deletePoint(int id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer ce point ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                pointService.deletePoint(id);
                loadPoints();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void navigate(String path, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}