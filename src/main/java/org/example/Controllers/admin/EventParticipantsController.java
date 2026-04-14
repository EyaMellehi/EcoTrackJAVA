package org.example.Controllers.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.example.Entities.Event;
import org.example.Services.ParticipationService;

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

    private final ParticipationService participationService = new ParticipationService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
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
                Text badge = new Text(status);
                badge.setStyle("-fx-font-weight: bold;");
                if ("present".equals(normalized)) {
                    badge.setFill(Color.web("#15803d"));
                } else if ("absent".equals(normalized)) {
                    badge.setFill(Color.web("#b91c1c"));
                } else {
                    badge.setFill(Color.web("#1d4ed8"));
                }
                setGraphic(badge);
                setText(null);
            }
        });
    }

    public void loadForEvent(Event event) {
        int eventId = event.getId();
        String eventTitle = event.getTitre();

        lblTitle.setText("Participations");
        lblBreadcrumbEvent.setText(eventTitle == null ? "Evenement" : eventTitle);
        String metaDate = event.getDateDeb() != null ? event.getDateDeb().format(formatter) : "";
        lblEventMeta.setText("Événement : " + eventTitle + " (" + (event.getLieu() == null ? "—" : event.getLieu()) + ", " + metaDate + ")");

        List<ParticipationService.EventParticipantItem> participants = participationService.getEventParticipants(eventId);
        tableParticipants.setItems(FXCollections.observableArrayList(participants));

        if (!participants.isEmpty()) {
            lblInfo.setText("Total: " + participants.size() + " citoyen(s)");
        } else {
            String err = participationService.getLastError();
            if (err != null && !err.isBlank()) {
                lblInfo.setText("Aucun participant affiche. Detail: " + err);
            } else {
                lblInfo.setText("Aucun citoyen inscrit pour cet evenement.");
            }
        }
    }
}

