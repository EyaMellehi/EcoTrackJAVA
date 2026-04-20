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

import java.io.IOException;
import java.sql.SQLException;

public class ShowPointRecyclageController {

    @FXML private NavbarCitoyenController navbarController;

    @FXML private Label lblTitle;
    @FXML private Label lblAddressTop;
    @FXML private Label lblStatut;
    @FXML private Label lblCategorie;
    @FXML private Label lblQuantite;
    @FXML private Label lblDate;
    @FXML private Label lblDescription;
    @FXML private Label lblCoords;
    @FXML private WebView mapView;

    @FXML private Button btnEdit;
    @FXML private Button btnViewReport;

    private final PointRecyclageService pointService = new PointRecyclageService();
    private final RapportRecycService rapportService = new RapportRecycService();

    private User loggedUser;
    private PointRecyclage currentPoint;
    private RapportRecyc currentRapport;

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (navbarController != null) {
            navbarController.setLoggedUser(user);
        }
    }

    public void setPoint(PointRecyclage point) {
        this.currentPoint = point;
        loadData();
    }

    private void loadData() {
        if (currentPoint == null) return;

        try {
            currentPoint = pointService.getPointById(currentPoint.getId());
            currentRapport = rapportService.getRapportByPointId(currentPoint.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le point.");
            return;
        }

        lblTitle.setText("Point #" + currentPoint.getId());
        lblAddressTop.setText(currentPoint.getAddress());
        lblStatut.setText(currentPoint.getStatut());
        lblCategorie.setText(currentPoint.getCategorie() != null ? currentPoint.getCategorie().getNom() : "-");
        lblQuantite.setText(currentPoint.getQuantite() + " kg");
        lblDate.setText(currentPoint.getDateDec() != null ? currentPoint.getDateDec().toString() : "-");
        lblDescription.setText(
                currentPoint.getDescription() != null && !currentPoint.getDescription().isEmpty()
                        ? currentPoint.getDescription()
                        : "-"
        );
        lblCoords.setText("Coordonnées : " + currentPoint.getLatitude() + ", " + currentPoint.getLongitude());

        applyStatusStyle();
        configureButtons();
        loadMap();
    }

    private void configureButtons() {
        String statut = currentPoint.getStatut() != null ? currentPoint.getStatut().toUpperCase() : "";

        boolean canEdit = "PENDING".equals(statut);
        btnEdit.setVisible(canEdit);
        btnEdit.setManaged(canEdit);

        boolean hasRapport = currentRapport != null;
        btnViewReport.setVisible(hasRapport);
        btnViewReport.setManaged(hasRapport);
    }

    private void applyStatusStyle() {
        String statut = currentPoint.getStatut() != null ? currentPoint.getStatut().toUpperCase() : "";

        String style = "-fx-padding: 8 16; -fx-background-radius: 999; -fx-font-weight: bold;";

        switch (statut) {
            case "PENDING":
                lblStatut.setStyle(style + "-fx-background-color: #facc15; -fx-text-fill: #111827;");
                break;
            case "IN_PROGRESS":
                lblStatut.setStyle(style + "-fx-background-color: #2563eb; -fx-text-fill: white;");
                break;
            case "COLLECTE":
                lblStatut.setStyle(style + "-fx-background-color: #16a34a; -fx-text-fill: white;");
                break;
            case "REFUSE":
                lblStatut.setStyle(style + "-fx-background-color: #ef4444; -fx-text-fill: white;");
                break;
            case "VALIDE":
                lblStatut.setStyle(style + "-fx-background-color: #7c3aed; -fx-text-fill: white;");
                break;
            default:
                lblStatut.setStyle(style + "-fx-background-color: #9ca3af; -fx-text-fill: white;");
                break;
        }
    }

    private void loadMap() {
        mapView.setContextMenuEnabled(false);

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

        WebEngine engine = mapView.getEngine();
        engine.loadContent(html);
    }

    @FXML
    void backToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/points_connected.fxml"));
            Parent root = loader.load();

            PointsConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) lblTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Points de recyclage");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/edit_point_connected.fxml"));
            Parent root = loader.load();

            EditPointRecyclageController controller = loader.getController();
            controller.setLoggedUser(loggedUser);
            controller.setPoint(currentPoint);

            Stage stage = (Stage) lblTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier point");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToViewReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/show_rapport_recyc_citoyen.fxml"));
            Parent root = loader.load();

            ShowRapportRecycCitoyenController controller = loader.getController();
            controller.setData(loggedUser, currentPoint);

            Stage stage = (Stage) lblTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Rapport de recyclage");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le rapport.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}