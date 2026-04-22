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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarMunicipalController;
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
import java.util.Optional;

public class MunicipalPointsController {

    @FXML private NavbarMunicipalController navbarIncludeController;

    @FXML private Label lblDelegation;
    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblInProgress;
    @FXML private Label lblCollected;
    @FXML private Label lblAssigned;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<String> cbPriority;

    @FXML private TableView<PointRecyclage> tablePoints;
    @FXML private TableColumn<PointRecyclage, Integer> colId;
    @FXML private TableColumn<PointRecyclage, String> colCitizen;
    @FXML private TableColumn<PointRecyclage, String> colCategory;
    @FXML private TableColumn<PointRecyclage, String> colQuantity;
    @FXML private TableColumn<PointRecyclage, String> colDate;
    @FXML private TableColumn<PointRecyclage, String> colStatus;
    @FXML private TableColumn<PointRecyclage, String> colPriority;
    @FXML private TableColumn<PointRecyclage, String> colAssignment;
    @FXML private TableColumn<PointRecyclage, Void> colActions;

    private User loggedUser;

    private final PointRecyclageService pointService = new PointRecyclageService();

    private final ObservableList<PointRecyclage> masterList = FXCollections.observableArrayList();
    private final ObservableList<PointRecyclage> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "Tous", "PENDING", "IN_PROGRESS", "COLLECTE", "VALIDE", "REFUSE"
        ));
        cbStatus.setValue("Tous");

        cbPriority.setItems(FXCollections.observableArrayList(
                "Toutes", "LOW", "MEDIUM", "HIGH", "URGENT", "None"
        ));
        cbPriority.setValue("Toutes");

        initTable();
        tablePoints.setPlaceholder(new Label("Aucun point de recyclage trouvé."));
        tablePoints.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbPriority.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user != null) {
            lblDelegation.setText("Délégation : " + safe(user.getDelegation()));
        } else {
            lblDelegation.setText("Délégation : -");
        }

        if (navbarIncludeController != null) {
            navbarIncludeController.setLoggedUser(user);
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



        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDateDec() != null ? data.getValue().getDateDec().toString() : "-"
        ));

        colStatus.setCellValueFactory(data -> new SimpleStringProperty(
                safe(data.getValue().getStatut())
        ));

        colPriority.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAiPriority() != null ? data.getValue().getAiPriority() : "None"
        ));

        colAssignment.setCellValueFactory(data -> {
            if (data.getValue().getAgentTerrain() != null) {
                return new SimpleStringProperty("Assigned: " + safe(data.getValue().getAgentTerrain().getName()));
            }
            return new SimpleStringProperty("Not assigned");
        });

        styleStatusColumn();
        stylePriorityColumn();
        addActionsColumn();

        tablePoints.setItems(filteredList);
    }

    private void styleStatusColumn() {
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null || statut.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(statut.toUpperCase());
                badge.setStyle(getStatusBadgeStyle(statut));
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void stylePriorityColumn() {
        colPriority.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);

                if (empty || priority == null || priority.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(priority.toUpperCase());
                badge.setStyle(getPriorityBadgeStyle(priority));
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnOpen = new Button("Open");
            private final Button btnRefuse = new Button("Refuse");
            private final HBox box = new HBox(8, btnOpen, btnRefuse);

            {
                btnOpen.setStyle("-fx-background-color: #eef7ee; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                btnRefuse.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");

                btnOpen.setOnAction(e -> {
                    PointRecyclage point = getTableView().getItems().get(getIndex());
                    handleOpen(point);
                });

                btnRefuse.setOnAction(e -> {
                    PointRecyclage point = getTableView().getItems().get(getIndex());
                    handleRefuse(point);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                PointRecyclage point = getTableView().getItems().get(getIndex());

                if (point == null) {
                    setGraphic(null);
                    return;
                }

                String statut = safe(point.getStatut()).toUpperCase();
                boolean canRefuse = !statut.equals("COLLECTE")
                        && !statut.equals("VALIDE")
                        && !statut.equals("REFUSE");

                btnRefuse.setVisible(canRefuse);
                btnRefuse.setManaged(canRefuse);

                setGraphic(box);
            }
        });
    }

    private void loadPoints() {
        if (loggedUser == null) return;

        try {
            List<PointRecyclage> points = pointService.getPointsForMunicipal(loggedUser);
            masterList.setAll(points);
            applyFilters();
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les points.");
        }
    }

    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String selectedStatus = cbStatus.getValue();
        String selectedPriority = cbPriority.getValue();

        List<PointRecyclage> result = new ArrayList<>();

        for (PointRecyclage p : masterList) {
            if (matchesSearch(p, keyword) && matchesStatus(p, selectedStatus) && matchesPriority(p, selectedPriority)) {
                result.add(p);
            }
        }

        filteredList.setAll(result);
        updateAssignedLabel();
    }

    private boolean matchesSearch(PointRecyclage p, String search) {
        if (search.isEmpty()) return true;

        String citizen = p.getCitoyen() != null ? safe(p.getCitoyen().getName()) : "";
        String category = p.getCategorie() != null ? safe(p.getCategorie().getNom()) : "";
        String address = safe(p.getAddress());
        String status = safe(p.getStatut());
        String priority = p.getAiPriority() != null ? p.getAiPriority() : "None";

        String all = (citizen + " " + category + " " + address + " " + status + " " + priority).toLowerCase();
        return all.contains(search);
    }

    private boolean matchesStatus(PointRecyclage p, String selectedStatus) {
        return selectedStatus == null
                || selectedStatus.equals("Tous")
                || safe(p.getStatut()).equalsIgnoreCase(selectedStatus);
    }

    private boolean matchesPriority(PointRecyclage p, String selectedPriority) {
        if (selectedPriority == null || selectedPriority.equals("Toutes")) return true;

        String priority = p.getAiPriority() != null ? p.getAiPriority() : "None";
        return priority.equalsIgnoreCase(selectedPriority);
    }

    private void updateStats() {
        lblTotal.setText(String.valueOf(masterList.size()));
        lblPending.setText(String.valueOf(masterList.stream().filter(p -> "PENDING".equalsIgnoreCase(safe(p.getStatut()))).count()));
        lblInProgress.setText(String.valueOf(masterList.stream().filter(p -> "IN_PROGRESS".equalsIgnoreCase(safe(p.getStatut()))).count()));
        lblCollected.setText(String.valueOf(masterList.stream().filter(p -> "COLLECTE".equalsIgnoreCase(safe(p.getStatut()))).count()));
        updateAssignedLabel();
    }

    private void updateAssignedLabel() {
        long assigned = filteredList.stream()
                .filter(p -> p.getAgentTerrain() != null)
                .count();

        lblAssigned.setText("Assigned: " + assigned + "/" + filteredList.size());
    }

    private void handleOpen(PointRecyclage point) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/municipal_point_details.fxml"));
            Parent root = loader.load();

            MunicipalPointDetailsController controller = loader.getController();
            controller.setData(loggedUser, point);

            Stage stage = (Stage) tablePoints.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du point");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails du point.");
        }
    }

    private void handleRefuse(PointRecyclage point) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Refuser le point");
        confirm.setContentText("Voulez-vous vraiment refuser ce point ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                pointService.refusePointByMunicipal(point.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Point refusé avec succès.");
                loadPoints();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de refuser ce point.");
            }
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

        String delegation = loggedUser != null && loggedUser.getDelegation() != null && !loggedUser.getDelegation().isBlank()
                ? loggedUser.getDelegation().replaceAll("[^a-zA-Z0-9-]", "")
                : "delegation";

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        fileChooser.setInitialFileName("municipal_points_" + delegation + "_" + timestamp + ".pdf");

        File file = fileChooser.showSaveDialog(tablePoints.getScene().getWindow());
        if (file == null) return;

        Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph title = new Paragraph("Municipal Recycling Points Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10f);
            document.add(title);

            Paragraph subtitle = new Paragraph(
                    "Delegation: " + (loggedUser != null ? safe(loggedUser.getDelegation()) : "-") +
                            " | Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    normalFont
            );
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(14f);
            document.add(subtitle);

            Paragraph stats = new Paragraph(
                    "Total: " + lblTotal.getText() +
                            " | Pending: " + lblPending.getText() +
                            " | In progress: " + lblInProgress.getText() +
                            " | Collected: " + lblCollected.getText() +
                            " | Assigned: " + lblAssigned.getText(),
                    normalFont
            );
            stats.setSpacingAfter(12f);
            document.add(stats);

            Paragraph filters = new Paragraph(
                    "Filters -> Search: " + safe(txtSearch.getText()) +
                            " | Status: " + safe(cbStatus.getValue()) +
                            " | Priority: " + safe(cbPriority.getValue()),
                    smallFont
            );
            filters.setSpacingAfter(12f);
            document.add(filters);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 1.6f, 1.3f, 1f, 3f, 1.2f, 1.3f, 1.6f});

            addHeaderCell(table, "#");
            addHeaderCell(table, "Citizen");
            addHeaderCell(table, "Category");
            addHeaderCell(table, "Quantity");
            addHeaderCell(table, "Address");
            addHeaderCell(table, "Date");
            addHeaderCell(table, "Status");
            addHeaderCell(table, "Assignment");

            for (PointRecyclage p : filteredList) {
                addBodyCell(table, String.valueOf(p.getId()));
                addBodyCell(table, p.getCitoyen() != null ? safe(p.getCitoyen().getName()) : "-");
                addBodyCell(table, p.getCategorie() != null ? safe(p.getCategorie().getNom()) : "-");
                addBodyCell(table, p.getQuantite() + " kg");
                addBodyCell(table, safe(p.getAddress()));
                addBodyCell(table, p.getDateDec() != null ? p.getDateDec().toString() : "-");
                addBodyCell(table, safe(p.getStatut()));
                addBodyCell(table, p.getAgentTerrain() != null ? safe(p.getAgentTerrain().getName()) : "Not assigned");
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

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String getStatusBadgeStyle(String statut) {
        String s = safe(statut).toUpperCase();

        String base = "-fx-padding: 6 12; "
                + "-fx-background-radius: 14; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 12px;";

        return switch (s) {
            case "PENDING" -> base + "-fx-background-color: #facc15; -fx-text-fill: #111827;";
            case "COLLECTE" -> base + "-fx-background-color: #16a34a; -fx-text-fill: white;";
            case "IN_PROGRESS" -> base + "-fx-background-color: #2563eb; -fx-text-fill: white;";
            case "REFUSE" -> base + "-fx-background-color: #ef4444; -fx-text-fill: white;";
            case "VALIDE" -> base + "-fx-background-color: #7c3aed; -fx-text-fill: white;";
            default -> base + "-fx-background-color: #9ca3af; -fx-text-fill: white;";
        };
    }

    private String getPriorityBadgeStyle(String priority) {
        String s = safe(priority).toUpperCase();

        String base = "-fx-padding: 6 12; "
                + "-fx-background-radius: 14; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 12px;";

        return switch (s) {
            case "LOW" -> base + "-fx-background-color: #dcfce7; -fx-text-fill: #166534;";
            case "MEDIUM" -> base + "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;";
            case "HIGH" -> base + "-fx-background-color: #fed7aa; -fx-text-fill: #9a3412;";
            case "URGENT" -> base + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;";
            default -> base + "-fx-background-color: #e5e7eb; -fx-text-fill: #374151;";
        };
    }
}