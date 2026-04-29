package org.example.Controllers.association;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import org.example.Controllers.components.NavbarCitoyenController;
import org.example.Entities.Association;
import org.example.Entities.User;
import org.example.Services.AssociationService;
import org.example.Services.OpenRouterSuggestService;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssocationClientIndex {
    @FXML private TextArea aiDescribeField;
    @FXML private Button aiDescribeBtn;
    @FXML private FlowPane cardContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> regionFilter;
    @FXML private Label pageLabel;

    @FXML private HBox navbar;
    @FXML private NavbarCitoyenController navbarController;

    @FXML private StackPane rootPane;
    @FXML private VBox loadingBox;
    @FXML private ProgressIndicator spinner;

    private User loggedUser;

    private final AssociationService service =
            new AssociationService();

    private final OpenRouterSuggestService ai =
            new OpenRouterSuggestService();

    private List<Association> allData =
            new ArrayList<>();

    private List<Association> filteredData =
            new ArrayList<>();
    @FXML
    private Label aiResponseLabel;

    private int currentPage = 0;
    private final int pageSize = 6;

    /* ================================================= */
    public void setLoggedUser(User user) {

        this.loggedUser = user;

        if (navbarController != null) {
            navbarController.setLoggedUser(user);
        }
    }

    /* ================================================= */
    @FXML
    public void initialize() {

        Platform.runLater(() -> {

            initRegions();
            loadData();

            // ✅ SAFE INIT (évite crash si FXML pas synchro)
            if (aiResponseLabel != null) {
                aiResponseLabel.setText("");
                aiResponseLabel.setVisible(false);
                aiResponseLabel.setWrapText(true);
            }

            if (aiDescribeBtn != null && aiDescribeField != null) {

                aiDescribeBtn.setOnAction(e -> {

                    String text = aiDescribeField.getText();

                    if (text == null || text.trim().length() < 10) {
                        Alert a = new Alert(Alert.AlertType.WARNING);
                        a.setContentText("Describe your situation in more detail.");
                        a.show();
                        return;
                    }

                    runSituationAiSearch(text.trim());
                });

            } else {
                System.err.println("⚠ AI components missing in FXML");
            }

            searchField.textProperty().addListener((obs, o, n) -> {
                if (n != null && n.trim().length() >= 5) {
                    runAiSearch(n.trim());
                } else {
                    applyFilters();
                }
            });
        });
    }
    private void runSituationAiSearch(String text) {

        showLoader();

        aiResponseLabel.setVisible(false);
        aiResponseLabel.setText("");

        Task<List<Integer>> task =
                new Task<>() {

                    @Override
                    protected List<Integer> call() {

                        List<JSONObject> data =
                                allData.stream()
                                        .map(a -> {

                                            JSONObject o =
                                                    new JSONObject();

                                            o.put("id", a.getId());
                                            o.put("nom", n(a.getNom()));
                                            o.put("type", n(a.getType()));
                                            o.put("region", n(a.getRegion()));
                                            o.put("description", n(a.getDescription()));
                                            o.put("isActive", a.isActive());

                                            return o;
                                        })
                                        .collect(Collectors.toList());

                        return ai.suggest(
                                text,
                                data,
                                6
                        );
                    }
                };

        task.setOnSucceeded(e -> {

            List<Integer> ids =
                    task.getValue();

            filteredData =
                    allData.stream()
                            .filter(a ->
                                    ids.contains(
                                            a.getId()
                                    )
                            )
                            .collect(Collectors.toList());

            currentPage = 0;

            hideLoader();

            refreshPage();

            /* ✅ SHOW MESSAGE UNDER INPUT */
            aiResponseLabel.setText(
                    ai.getLastMessage()
            );

            aiResponseLabel.setVisible(true);
        });

        task.setOnFailed(e -> {

            hideLoader();

            aiResponseLabel.setText(
                    "AI unavailable right now."
            );

            aiResponseLabel.setVisible(true);
        });

        Thread th =
                new Thread(task);

        th.setDaemon(true);
        th.start();
    }    /* ================================================= */
    private void initRegions() {

        regionFilter.setItems(
                FXCollections.observableArrayList(
                        "All",
                        "Tunis",
                        "Ariana",
                        "Ben Arous",
                        "Manouba",
                        "Nabeul",
                        "Bizerte",
                        "Béja",
                        "Jendouba",
                        "Kef",
                        "Siliana",
                        "Sousse",
                        "Monastir",
                        "Mahdia",
                        "Sfax",
                        "Kairouan",
                        "Kasserine",
                        "Gabès",
                        "Medenine",
                        "Gafsa",
                        "Tozeur"
                )
        );

        regionFilter.setValue("All");
        regionFilter.setOnAction(e -> applyFilters());
    }

    /* ================================================= */
    private void showLoader() {

        if (loadingBox != null) {
            loadingBox.setVisible(true);
            loadingBox.setManaged(true);
        }

        if (spinner != null) {
            spinner.setVisible(true);
        }
    }

    private void hideLoader() {

        if (loadingBox != null) {
            loadingBox.setVisible(false);
            loadingBox.setManaged(false);
        }

        if (spinner != null) {
            spinner.setVisible(false);
        }
    }

    /* ================================================= */
    private void loadData() {

        try {

            allData = service.getAll();

            if (allData == null) {
                allData = new ArrayList<>();
            }

        } catch (Exception e) {

            allData = new ArrayList<>();
            e.printStackTrace();
        }

        applyFilters();
    }

    /* ================================================= */
    private void runAiSearch(String text) {

        showLoader();

        Task<List<Integer>> task =
                new Task<>() {

                    @Override
                    protected List<Integer> call() {

                        List<JSONObject> data =
                                allData.stream()
                                        .map(a -> {

                                            JSONObject o =
                                                    new JSONObject();

                                            o.put("id", a.getId());
                                            o.put("nom", n(a.getNom()));
                                            o.put("type", n(a.getType()));
                                            o.put("region", n(a.getRegion()));
                                            o.put("description", n(a.getDescription()));
                                            o.put("isActive", a.isActive());

                                            return o;
                                        })
                                        .collect(Collectors.toList());

                        return ai.suggest(
                                text,
                                data,
                                6
                        );
                    }
                };

        task.setOnSucceeded(e -> {

            List<Integer> ids =
                    task.getValue();

            filteredData =
                    allData.stream()
                            .filter(a ->
                                    ids.contains(
                                            a.getId()
                                    )
                            )
                            .collect(Collectors.toList());

            currentPage = 0;

            hideLoader();

            refreshPage();

            aiResponseLabel.setText(
                    ai.getLastMessage()
            );

            aiResponseLabel.setVisible(true);
        });

        task.setOnFailed(e -> {

            hideLoader();

            applyFilters();
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    /* ================================================= */
    private void applyFilters() {

        String keyword =
                searchField.getText() == null
                        ? ""
                        : searchField.getText()
                        .trim()
                        .toLowerCase();

        String selectedRegion =
                regionFilter.getValue() == null
                        ? "All"
                        : regionFilter.getValue();

        filteredData =
                allData.stream()

                        .filter(a -> {

                            if (keyword.isEmpty()) {
                                return true;
                            }

                            return n(a.getNom()).toLowerCase().contains(keyword)
                                    || n(a.getDescription()).toLowerCase().contains(keyword)
                                    || n(a.getRegion()).toLowerCase().contains(keyword);
                        })

                        .filter(a -> {

                            if ("All".equals(selectedRegion)) {
                                return true;
                            }

                            return n(a.getRegion())
                                    .equalsIgnoreCase(selectedRegion);
                        })

                        .collect(Collectors.toList());

        currentPage = 0;

        refreshPage();
    }

    /* ================================================= */
    private void refreshPage() {

        Platform.runLater(() -> {

            cardContainer.getChildren().clear();

            if (filteredData.isEmpty()) {

                Label empty =
                        new Label(
                                "🤖 No association found"
                        );

                empty.setStyle(
                        "-fx-font-size:18px;" +
                                "-fx-text-fill:#64748b;"
                );

                cardContainer.getChildren()
                        .add(empty);

                pageLabel.setText("0 / 0");
                return;
            }

            int totalPages =
                    (int) Math.ceil(
                            (double) filteredData.size()
                                    / pageSize
                    );

            if (currentPage >= totalPages) {
                currentPage = totalPages - 1;
            }

            int start =
                    currentPage * pageSize;

            int end =
                    Math.min(
                            start + pageSize,
                            filteredData.size()
                    );

            List<Association> page =
                    filteredData.subList(start, end);

            for (Association a : page) {
                cardContainer.getChildren()
                        .add(createCard(a));
            }

            pageLabel.setText(
                    (currentPage + 1)
                            + " / "
                            + totalPages
            );
        });
    }

    /* ================================================= */
    private VBox createCard(Association a) {

        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPrefWidth(255);

        StackPane imageWrap =
                new StackPane();

        imageWrap.getStyleClass()
                .add("image-container");

        ImageView logo =
                new ImageView();

        logo.setFitWidth(80);
        logo.setFitHeight(80);
        logo.setPreserveRatio(true);

        try {

            if (a.getLogo() != null &&
                    !a.getLogo().isBlank()) {

                logo.setImage(
                        new Image(
                                "file:" + a.getLogo(),
                                true
                        )
                );

            } else {

                logo.setImage(
                        new Image(
                                getClass()
                                        .getResourceAsStream(
                                                "/images/default.png"
                                        )
                        )
                );
            }

        } catch (Exception e) {

            logo.setImage(
                    new Image(
                            getClass()
                                    .getResourceAsStream(
                                            "/images/default.png"
                                    )
                    )
            );
        }

        imageWrap.getChildren().add(logo);

        Label name =
                new Label(
                        n(a.getNom())
                );

        name.getStyleClass()
                .add("card-title");

        Label region =
                new Label(
                        "📍 " + n(a.getRegion())
                );

        Label phone =
                new Label(
                        "📞 " + a.getTel()
                );

        Label status =
                new Label(
                        a.isActive()
                                ? "ACTIVE"
                                : "INACTIVE"
                );

        status.getStyleClass().add(
                a.isActive()
                        ? "badge-active"
                        : "badge-inactive"
        );

        Button viewBtn =
                new Button("👁 View");

        viewBtn.getStyleClass()
                .add("icon-btn");

        viewBtn.setOnAction(
                e -> openShowPage(a)
        );

        HBox footer =
                new HBox(10);

        footer.setAlignment(
                Pos.CENTER_LEFT
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        footer.getChildren().addAll(
                status,
                spacer,
                viewBtn
        );

        card.getChildren().addAll(
                imageWrap,
                name,
                region,
                phone,
                footer
        );

        return card;
    }

    /* ================================================= */
    private void openShowPage(Association a) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/client_association/show.fxml"
                                    )
                    );

            Parent root =
                    loader.load();

            AssociationClientShow controller =
                    loader.getController();

            controller.setAssociation(a);
            controller.setLoggedUser(loggedUser);

            Stage stage =
                    (Stage) cardContainer
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Association Details"
            );

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ================================================= */
    private String n(String s) {

        return s == null || s.isBlank()
                ? "--"
                : s;
    }

    /* ================================================= */
    @FXML
    private void onSearch() {
        applyFilters();
    }

    @FXML
    private void onFilter() {
        applyFilters();
    }

    @FXML
    private void nextPage() {

        int totalPages =
                (int) Math.ceil(
                        (double) filteredData.size()
                                / pageSize
                );

        if (currentPage < totalPages - 1) {

            currentPage++;
            refreshPage();
        }
    }

    @FXML
    private void prevPage() {

        if (currentPage > 0) {

            currentPage--;
            refreshPage();
        }
    }





}