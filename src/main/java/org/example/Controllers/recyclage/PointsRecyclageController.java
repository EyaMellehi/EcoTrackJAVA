package org.example.Controllers.recyclage;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.Entities.PointRecyclage;
import org.example.Services.PointRecyclageService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PointsRecyclageController {

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

    private final PointRecyclageService pointService = new PointRecyclageService();
    private ObservableList<PointRecyclage> masterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("", "PENDING", "IN_PROGRESS", "COLLECTE", "VALIDE"));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDec"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colCategorie.setCellValueFactory(cellData -> {
            String nom = cellData.getValue().getCategorie() != null ? cellData.getValue().getCategorie().getNom() : "-";
            return new SimpleStringProperty(nom);
        });

        loadPoints();
    }

    private void loadPoints() {
        try {
            List<PointRecyclage> points = pointService.getAllPoints();
            masterList.setAll(points);
            tablePoints.setItems(masterList);
            updateStats(masterList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les points.");
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
        tablePoints.setItems(masterList);
        updateStats(masterList);
    }

    @FXML
    void goToAddPoint() {
        navigate("/recyclage/add_point_connected.fxml", "Créer un point");
    }

    @FXML
    void showSelectedPoint() {
        PointRecyclage selected = tablePoints.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionne un point.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/show_point_connected.fxml"));
            Parent root = loader.load();

            ShowPointRecyclageController controller = loader.getController();
            controller.setPointId(selected.getId());

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du point");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void editSelectedPoint() {
        PointRecyclage selected = tablePoints.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionne un point.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/edit_point_connected.fxml"));
            Parent root = loader.load();

            EditPointRecyclageController controller = loader.getController();
            controller.setPointId(selected.getId());

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier point");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteSelectedPoint() {
        PointRecyclage selected = tablePoints.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionne un point à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer ce point ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                pointService.deletePoint(selected.getId());
                loadPoints();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer ce point.");
                e.printStackTrace();
            }
        }
    }

    private void navigate(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}