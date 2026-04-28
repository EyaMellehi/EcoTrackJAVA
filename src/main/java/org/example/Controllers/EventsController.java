package org.example.Controllers;

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

import org.example.Entities.Event;
import org.example.Entities.User;
import org.example.Services.EventService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class EventsController {

    private static final String DEFAULT_EVENT_IMAGE = "/images/entretien-jardin-2.jpg";
    private static final int EVENTS_PER_PAGE = 16;

    @FXML
    private TextField tfSearch;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private ComboBox<String> cbSortBy;
    @FXML
    private ComboBox<String> cbOrder;
    @FXML
    private Button btnFilter;
    @FXML
    private VBox eventsContainer;
    @FXML
    private VBox emptyContainer;
    @FXML
    private Pagination paginationEvents;
    @FXML
    private Label lblPoints;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnProfile;
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnHistory;

    private EventService eventService;
    private User currentUser;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private List<Event> currentEvents = Collections.emptyList();

    public void setLoggedUser(User user) {
        this.currentUser = user;
        updatePointsCounter();
    }

    @FXML
    public void initialize() {
        eventService = new EventService();
        
        // Récupérer l'utilisateur connecté depuis le contexte global
        // Vous devez avoir une classe pour gérer le contexte global
        loadEvents();
        updatePointsCounter();

        if (paginationEvents != null) {
            paginationEvents.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
                    renderPage(newIndex.intValue())
            );
        }
        
        // Ajouter un listener sur les champs pour un filtrage en temps réel si nécessaire
    }

    private void updatePointsCounter() {
        if (lblPoints == null) {
            return;
        }
        int points = currentUser != null ? currentUser.getPoints() : 0;
        lblPoints.setText("Points: " + points);
    }

    private void loadEvents() {
        List<Event> events = eventService.getAllPublishedEvents();
        displayEvents(events);
    }

    @FXML
    private void filterEvents() {
        String query = tfSearch.getText().trim();
        String statut = cbStatut.getValue();
        String sortBy = cbSortBy.getValue();
        String order = cbOrder.getValue();

        List<Event> events = eventService.searchEvents(query, statut, sortBy, order);
        displayEvents(events);
    }

    private void displayEvents(List<Event> events) {
        currentEvents = events == null ? Collections.emptyList() : events;

        if (currentEvents.isEmpty()) {
            eventsContainer.getChildren().clear();
            emptyContainer.setVisible(true);
            eventsContainer.setVisible(false);
            if (paginationEvents != null) {
                paginationEvents.setVisible(false);
                paginationEvents.setManaged(false);
                paginationEvents.setPageCount(1);
                paginationEvents.setCurrentPageIndex(0);
            }
        } else {
            emptyContainer.setVisible(false);
            eventsContainer.setVisible(true);

            if (paginationEvents != null) {
                int pageCount = (int) Math.ceil((double) currentEvents.size() / EVENTS_PER_PAGE);
                paginationEvents.setPageCount(Math.max(pageCount, 1));
                paginationEvents.setVisible(pageCount > 1);
                paginationEvents.setManaged(pageCount > 1);
                paginationEvents.setCurrentPageIndex(0);
                renderPage(0);
            } else {
                renderPage(0);
            }
        }
    }

    private void renderPage(int pageIndex) {
        eventsContainer.getChildren().clear();

        if (currentEvents == null || currentEvents.isEmpty()) {
            return;
        }

        int safePageIndex = Math.max(0, pageIndex);
        int fromIndex = safePageIndex * EVENTS_PER_PAGE;
        if (fromIndex >= currentEvents.size()) {
            fromIndex = 0;
            safePageIndex = 0;
            if (paginationEvents != null && paginationEvents.getCurrentPageIndex() != 0) {
                paginationEvents.setCurrentPageIndex(0);
            }
        }

        int toIndex = Math.min(fromIndex + EVENTS_PER_PAGE, currentEvents.size());
        List<Event> pageEvents = currentEvents.subList(fromIndex, toIndex);

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(20);
        flowPane.setVgap(20);
        flowPane.setPadding(new Insets(0));
        flowPane.setPrefWrapLength(1300);

        for (Event event : pageEvents) {
            VBox eventCard = createEventCard(event);
            flowPane.getChildren().add(eventCard);
        }

        eventsContainer.getChildren().add(flowPane);

        int totalPages = (int) Math.ceil((double) currentEvents.size() / EVENTS_PER_PAGE);
        VBox statsBox = createStatsBox(pageEvents.size(), currentEvents.size(), safePageIndex + 1, Math.max(totalPages, 1));
        eventsContainer.getChildren().add(statsBox);
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox();
        card.setPrefWidth(320);
        card.getStyleClass().add("event-card");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Image de couverture
        HBox imageBox = new HBox();
        imageBox.setPrefHeight(160);
        imageBox.getStyleClass().add("event-card-image");
        imageBox.setAlignment(javafx.geometry.Pos.CENTER);

        if (event.getCoverMediaPath() != null && !event.getCoverMediaPath().isEmpty()) {
            try {
                ImageView imageView = new ImageView(new Image("file:///" + event.getCoverMediaPath()));
                imageView.setFitWidth(320);
                imageView.setFitHeight(160);
                imageView.setPreserveRatio(false);
                imageBox.getChildren().add(imageView);
            } catch (Exception e) {
                setDefaultEventImage(imageBox);
            }
        } else {
            setDefaultEventImage(imageBox);
        }
        
        card.getChildren().add(imageBox);

        // Contenu de la carte
        VBox contentBox = new VBox();
        contentBox.setSpacing(10);
        contentBox.setPadding(new Insets(20));
        contentBox.getStyleClass().add("event-card-body");

        // Titre
        Label titleLabel = new Label(event.getTitre());
        titleLabel.getStyleClass().add("event-card-title");
        titleLabel.setWrapText(true);
        contentBox.getChildren().add(titleLabel);

        // Infos principales
        VBox infoBox = new VBox();
        infoBox.setSpacing(8);

        // Lieu
        HBox lieuBox = new HBox();
        lieuBox.setSpacing(8);
        lieuBox.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        Label iconLieu = new Label("📍");
        Label lieuLabel = new Label(event.getLieu());
        lieuLabel.getStyleClass().add("event-meta");
        lieuBox.getChildren().addAll(iconLieu, lieuLabel);
        infoBox.getChildren().add(lieuBox);

        // Date
        if (event.getDateDeb() != null) {
            HBox dateBox = new HBox();
            dateBox.setSpacing(8);
            dateBox.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            Label iconDate = new Label("📅");
            Label dateLabel = new Label(event.getDateDeb().format(dateFormatter));
            dateLabel.getStyleClass().add("event-meta");
            dateBox.getChildren().addAll(iconDate, dateLabel);
            infoBox.getChildren().add(dateBox);
        }

        // Participants
        HBox participantsBox = new HBox();
        participantsBox.setSpacing(8);
        participantsBox.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        Label iconParticipants = new Label("👥");
        String capacityStr = event.getParticipationCount() + " participants";
        if (event.getCapaciteMax() > 0) {
            capacityStr += " / " + event.getCapaciteMax();
        }
        Label participantsLabel = new Label(capacityStr);
        participantsLabel.getStyleClass().add("event-meta");
        participantsBox.getChildren().addAll(iconParticipants, participantsLabel);
        infoBox.getChildren().add(participantsBox);

        contentBox.getChildren().add(infoBox);

        // Description courte
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            String desc = event.getDescription();
            if (desc.length() > 80) {
                desc = desc.substring(0, 80) + "...";
            }
            Label descLabel = new Label(desc);
            descLabel.getStyleClass().add("event-meta");
            descLabel.setWrapText(true);
            contentBox.getChildren().add(descLabel);
        }

        // Points
        if (event.getPointGain() > 0) {
            Label pointsLabel = new Label("⭐ " + event.getPointGain() + " points");
            pointsLabel.getStyleClass().add("event-points");
            contentBox.getChildren().add(pointsLabel);
        }

        // Bouton d'action
        Button btnSeeEvent = new Button("Voir et participer");
        btnSeeEvent.setPrefWidth(Double.MAX_VALUE);
        btnSeeEvent.getStyleClass().add("event-primary-button");
        btnSeeEvent.setOnAction(e -> goToEventDetails(event.getId()));
        contentBox.getChildren().add(btnSeeEvent);

        VBox.setVgrow(contentBox, Priority.ALWAYS);
        card.getChildren().add(contentBox);

        return card;
    }

    private void createPlaceholder(HBox imageBox) {
        Label placeholderLabel = new Label("🍃");
        placeholderLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");
        imageBox.getChildren().add(placeholderLabel);
    }

    private void setDefaultEventImage(HBox imageBox) {
        try {
            java.net.URL defaultUrl = getClass().getResource(DEFAULT_EVENT_IMAGE);
            if (defaultUrl == null) {
                createPlaceholder(imageBox);
                return;
            }
            ImageView imageView = new ImageView(new Image(defaultUrl.toExternalForm()));
            imageView.setFitWidth(320);
            imageView.setFitHeight(160);
            imageView.setPreserveRatio(false);
            imageBox.getChildren().add(imageView);
        } catch (Exception e) {
            createPlaceholder(imageBox);
        }
    }

    private VBox createStatsBox(int displayedEvents, int totalEvents, int currentPage, int totalPages) {
        VBox statsBox = new VBox();
        statsBox.getStyleClass().add("event-stat-box");
        statsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label statsLabel = new Label("Affichage de " + displayedEvents + " sur " + totalEvents
                + " événement(s) - Page " + currentPage + "/" + totalPages);
        statsLabel.getStyleClass().add("event-stat-text");
        statsBox.getChildren().add(statsLabel);

        return statsBox;
    }

    private void goToEventDetails(int eventId) {
        if (currentUser == null) {
            showNavigationError("Vous devez être connecté pour participer à un événement.", new IllegalStateException("Utilisateur non connecté"));
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event_participation.fxml"));
            Parent root = loader.load();

            EventParticipationController controller = loader.getController();
            Event event = eventService.getEventById(eventId);
            if (event == null) {
                showNavigationError("Événement introuvable.", new IllegalStateException("Event null"));
                return;
            }
            controller.setLoggedUser(currentUser);
            controller.setEvent(event);

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Participer - " + event.getTitre());
            stage.show();
        } catch (Exception e) {
            showNavigationError("Impossible d'ouvrir la page de participation.", e);
        }
    }

    @FXML
    private void goHome() {
        if (currentUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
                Parent root = loader.load();

                HomeConnectedController controller = loader.getController();
                controller.setLoggedUser(currentUser);

                Stage stage = (Stage) btnHome.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("EcoTrack - Home");
                stage.show();
                return;
            } catch (Exception e) {
                showNavigationError("Impossible d'ouvrir la page d'accueil connectée.", e);
                return;
            }
        }
        loadScene("home.fxml");
    }

    @FXML
    private void goProfile() {
        // À implémenter si l'utilisateur est connecté
        loadScene("User/profile.fxml");
    }

    @FXML
    private void goHistory() {
        if (currentUser == null) {
            showNavigationError("Vous devez être connecté pour voir votre historique.", new IllegalStateException("Utilisateur non connecté"));
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event_history.fxml"));
            Parent root = loader.load();

            EventHistoryController controller = loader.getController();
            controller.setLoggedUser(currentUser);

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon historique");
            stage.show();
        } catch (Exception e) {
            showNavigationError("Impossible d'ouvrir l'historique des participations.", e);
        }
    }

    @FXML
    private void goToEventsManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/events_management.fxml"));
            Parent root = loader.load();

            org.example.Controllers.admin.EventsManagementController controller = loader.getController();
            controller.setLoggedUser(currentUser);

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des evenements");
            stage.show();
        } catch (IOException e) {
            showNavigationError("Impossible d'ouvrir la gestion des evenements", e);
        }
    }

    @FXML
    private void logout() {
        // À implémenter : déconnexion
        loadScene("home.fxml");
    }

    private void loadScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent root = loader.load();
            
            Stage stage = (Stage) btnHome.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            showNavigationError("Erreur lors du chargement de " + fxmlFile, e);
        }
    }

    private void showNavigationError(String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, message + "\n\n" + e.getMessage(), ButtonType.OK);
        alert.setHeaderText("Erreur de navigation");
        alert.showAndWait();
    }
}

