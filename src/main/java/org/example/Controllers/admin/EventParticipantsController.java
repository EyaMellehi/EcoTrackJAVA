package org.example.Controllers.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.example.Entities.Event;
import org.example.Services.ParticipationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventParticipantsController {

    @FXML
    private Label lblBreadcrumbEvent;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblInfo;
    @FXML
    private Label lblEventMeta;
    @FXML
    private TableView<ParticipationService.EventParticipantItem> tableParticipants;
    @FXML
    private TableColumn<ParticipationService.EventParticipantItem, String> colIndex;
    @FXML
    private TableColumn<ParticipationService.EventParticipantItem, String> colName;
    @FXML
    private TableColumn<ParticipationService.EventParticipantItem, String> colEmail;
    @FXML
    private TableColumn<ParticipationService.EventParticipantItem, String> colPhone;
    @FXML
    private TableColumn<ParticipationService.EventParticipantItem, String> colStatus;
    @FXML
    private TableColumn<ParticipationService.EventParticipantItem, String> colDate;
    @FXML
    private TableColumn<ParticipationService.EventParticipantItem, Void> colActions;

    private final ParticipationService participationService = new ParticipationService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Event currentEvent;

    @FXML
    public void initialize() {
        if (tableParticipants != null) {
            tableParticipants.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        colIndex.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName() == null ? "—" : cell.getValue().getName()));
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail() == null ? "—" : cell.getValue().getEmail()));
        colPhone.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone() == null ? "—" : cell.getValue().getPhone()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus() == null ? "inscrit" : cell.getValue().getStatus()));
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getParticipationDate() == null ? "—" : cell.getValue().getParticipationDate().format(formatter)
        ));
        // Ensure action cells are instantiated even if there is no backing model property.
        colActions.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(null));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                String normalized = status.toLowerCase();
                Label badge = new Label(status);
                badge.getStyleClass().add("event-badge");
                if ("present".equals(normalized)) {
                    badge.getStyleClass().add("event-badge-present");
                } else if ("absent".equals(normalized)) {
                    badge.getStyleClass().add("event-badge-absent");
                } else {
                    badge.getStyleClass().add("event-badge-default");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final TextField txtCode = new TextField();
            private final Button btnPresent = new Button("Valider le code");
            private final Button btnAbsent = new Button("Absent");
            private final HBox actions = new HBox(8, txtCode, btnPresent, btnAbsent);

            {
                txtCode.setPromptText("PIN 5 chiffres");
                txtCode.setPrefWidth(130);
                btnPresent.getStyleClass().add("event-primary-button");
                btnAbsent.getStyleClass().addAll("event-primary-button", "btn-danger");
                btnPresent.setMinWidth(130);
                btnPresent.setPrefWidth(130);
                btnAbsent.setMinWidth(80);
                btnAbsent.setPrefWidth(80);

                btnPresent.setOnAction(e -> {
                    ParticipationService.EventParticipantItem item = getCurrentItem();
                    if (item != null) {
                        updateStatus(item, "present", txtCode.getText());
                    }
                });

                btnAbsent.setOnAction(e -> {
                    ParticipationService.EventParticipantItem item = getCurrentItem();
                    if (item != null) {
                        updateStatus(item, "absent", null);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                ParticipationService.EventParticipantItem participant = getCurrentItem();
                if (participant == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                boolean canMark = canMarkAttendance();
                if (!canMark) {
                    setGraphic(null);
                    setText("Disponible apres fin");
                    return;
                }

                String status = normalizeStatus(participant.getStatus());
                btnPresent.setDisable("present".equals(status));
                btnAbsent.setDisable("absent".equals(status));
                txtCode.setDisable("present".equals(status));
                txtCode.setText("");
                setGraphic(actions);
                setText(null);
            }

            private ParticipationService.EventParticipantItem getCurrentItem() {
                if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(getIndex());
            }
        });
    }

    public void loadForEvent(Event event) {
        this.currentEvent = event;
        int eventId = event.getId();
        String eventTitle = event.getTitre();

        lblTitle.setText("Participations");
        lblBreadcrumbEvent.setText(eventTitle == null ? "Evenement" : eventTitle);
        String metaDate = event.getDateDeb() != null ? event.getDateDeb().format(formatter) : "";
        lblEventMeta.setText("Événement : " + eventTitle + " (" + (event.getLieu() == null ? "—" : event.getLieu()) + ", " + metaDate + ")");

        if (colActions != null) {
            colActions.setMinWidth(380);
            colActions.setPrefWidth(410);
        }

        List<ParticipationService.EventParticipantItem> participants = participationService.getEventParticipants(eventId);
        tableParticipants.setItems(FXCollections.observableArrayList(participants));

        if (!participants.isEmpty()) {
            String suffix = canMarkAttendance()
                    ? " - Vous pouvez marquer Present/Absent (PIN 5 chiffres requis pour Present)."
                    : " - Marquage disponible apres la date de fin de l'evenement.";
            lblInfo.setText("Total: " + participants.size() + " citoyen(s)" + suffix);
        } else {
            String err = participationService.getLastError();
            if (err != null && !err.isBlank()) {
                lblInfo.setText("Aucun participant affiche. Detail: " + err);
            } else {
                lblInfo.setText("Aucun citoyen inscrit pour cet evenement.");
            }
        }

        if (tableParticipants != null) {
            tableParticipants.refresh();
        }
    }

    private void updateStatus(ParticipationService.EventParticipantItem participant, String status, String attendanceCode) {
        if (!canMarkAttendance()) {
            showMessage(Alert.AlertType.WARNING, "Marquage indisponible", "Le marquage de presence est autorise uniquement apres la fin de l'evenement.");
            return;
        }

        if ("present".equals(status)) {
            String normalizedCode = attendanceCode == null ? "" : attendanceCode.trim();
            if (normalizedCode.isBlank()) {
                showMessage(Alert.AlertType.WARNING, "Code invalide", "Saisissez le code presence a 5 chiffres avant de marquer Present.");
                return;
            }
            if (!normalizedCode.matches("\\d{5}")) {
                showMessage(Alert.AlertType.WARNING, "Code invalide", "Entrez uniquement un PIN a 5 chiffres.");
                return;
            }
            attendanceCode = normalizedCode;
        }

        boolean ok = participationService.updateParticipationStatus(currentEvent, participant.getUserId(), status, attendanceCode);
        if (!ok) {
            String error = participationService.getLastError();
            showMessage(Alert.AlertType.ERROR, "Erreur", error == null || error.isBlank() ? "Impossible de mettre a jour le statut." : error);
            return;
        }

        loadForEvent(currentEvent);
    }

    private boolean canMarkAttendance() {
        return currentEvent != null
                && currentEvent.getDateFin() != null
                && LocalDateTime.now().isAfter(currentEvent.getDateFin());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "inscrit";
        }
        return status.trim().toLowerCase();
    }

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }
}

