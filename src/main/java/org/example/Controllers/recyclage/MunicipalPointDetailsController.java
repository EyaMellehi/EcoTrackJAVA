package org.example.Controllers.recyclage;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarMunicipalController;
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;
import org.example.Services.UserService;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MunicipalPointDetailsController {

    @FXML private NavbarMunicipalController navbarIncludeController;

    @FXML private Label lblDelegationTop;
    @FXML private Label lblPointIdTop;

    @FXML private Label lblPointTitle;
    @FXML private Label lblPointAddress;

    @FXML private Label lblCitizen;
    @FXML private Label lblCategory;
    @FXML private Label lblQuantity;
    @FXML private Label lblReportedOn;
    @FXML private Label lblAddress;
    @FXML private Label lblLatitude;
    @FXML private Label lblLongitude;
    @FXML private Label lblAiPriority;
    @FXML private Label lblFieldAgent;
    @FXML private Label lblStatusBadge;

    @FXML private Label lblSummaryId;
    @FXML private Label lblSummaryDelegation;
    @FXML private Label lblSummaryStatus;
    @FXML private Label lblSummaryReport;

    @FXML private RadioButton rbManual;
    @FXML private RadioButton rbAuto;
    @FXML private TextField txtAgentSearch;
    @FXML private ComboBox<User> cbAgents;
    @FXML private Label lblAutoInfo;
    @FXML private Button btnConfirmAssign;

    private final PointRecyclageService pointService = new PointRecyclageService();
    private final UserService userService = new UserService();

    private User loggedUser;
    private PointRecyclage currentPoint;

    public void setData(User loggedUser, PointRecyclage point) {
        this.loggedUser = loggedUser;
        this.currentPoint = point;

        if (navbarIncludeController != null) {
            navbarIncludeController.setLoggedUser(loggedUser);
        }

        loadPointFresh();
        loadAgents();
    }

    private void loadPointFresh() {
        try {
            if (currentPoint != null) {
                currentPoint = pointService.getPointById(currentPoint.getId());
            }
            fillData();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les détails du point.");
        }
    }

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        rbManual.setToggleGroup(group);
        rbAuto.setToggleGroup(group);
        rbManual.setSelected(true);

        cbAgents.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(User user) {
                if (user == null) return "";
                return user.getName() + " (" + user.getEmail() + ")";
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });

        txtAgentSearch.textProperty().addListener((obs, oldVal, newVal) -> filterAgents());
        rbManual.selectedProperty().addListener((obs, oldVal, newVal) -> refreshAssignMode());
        rbAuto.selectedProperty().addListener((obs, oldVal, newVal) -> refreshAssignMode());
    }

    private void fillData() {
        if (currentPoint == null) return;

        String delegation = loggedUser != null ? safe(loggedUser.getDelegation()) : "-";

        lblDelegationTop.setText("Delegation: " + delegation);
        lblPointIdTop.setText("Point: #" + currentPoint.getId());

        lblPointTitle.setText("Point #" + currentPoint.getId());
        lblPointAddress.setText(safe(currentPoint.getAddress()));

        lblCitizen.setText(currentPoint.getCitoyen() != null ? safe(currentPoint.getCitoyen().getName()) : "-");
        lblCategory.setText(currentPoint.getCategorie() != null ? safe(currentPoint.getCategorie().getNom()) : "-");
        lblQuantity.setText(currentPoint.getQuantite() + " kg");
        lblReportedOn.setText(currentPoint.getDateDec() != null ? currentPoint.getDateDec().toString() : "-");
        lblAddress.setText(safe(currentPoint.getAddress()));
        lblLatitude.setText(String.valueOf(currentPoint.getLatitude()));
        lblLongitude.setText(String.valueOf(currentPoint.getLongitude()));
        lblAiPriority.setText(currentPoint.getAiPriority() != null ? currentPoint.getAiPriority() : "Not estimated");

        if (currentPoint.getAgentTerrain() != null) {
            lblFieldAgent.setText(currentPoint.getAgentTerrain().getName() + " — " + currentPoint.getAgentTerrain().getEmail());
        } else {
            lblFieldAgent.setText("Not assigned");
        }

        lblStatusBadge.setText(safe(currentPoint.getStatut()));
        applyStatusStyle(lblStatusBadge, safe(currentPoint.getStatut()));

        lblSummaryId.setText("#" + currentPoint.getId());
        lblSummaryDelegation.setText(delegation);
        lblSummaryStatus.setText(safe(currentPoint.getStatut()));
        lblSummaryReport.setText(currentPoint.getRapportRecyc() != null ? "Yes" : "No");

        boolean locked = isLocked();
        btnConfirmAssign.setDisable(locked);
        rbManual.setDisable(locked);
        rbAuto.setDisable(locked);
        cbAgents.setDisable(locked);
        txtAgentSearch.setDisable(locked);

        if (locked) {
            lblAutoInfo.setText("Assignment locked: point already processed.");
        } else {
            lblAutoInfo.setText("");
        }

        refreshAssignMode();
    }

    private void applyStatusStyle(Label label, String status) {
        String style = "-fx-text-fill: black; -fx-padding: 6 14; -fx-background-radius: 18; -fx-font-weight: bold;";

        if ("PENDING".equalsIgnoreCase(status) || "DECLARE".equalsIgnoreCase(status) || "EN_ATTENTE".equalsIgnoreCase(status)) {
            style += "-fx-background-color: #fbc02d;";
        } else if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            style += "-fx-background-color: #4dd0e1;";
        } else if ("COLLECTE".equalsIgnoreCase(status)) {
            style += "-fx-background-color: #43a047; -fx-text-fill: white;";
        } else if ("VALIDE".equalsIgnoreCase(status)) {
            style += "-fx-background-color: #1e88e5; -fx-text-fill: white;";
        } else if ("REFUSE".equalsIgnoreCase(status)) {
            style += "-fx-background-color: #e53935; -fx-text-fill: white;";
        } else {
            style += "-fx-background-color: #e0e0e0;";
        }

        label.setStyle(style);
    }

    private boolean isLocked() {
        if (currentPoint == null) return true;

        String statut = safe(currentPoint.getStatut()).toUpperCase();
        return statut.equals("COLLECTE") || statut.equals("VALIDE") || statut.equals("REFUSE");
    }

    private void loadAgents() {
        try {
            if (loggedUser == null) return;

            List<User> agents = userService.getFieldAgentsByDelegation(loggedUser.getDelegation());
            cbAgents.setItems(FXCollections.observableArrayList(agents));

            if (!agents.isEmpty()) {
                cbAgents.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les agents terrain.");
        }
    }

    private void filterAgents() {
        try {
            if (loggedUser == null) return;

            String q = txtAgentSearch.getText() == null ? "" : txtAgentSearch.getText().toLowerCase().trim();
            List<User> agents = userService.getFieldAgentsByDelegation(loggedUser.getDelegation());

            if (!q.isEmpty()) {
                agents = agents.stream()
                        .filter(a ->
                                safe(a.getName()).toLowerCase().contains(q) ||
                                        safe(a.getEmail()).toLowerCase().contains(q))
                        .toList();
            }

            cbAgents.setItems(FXCollections.observableArrayList(agents));
            if (!agents.isEmpty()) {
                cbAgents.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshAssignMode() {
        boolean auto = rbAuto.isSelected();
        cbAgents.setVisible(!auto);
        cbAgents.setManaged(!auto);
        txtAgentSearch.setVisible(!auto);
        txtAgentSearch.setManaged(!auto);

        if (auto && !isLocked()) {
            lblAutoInfo.setText("Auto mode will assign the least loaded field agent.");
        } else if (!isLocked()) {
            lblAutoInfo.setText("");
        }
    }

    @FXML
    private void confirmAssign() {
        if (currentPoint == null || loggedUser == null) return;

        try {
            User selectedAgent;

            if (rbAuto.isSelected()) {
                List<User> agents = userService.getFieldAgentsByDelegation(loggedUser.getDelegation());

                if (agents.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Aucun agent", "Aucun agent terrain disponible.");
                    return;
                }

                selectedAgent = agents.stream()
                        .min(Comparator.comparingInt(a -> {
                            try {
                                return pointService.countInProgressPointsForFieldAgent(a.getId());
                            } catch (SQLException e) {
                                return Integer.MAX_VALUE;
                            }
                        }))
                        .orElse(null);

            } else {
                selectedAgent = cbAgents.getValue();
            }

            if (selectedAgent == null) {
                showAlert(Alert.AlertType.WARNING, "Agent requis", "Choisissez un agent terrain.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Assign point");
            confirm.setContentText("Confirmer l'affectation à " + selectedAgent.getName() + " ?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                pointService.assignPointToFieldAgent(currentPoint.getId(), selectedAgent.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Point affecté avec succès.");
                loadPointFresh();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'affecter ce point.");
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/municipal_points.fxml"));
            Parent root = loader.load();

            MunicipalPointsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) lblPointTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Recycling Points");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
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