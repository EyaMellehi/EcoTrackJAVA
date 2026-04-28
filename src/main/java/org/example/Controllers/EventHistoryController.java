package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Entities.Event;
import org.example.Entities.User;
import org.example.Services.EventService;
import org.example.Services.ParticipationService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class EventHistoryController {

    @FXML
    private Label lblInfo;
    @FXML
    private Label lblSummary;
    @FXML
    private FlowPane historyFlow;
    @FXML
    private VBox emptyBox;
    @FXML
    private TextField tfSearch;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private ComboBox<String> cbSort;

    private final ParticipationService participationService = new ParticipationService();
    private final EventService eventService = new EventService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private User loggedUser;
    private List<ParticipationService.ParticipationHistoryItem> allHistory = new ArrayList<>();

    @FXML
    public void initialize() {
        if (cbStatus != null) {
            cbStatus.setItems(FXCollections.observableArrayList("Tous", "inscrit", "present", "absent"));
            cbStatus.setValue("Tous");
        }
        if (cbSort != null) {
            cbSort.setItems(FXCollections.observableArrayList("Inscription recente", "Date evenement"));
            cbSort.setValue("Inscription recente");
        }
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        loadHistory();
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/events.fxml"));
            Parent root = loader.load();

            EventsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) historyFlow.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Events");
            stage.show();
        } catch (IOException e) {
            showError("Impossible de revenir à la liste des événements.");
        }
    }

    @FXML
    private void goHome() {
        if (loggedUser == null) {
            goBack();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
            Parent root = loader.load();

            HomeConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) historyFlow.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (IOException e) {
            showError("Impossible d'ouvrir la page d'accueil.");
        }
    }

    private void loadHistory() {
        historyFlow.getChildren().clear();
        allHistory.clear();

        if (loggedUser == null) {
            lblInfo.setText("Connectez-vous pour voir votre historique.");
            updateSummary(0, 0);
            emptyBox.setVisible(true);
            emptyBox.setManaged(true);
            return;
        }

        allHistory = participationService.getParticipationHistory(loggedUser.getId());
        if (!allHistory.isEmpty()) {
            lblInfo.setText("Total: " + allHistory.size() + " participation(s)");
            applyFilters();
        } else {
            emptyBox.setVisible(true);
            emptyBox.setManaged(true);
            updateSummary(0, 0);
            String lastError = participationService.getLastError();
            if (lastError != null && !lastError.isBlank()) {
                lblInfo.setText("Aucune participation affichée. Détail: " + lastError);
            } else {
                lblInfo.setText("Aucune participation trouvée.");
            }
        }
    }

    @FXML
    private void applyFilters() {
        if (allHistory == null) {
            allHistory = new ArrayList<>();
        }

        String query = tfSearch != null && tfSearch.getText() != null ? tfSearch.getText().trim().toLowerCase(Locale.ROOT) : "";
        String selectedStatus = cbStatus != null && cbStatus.getValue() != null ? cbStatus.getValue() : "Tous";
        String selectedSort = cbSort != null && cbSort.getValue() != null ? cbSort.getValue() : "Inscription recente";

        List<ParticipationService.ParticipationHistoryItem> filtered = new ArrayList<>();
        for (ParticipationService.ParticipationHistoryItem item : allHistory) {
            if (!matchesSearch(item, query)) {
                continue;
            }

            String status = normalizeStatus(item.getParticipationStatus());
            if (!"Tous".equalsIgnoreCase(selectedStatus) && !selectedStatus.equalsIgnoreCase(status)) {
                continue;
            }
            filtered.add(item);
        }

        Comparator<ParticipationService.ParticipationHistoryItem> comparator;
        if ("Date evenement".equals(selectedSort)) {
            comparator = Comparator.comparing(ParticipationService.ParticipationHistoryItem::getEventDate,
                    Comparator.nullsLast(Comparator.reverseOrder()));
        } else {
            comparator = Comparator.comparing(ParticipationService.ParticipationHistoryItem::getParticipationDate,
                    Comparator.nullsLast(Comparator.reverseOrder()));
        }
        filtered.sort(comparator);

        renderHistory(filtered);
        updateSummary(filtered.size(), allHistory.size());
    }

    @FXML
    private void resetFilters() {
        if (tfSearch != null) {
            tfSearch.clear();
        }
        if (cbStatus != null) {
            cbStatus.setValue("Tous");
        }
        if (cbSort != null) {
            cbSort.setValue("Inscription recente");
        }
        applyFilters();
    }

    private void renderHistory(List<ParticipationService.ParticipationHistoryItem> history) {
        historyFlow.getChildren().clear();
        boolean hasItems = history != null && !history.isEmpty();
        emptyBox.setVisible(!hasItems);
        emptyBox.setManaged(!hasItems);

        if (!hasItems) {
            return;
        }

        for (ParticipationService.ParticipationHistoryItem item : history) {
            historyFlow.getChildren().add(createHistoryCard(item));
        }
    }

    private void updateSummary(int filteredCount, int totalCount) {
        if (lblSummary == null) {
            return;
        }
        if (totalCount <= 0) {
            lblSummary.setText("Aucun resultat");
            return;
        }
        if (filteredCount == totalCount) {
            lblSummary.setText(filteredCount + " participation(s) affichee(s)");
            return;
        }
        lblSummary.setText(filteredCount + " / " + totalCount + " participation(s) affichee(s)");
    }

    private boolean matchesSearch(ParticipationService.ParticipationHistoryItem item, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String titre = item.getTitre() == null ? "" : item.getTitre().toLowerCase(Locale.ROOT);
        String lieu = item.getLieu() == null ? "" : item.getLieu().toLowerCase(Locale.ROOT);
        return titre.contains(query) || lieu.contains(query);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "inscrit";
        }
        return status.trim().toLowerCase(Locale.ROOT);
    }

    private VBox createHistoryCard(ParticipationService.ParticipationHistoryItem item) {
        VBox card = new VBox(10);
        card.setPrefWidth(350);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("event-history-card");

        HBox titleRow = new HBox();
        Label lblTitle = new Label(item.getTitre() == null ? "(Sans titre)" : item.getTitre());
        lblTitle.getStyleClass().add("event-card-title");

        Label badge = new Label(item.getParticipationStatus() == null ? "inscrit" : item.getParticipationStatus());
        String status = badge.getText().toLowerCase();
        if ("present".equals(status)) {
            badge.getStyleClass().addAll("event-badge", "event-badge-present");
        } else if ("absent".equals(status)) {
            badge.getStyleClass().addAll("event-badge", "event-badge-absent");
        } else {
            badge.getStyleClass().addAll("event-badge", "event-badge-default");
        }

        HBox.setHgrow(lblTitle, Priority.ALWAYS);
        titleRow.getChildren().addAll(lblTitle, badge);

        Label lblLieu = new Label("Lieu: " + (item.getLieu() == null ? "—" : item.getLieu()));
        lblLieu.getStyleClass().add("event-meta");
        Label lblEventDate = new Label("Date événement: " + (item.getEventDate() == null ? "—" : item.getEventDate().format(formatter)));
        lblEventDate.getStyleClass().add("event-meta");
        Label lblInscriptionDate = new Label("Inscrit le: " + (item.getParticipationDate() == null ? "—" : item.getParticipationDate().format(formatter)));
        lblInscriptionDate.getStyleClass().add("event-meta");
        Label lblPoints = new Label("Points: " + item.getPointGain());
        lblPoints.getStyleClass().add("event-points");

        Button btnDetails = new Button("Détails");
        btnDetails.getStyleClass().add("event-outline-button");
        btnDetails.setOnAction(e -> openParticipationDetails(item.getEventId()));

        card.getChildren().addAll(titleRow, lblLieu, lblEventDate, lblInscriptionDate, lblPoints, btnDetails);
        return card;
    }

    private void openParticipationDetails(int eventId) {
        if (loggedUser == null) {
            showError("Session expirée.");
            return;
        }
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            showError("Evenement introuvable.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event_participation.fxml"));
            Parent root = loader.load();

            EventParticipationController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setEvent(event);

            Stage stage = (Stage) historyFlow.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Participer - " + event.getTitre());
            stage.show();
        } catch (IOException e) {
            showError("Impossible d'ouvrir les détails.");
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText("Erreur");
        alert.showAndWait();
    }
}

