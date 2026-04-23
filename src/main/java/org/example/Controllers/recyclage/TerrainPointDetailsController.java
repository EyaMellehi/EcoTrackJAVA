package org.example.Controllers.recyclage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Entities.PointRecyclage;
import org.example.Entities.RapportRecyc;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;
import org.example.Services.RapportRecycService;

import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;

public class TerrainPointDetailsController {

    @FXML private NavbarCitoyenController navbarCitoyenController;

    @FXML private Label lblPointIdTop;
    @FXML private Label lblPointTitle;
    @FXML private Label lblAddress;
    @FXML private Label lblStatus;
    @FXML private Label lblCategory;
    @FXML private Label lblQuantity;
    @FXML private Label lblDate;
    @FXML private Label lblDescription;
    @FXML private Label lblAiPriority;
    @FXML private Label lblAiScore;
    @FXML private Label lblAiExplanation;
    @FXML private Label lblCoordinates;
    @FXML private Label lblFieldAgent;

    @FXML private Button btnCreateReport;
    @FXML private Button btnViewReport;

    @FXML private WebView webMap;

    private final PointRecyclageService pointService = new PointRecyclageService();
    private final RapportRecycService rapportService = new RapportRecycService();

    private User loggedUser;
    private PointRecyclage currentPoint;
    private RapportRecyc currentRapport;

    public void setData(User loggedUser, PointRecyclage point) {
        this.loggedUser = loggedUser;
        this.currentPoint = point;

        if (navbarCitoyenController != null) {
            navbarCitoyenController.setLoggedUser(loggedUser);
        }

        loadData();
    }

    private void loadData() {
        try {
            currentPoint = pointService.getPointById(currentPoint.getId());
            currentRapport = rapportService.getRapportByPointId(currentPoint.getId());

            lblPointIdTop.setText("Point: #" + currentPoint.getId());
            lblPointTitle.setText("Point #" + currentPoint.getId());
            lblAddress.setText(safe(currentPoint.getAddress()).isEmpty() ? "-" : safe(currentPoint.getAddress()));

            lblStatus.setText(safe(currentPoint.getStatut()).isEmpty() ? "-" : safe(currentPoint.getStatut()));
            applyStatusStyle();

            lblCategory.setText(currentPoint.getCategorie() != null ? safe(currentPoint.getCategorie().getNom()) : "-");
            lblQuantity.setText(currentPoint.getQuantite() + " kg");
            lblDate.setText(currentPoint.getDateDec() != null ? currentPoint.getDateDec().toString() : "-");
            lblDescription.setText(safe(currentPoint.getDescription()).isEmpty() ? "-" : safe(currentPoint.getDescription()));

            String aiPriority = currentPoint.getAiPriority() != null ? currentPoint.getAiPriority() : "None";
            lblAiPriority.setText(aiPriority);
            applyAiPriorityStyle(aiPriority);

            lblAiScore.setText(currentPoint.getAiScore() != null ? String.valueOf(currentPoint.getAiScore()) + "/100" : "-");
            lblAiExplanation.setText(
                    safe(currentPoint.getAiExplanation()).isEmpty()
                            ? "Aucune explication IA disponible."
                            : safe(currentPoint.getAiExplanation())
            );

            lblCoordinates.setText(currentPoint.getLatitude() + ", " + currentPoint.getLongitude());

            if (currentPoint.getAgentTerrain() != null) {
                String agentName = safe(currentPoint.getAgentTerrain().getName());
                String agentEmail = safe(currentPoint.getAgentTerrain().getEmail());

                if (!agentName.isEmpty() && !agentEmail.isEmpty()) {
                    lblFieldAgent.setText(agentName + " (" + agentEmail + ")");
                } else if (!agentName.isEmpty()) {
                    lblFieldAgent.setText(agentName);
                } else if (!agentEmail.isEmpty()) {
                    lblFieldAgent.setText(agentEmail);
                } else {
                    lblFieldAgent.setText("Affecté");
                }
            } else {
                lblFieldAgent.setText("Non affecté");
            }

            boolean canCreateReport = currentRapport == null && "IN_PROGRESS".equalsIgnoreCase(safe(currentPoint.getStatut()));
            btnCreateReport.setVisible(canCreateReport);
            btnCreateReport.setManaged(canCreateReport);

            boolean canViewReport = currentRapport != null;
            btnViewReport.setVisible(canViewReport);
            btnViewReport.setManaged(canViewReport);

            loadMap();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails du point.");
        }
    }

