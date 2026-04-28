package org.example.Controllers.admin;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.Categorie;
import org.example.Entities.User;
import org.example.Services.CategorieService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CategoriesController {

    @FXML private MenuButton menuAdmin;
    @FXML private TextField tfSearch;
    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, Integer> colIndex;
    @FXML private TableColumn<Categorie, String> colNom;
    @FXML private TableColumn<Categorie, Number> colCoef;
    @FXML private TableColumn<Categorie, String> colDescription;
    @FXML private TableColumn<Categorie, Number> colNbPoints;
    private User loggedUser;
    private final CategorieService categorieService = new CategorieService();
    private final ObservableList<Categorie> categorieList = FXCollections.observableArrayList();

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        if (user != null && user.getName() != null) {
            menuAdmin.setText(user.getName());
        }
    }
    @FXML
    public void initialize() {
        colIndex.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(tableCategories.getItems().indexOf(cellData.getValue()) + 1));

        colNom.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom()));

        colCoef.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getCoefPoints()));

        colDescription.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDescription() == null || cellData.getValue().getDescription().isEmpty()
                                ? "—"
                                : cellData.getValue().getDescription()
                ));

        colNbPoints.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getNbPoints()));

        loadCategories();
    }

    private void loadCategories() {
        try {
            List<Categorie> categories = categorieService.getAllCategories();

            for (Categorie c : categories) {
                c.setNbPoints(categorieService.countPointsByCategory(c.getId()));
            }

            categorieList.setAll(categories);
            tableCategories.setItems(categorieList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories.");
            e.printStackTrace();
        }
    }

    @FXML
    void filterCategories() {
        String keyword = tfSearch.getText() == null ? "" : tfSearch.getText().trim();

        try {
            List<Categorie> categories;

            if (keyword.isEmpty()) {
                categories = categorieService.getAllCategories();
            } else {
                categories = categorieService.searchCategories(keyword);
            }

            for (Categorie c : categories) {
                c.setNbPoints(categorieService.countPointsByCategory(c.getId()));
            }

            categorieList.setAll(categories);
            tableCategories.setItems(categorieList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du filtre.");
            e.printStackTrace();
        }
    }

    @FXML
    void resetCategories() {
        tfSearch.clear();
        loadCategories();
    }

    @FXML
    void goToAddCategory() {
        navigate("/admin/add_category.fxml", "Nouvelle catégorie");
    }

    @FXML
    void showCategory() {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionne une catégorie.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION,
                "Détails catégorie",
                "Nom : " + selected.getNom()
                        + "\nCoefficient : " + selected.getCoefPoints()
                        + "\nDescription : " + (selected.getDescription() == null ? "—" : selected.getDescription())
                        + "\nNb points : " + selected.getNbPoints());
    }

    @FXML
    void editCategory() {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionne une catégorie.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/edit_category.fxml"));
            Parent root = loader.load();

            EditCategoryController controller = loader.getController();
            controller.setCategorieData(selected);

            Stage stage = (Stage) tableCategories.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier catégorie");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteCategory() {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionne une catégorie.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer cette catégorie ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                categorieService.deleteCategorie(selected.getId());
                loadCategories();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie supprimée.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la catégorie.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/admin_dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
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
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Subscribers");
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

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
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

            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("Field Agents");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToCategories() {
        navigate("/admin/categories.fxml", "Catégories");
    }

    @FXML
    void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("EcoTrack - Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigate(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) tableCategories.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public void goToAssociation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_association/association.fxml"));
            Stage stage = (Stage) menuAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
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
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setTitle("donations");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}