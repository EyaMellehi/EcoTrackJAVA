package org.example.Controllers;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.example.Controllers.User.ProfileController;
import org.example.Entities.User;

public class HomeConnectedController {
    @FXML private Button btnHome;
    @FXML private Button btnReport;
    @FXML private Button btnBlogs;
    @FXML private Button btnAssociations;
    @FXML private Button btnRecycling;
    @FXML private Button btnEvents;
    @FXML private MenuItem menuProfile;
    @FXML private MenuItem menuDonations;
    @FXML private MenuItem menuLogout;

    private User loggedUser;

    @FXML
    private Label lblWelcomeUser;


    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user == null || user.getRoles() == null) {
            return;
        }

        String roles = user.getRoles();

        if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
            applyMunicipalNavbar();
        } else if (roles.contains("ROLE_CITOYEN") || roles.contains("ROLE_AGENT_TERRAIN")) {
            applyStandardNavbar();
        }

        if (!roles.contains("ROLE_CITOYEN")) {
            menuDonations.setVisible(false);
            menuDonations.setDisable(true);
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
    @FXML
    void goToDonations() {
        System.out.println("Open donations");
    }

    @FXML
    void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) lblWelcomeUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(loggedUser);

            Stage stage = (Stage) lblWelcomeUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Profile");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
