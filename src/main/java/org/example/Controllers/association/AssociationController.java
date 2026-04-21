package org.example.Controllers.association;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.Entities.Association;
import org.example.Services.AssociationService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AssociationController {
// METHODS TO ADD IN AssociationController

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblActive;

    @FXML
    private Label lblInactive;

    @FXML
    private Label lblRegions;


    /* =======================================================
       UPDATE STATS
    ======================================================= */
    private void updateStats() {

        if (allData == null) return;

        long total = allData.size();

        long active = allData.stream()
                .filter(Association::isActive)
                .count();

        long inactive = total - active;

        long regions = allData.stream()
                .map(Association::getRegion)
                .filter(r -> r != null && !r.isEmpty())
                .distinct()
                .count();

        lblTotal.setText(String.valueOf(total));
        lblActive.setText(String.valueOf(active));
        lblInactive.setText(String.valueOf(inactive));
        lblRegions.setText(String.valueOf(regions));
    }


    /* =======================================================
       CALL THIS INSIDE loadData()
    ======================================================= */
    private void loadData() {
        allData = service.getAll();
        updateStats();
        refresh();
    }


/* =======================================================
   NAVIGATION METHODS
======================================================= */

    @FXML
    void goToDashboard() {
        navigate("/admin/dashboard.fxml", "Dashboard");
    }

    @FXML
    void goToSubscribers() {
        navigate("/admin/subscribers.fxml", "Subscribers");
    }

    @FXML
    void goToMunicipalAgents() {
        navigate("/admin/municipal_agents.fxml", "Municipal Agents");
    }

    @FXML
    void goToFieldAgents() {
        navigate("/admin/field_agents.fxml", "Field Agents");
    }

    @FXML
    void goToCategories() {
        navigate("/admin/categories.fxml", "Categories");
    }

    @FXML
    void goToEvents() {
        navigate("/admin/events.fxml", "Events");
    }

    @FXML
    void logout() {

        navigate("/home.fxml", "Events");
    }
    @FXML
    private void goToAssociation() {
        // already on this page
    }


    /* =======================================================
       UNIVERSAL NAVIGATE
    ======================================================= */
    private void navigate(String path, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));

            Stage stage = (Stage) cardContainer.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private FlowPane cardContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> regionFilter;
    @FXML private Label pageLabel;
    @FXML
    void goToDonation(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/donation/donationIndex.fxml")
            );

            Parent root = loader.load();

            // fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // taille écran disponible (sans fullscreen)
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());

            stage.setScene(scene);

            // occupe tout l'écran sans mode fullscreen
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            stage.setMaximized(true);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("❌ Impossible d'ouvrir la page");
            alert.showAndWait();
        }
    }








    private final AssociationService service = new AssociationService();

    private List<Association> allData;
    private int currentPage = 0;
    private final int pageSize = 6;

    private final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // ======================================================
    // INIT
    // ======================================================
    @FXML
    public void initialize() {
        regionFilter.getItems().addAll(
                "All",
                "Tunis",
                "Ariana",
                "Ben Arous",
                "Manouba",
                "Nabeul",
                "Zaghouan",
                "Bizerte",
                "Béja",
                "Jendouba",
                "Le Kef",
                "Siliana",
                "Sousse",
                "Monastir",
                "Mahdia",
                "Sfax",
                "Kairouan",
                "Kasserine",
                "Sidi Bouzid",
                "Gabès",
                "Medenine",
                "Tataouine",
                "Gafsa",
                "Tozeur",
                "Kébili"
        );

        regionFilter.setValue("All");

        loadData();
    }



    private void refresh() {

        cardContainer.getChildren().clear();

        String search = searchField.getText().trim().toLowerCase();
        String region = regionFilter.getValue();

        List<Association> filtered = allData.stream()
                .filter(a ->
                        a.getNom() != null &&
                                a.getNom().toLowerCase().contains(search)
                )
                .filter(a ->
                        region.equals("All") ||
                                (a.getRegion() != null &&
                                        a.getRegion().equalsIgnoreCase(region))
                )
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            pageLabel.setText("0 Result");
            return;
        }

        int totalPages = (int) Math.ceil((double) filtered.size() / pageSize);

        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;

        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, filtered.size());

        List<Association> page = filtered.subList(start, end);

        for (Association a : page) {
            cardContainer.getChildren().add(createCard(a));
        }

        pageLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);
    }

    // ======================================================
    // CARD DESIGN
    // ======================================================
    private VBox createCard(Association a) {

        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setPrefWidth(250);
        card.getStyleClass().add("card");

        // image
        StackPane imageBox = new StackPane();
        imageBox.setPrefHeight(120);
        imageBox.getStyleClass().add("image-container");

        ImageView logo = new ImageView();
        logo.setFitWidth(90);
        logo.setFitHeight(90);
        logo.setPreserveRatio(true);

        try {
            if (a.getLogo() != null && !a.getLogo().isEmpty()) {
                logo.setImage(new Image("file:" + a.getLogo(), true));
            }
        } catch (Exception ignored) {}

        imageBox.getChildren().add(logo);

        Label title = new Label(
                a.getNom() == null ? "No Name" : a.getNom()
        );
        title.getStyleClass().add("card-title");

        Label region = new Label("📍 " + safe(a.getRegion()));
        Label tel = new Label("📞 " + a.getTel());

        region.getStyleClass().add("card-sub");
        tel.getStyleClass().add("card-sub");

        Label status = new Label(
                a.isActive() ? "ACTIVE" : "INACTIVE"
        );
        status.getStyleClass().add(
                a.isActive() ? "badge-active" : "badge-inactive"
        );

        Button view = new Button("👁");
        Button edit = new Button("✏");
        Button del = new Button("🗑");

        view.getStyleClass().add("icon-btn");
        edit.getStyleClass().add("icon-btn");
        del.getStyleClass().add("icon-btn-danger");

        view.setOnAction(e -> showDetails(a));
        edit.setOnAction(e -> openForm(a));
        del.setOnAction(e -> deleteAssociation(a));

        HBox actions = new HBox(8, view, edit, del);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(status, spacer, actions);

        card.getChildren().addAll(
                imageBox, title, region, tel, footer
        );

        return card;
    }

    // ======================================================
    // DELETE
    // ======================================================
    private void deleteAssociation(Association a) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Delete Association ?");
        confirm.setContentText(a.getNom());

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(a.getId());
            loadData();
        }
    }

    // ======================================================
    // DETAILS
    // ======================================================
    private void showDetails(Association a) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/admin_association/show.fxml")
            );

            Parent root = loader.load();

            ShowControllerAdmin controller = loader.getController();
            controller.setAssociation(a);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails Association");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ======================================================
    // SEARCH
    // ======================================================
    @FXML
    void onSearch() {
        currentPage = 0;
        refresh();
    }

    @FXML
    void onFilter() {
        currentPage = 0;
        refresh();
    }

    // ======================================================
    // PAGINATION
    // ======================================================
    @FXML
    void nextPage() {
        currentPage++;
        refresh();
    }

    @FXML
    void prevPage() {
        currentPage--;
        refresh();
    }

    // ======================================================
    // ADD NEW
    // ======================================================
    @FXML
    void addNew() {
        openForm(null);
    }

    // ======================================================
    // MODERN FORM + VALIDATION
    // ======================================================
    void openForm(Association old) {

        Dialog<Association> dialog = new Dialog<>();
        dialog.setTitle(old == null ? "➕ Add Association" : "✏ Edit Association");

        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        // fields
        TextField nom = new TextField();

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll(
                "Environmental Protection",
                "Animal Welfare",
                "Humanitarian Aid",
                "Education & Youth",
                "Health & Medical",
                "Women Support",
                "Children Support",
                "Recycling & Waste",
                "Sports & Culture",
                "Social Development"
        );
        type.setPromptText("Choose association type");
        type.setPrefWidth(260);

        TextArea desc = new TextArea();
        desc.setPrefRowCount(3);

        ComboBox<String> region = new ComboBox<>();
        region.getItems().addAll(
                "Tunis", "Ariana", "Ben Arous", "Manouba",
                "Nabeul", "Sousse", "Monastir", "Mahdia",
                "Sfax", "Gabes", "Kairouan", "Bizerte",
                "Gafsa", "Kasserine", "Medenine"
        );

        TextField tel = new TextField();
        TextField email = new TextField();
        TextField adr = new TextField();

        CheckBox active = new CheckBox("Association Active");

        Label imageName = new Label("No file selected");
        Button upload = new Button("Upload Logo");

        final String[] image = {null};

        upload.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            File file = fc.showOpenDialog(null);

            if (file != null) {
                image[0] = file.getAbsolutePath();
                imageName.setText(file.getName());
            }
        });

        // Prefill
        if (old != null) {
            nom.setText(old.getNom());
            type.setValue(old.getType());
            desc.setText(old.getDescription());
            region.setValue(old.getRegion());
            tel.setText(String.valueOf(old.getTel()));
            email.setText(old.getEmail());
            adr.setText(old.getAddresse());
            active.setSelected(old.isActive());
            image[0] = old.getLogo();

            if (old.getLogo() != null && !old.getLogo().isEmpty()) {
                File oldFile = new File(old.getLogo());
                imageName.setText(oldFile.getName());
            }
        }

        // errors
        Label errNom = errorLabel();
        Label errType = errorLabel();
        Label errTel = errorLabel();
        Label errMail = errorLabel();
        Label errRegion = errorLabel();
        Label errAdr = errorLabel();

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(12);
        grid.setPadding(new Insets(20));

        int r = 0;

        grid.add(new Label("Name"), 0, r);
        grid.add(nom, 1, r++);
        grid.add(errNom, 1, r++);

        grid.add(new Label("Type"), 0, r);
        grid.add(type, 1, r++);
        grid.add(errType, 1, r++);

        grid.add(new Label("Description"), 0, r);
        grid.add(desc, 1, r++);

        grid.add(new Label("Region"), 0, r);
        grid.add(region, 1, r++);
        grid.add(errRegion, 1, r++);

        grid.add(new Label("Phone"), 0, r);
        grid.add(tel, 1, r++);
        grid.add(errTel, 1, r++);

        grid.add(new Label("Email"), 0, r);
        grid.add(email, 1, r++);
        grid.add(errMail, 1, r++);

        grid.add(new Label("Address"), 0, r);
        grid.add(adr, 1, r++);
        grid.add(errAdr, 1, r++);

        grid.add(active, 1, r++);

        grid.add(upload, 0, r);
        grid.add(imageName, 1, r);

        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

            boolean valid = true;

            clearErrors(errNom, errType, errTel, errMail, errRegion, errAdr);

            if (nom.getText().trim().length() < 3) {
                errNom.setText("Minimum 3 letters");
                valid = false;
            }

            if (type.getValue() == null || type.getValue().trim().isEmpty()) {
                errType.setText("Choose type");
                valid = false;
            }

            if (region.getValue() == null) {
                errRegion.setText("Choose region");
                valid = false;
            }

            if (!tel.getText().matches("\\d{8}")) {
                errTel.setText("8 digits required");
                valid = false;
            }

            if (!EMAIL_PATTERN.matcher(email.getText().trim()).matches()) {
                errMail.setText("Invalid email");
                valid = false;
            }

            if (adr.getText().trim().length() < 4) {
                errAdr.setText("Address too short");
                valid = false;
            }

            if (!valid) {
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {

            if (btn == ButtonType.OK) {

                Association a = (old == null) ? new Association() : old;

                a.setNom(nom.getText().trim());
                a.setType(type.getValue());
                a.setDescription(desc.getText().trim());
                a.setRegion(region.getValue());
                a.setTel(Integer.parseInt(tel.getText()));
                a.setEmail(email.getText().trim());
                a.setAddresse(adr.getText().trim());
                a.setActive(active.isSelected());
                a.setLogo(image[0]);

                return a;
            }

            return null;
        });

        dialog.showAndWait().ifPresent(result -> {

            if (old == null) {
                service.add(result);
            } else {
                service.update(result);
            }

            loadData();
        });
    }    // ======================================================
    // HELPERS
    // ======================================================
    private Label errorLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill:#ef4444;-fx-font-size:11;");
        return l;
    }

    private void clearErrors(Label... labels) {
        for (Label l : labels) l.setText("");
    }

    private String safe(String v) {
        return v == null ? "-" : v;
    }
}