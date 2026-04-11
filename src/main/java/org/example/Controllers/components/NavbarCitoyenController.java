package org.example.Controllers.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    private User loggedUser;

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user == null || user.getRoles() == null) {
            applyStandardNavbar();
            return;
        }

        String roles = user.getRoles();

        if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
            applyMunicipalNavbar();
        } else {
            applyStandardNavbar();
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
        if (loggedUser != null && loggedUser.getRoles() != null &&
                loggedUser.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {
            System.out.println("Open announcements management");
        } else {
            System.out.println("Open blogs");
        }
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
        if (loggedUser != null && loggedUser.getRoles() != null &&
                loggedUser.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {
            System.out.println("Open events management");
        } else {
            System.out.println("Open events");
        }
    }

    @FXML
    void goToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(loggedUser);

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Profile");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToDonations() {
        System.out.println("Open donations");
    }

    @FXML
    void logout() {
        navigate("/home.fxml", "Home");
    }
}