package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.example.Controllers.User.ProfileController;
import org.example.Controllers.annonces.AgentAnnoncesDashboardController;
import org.example.Controllers.annonces.ListAnnonceAdminController;
import org.example.Controllers.annonces.ListAnnonceUserController;
import org.example.Controllers.admin.EventsManagementController;
import org.example.Controllers.signalement.ListAssignedSignalementController;
import org.example.Controllers.signalement.ListMunicipalSignalementController;
import org.example.Controllers.signalement.ListSignalementController;
import org.example.Entities.User;

import java.io.IOException;
import javafx.scene.layout.StackPane;
import org.example.Services.UserService;

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
    @FXML private Label lblWelcomeUser;
    @FXML private StackPane navbarContainer;
    @FXML private Label lblReportsCount;
    @FXML private Label lblRecyclingCount;
    @FXML private Label lblAssociationsCount;
    @FXML private Label lblEventsCount;

    private User loggedUser;
    private final UserService userService = new UserService();

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user == null || user.getRoles() == null) {
            return;
        }

        loadNavbarByRole();

        if (user.getName() != null && !user.getName().isEmpty()) {
            lblWelcomeUser.setText("Welcome, " + user.getName());
        } else {
            lblWelcomeUser.setText("Welcome");
        }
        loadStats();
    }
    private void loadStats() {
        try {
            if (loggedUser == null) {
                return;
            }

            lblReportsCount.setText(String.valueOf(userService.countReportsForUser(loggedUser)));
            lblRecyclingCount.setText(String.valueOf(userService.countRecyclingForUser(loggedUser)));
            lblAssociationsCount.setText(String.valueOf(userService.countAssociations()));
            lblEventsCount.setText(String.valueOf(userService.countPublishedEvents()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadNavbarByRole() {
        try {
            if (navbarContainer == null) {
                return;
            }

            navbarContainer.getChildren().clear();

            String roles = loggedUser != null && loggedUser.getRoles() != null ? loggedUser.getRoles() : "";
            FXMLLoader loader;

            if (roles.contains("ROLE_CITOYEN") || roles.contains("ROLE_AGENT_TERRAIN")) {
                loader = new FXMLLoader(getClass().getResource("/components/navbar_citoyen.fxml"));
            } else if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
                loader = new FXMLLoader(getClass().getResource("/components/navbar_municipal.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/components/navbar_citoyen.fxml"));
            }

            Parent navbar = loader.load();
            Object controller = loader.getController();

            if (controller instanceof org.example.Controllers.components.NavbarCitoyenController c) {
                c.setLoggedUser(loggedUser);
            } else if (controller instanceof org.example.Controllers.components.NavbarMunicipalController c) {
                c.setLoggedUser(loggedUser);
            }

            navbarContainer.getChildren().add(navbar);

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
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) lblWelcomeUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(loggedUser);

            Stage stage = (Stage) lblWelcomeUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("My Profile");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToRecycling() {
        if (loggedUser == null || loggedUser.getRoles() == null) {
            System.out.println("No logged user found.");
            return;
        }

        String roles = loggedUser.getRoles();

        try {
            FXMLLoader loader;
            Parent root;

            if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
                loader = new FXMLLoader(getClass().getResource("/recyclage/municipal_points.fxml"));
                root = loader.load();

                org.example.Controllers.recyclage.MunicipalPointsController controller = loader.getController();
                controller.setLoggedUser(loggedUser);

            } else if (roles.contains("ROLE_AGENT_TERRAIN")) {
                loader = new FXMLLoader(getClass().getResource("/recyclage/terrain_points.fxml"));
                root = loader.load();

                org.example.Controllers.recyclage.TerrainPointsController controller = loader.getController();
                controller.setLoggedUser(loggedUser);

            } else {
                loader = new FXMLLoader(getClass().getResource("/recyclage/points_connected.fxml"));
                root = loader.load();

                org.example.Controllers.recyclage.PointsConnectedController controller = loader.getController();
                controller.setLoggedUser(loggedUser);
            }

            Stage stage = (Stage) btnRecycling.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Points de recyclage");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home_Connected.fxml"));
            Parent root = loader.load();
            HomeConnectedController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToReport() {
        System.out.println("Open report page");
    }

    @FXML
    void goToBlogs() {
        try {
            FXMLLoader loader;
            Parent root;

            if (loggedUser != null && loggedUser.getRoles() != null &&
                    (loggedUser.getRoles().contains("ROLE_AGENT_MUNICIPAL") || loggedUser.getRoles().contains("ROLE_ADMIN"))) {
                loader = new FXMLLoader(getClass().getResource("/annonces/agent_annonces_dashboard.fxml"));
                root = loader.load();

                AgentAnnoncesDashboardController controller = loader.getController();
                controller.setLoggedUser(loggedUser);
            } else {
                loader = new FXMLLoader(getClass().getResource("/annonces/list_annonces_user.fxml"));
                root = loader.load();

                ListAnnonceUserController controller = loader.getController();
                controller.setLoggedUser(loggedUser);
            }

            Stage stage = (Stage) btnBlogs.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Annonces");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToAssociations() {


        try {
            FXMLLoader loader;
            Parent root;

            loader = new FXMLLoader(getClass().getResource("/client_association/index.fxml"));
            root = loader.load();



            Stage stage = (Stage) btnRecycling.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Points de recyclage");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    void goToEvents() {
        try {
            FXMLLoader loader;
            Parent root;

            if (loggedUser != null && loggedUser.getRoles() != null &&
                    (loggedUser.getRoles().contains("ROLE_AGENT_MUNICIPAL") || loggedUser.getRoles().contains("ROLE_ADMIN"))) {
                loader = new FXMLLoader(getClass().getResource("/admin/events_management.fxml"));
                root = loader.load();

                EventsManagementController controller = loader.getController();
                controller.setLoggedUser(loggedUser);
            } else {
                loader = new FXMLLoader(getClass().getResource("/events.fxml"));
                root = loader.load();

                EventsController controller = loader.getController();
                controller.setLoggedUser(loggedUser);
            }

            Stage stage = (Stage) btnEvents.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Events");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToMySignalements() {
        try {
            FXMLLoader loader;

            if (loggedUser != null && loggedUser.getRoles() != null) {
                if (loggedUser.getRoles().contains("ROLE_AGENT_TERRAIN")) {
                    loader = new FXMLLoader(getClass().getResource("/signalement/list_assigned_signalements.fxml"));
                } else if (loggedUser.getRoles().contains("ROLE_AGENT_MUNICIPAL")) {
                    loader = new FXMLLoader(getClass().getResource("/signalement/list_municipal_signalements.fxml"));
                } else {
                    loader = new FXMLLoader(getClass().getResource("/signalement/list_signalement.fxml"));
                }
            } else {
                loader = new FXMLLoader(getClass().getResource("/signalement/list_signalement.fxml"));
            }

            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof ListSignalementController) {
                ((ListSignalementController) controller).setLoggedUser(loggedUser);
            } else if (controller instanceof ListAssignedSignalementController) {
                ((ListAssignedSignalementController) controller).setLoggedUser(loggedUser);
            } else if (controller instanceof ListMunicipalSignalementController) {
                ((ListMunicipalSignalementController) controller).setLoggedUser(loggedUser);
            }

            Stage stage = (Stage) btnReport.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reports");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }













    @FXML
    void goToAssociation() {
        if (loggedUser == null || loggedUser.getRoles() == null) {
            System.out.println("No logged user found.");
            return;
        }

        String roles = loggedUser.getRoles();

        try {
            FXMLLoader loader;
            Parent root;

            if (roles.contains("ROLE_AGENT_MUNICIPAL")) {
                loader = new FXMLLoader(getClass().getResource("/client_association/index.fxml"));
                root = loader.load();


            } else if (roles.contains("ROLE_AGENT_TERRAIN")) {
                loader = new FXMLLoader(getClass().getResource("/client_association/index.fxml"));
                root = loader.load();


            } else {
                loader = new FXMLLoader(getClass().getResource("/client_association/index.fxml"));
                root = loader.load();


            }

            Stage stage = (Stage) btnRecycling.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Points de recyclage");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }













}