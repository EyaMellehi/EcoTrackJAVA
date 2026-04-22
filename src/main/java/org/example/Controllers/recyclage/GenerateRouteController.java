package org.example.Controllers.recyclage;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Entities.PointRecyclage;
import org.example.Entities.User;
import org.example.Services.PointRecyclageService;
import org.example.Services.RecyclageRouteOptimizer;
import org.example.Services.RouteResult;
import org.example.Services.RouteStep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GenerateRouteController {

    @FXML private NavbarCitoyenController navbarCitoyenController;

    @FXML private TextField tfCapacity;
    @FXML private TextField tfStartLat;
    @FXML private TextField tfStartLng;
    @FXML private TextField tfStartAddress;
    @FXML private TextField tfSearchAddress;
    @FXML private Label lblLocationStatus;

    @FXML private Label lblTotalKg;
    @FXML private Label lblSelectedCount;
    @FXML private Label lblTotalKm;

    @FXML private VBox stepsContainer;
    @FXML private WebView mapView;

    private final PointRecyclageService pointService = new PointRecyclageService();
    private final RecyclageRouteOptimizer optimizer = new RecyclageRouteOptimizer();

    private User loggedUser;
    private List<PointRecyclage> assignedPoints = new ArrayList<>();

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (navbarCitoyenController != null) {
            navbarCitoyenController.setLoggedUser(user);
        }

        loadAssignedPoints();
    }

    @FXML
    public void initialize() {
        resetSummary();
        initMap();

        setPositionFields(36.8065, 10.1815);
        tfStartAddress.setText("Adresse non encore récupérée");
    }

    private void loadAssignedPoints() {
        if (loggedUser == null) return;

        try {
            assignedPoints = pointService.getPointsForFieldAgent(loggedUser);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les points affectés.");
        }
    }

    private void initMap() {
        mapView.setContextMenuEnabled(false);
        WebEngine engine = mapView.getEngine();

        String html = """
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
                  width: 100%;
                  height: 100%;
                  margin: 0;
                  padding: 0;
                  overflow: hidden;
                  background: white;
                }

                #map {
                  width: 100%;
                  height: 100%;
                  margin: 0;
                  padding: 0;
                  background: #e5e7eb;
                }

                .leaflet-container {
                  width: 100% !important;
                  height: 100% !important;
                  background: #e5e7eb;
                }
              </style>
            </head>
            <body>
              <div id="map"></div>

              <script>
                var map = L.map('map', {
                  preferCanvas: true,
                  zoomAnimation: false,
                  fadeAnimation: false,
                  markerZoomAnimation: false,
                  inertia: false
                }).setView([36.8065, 10.1815], 12);

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                  maxZoom: 19,
                  attribution: '&copy; OpenStreetMap contributors',
                  updateWhenZooming: false,
                  updateWhenIdle: true,
                  keepBuffer: 1
                }).addTo(map);

                var marker = L.marker([36.8065, 10.1815], {
                  draggable: true
                }).addTo(map);

                function notify(lat, lng) {
                  if (window.javaConnector) {
                    window.javaConnector.updatePosition(lat, lng);
                  }
                }

                map.on('click', function(e) {
                  var lat = e.latlng.lat;
                  var lng = e.latlng.lng;
                  marker.setLatLng([lat, lng]);
                  notify(lat, lng);
                });

                marker.on('dragend', function() {
                  var p = marker.getLatLng();
                  notify(p.lat, p.lng);
                });

                function setMarkerAndView(lat, lng) {
                  marker.setLatLng([lat, lng]);
                  map.setView([lat, lng], 13);
                  setTimeout(function () {
                    map.invalidateSize(true);
                  }, 100);
                  notify(lat, lng);
                }

                map.on('zoomend', function() {
                  setTimeout(function () {
                    map.invalidateSize(true);
                  }, 100);
                });

                setTimeout(function () {
                  map.invalidateSize(true);
                }, 300);

                window.setMarkerAndView = setMarkerAndView;

                notify(36.8065, 10.1815);
              </script>
            </body>
            </html>
            """;

        engine.loadContent(html);

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            try {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector());
                engine.executeScript("setTimeout(function(){ map.invalidateSize(true); }, 500);");
            } catch (Exception ignored) {
            }
        });
    }

    public class JavaConnector {
        public void updatePosition(double lat, double lng) {
            javafx.application.Platform.runLater(() -> {
                setPositionFields(lat, lng);
                tfStartAddress.setText("Position choisie. Clique sur 'Récupérer l’adresse'.");
            });
        }
    }

    @FXML
    private void takeMyLocation() {
        try {
            lblLocationStatus.setText("Localisation : récupération...");
            lblLocationStatus.setStyle("-fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-padding: 8 14; -fx-background-radius: 18;");

            String json = fetchUrl("http://ip-api.com/json/");

            String latStr = extractJsonValue(json, "lat");
            String lngStr = extractJsonValue(json, "lon");

            if (latStr == null || lngStr == null || latStr.isBlank() || lngStr.isBlank()) {
                throw new IOException("Latitude/Longitude introuvables.");
            }

            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);

            setPositionFields(lat, lng);

            try {
                mapView.getEngine().executeScript(
                        "setMarkerAndView(" +
                                String.format(Locale.US, "%.6f", lat) + "," +
                                String.format(Locale.US, "%.6f", lng) + ")"
                );
            } catch (Exception ignored) {
            }

            tfStartAddress.setText("Position chargée. Clique sur 'Récupérer l’adresse'.");

            lblLocationStatus.setText("Localisation : OK");
            lblLocationStatus.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 8 14; -fx-background-radius: 18;");

        } catch (Exception e) {
            e.printStackTrace();
            lblLocationStatus.setText("Localisation : indisponible");
            lblLocationStatus.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 8 14; -fx-background-radius: 18;");
            showAlert(Alert.AlertType.WARNING, "Localisation", "Impossible de récupérer votre position automatiquement.");
        }
    }

    @FXML
    private void goToAddress() {
        String query = tfSearchAddress.getText() == null ? "" : tfSearchAddress.getText().trim();
        if (query.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Recherche", "Saisis une adresse ou une ville.");
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                String url = "https://photon.komoot.io/api/?q="
                        + URLEncoder.encode(query, StandardCharsets.UTF_8)
                        + "&lang=fr&limit=1";

                String json = fetchUrl(url);

                Double lon = extractFirstPhotonCoordinate(json, 0);
                Double lat = extractFirstPhotonCoordinate(json, 1);
                String address = extractPhotonAddress(json);

                javafx.application.Platform.runLater(() -> {
                    if (lat == null || lon == null) {
                        showAlert(Alert.AlertType.WARNING, "Recherche", "Adresse introuvable.");
                        return;
                    }

                    setPositionFields(lat, lon);

                    if (address != null && !address.isBlank()) {
                        tfStartAddress.setText(address);
                    } else {
                        tfStartAddress.setText("Adresse trouvée partiellement.");
                    }

                    try {
                        mapView.getEngine().executeScript(
                                "setMarkerAndView(" +
                                        String.format(Locale.US, "%.6f", lat) + "," +
                                        String.format(Locale.US, "%.6f", lon) + ")"
                        );
                    } catch (Exception ignored) {
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Recherche", "Impossible de rechercher cette adresse.")
                );
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void resolveSelectedAddress() {
        double lat;
        double lng;

        try {
            lat = Double.parseDouble(tfStartLat.getText().trim().replace(",", "."));
            lng = Double.parseDouble(tfStartLng.getText().trim().replace(",", "."));
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Adresse", "Choisis d'abord une position sur la carte.");
            return;
        }

        tfStartAddress.setText("Chargement de l'adresse...");
        resolveAddressFromCoordinates(lat, lng);
    }

    private void resolveAddressFromCoordinates(double lat, double lng) {
        Thread thread = new Thread(() -> {
            try {
                String latStr = String.format(Locale.US, "%.6f", lat);
                String lngStr = String.format(Locale.US, "%.6f", lng);

                String url = "https://photon.komoot.io/reverse"
                        + "?lat=" + URLEncoder.encode(latStr, StandardCharsets.UTF_8)
                        + "&lon=" + URLEncoder.encode(lngStr, StandardCharsets.UTF_8)
                        + "&lang=fr";

                String json = fetchUrl(url);
                String address = extractPhotonAddress(json);

                javafx.application.Platform.runLater(() -> {
                    if (address == null || address.isBlank()) {
                        tfStartAddress.setText("Adresse non disponible");
                    } else {
                        tfStartAddress.setText(address);
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        tfStartAddress.setText("Adresse non disponible")
                );
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void generateRoute() {
        double capacity;
        double startLat;
        double startLng;

        try {
            capacity = Double.parseDouble(tfCapacity.getText().trim().replace(",", "."));
            if (capacity <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Capacité invalide. (ex: 50)");
                return;
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Capacité invalide. (ex: 50)");
            return;
        }

        try {
            startLat = Double.parseDouble(tfStartLat.getText().trim().replace(",", "."));
            startLng = Double.parseDouble(tfStartLng.getText().trim().replace(",", "."));
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Position non disponible. Prends ta position d’abord.");
            return;
        }

        List<PointRecyclage> pointsInProgress = assignedPoints.stream()
                .filter(p -> "IN_PROGRESS".equalsIgnoreCase(safe(p.getStatut())))
                .toList();

        RouteResult route = optimizer.compute(pointsInProgress, capacity, startLat, startLng);

        if (route.getSelectedCount() == 0) {
            showAlert(Alert.AlertType.WARNING, "Aucune tournée", "Aucun point ne rentre dans la capacité " + capacity + " kg.");
            resetSummary();
            stepsContainer.getChildren().clear();
            return;
        }

        lblTotalKg.setText(route.getTotalKg() + " kg");
        lblSelectedCount.setText(route.getSelectedCount() + " points sélectionnés");
        lblTotalKm.setText(route.getTotalKm() + " km");

        renderSteps(route);
    }

    private void renderSteps(RouteResult route) {
        stepsContainer.getChildren().clear();

        int index = 1;
        for (RouteStep step : route.getOrdered()) {
            PointRecyclage p = step.getPoint();

            VBox card = new VBox(10);
            card.setPadding(new Insets(18));
            card.setStyle("-fx-background-color: #f8faf8; -fx-background-radius: 14; -fx-border-color: #e5e7eb; -fx-border-radius: 14;");

            HBox top = new HBox(10);
            Label order = new Label(String.valueOf(index));
            order.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-padding: 6 12; -fx-background-radius: 999; -fx-border-radius: 999; -fx-font-weight: bold;");
            Label pointId = new Label("Point #" + p.getId());
            pointId.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f3d23;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label priority = new Label(p.getAiPriority() != null ? p.getAiPriority() : "NONE");
            priority.setStyle(getPriorityBadgeStyle(p.getAiPriority()));

            Label qty = new Label(p.getQuantite() + " kg");
            qty.setStyle("-fx-background-color: white; -fx-text-fill: #374151; -fx-padding: 6 12; -fx-background-radius: 999;");

            top.getChildren().addAll(order, pointId, spacer, priority, qty);

            Label distance = new Label("Distance depuis l’étape précédente : " + step.getDistanceFromPrevKm() + " km");
            distance.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

            String scoreText = p.getAiScore() != null ? " • Score: " + p.getAiScore() + "/100" : "";
            distance.setText(distance.getText() + scoreText);

            Label address = new Label(safe(p.getAddress()));
            address.setWrapText(true);
            address.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");

            Label explanation = new Label(
                    p.getAiExplanation() != null && !p.getAiExplanation().isBlank()
                            ? p.getAiExplanation()
                            : "Aucune explication IA."
            );
            explanation.setWrapText(true);
            explanation.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

            HBox actions = new HBox(10);
            Button openBtn = new Button("Ouvrir");
            openBtn.setStyle("-fx-background-color: #eef7ee; -fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-background-radius: 8;");
            openBtn.setOnAction(e -> openPointDetails(p));

            actions.getChildren().add(openBtn);

            card.getChildren().addAll(top, distance, address, explanation, actions);
            stepsContainer.getChildren().add(card);

            index++;
        }
    }

    private void openPointDetails(PointRecyclage point) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/terrain_point_details.fxml"));
            Parent root = loader.load();

            TerrainPointDetailsController controller = loader.getController();
            controller.setData(loggedUser, point);

            Stage stage = (Stage) tfCapacity.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Point details");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le point.");
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/recyclage/terrain_points.fxml"));
            Parent root = loader.load();

            TerrainPointsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) tfCapacity.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes points affectés");
            stage.setFullScreen(false);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPositionFields(double lat, double lng) {
        tfStartLat.setText(String.format(Locale.US, "%.6f", lat));
        tfStartLng.setText(String.format(Locale.US, "%.6f", lng));
    }

    private String extractPhotonAddress(String json) {
        String name = parseJsonFieldAnywhere(json, "\"name\":\"");
        String street = parseJsonFieldAnywhere(json, "\"street\":\"");
        String housenumber = parseJsonFieldAnywhere(json, "\"housenumber\":\"");
        String district = parseJsonFieldAnywhere(json, "\"district\":\"");
        String city = parseJsonFieldAnywhere(json, "\"city\":\"");
        String county = parseJsonFieldAnywhere(json, "\"county\":\"");
        String state = parseJsonFieldAnywhere(json, "\"state\":\"");
        String country = parseJsonFieldAnywhere(json, "\"country\":\"");

        List<String> parts = new ArrayList<>();

        if (street != null && !street.isBlank()) {
            String s = street;
            if (housenumber != null && !housenumber.isBlank()) {
                s += " " + housenumber;
            }
            parts.add(s);
        } else if (name != null && !name.isBlank()) {
            parts.add(name);
        }

        if (district != null && !district.isBlank()) parts.add(district);
        if (city != null && !city.isBlank()) parts.add(city);
        if (county != null && !county.isBlank()) parts.add(county);
        if (state != null && !state.isBlank()) parts.add(state);
        if (country != null && !country.isBlank()) parts.add(country);

        if (parts.isEmpty()) return null;
        return String.join(", ", parts);
    }

    private Double extractFirstPhotonCoordinate(String json, int index) {
        String marker = "\"coordinates\":[";
        int start = json.indexOf(marker);
        if (start == -1) return null;

        start += marker.length();
        int end = json.indexOf("]", start);
        if (end == -1) return null;

        String content = json.substring(start, end);
        String[] parts = content.split(",");

        if (parts.length < 2) return null;

        try {
            return Double.parseDouble(parts[index].trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String parseJsonFieldAnywhere(String json, String marker) {
        int start = json.indexOf(marker);
        if (start == -1) return null;

        start += marker.length();

        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);

            if (escaped) {
                switch (ch) {
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    case '/' -> result.append('/');
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> {
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try {
                                result.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                result.append("\\u").append(hex);
                                i += 4;
                            }
                        }
                    }
                    default -> result.append(ch);
                }
                escaped = false;
                continue;
            }

            if (ch == '\\') {
                escaped = true;
                continue;
            }

            if (ch == '"') {
                break;
            }

            result.append(ch);
        }

        return result.toString().trim();
    }

    private String fetchUrl(String urlString) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(urlString).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } finally {
            con.disconnect();
        }

        return response.toString();
    }

    private String extractJsonValue(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start == -1) return null;

        start += marker.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        int end = start;

        if (start < json.length() && json.charAt(start) == '"') {
            start++;
            end = start;
            while (end < json.length() && json.charAt(end) != '"') {
                end++;
            }
            return json.substring(start, end);
        }

        while (end < json.length() &&
                (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
            end++;
        }

        return json.substring(start, end);
    }

    private String getPriorityBadgeStyle(String priority) {
        String p = safe(priority).toUpperCase();
        String base = "-fx-padding: 6 12; -fx-background-radius: 999; -fx-font-weight: bold;";

        return switch (p) {
            case "LOW" -> base + "-fx-background-color: #dcfce7; -fx-text-fill: #166534;";
            case "MEDIUM" -> base + "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;";
            case "HIGH" -> base + "-fx-background-color: #fed7aa; -fx-text-fill: #9a3412;";
            case "URGENT" -> base + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;";
            default -> base + "-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563;";
        };
    }

    private void resetSummary() {
        lblTotalKg.setText("0 kg");
        lblSelectedCount.setText("0 points sélectionnés");
        lblTotalKm.setText("0 km");
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