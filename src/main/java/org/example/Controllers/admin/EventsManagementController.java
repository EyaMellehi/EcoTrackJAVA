package org.example.Controllers.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Controllers.HomeConnectedController;
import org.example.Entities.Event;
import org.example.Entities.User;
import org.example.Services.EventService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventsManagementController {

    @FXML
    private Button btnHome;
    @FXML
    private TextField tfSearch;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private ComboBox<String> cbSort;
    @FXML
    private ComboBox<String> cbOrder;
    @FXML
    private TableView<Event> tableEvents;
    @FXML
    private TableColumn<Event, Number> colId;
    @FXML
    private TableColumn<Event, String> colTitre;
    @FXML
    private TableColumn<Event, String> colLieu;
    @FXML
    private TableColumn<Event, String> colDateDeb;
    @FXML
    private TableColumn<Event, String> colStatut;
    @FXML
    private TableColumn<Event, String> colParticipants;
    @FXML
    private TableColumn<Event, Number> colCreateur;
    @FXML
    private TableColumn<Event, Void> colActions;

    private final EventService eventService = new EventService();
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private User loggedUser;

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        loadEvents();
    }

    @FXML
    public void initialize() {
        if (btnHome != null) {
            btnHome.setOnAction(e -> goHome());
        }
        cbStatut.setItems(FXCollections.observableArrayList("Tous", "brouillon", "publie", "termine", "annule"));
        cbSort.setItems(FXCollections.observableArrayList("date_deb", "titre", "lieu"));
        cbOrder.setItems(FXCollections.observableArrayList("ASC", "DESC"));

        cbStatut.setValue("Tous");
        cbSort.setValue("date_deb");
        cbOrder.setValue("ASC");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colCreateur.setCellValueFactory(new PropertyValueFactory<>("createurId"));

        colDateDeb.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateDeb() == null) {
                return new SimpleStringProperty("-");
            }
            return new SimpleStringProperty(cellData.getValue().getDateDeb().format(dateFormat));
        });

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colParticipants.setCellValueFactory(cellData -> {
            Event event = cellData.getValue();
            String value = String.valueOf(event.getParticipationCount());
            if (event.getCapaciteMax() > 0) {
                value += " / " + event.getCapaciteMax();
            }
            return new SimpleStringProperty(value);
        });

        configureActionsColumn();
        loadEvents();
    }

    @FXML
    private void filterEvents() {
        loadEvents();
    }

    @FXML
    private void openCreateForm() {
        openEventForm(null);
    }

    @FXML
    private void goBackToEventsPage() {
        loadScene("/events.fxml", "Events");
    }

    @FXML
    private void goHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
            Parent root = loader.load();

            HomeConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) tableEvents.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation impossible vers Home");
        }
    }

    private void loadEvents() {
        String query = tfSearch.getText() == null ? "" : tfSearch.getText().trim();
        String statut = cbStatut.getValue();
        String sort = cbSort.getValue();
        String order = cbOrder.getValue();

        String regionFilter = null;
        if (loggedUser != null && loggedUser.getRoles() != null && loggedUser.getRoles().contains("ROLE_AGENT_MUNICIPAL")
                && loggedUser.getRegion() != null && !loggedUser.getRegion().isBlank()) {
            regionFilter = loggedUser.getRegion();
        }

        List<Event> events = eventService.searchEventsForManagement(query, statut, sort, order, regionFilter);
        ObservableList<Event> list = FXCollections.observableArrayList(events);
        tableEvents.setItems(list);
    }

    private void configureActionsColumn() {
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final Button btnParticipants = new Button("Participants");
            private final HBox box = new HBox(8, btnParticipants, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("event-primary-button");
                btnDelete.getStyleClass().addAll("event-primary-button", "btn-danger");
                btnParticipants.getStyleClass().add("event-secondary-button");

                btnEdit.setOnAction(event -> {
                    Event selected = getTableView().getItems().get(getIndex());
                    openEventForm(selected);
                });

                btnParticipants.setOnAction(event -> {
                    Event selected = getTableView().getItems().get(getIndex());
                    openParticipantsDialog(selected);
                });

                btnDelete.setOnAction(event -> {
                    Event selected = getTableView().getItems().get(getIndex());
                    confirmAndDelete(selected);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void confirmAndDelete(Event event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cet evenement ?\n\n" + event.getTitre(),
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation suppression");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                boolean deleted = eventService.deleteEvent(event.getId());
                if (deleted) {
                    showInfo("Evenement supprime.");
                    loadEvents();
                } else {
                    String detail = eventService.getLastDeleteError();
                    if (detail == null || detail.isBlank()) {
                        showError("La suppression a echoue.");
                    } else {
                        showError("La suppression a echoue.\n\n" + detail);
                    }
                }
            }
        });
    }

    private void openEventForm(Event eventToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/event_form.fxml"));
            Parent root = loader.load();

            EventFormController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setEventToEdit(eventToEdit);

            Stage stage = (Stage) tableEvents.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(eventToEdit == null ? "Nouvel evenement" : "Modifier evenement");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir le formulaire evenement.");
        }
    }

    private void openParticipantsDialog(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/event_participants.fxml"));
            Parent root = loader.load();

            EventParticipantsController controller = loader.getController();
            controller.loadForEvent(event);

            Stage dialog = new Stage();
            dialog.initOwner(tableEvents.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Participants - " + event.getTitre());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir la liste des participants.");
        }
    }

    private void loadScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) tableEvents.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation impossible vers " + title);
        }
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText("Erreur");
        alert.showAndWait();
    }
}

