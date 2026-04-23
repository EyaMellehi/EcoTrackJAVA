package org.example.Controllers.recyclage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Controllers.components.NavbarMunicipalController;
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;
import org.example.Services.UserService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    @FXML private Label lblDescription;
    @FXML private Label lblLatitude;
    @FXML private Label lblLongitude;
    @FXML private Label lblAiPriority;
    @FXML private Label lblAiExplanation;
    @FXML private Label lblFieldAgent;
    @FXML private Label lblStatusBadge;

    @FXML private Label lblSummaryId;
    @FXML private Label lblSummaryDelegation;
    @FXML private Label lblSummaryStatus;
    @FXML private Label lblSummaryReport;

    @FXML private RadioButton rbManual;
    @FXML private RadioButton rbAuto;
    @FXML private TextField txtAgentSearch;
    @FXML private Label lblAutoInfo;
    @FXML private Button btnConfirmAssign;

    @FXML private FlowPane agentsContainer;
    @FXML private ScrollPane agentsScrollPane;
    @FXML private Label lblSelectedAgent;

    @FXML private VBox assignmentSection;
    @FXML private Label lblTimer;
    @FXML private Button btnCancelAssign;

    private final PointRecyclageService pointService = new PointRecyclageService();
    private final UserService userService = new UserService();

    private User loggedUser;
    private PointRecyclage currentPoint;

    private List<User> allAgents = new ArrayList<>();
    private User selectedAgent;

    private Timeline countdownTimeline;

    public void setData(User loggedUser, PointRecyclage point) {
        this.loggedUser = loggedUser;
        this.currentPoint = point;

        if (navbarIncludeController != null) {
            navbarIncludeController.setLoggedUser(loggedUser);
        }

        loadPointFresh();
        loadAgents();
    }

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        rbManual.setToggleGroup(group);
        rbAuto.setToggleGroup(group);
        rbManual.setSelected(true);

        styleModeButtons();

        txtAgentSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshAgentCards());
        rbManual.selectedProperty().addListener((obs, oldVal, newVal) -> refreshAssignMode());
        rbAuto.selectedProperty().addListener((obs, oldVal, newVal) -> refreshAssignMode());
    }

    private void loadPointFresh() {
        try {
            if (currentPoint != null) {
                currentPoint = pointService.getPointById(currentPoint.getId());
            }
            fillData();
            updateAssignmentVisibility();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les détails du point.");
        }
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

        String description = currentPoint.getDescription();
        if (description == null || description.isBlank()) {
            lblDescription.setText("No description.");
        } else {
            lblDescription.setText(description);
        }

        lblLatitude.setText(String.valueOf(currentPoint.getLatitude()));
        lblLongitude.setText(String.valueOf(currentPoint.getLongitude()));

        fillAiSection();

        if (currentPoint.getAgentTerrain() != null) {
            lblFieldAgent.setText(currentPoint.getAgentTerrain().getName() + " — " + currentPoint.getAgentTerrain().getEmail());
            lblFieldAgent.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f3d23;");
        } else {
            lblFieldAgent.setText("Not assigned");
            lblFieldAgent.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;");
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
        txtAgentSearch.setDisable(locked);

        if (locked) {
            lblAutoInfo.setText("Assignment locked: point already processed.");
        } else {
            lblAutoInfo.setText("");
        }

        refreshAssignMode();
    }

    private void fillAiSection() {
        String priority = currentPoint.getAiPriority();
        String explanation = currentPoint.getAiExplanation();

        if (priority == null || priority.isBlank()) {
            lblAiPriority.setText("Not estimated");
            lblAiPriority.setStyle(
                    "-fx-background-color: #f3f4f6;" +
                            "-fx-text-fill: #4b5563;" +
                            "-fx-padding: 6 12;" +
                            "-fx-background-radius: 16;" +
                            "-fx-font-weight: bold;"
            );
        } else {
            lblAiPriority.setText(priority.toUpperCase());
            applyAiPriorityStyle(lblAiPriority, priority);
        }

        if (explanation == null || explanation.isBlank()) {
            lblAiExplanation.setText("No explanation available.");
        } else {
            lblAiExplanation.setText(explanation);
        }
    }

    private void applyAiPriorityStyle(Label label, String priority) {
        String p = safe(priority).toUpperCase();

        String base =
                "-fx-padding: 6 12;" +
                        "-fx-background-radius: 16;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;";

        switch (p) {
            case "LOW":
                label.setStyle(base + "-fx-background-color: #dcfce7; -fx-text-fill: #166534;");
                break;
            case "MEDIUM":
                label.setStyle(base + "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;");
                break;
            case "HIGH":
                label.setStyle(base + "-fx-background-color: #fed7aa; -fx-text-fill: #9a3412;");
                break;
            case "URGENT":
                label.setStyle(base + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;");
                break;
            default:
                label.setStyle(base + "-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563;");
                break;
        }
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

            allAgents = userService.getFieldAgentsByDelegation(loggedUser.getDelegation());
            selectedAgent = null;
            lblSelectedAgent.setText("None");
            refreshAgentCards();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les agents terrain.");
        }
    }

    private void refreshAgentCards() {
        agentsContainer.getChildren().clear();

        String q = txtAgentSearch.getText() == null ? "" : txtAgentSearch.getText().toLowerCase().trim();

        List<User> filtered = allAgents.stream()
                .filter(a ->
                        q.isEmpty() ||
                                safe(a.getName()).toLowerCase().contains(q) ||
                                safe(a.getEmail()).toLowerCase().contains(q))
                .toList();

        if (filtered.isEmpty()) {
            Label empty = new Label("No field agents found.");
            empty.setStyle("-fx-text-fill: #777; -fx-font-size: 15px;");
            agentsContainer.getChildren().add(empty);
            return;
        }

        for (User agent : filtered) {
            VBox card = createAgentCard(agent);
            agentsContainer.getChildren().add(card);
        }
    }

    private VBox createAgentCard(User agent) {
        VBox card = new VBox(10);
        card.setPrefWidth(240);
        card.setMinWidth(240);
        card.setPadding(new Insets(18));
        card.setStyle(getCardStyle(agent.equals(selectedAgent)));

        Label avatar = new Label(getInitials(agent));
        avatar.setMinSize(52, 52);
        avatar.setPrefSize(52, 52);
        avatar.setMaxSize(52, 52);
        avatar.setAlignment(javafx.geometry.Pos.CENTER);
        avatar.setStyle(
                "-fx-background-color: #dff3e2;" +
                        "-fx-text-fill: #1f5130;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 999;"
        );

        Label name = new Label(safe(agent.getName()).isEmpty() ? "Unnamed agent" : agent.getName());
        name.setWrapText(true);
        name.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #0f3d23;");

        Label email = new Label(safe(agent.getEmail()).isEmpty() ? "-" : agent.getEmail());
        email.setWrapText(true);
        email.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

        int load = getAgentLoad(agent.getId());
        Label loadBadge = new Label("Load: " + load + " point(s)");
        loadBadge.setStyle(
                "-fx-background-color: #eef7ee;" +
                        "-fx-text-fill: #2e7d32;" +
                        "-fx-padding: 6 10;" +
                        "-fx-background-radius: 999;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        Label action = new Label(agent.equals(selectedAgent) ? "Selected" : "Click to select");
        action.setStyle(
                agent.equals(selectedAgent)
                        ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;"
                        : "-fx-text-fill: #94a3b8;"
        );

        card.getChildren().addAll(avatar, name, email, loadBadge, action);

        card.setOnMouseClicked(event -> {
            if (isLocked() || rbAuto.isSelected()) return;
            selectedAgent = agent;
            lblSelectedAgent.setText(agent.getName() + " (" + agent.getEmail() + ")");
            refreshAgentCards();
        });

        return card;
    }

    private String getCardStyle(boolean selected) {
        if (selected) {
            return "-fx-background-color: linear-gradient(to bottom right, #eefaf0, #dff3e2);" +
                    "-fx-border-color: #2e7d32;" +
                    "-fx-border-width: 2;" +
                    "-fx-background-radius: 16;" +
                    "-fx-border-radius: 16;" +
                    "-fx-cursor: hand;";
        }

        return "-fx-background-color: white;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-width: 1;" +
                "-fx-background-radius: 16;" +
                "-fx-border-radius: 16;" +
                "-fx-cursor: hand;";
    }

    private int getAgentLoad(int agentId) {
        try {
            return pointService.countInProgressPointsForFieldAgent(agentId);
        } catch (SQLException e) {
            return 0;
        }
    }

    private String getInitials(User user) {
        String name = safe(user.getName()).trim();
        if (name.isEmpty()) return "AG";

        String[] parts = name.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        String first = parts[0].isEmpty() ? "" : parts[0].substring(0, 1);
        String second = parts[1].isEmpty() ? "" : parts[1].substring(0, 1);
        return (first + second).toUpperCase();
    }

    private void refreshAssignMode() {
        boolean auto = rbAuto.isSelected();

        agentsScrollPane.setVisible(!auto);
        agentsScrollPane.setManaged(!auto);
        txtAgentSearch.setVisible(!auto);
        txtAgentSearch.setManaged(!auto);

        if (auto && !isLocked()) {
            selectedAgent = null;
            lblSelectedAgent.setText("Auto selection");
            lblAutoInfo.setText("Auto mode will assign the least loaded field agent.");
        } else if (!isLocked()) {
            lblAutoInfo.setText("");
            if (selectedAgent == null) {
                lblSelectedAgent.setText("None");
            }
        }

        refreshAgentCards();
    }

    @FXML
    private void confirmAssign() {
        if (currentPoint == null || loggedUser == null) return;

        try {
            User agentToAssign;

            if (rbAuto.isSelected()) {
                if (allAgents.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Aucun agent", "Aucun agent terrain disponible.");
                    return;
                }

                agentToAssign = allAgents.stream()
                        .min(Comparator.comparingInt(a -> {
                            try {
                                return pointService.countInProgressPointsForFieldAgent(a.getId());
                            } catch (SQLException e) {
                                return Integer.MAX_VALUE;
                            }
                        }))
                        .orElse(null);

            } else {
                agentToAssign = selectedAgent;
            }

            if (agentToAssign == null) {
                showAlert(Alert.AlertType.WARNING, "Agent requis", "Choisissez un agent terrain.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Assign point");
            confirm.setContentText("Confirmer l'affectation à " + agentToAssign.getName() + " ?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                pointService.assignPointToFieldAgent(currentPoint.getId(), agentToAssign.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Point affecté avec succès.");
                loadPointFresh();
                loadAgents();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'affecter ce point.");
        }
    }

    @FXML
    private void cancelAssign() {
        if (currentPoint == null) return;

        if (!canCancelAssignment()) {
            showAlert(Alert.AlertType.WARNING, "Temps écoulé", "Le délai de 60 secondes est dépassé.");
            loadPointFresh();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Cancel assignment");
        confirm.setContentText("Voulez-vous annuler cette affectation ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                pointService.cancelAssignment(currentPoint.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'affectation a été annulée.");
                loadPointFresh();
                loadAgents();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler l'affectation.");
            }
        }
    }

    private boolean canCancelAssignment() {
        if (currentPoint == null) return false;
        if (currentPoint.getAssignedAt() == null) return false;
        if (currentPoint.getAgentTerrain() == null) return false;

        long seconds = ChronoUnit.SECONDS.between(currentPoint.getAssignedAt(), LocalDateTime.now());
        return seconds < 60;
    }

    private void updateAssignmentVisibility() {
        stopCountdown();

        boolean assigned = currentPoint != null && currentPoint.getAgentTerrain() != null;
        boolean canCancel = canCancelAssignment();

        btnCancelAssign.setVisible(canCancel);
        btnCancelAssign.setManaged(canCancel);

        lblTimer.setVisible(canCancel);
        lblTimer.setManaged(canCancel);

        boolean showAssignmentSection = !isLocked() && (!assigned || canCancel);
        assignmentSection.setVisible(showAssignmentSection);
        assignmentSection.setManaged(showAssignmentSection);

        if (canCancel) {
            startCountdown();
        } else {
            lblTimer.setText("");
        }
    }

    private void startCountdown() {
        updateTimerLabel();

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (!canCancelAssignment()) {
                stopCountdown();
                btnCancelAssign.setVisible(false);
                btnCancelAssign.setManaged(false);
                lblTimer.setVisible(false);
                lblTimer.setManaged(false);
                assignmentSection.setVisible(false);
                assignmentSection.setManaged(false);
                return;
            }

            updateTimerLabel();
        }));

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateTimerLabel() {
        if (currentPoint == null || currentPoint.getAssignedAt() == null) return;

        long elapsed = ChronoUnit.SECONDS.between(currentPoint.getAssignedAt(), LocalDateTime.now());
        long remaining = Math.max(0, 60 - elapsed);

        lblTimer.setText("Cancellation available for " + remaining + " second(s).");
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    @FXML
    private void goBack() {
        stopCountdown();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/municipal_points.fxml"));
            Parent root = loader.load();

            MunicipalPointsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) lblPointTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Recycling Points");
            stage.setFullScreen(true);
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

    private void styleModeButtons() {
        updateModeButtonStyle();

        rbManual.selectedProperty().addListener((obs, oldVal, newVal) -> updateModeButtonStyle());
        rbAuto.selectedProperty().addListener((obs, oldVal, newVal) -> updateModeButtonStyle());
    }

    private void updateModeButtonStyle() {
        String selectedStyle =
                "-fx-background-color: linear-gradient(to right, #2e7d32, #43a047);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 22;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;";

        String unselectedStyle =
                "-fx-background-color: white;" +
                        "-fx-text-fill: #2f3e2f;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 22;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-border-color: #cfd8cf;" +
                        "-fx-border-width: 1;" +
                        "-fx-cursor: hand;";

        rbManual.setStyle(rbManual.isSelected() ? selectedStyle : unselectedStyle);
        rbAuto.setStyle(rbAuto.isSelected() ? selectedStyle : unselectedStyle);
    }
}