    private void applyStatusStyle() {
        String s = safe(currentPoint.getStatut()).toUpperCase();
        String style = "-fx-padding: 6 14; -fx-background-radius: 18; -fx-font-weight: bold; -fx-text-fill: black;";

        if (s.equals("IN_PROGRESS")) {
            style += "-fx-background-color: #26c6da;";
        } else if (s.equals("COLLECTE")) {
            style += "-fx-background-color: #2e7d32; -fx-text-fill: white;";
        } else if (s.equals("PENDING")) {
            style += "-fx-background-color: #fbc02d;";
        } else {
            style += "-fx-background-color: #e0e0e0;";
        }

        lblStatus.setStyle(style);
    }

    private void applyAiPriorityStyle(String priority) {
        String p = safe(priority).toUpperCase();
        String base = "-fx-padding: 6 14; -fx-background-radius: 18; -fx-font-weight: bold;";

        switch (p) {
            case "LOW" -> lblAiPriority.setStyle(base + "-fx-background-color: #dcfce7; -fx-text-fill: #166534;");
            case "MEDIUM" -> lblAiPriority.setStyle(base + "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;");
            case "HIGH" -> lblAiPriority.setStyle(base + "-fx-background-color: #fed7aa; -fx-text-fill: #9a3412;");
            case "URGENT" -> lblAiPriority.setStyle(base + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;");
            default -> lblAiPriority.setStyle(base + "-fx-background-color: #eef2f7; -fx-text-fill: #475467;");
        }
    }

    private void loadMap() {
        webMap.setContextMenuEnabled(false);

        String html = String.format("""
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">

          <script>
            var L_DISABLE_3D = true;
          </script>

          <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
          <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

          <style>
            html, body {
              width: 100%%;
              height: 100%%;
              margin: 0;
              padding: 0;
              overflow: hidden;
              background: white;
            }

            #map {
              width: 100%%;
              height: 100%%;
              margin: 0;
              padding: 0;
              background: #e5e7eb;
            }

            .leaflet-container {
              width: 100%% !important;
              height: 100%% !important;
              background: #e5e7eb;
            }

            .leaflet-tile,
            .leaflet-pane,
            .leaflet-map-pane,
            .leaflet-tile-container,
            .leaflet-zoom-animated {
              transform: none !important;
              -webkit-transform: none !important;
            }
          </style>
        </head>
        <body>
          <div id="map"></div>

          <script>
            var lat = %f;
            var lng = %f;

            var map = L.map('map', {
              preferCanvas: true,
              zoomAnimation: false,
              fadeAnimation: false,
              markerZoomAnimation: false,
              inertia: false
            }).setView([lat, lng], 15);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
              maxZoom: 19,
              attribution: '&copy; OpenStreetMap contributors',
              updateWhenZooming: false,
              updateWhenIdle: true,
              keepBuffer: 1
            }).addTo(map);

            L.marker([lat, lng]).addTo(map);

            setTimeout(function () {
              map.invalidateSize();
            }, 300);
          </script>
        </body>
        </html>
        """, currentPoint.getLatitude(), currentPoint.getLongitude());

        WebEngine engine = webMap.getEngine();
        engine.loadContent(html);
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/terrain_points.fxml"));
            Parent root = loader.load();

            TerrainPointsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) lblPointTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes points affectés");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openGoogleMaps() {
        try {
            Desktop.getDesktop().browse(new URI(
                    "https://www.google.com/maps?q=" + currentPoint.getLatitude() + "," + currentPoint.getLongitude()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir Google Maps.");
        }
    }

    @FXML
    private void createReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/create_rapport_recyc.fxml"));
            Parent root = loader.load();

            CreateRapportRecycController controller = loader.getController();
            controller.setData(loggedUser, currentPoint);

            Stage stage = (Stage) lblPointTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer rapport");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page de création du rapport.");
        }
    }

    @FXML
    private void viewReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/show_rapport_recyc_terrain.fxml"));
            Parent root = loader.load();

            ShowRapportRecycTerrainController controller = loader.getController();
            controller.setData(loggedUser, currentPoint);

            Stage stage = (Stage) lblPointTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Voir rapport");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le rapport.");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}