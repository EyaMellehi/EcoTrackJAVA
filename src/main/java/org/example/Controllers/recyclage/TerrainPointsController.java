
package org.example.Controllers.recyclage;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TerrainPointsController {

    @FXML private NavbarCitoyenController navbarCitoyenController;

    @FXML private Label lblAgentName;
    @FXML private Label lblTotal;
    @FXML private Label lblInProgress;
    @FXML private Label lblCollected;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatus;

    @FXML private TableView<PointRecyclage> tablePoints;
    @FXML private TableColumn<PointRecyclage, Integer> colId;
    @FXML private TableColumn<PointRecyclage, String> colCitizen;
    @FXML private TableColumn<PointRecyclage, String> colCategory;
    @FXML private TableColumn<PointRecyclage, String> colQuantity;
    @FXML private TableColumn<PointRecyclage, String> colAddress;
    @FXML private TableColumn<PointRecyclage, String> colDate;
    @FXML private TableColumn<PointRecyclage, String> colStatus;
    @FXML private TableColumn<PointRecyclage, Void> colActions;

    private User loggedUser;
    private final PointRecyclageService pointService = new PointRecyclageService();

    private final ObservableList<PointRecyclage> masterList = FXCollections.observableArrayList();
    private final ObservableList<PointRecyclage> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "All statuses", "IN_PROGRESS", "COLLECTE", "VALIDE"
        ));
        cbStatus.setValue("All statuses");

        initTable();
        tablePoints.setPlaceholder(new Label("Aucun point affecté trouvé."));
        tablePoints.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (navbarCitoyenController != null) {
            navbarCitoyenController.setLoggedUser(user);
        }

        if (user != null) {
            lblAgentName.setText("Agent terrain : " + safe(user.getName()));
        }

        loadPoints();
    }

    private void initTable() {
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));

        colCitizen.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCitoyen() != null ? safe(data.getValue().getCitoyen().getName()) : "-"
        ));

        colCategory.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCategorie() != null ? safe(data.getValue().getCategorie().getNom()) : "-"
        ));

        colQuantity.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getQuantite() + " kg"
        ));

        colAddress.setCellValueFactory(data -> new SimpleStringProperty(
                safe(data.getValue().getAddress())
        ));

        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDateDec() != null ? data.getValue().getDateDec().toString() : ""
        ));

        colStatus.setCellValueFactory(data -> new SimpleStringProperty(
                safe(data.getValue().getStatut())
        ));

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null || statut.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(statut.toUpperCase());
                badge.setStyle(getStatusBadgeStyle(statut));

                setGraphic(badge);
                setText(null);
            }
        });

        addActionsColumn();
        tablePoints.setItems(filteredList);
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnOpen = new Button("Ouvrir");
            private final HBox pane = new HBox(8, btnOpen);

            {
                btnOpen.setStyle(
                        "-fx-background-color: #eef7ee; " +
                                "-fx-text-fill: #2e7d32; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 8;"
                );

                btnOpen.setOnAction(event -> {
                    PointRecyclage point = getTableView().getItems().get(getIndex());
                    handleOpen(point);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadPoints() {
        if (loggedUser == null) return;

        try {
            List<PointRecyclage> points = pointService.getPointsForFieldAgent(loggedUser);
            masterList.setAll(points);
            applyFilters();
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les points.");
        }
    }

    private void applyFilters() {
        String search = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        String selectedStatus = cbStatus.getValue();

        List<PointRecyclage> result = new ArrayList<>();

        for (PointRecyclage p : masterList) {
            boolean matchesSearch = matchesSearch(p, search);
            boolean matchesStatus = matchesStatus(p, selectedStatus);

            if (matchesSearch && matchesStatus) {
                result.add(p);
            }
        }

        filteredList.setAll(result);
    }

    private boolean matchesSearch(PointRecyclage p, String search) {
        if (search.isEmpty()) return true;

        String citizen = p.getCitoyen() != null ? safe(p.getCitoyen().getName()) : "";
        String category = p.getCategorie() != null ? safe(p.getCategorie().getNom()) : "";
        String address = safe(p.getAddress());
        String status = safe(p.getStatut());

        String all = (citizen + " " + category + " " + address + " " + status).toLowerCase();
        return all.contains(search);
    }

    private boolean matchesStatus(PointRecyclage p, String selectedStatus) {
        if (selectedStatus == null || selectedStatus.equals("All statuses")) return true;
        return safe(p.getStatut()).equalsIgnoreCase(selectedStatus);
    }

    private void updateStats() {
        int total = masterList.size();
        int inProgress = 0;
        int collected = 0;

        for (PointRecyclage p : masterList) {
            String s = safe(p.getStatut()).toUpperCase();
            if (s.equals("IN_PROGRESS")) inProgress++;
            if (s.equals("COLLECTE")) collected++;
        }

        lblTotal.setText(String.valueOf(total));
        lblInProgress.setText(String.valueOf(inProgress));
        lblCollected.setText(String.valueOf(collected));
    }

    private void handleOpen(PointRecyclage point) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/terrain_point_details.fxml"));
            Parent root = loader.load();

            TerrainPointDetailsController controller = loader.getController();
            controller.setData(loggedUser, point);

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Point details");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails du point.");
        }
    }

    @FXML
    private void refreshTable() {
        loadPoints();
    }

    @FXML
    private void exportPdf() {
        if (filteredList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune donnée", "Il n'y a aucun point à exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf")
        );

        String agentName = loggedUser != null && loggedUser.getName() != null && !loggedUser.getName().isBlank()
                ? loggedUser.getName().replaceAll("[^a-zA-Z0-9-_]", "_")
                : "agent";

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        fileChooser.setInitialFileName("terrain_points_" + agentName + "_" + timestamp + ".pdf");

        File file = fileChooser.showSaveDialog(tablePoints.getScene().getWindow());
        if (file == null) {
            return;
        }

        Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph title = new Paragraph("Terrain Agent Assigned Points Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10f);
            document.add(title);

            Paragraph subtitle = new Paragraph(
                    "Agent: " + (loggedUser != null ? safe(loggedUser.getName()) : "-") +
                            " | Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    normalFont
            );
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(14f);
            document.add(subtitle);

            Paragraph stats = new Paragraph(
                    "Total: " + lblTotal.getText() +
                            " | In progress: " + lblInProgress.getText() +
                            " | Collected: " + lblCollected.getText(),
                    normalFont
            );
            stats.setSpacingAfter(12f);
            document.add(stats);

            Paragraph filters = new Paragraph(
                    "Filters -> Search: " + safe(txtSearch.getText()) +
                            " | Status: " + safe(cbStatus.getValue()),
                    smallFont
            );
            filters.setSpacingAfter(12f);
            document.add(filters);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 1.6f, 1.4f, 1.1f, 3.2f, 1.2f, 1.3f});

            addHeaderCell(table, "#");
            addHeaderCell(table, "Citizen");
            addHeaderCell(table, "Category");
            addHeaderCell(table, "Quantity");
            addHeaderCell(table, "Address");
            addHeaderCell(table, "Date");
            addHeaderCell(table, "Status");

            for (PointRecyclage p : filteredList) {
                addBodyCell(table, String.valueOf(p.getId()));
                addBodyCell(table, p.getCitoyen() != null ? safe(p.getCitoyen().getName()) : "-");
                addBodyCell(table, p.getCategorie() != null ? safe(p.getCategorie().getNom()) : "-");
                addBodyCell(table, p.getQuantite() + " kg");
                addBodyCell(table, safe(p.getAddress()));
                addBodyCell(table, p.getDateDec() != null ? p.getDateDec().toString() : "-");
                addBodyCell(table, safe(p.getStatut()));
            }

            document.add(table);
            document.close();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF exporté avec succès.");

        } catch (Exception e) {
            e.printStackTrace();
            if (document.isOpen()) {
                document.close();
            }
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'exporter le PDF.");
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new java.awt.Color(46, 125, 50));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8f);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9);
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setPadding(7f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private String getStatusBadgeStyle(String statut) {
        String s = safe(statut).toUpperCase();

        String base = "-fx-padding: 6 14; "
                + "-fx-background-radius: 14; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 12px;";

        return switch (s) {
            case "IN_PROGRESS" -> base + "-fx-background-color: #2563eb; -fx-text-fill: white;";
            case "COLLECTE" -> base + "-fx-background-color: #16a34a; -fx-text-fill: white;";
            case "VALIDE" -> base + "-fx-background-color: #7c3aed; -fx-text-fill: white;";
            default -> base + "-fx-background-color: #9ca3af; -fx-text-fill: white;";
        };
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
