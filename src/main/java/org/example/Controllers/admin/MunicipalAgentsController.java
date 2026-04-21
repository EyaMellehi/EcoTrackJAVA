package org.example.Controllers.admin;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.Entities.User;
import org.example.Services.UserService;

import java.util.List;
import java.util.stream.Collectors;

public class MunicipalAgentsController {

    @FXML private MenuButton menuAdmin;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbRegion;
    @FXML private ComboBox<String> cbStatus;

    @FXML private TableView<User> tableMunicipalAgents;
    @FXML private TableColumn<User, Number> colNum;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRegion;
    @FXML private TableColumn<User, String> colDelegation;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void> colActions;

    private final UserService userService = new UserService();
    private ObservableList<User> allMunicipalAgents = FXCollections.observableArrayList();
    private User loggedUser;

    @FXML
    public void initialize() {
        colNum.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(tableMunicipalAgents.getItems().indexOf(cellData.getValue()) + 1)
        );

        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPhone() == null || data.getValue().getPhone().isEmpty() ? "—" : data.getValue().getPhone()
        ));
        colRegion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRegion()));
        colDelegation.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDelegation() == null ? "" : data.getValue().getDelegation()
        ));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isActive() ? "Active" : "Inactive"
        ));

        colStatus.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(
                            item.equals("Active")
                                    ? "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: bold;"
                                    : "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: bold;"
                    );
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(8, btnEdit, btnDelete);

            {
                pane.setAlignment(Pos.CENTER);

                btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6;");
                btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6;");

                btnEdit.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editMunicipalAgent(user);
                });

                btnDelete.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteMunicipalAgent(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        cbStatus.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        cbStatus.setValue("All");

        cbRegion.setItems(FXCollections.observableArrayList(
                "All regions",
                "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa", "Jendouba",
                "Kairouan", "Kasserine", "Kébili", "Le Kef", "Mahdia", "La Manouba",
                "Médenine", "Monastir", "Nabeul", "Sfax", "Sidi Bouzid", "Siliana",
                "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
        ));
        cbRegion.setValue("All regions");

        loadMunicipalAgents();
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        if (user != null && user.getName() != null) {
            menuAdmin.setText(user.getName());
        }
    }

    private void loadMunicipalAgents() {
        try {
            List<User> users = userService.getMunicipalAgents();
            allMunicipalAgents.setAll(users);
            tableMunicipalAgents.setItems(allMunicipalAgents);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void filterMunicipalAgents() {
        String search = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        String region = cbRegion.getValue();
        String status = cbStatus.getValue();

        List<User> filtered = allMunicipalAgents.stream().filter(user -> {
            boolean matchesSearch =
                    search.isEmpty()
                            || (user.getName() != null && user.getName().toLowerCase().contains(search))
                            || (user.getEmail() != null && user.getEmail().toLowerCase().contains(search));

            boolean matchesRegion =
                    region == null || region.equals("All regions")
                            || (user.getRegion() != null && user.getRegion().equalsIgnoreCase(region));

            boolean matchesStatus =
                    status == null || status.equals("All")
                            || (status.equals("Active") && user.isActive())
                            || (status.equals("Inactive") && !user.isActive());

            return matchesSearch && matchesRegion && matchesStatus;
        }).collect(Collectors.toList());

        tableMunicipalAgents.setItems(FXCollections.observableArrayList(filtered));
    }

    private void deleteMunicipalAgent(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Municipal Agent");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete " + user.getName() + " ?");

        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, no);

        alert.showAndWait().ifPresent(response -> {
            if (response == yes) {
                try {
                    userService.deleteUser(user.getId());
                    loadMunicipalAgents();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/admin_dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToSubscribers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/subscribers.fxml"));
            Parent root = loader.load();

            SubscribersController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Subscribers");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToMunicipalAgents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/municipal_agents.fxml"));
            Parent root = loader.load();

            MunicipalAgentsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Municipal Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToFieldAgents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/field_agents.fxml"));
            Parent root = loader.load();

            FieldAgentsController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Field Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToAddMunicipalAgent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/add_municipal_agent.fxml"));
            Parent root = loader.load();

            AddMunicipalAgentController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Add Municipal Agent");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void editMunicipalAgent(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/edit_municipal_agent.fxml"));
            Parent root = loader.load();

            EditMunicipalAgentController controller = loader.getController();
            controller.setUser(user);
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Edit Municipal Agent");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToAssocation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_association/association.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
             stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/categories.fxml"));
            Parent root = loader.load();

            CategoriesController controller = loader.getController();
            controller.setLoggedUser(loggedUser);

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Catégories");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void goToAssociation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_association/association.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("associaitons");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToDonation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/donation/donationIndex.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("donations");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}