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
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PointsConnectedController {

    @FXML private HBox navbar;
    @FXML private NavbarCitoyenController navbarController;

    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblInProgress;
    @FXML private Label lblCollecte;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbStatus;

    @FXML private TableView<PointRecyclage> tablePoints;
    @FXML private TableColumn<PointRecyclage, Integer> colId;
    @FXML private TableColumn<PointRecyclage, String> colCategorie;
    @FXML private TableColumn<PointRecyclage, String> colQuantite;
    @FXML private TableColumn<PointRecyclage, String> colAddress;
    @FXML private TableColumn<PointRecyclage, String> colDate;
    @FXML private TableColumn<PointRecyclage, String> colStatut;
    @FXML private TableColumn<PointRecyclage, Void> colActions;

    private final PointRecyclageService pointService = new PointRecyclageService();

    private User loggedUser;
    private final ObservableList<PointRecyclage> masterList = FXCollections.observableArrayList();
    private final ObservableList<PointRecyclage> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "Tous", "PENDING", "IN_PROGRESS", "COLLECTE", "VALIDE"
        ));
        cbStatus.setValue("Tous");

        initTable();

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (navbarController != null) {
            navbarController.setLoggedUser(user);
        }

        loadPoints();
    }

    private void initTable() {
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));

        colCategorie.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCategorie() != null ? safe(data.getValue().getCategorie().getNom()) : "-"
        ));

        colQuantite.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getQuantite() + " kg"
        ));

        colAddress.setCellValueFactory(data -> new SimpleStringProperty(
                safe(data.getValue().getAddress())
        ));

        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDateDec() != null ? data.getValue().getDateDec().toString() : "-"
        ));

        colStatut.setCellValueFactory(data -> new SimpleStringProperty(
                safe(data.getValue().getStatut())
        ));

        addActionsColumn();
        tablePoints.setItems(filteredList);
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnShow = new Button("Voir");
            private final Button btnEdit = new Button("Modifier");
            private final HBox box = new HBox(8, btnShow, btnEdit);

            {
                btnShow.setOnAction(e -> {
                    PointRecyclage point = getTableView().getItems().get(getIndex());
                    goToShowPoint(point);
                });

                btnEdit.setOnAction(e -> {
                    PointRecyclage point = getTableView().getItems().get(getIndex());
                    goToEditPoint(point);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    PointRecyclage point = getTableView().getItems().get(getIndex());
                    boolean canEdit = point != null &&
                            !"COLLECTE".equalsIgnoreCase(safe(point.getStatut())) &&
                            !"VALIDE".equalsIgnoreCase(safe(point.getStatut()));

                    btnEdit.setVisible(canEdit);
                    btnEdit.setManaged(canEdit);

                    setGraphic(box);
                }
            }
        });
    }

    private void loadPoints() {
        if (loggedUser == null) {
            return;
        }

        try {
            List<PointRecyclage> points = pointService.getPointsByCitizen(loggedUser.getId());
            masterList.setAll(points);
            applyFilters();
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les points.");
        }
    }

    private void applyFilters() {
        String keyword = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        String selectedStatus = cbStatus.getValue();

        List<PointRecyclage> result = new ArrayList<>();

        for (PointRecyclage p : masterList) {
            boolean okSearch =
                    safe(p.getAddress()).toLowerCase().contains(keyword) ||
                            safe(p.getStatut()).toLowerCase().contains(keyword) ||
                            (p.getCategorie() != null && safe(p.getCategorie().getNom()).toLowerCase().contains(keyword));

            boolean okStatus = selectedStatus == null || selectedStatus.equals("Tous")
                    || safe(p.getStatut()).equalsIgnoreCase(selectedStatus);

            if (okSearch && okStatus) {
                result.add(p);
            }
        }

        filteredList.setAll(result);
    }

    private void updateStats() {
        lblTotal.setText(String.valueOf(masterList.size()));
        lblPending.setText(String.valueOf(masterList.stream().filter(p -> "PENDING".equalsIgnoreCase(safe(p.getStatut()))).count()));
        lblInProgress.setText(String.valueOf(masterList.stream().filter(p -> "IN_PROGRESS".equalsIgnoreCase(safe(p.getStatut()))).count()));
        lblCollecte.setText(String.valueOf(masterList.stream().filter(p -> "COLLECTE".equalsIgnoreCase(safe(p.getStatut()))).count()));
    }

    @FXML
    void filterPoints() {
        applyFilters();
    }

    @FXML
    void resetFilter() {
        tfSearch.clear();
        cbStatus.setValue("Tous");
        applyFilters();
    }

    @FXML
    void goToAddPoint() {
        if (loggedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/add_point_recyclage.fxml"));
            Parent root = loader.load();

            AddPointRecyclageController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer un point");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page d'ajout.");
        }
    }

    private void goToShowPoint(PointRecyclage point) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/show_point_recyclage.fxml"));
            Parent root = loader.load();

            ShowPointRecyclageController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setPoint(point);

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du point");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToEditPoint(PointRecyclage point) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/edit_point_recyclage.fxml"));
            Parent root = loader.load();

            EditPointRecyclageController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setPoint(point);

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier point");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}