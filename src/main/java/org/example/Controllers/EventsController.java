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
import java.util.List;
import javafx.scene.layout.StackPane;

public class EventsController {

    private static final String DEFAULT_EVENT_IMAGE = "/images/entretien-jardin-2.jpg";

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
    private Label lblPoints;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnProfile;
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnHistory;

    @FXML private StackPane navbarContainer;
    private User loggedUser;
    private EventService eventService;
    private User currentUser;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        this.currentUser = user;
        loadNavbarByRole();

        updatePointsCounter();
    }

    private void loadNavbarByRole() {
        try {
            if (navbarContainer == null) {
                return;
            }

            navbarContainer.getChildren().clear();

            String roles = loggedUser != null && loggedUser.getRoles() != null ? loggedUser.getRoles() : "";
            FXMLLoader loader;

            if (roles.contains("ROLE_CITOYEN") || roles.contains("ROLE_AGENT_TERRAIN")) {
                loader = new FXMLLoader(getClass().getResource("/components/navbar_citoyen.fxml"));
            } else if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
                loader = new FXMLLoader(getClass().getResource("/components/navbar_municipal.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/components/navbar_citoyen.fxml"));
            }

            Parent navbar = loader.load();
            Object controller = loader.getController();

            if (controller instanceof org.example.Controllers.components.NavbarCitoyenController c) {
                c.setLoggedUser(loggedUser);
            } else if (controller instanceof org.example.Controllers.components.NavbarMunicipalController c) {
                c.setLoggedUser(loggedUser);
            }

            navbarContainer.getChildren().add(navbar);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        eventService = new EventService();
        
        // Récupérer l'utilisateur connecté depuis le contexte global
        // Vous devez avoir une classe pour gérer le contexte global
        loadEvents();
        updatePointsCounter();
        
        // Ajouter un listener sur les champs pour un filtrage en temps réel si nécessaire
        tfSearch.setStyle("-fx-control-inner-background: white;");
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
        eventsContainer.getChildren().clear();
        
        if (events.isEmpty()) {
            emptyContainer.setVisible(true);
            eventsContainer.setVisible(false);
        } else {
            emptyContainer.setVisible(false);
            eventsContainer.setVisible(true);
            
            // Créer une grille pour afficher les événements
            FlowPane flowPane = new FlowPane();
            flowPane.setHgap(20);
            flowPane.setVgap(20);
            flowPane.setPadding(new Insets(0));
            flowPane.setStyle("-fx-pref-wrap-length: 1300;");

            for (Event event : events) {
                VBox eventCard = createEventCard(event);
                flowPane.getChildren().add(eventCard);
            }

            eventsContainer.getChildren().add(flowPane);
            
            // Ajouter les statistiques en bas
            VBox statsBox = createStatsBox(events.size());
            eventsContainer.getChildren().add(statsBox);
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox();
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e5e5e5; -fx-border-width: 1; " +
                      "-fx-border-radius: 8; -fx-padding: 0; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Image de couverture
        HBox imageBox = new HBox();
        imageBox.setPrefHeight(160);
        imageBox.setStyle("-fx-background-color: linear-gradient(135deg, #667eea, #764ba2); " +
                          "-fx-background-radius: 8 8 0 0;");
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
        contentBox.setStyle("-fx-background-color: white;");

        // Titre
        Label titleLabel = new Label(event.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #104b2c;");
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
        lieuLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        lieuBox.getChildren().addAll(iconLieu, lieuLabel);
        infoBox.getChildren().add(lieuBox);

        // Date
        if (event.getDateDeb() != null) {
            HBox dateBox = new HBox();
            dateBox.setSpacing(8);
            dateBox.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            Label iconDate = new Label("📅");
            Label dateLabel = new Label(event.getDateDeb().format(dateFormatter));
            dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
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
        participantsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
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
            descLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
            descLabel.setWrapText(true);
            contentBox.getChildren().add(descLabel);
        }

        // Points
        if (event.getPointGain() > 0) {
            Label pointsLabel = new Label("⭐ " + event.getPointGain() + " points");
            pointsLabel.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #000; " +
                                "-fx-padding: 5 10; -fx-border-radius: 3; -fx-background-radius: 3;");
            contentBox.getChildren().add(pointsLabel);
        }

        // Bouton d'action
        Button btnSeeEvent = new Button("Voir et participer");
        btnSeeEvent.setPrefWidth(Double.MAX_VALUE);
        btnSeeEvent.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-padding: 10; -fx-border-radius: 4;");
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

    private VBox createStatsBox(int totalEvents) {
        VBox statsBox = new VBox();
        statsBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 20; -fx-border-color: #e5e5e5; -fx-border-width: 1;");
        statsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label statsLabel = new Label("Affichage de " + totalEvents + " événement(s)");
        statsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
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

