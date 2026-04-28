package org.example.Controllers.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.Event;
import org.example.Entities.User;
import org.example.Services.EventService;
import org.example.Services.SmsService;
import org.example.Services.EventValidator;
import org.example.Services.UserService;
import org.example.Services.WeatherService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EventFormController {

    @FXML
    private Label lblTitle;
    @FXML
    private Button btnHome;
    @FXML
    private Label lblFormError;
    @FXML
    private Label lblSuggestion;
    @FXML
    private Button btnSubmit;
    @FXML
    private TextField tfTitre;
    @FXML
    private TextArea taDescription;
    @FXML
    private TextField tfLieu;
    @FXML
    private DatePicker dpDateDeb;
    @FXML
    private TextField tfHeureDeb;
    @FXML
    private DatePicker dpDateFin;
    @FXML
    private TextField tfHeureFin;
    @FXML
    private TextField tfCapaciteMax;
    @FXML
    private TextField tfPointGain;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private TextField tfCoverMediaId;

    private final EventService eventService = new EventService();
    private final EventValidator eventValidator = new EventValidator();
    private final WeatherService weatherService = new WeatherService();
    private final UserService userService = new UserService();
    private final SmsService smsService = new SmsService();
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Event eventToEdit;
    private User loggedUser;
    private LocalDateTime suggestedDateDeb;
    private LocalDateTime suggestedDateFin;

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        applyDefaultLieuForMunicipalAgent();
        refreshWeatherSuggestion();
    }

    @FXML
    public void initialize() {
        if (btnHome != null) {
            btnHome.setOnAction(e -> goHome());
        }
        cbStatut.setItems(FXCollections.observableArrayList("brouillon", "publie", "termine", "annule"));
        cbStatut.setValue("publie");
        tfHeureDeb.setText("09:00");
        tfHeureFin.setText("11:00");

        // Fallback local en attendant la suggestion meteo.
        suggestedDateDeb = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        suggestedDateFin = suggestedDateDeb.plusHours(2);
        updateSuggestionLabel("Suggestion initiale", "Chargement de la meteo...");

        tfLieu.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) {
                refreshWeatherSuggestion();
            }
        });
    }

    public void setEventToEdit(Event event) {
        this.eventToEdit = event;
        if (event == null) {
            lblTitle.setText("Nouvel evenement");
            btnSubmit.setText("Creer l'evenement");
            applyDefaultLieuForMunicipalAgent();
            refreshWeatherSuggestion();
            return;
        }

        lblTitle.setText("Modifier evenement");
        btnSubmit.setText("Enregistrer les modifications");
        tfTitre.setText(event.getTitre());
        taDescription.setText(event.getDescription());
        tfLieu.setText(event.getLieu());

        if (event.getDateDeb() != null) {
            dpDateDeb.setValue(event.getDateDeb().toLocalDate());
            tfHeureDeb.setText(event.getDateDeb().toLocalTime().format(timeFormat));
        }

        if (event.getDateFin() != null) {
            dpDateFin.setValue(event.getDateFin().toLocalDate());
            tfHeureFin.setText(event.getDateFin().toLocalTime().format(timeFormat));
        }

        tfCapaciteMax.setText(String.valueOf(event.getCapaciteMax()));
        tfPointGain.setText(String.valueOf(event.getPointGain()));
        cbStatut.setValue(event.getStatut());
        tfCoverMediaId.setText(event.getCoverMediaId() == null ? "" : String.valueOf(event.getCoverMediaId()));
    }

    @FXML
    private void saveEvent() {
        hideFormError();
        try {
            Event event = buildEventFromForm();
            eventValidator.validate(event);

            if (eventToEdit == null && !event.getDateDeb().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("La date de debut doit etre dans le futur");
            }

            boolean ok;

            if (eventToEdit == null) {
                ok = eventService.createEvent(event);
            } else {
                event.setId(eventToEdit.getId());
                ok = eventService.updateEvent(event);
            }

            if (!ok) {
                showFormError("Enregistrement echoue. Verifie les donnees et la connexion DB.");
                return;
            }

            String smsSummary = null;
            if (eventToEdit == null) {
                smsSummary = sendRegionalSmsNotifications(event);
            }

            goBackToManagement();
            if (smsSummary != null) {
                showInfo(smsSummary);
            }
        } catch (IllegalArgumentException ex) {
            showFormError(ex.getMessage());
        }
    }

    @FXML
    private void applySuggestion() {
        refreshWeatherSuggestion();
        if (suggestedDateDeb == null || suggestedDateFin == null) {
            return;
        }
        dpDateDeb.setValue(suggestedDateDeb.toLocalDate());
        tfHeureDeb.setText(suggestedDateDeb.toLocalTime().format(timeFormat));
        dpDateFin.setValue(suggestedDateFin.toLocalDate());
        tfHeureFin.setText(suggestedDateFin.toLocalTime().format(timeFormat));
    }

    @FXML
    private void cancel() {
        goBackToManagement();
    }

    @FXML
    private void goHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
            Parent root = loader.load();

            org.example.Controllers.HomeConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) tfTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir la page Home");
        }
    }

    private Event buildEventFromForm() {
        String titre = requireValue(tfTitre.getText(), "Titre obligatoire");
        String lieu = requireValue(tfLieu.getText(), "Lieu obligatoire");

        LocalDate dateDeb = dpDateDeb.getValue();
        LocalDate dateFin = dpDateFin.getValue();
        if (dateDeb == null || dateFin == null) {
            throw new IllegalArgumentException("Dates debut et fin obligatoires");
        }

        LocalTime heureDeb = parseTime(tfHeureDeb.getText(), "Heure debut invalide (HH:mm)");
        LocalTime heureFin = parseTime(tfHeureFin.getText(), "Heure fin invalide (HH:mm)");

        LocalDateTime dateDebDt = LocalDateTime.of(dateDeb, heureDeb);
        LocalDateTime dateFinDt = LocalDateTime.of(dateFin, heureFin);
        if (!dateFinDt.isAfter(dateDebDt)) {
            throw new IllegalArgumentException("La date/heure de fin doit etre apres la date/heure de debut");
        }

        int capacite = parseInt(tfCapaciteMax.getText(), "Capacite invalide");
        int points = parseInt(tfPointGain.getText(), "Point gain invalide");
        Integer coverMediaId = null;
        String coverMediaRaw = tfCoverMediaId.getText();
        if (coverMediaRaw != null && !coverMediaRaw.isBlank()) {
            coverMediaId = parseInt(coverMediaRaw, "Cover media ID invalide");
        }

        Event event = new Event();
        event.setTitre(titre);
        event.setDescription(taDescription.getText());
        event.setLieu(lieu);
        event.setDateDeb(dateDebDt);
        event.setDateFin(dateFinDt);
        event.setCapaciteMax(capacite);
        event.setPointGain(points);
        event.setStatut(cbStatut.getValue());
        event.setCreateurId(resolveCreateurId());
        event.setCoverMediaId(coverMediaId);
        return event;
    }

    private int resolveCreateurId() {
        if (eventToEdit != null) {
            return eventToEdit.getCreateurId();
        }
        return loggedUser != null ? loggedUser.getId() : 1;
    }

    private void applyDefaultLieuForMunicipalAgent() {
        if (eventToEdit != null || loggedUser == null || tfLieu == null) {
            return;
        }

        String roles = loggedUser.getRoles();
        boolean isMunicipal = roles != null && roles.contains("ROLE_AGENT_MUNICIPAL");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        String region = loggedUser.getRegion();

        if (isMunicipal && !isAdmin && region != null && !region.isBlank() && (tfLieu.getText() == null || tfLieu.getText().isBlank())) {
            tfLieu.setText(region);
        }
    }

    private void refreshWeatherSuggestion() {
        String lieu = tfLieu != null ? tfLieu.getText() : null;
        if (lieu == null || lieu.isBlank()) {
            updateSuggestionLabel("Suggestion initiale", suggestedDateDeb.format(dateTimeFormat) + " - " + suggestedDateFin.toLocalTime().format(timeFormat));
            return;
        }

        updateSuggestionLabel("Analyse meteo", "Verification en cours pour " + lieu.trim() + "...");

        Thread worker = new Thread(() -> {
            WeatherService.WeatherSuggestion suggestion = weatherService.suggestBestSlot(
                    lieu.trim(),
                    LocalDateTime.now().plusHours(6)
            );

            Platform.runLater(() -> {
                if (suggestion == null) {
                    String reason = weatherService.getLastError();
                    if (reason == null || reason.isBlank()) {
                        reason = "Aucune suggestion disponible.";
                    }
                    updateSuggestionLabel("Suggestion meteo indisponible", reason);
                    return;
                }

                suggestedDateDeb = suggestion.getStart();
                suggestedDateFin = suggestion.getEnd();
                String range = suggestion.getStart().format(dateTimeFormat) + " - " + suggestion.getEnd().toLocalTime().format(timeFormat);
                updateSuggestionLabel(range, suggestion.getSummary() + " | " + suggestion.getRecommendation());
            });
        });
        worker.setDaemon(true);
        worker.start();
    }

    private void updateSuggestionLabel(String line1, String line2) {
        if (lblSuggestion == null) {
            return;
        }
        lblSuggestion.setText(line1 + "\n" + line2);
    }

    private String requireValue(String value, String error) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(error);
        }
        return value.trim();
    }

    private int parseInt(String value, String error) {
        try {
            return Integer.parseInt(requireValue(value, error));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(error);
        }
    }

    private LocalTime parseTime(String value, String error) {
        try {
            return LocalTime.parse(requireValue(value, error), timeFormat);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(error);
        }
    }

    private void goBackToManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/events_management.fxml"));
            Parent root = loader.load();

            EventsManagementController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) tfTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des evenements");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible de revenir a la gestion des evenements");
        }
    }

    private String sendRegionalSmsNotifications(Event event) {
        if (event == null || event.getLieu() == null || event.getLieu().isBlank()) {
            return "Evenement cree. SMS non envoye: lieu/region vide.";
        }

        if (!smsService.isConfigured()) {
            System.err.println("SMS non configure: verifier TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER dans l'environnement du processus Java.");
            return "Evenement cree. SMS non configure (SID/token manquants).";
        }

        try {
            System.out.println("SMS: recherche des citoyens pour la region/lieu = " + event.getLieu());
            List<User> citizens = userService.getCitoyensByRegion(event.getLieu());
            System.out.println("SMS: citoyens trouves = " + citizens.size());

            if (citizens.isEmpty()) {
                return "Evenement cree. Aucun citoyen cible trouve pour la region: " + event.getLieu();
            }

            int sentCount = smsService.notifyCitizensForNewEvent(event, citizens);
            System.out.println("SMS evenement envoyes: " + sentCount + " (region: " + event.getLieu() + ")");
            return "Evenement cree. SMS envoyes: " + sentCount + " / " + citizens.size() + " (region: " + event.getLieu() + ")";
        } catch (Exception ex) {
            System.err.println("Echec envoi SMS evenement: " + ex.getMessage());
            return "Evenement cree. Echec notification SMS: " + ex.getMessage();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Erreur");
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText("Information");
        alert.showAndWait();
    }

    private void showFormError(String message) {
        lblFormError.setText(message);
        lblFormError.setVisible(true);
        lblFormError.setManaged(true);
    }

    private void hideFormError() {
        lblFormError.setVisible(false);
        lblFormError.setManaged(false);
        lblFormError.setText("");
    }
}
