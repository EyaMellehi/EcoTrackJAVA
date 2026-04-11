package org.example.Controllers.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.example.Controllers.HomeConnectedController;
import org.example.Controllers.User.ProfileController;
import org.example.Controllers.recyclage.PointsConnectedController;
import org.example.Controllers.recyclage.TerrainPointsController;
import org.example.Entities.User;

public class NavbarCitoyenController {

    @FXML private Button btnHome;
    @FXML private Button btnReport;
    @FXML private Button btnBlogs;
    @FXML private Button btnAssociations;
    @FXML private Button btnRecycling;
    @FXML private Button btnEvents;

    @FXML private MenuItem menuDonations;

    private User loggedUser;

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user == null || user.getRoles() == null) {
            applyStandardNavbar();
            hideDonations();
            return;
        }

        String roles = user.getRoles();

        if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
            applyMunicipalNavbar();
            hideDonations();
        } else if (roles.contains("ROLE_CITOYEN")) {
            applyStandardNavbar();
            showDonations();
        } else if (roles.contains("ROLE_AGENT_TERRAIN")) {
            applyStandardNavbar();
            hideDonations();
        } else {
            applyStandardNavbar();
            hideDonations();
        }
    }

    private void applyStandardNavbar() {
        btnHome.setText("Home");
        btnReport.setText("Report");
        btnBlogs.setText("Blogs");
        btnAssociations.setText("Associations");
        btnRecycling.setText("Recycling");
        btnEvents.setText("Events");
    }

    private void applyMunicipalNavbar() {
        btnHome.setText("Home");
        btnReport.setText("Report");
        btnBlogs.setText("Announcements Management");
        btnAssociations.setText("Associations");
        btnRecycling.setText("Recycling");
        btnEvents.setText("Events Management");
    }

    private void showDonations() {
        if (menuDonations != null) {
            menuDonations.setVisible(true);
            menuDonations.setDisable(false);
        }
    }

    private void hideDonations() {
        if (menuDonations != null) {
            menuDonations.setVisible(false);
            menuDonations.setDisable(true);
        }
    }

    private void navigate(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof HomeConnectedController homeController) {
                homeController.setLoggedUser(loggedUser);
            }

            if (controller instanceof PointsConnectedController pointsController) {
                pointsController.setLoggedUser(loggedUser);
            }

            if (controller instanceof TerrainPointsController terrainController) {
                terrainController.setLoggedUser(loggedUser);
            }

            if (controller instanceof ProfileController profileController) {
                profileController.setUser(loggedUser);
            }

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToHome() {
        navigate("/Home_Connected.fxml", "Home");
    }

    @FXML
    void goToReport() {
        System.out.println("Open reports");
    }

    @FXML
    void goToBlogs() {
        System.out.println("Open blogs");
    }

    @FXML
    void goToAssociations() {
        System.out.println("Open associations");
    }

    @FXML
    void goToRecycling() {
        if (loggedUser != null && loggedUser.getRoles() != null) {
            String roles = loggedUser.getRoles();

            if (roles.contains("ROLE_AGENT_TERRAIN")) {
                navigate("/recyclage/terrain_points.fxml", "Mes points affectés");
                return;
            }

            if (roles.contains("ROLE_CITOYEN")) {
                navigate("/recyclage/points_connected.fxml", "Points de recyclage");
                return;
            }

            if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
                navigate("/recyclage/municipal_points.fxml", "Points de recyclage");
                return;
            }
        }

        navigate("/recyclage/points_connected.fxml", "Points de recyclage");
    }

    @FXML
    void goToEvents() {
        System.out.println("Open events");
    }

    @FXML
    void goToProfile() {
        navigate("/user/profile.fxml", "My Profile");
    }

    @FXML
    void goToDonations() {
        if (loggedUser != null && loggedUser.getRoles() != null &&
                loggedUser.getRoles().contains("ROLE_CITOYEN")) {
            System.out.println("Open donations");
        }
    }

    @FXML
    void logout() {
        navigate("/home.fxml", "Home");
    }
}