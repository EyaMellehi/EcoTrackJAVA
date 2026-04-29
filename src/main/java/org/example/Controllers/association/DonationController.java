package org.example.Controllers.association;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.Association;
import org.example.Entities.Donation;
import org.example.Entities.User;
import org.example.Services.DonationMailService;
import org.example.Services.DonationService;
import org.example.Services.StripePaymentService;

public class DonationController {

    @FXML private ComboBox<String> typeBox;

    @FXML private TextField montantField;
    @FXML private TextArea descriptionField;
    @FXML private TextArea messageField;

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    @FXML private Label errorType;
    @FXML private Label errorMontant;
    @FXML private Label errorDescription;
    @FXML private Label errorNom;
    @FXML private Label errorEmail;
    @FXML private Label errorPhone;

    private Association association;

    /* ✅ REAL CONNECTED USER */
    private User currentUser;

    private final DonationService service = new DonationService();

    /* ========================================= */
    public void setAssociation(Association association) {
        this.association = association;
    }

    /* ========================================= */
    public void setLoggedUser(User user) {

        this.currentUser = user;

        if (user != null) {

            nomField.setText(
                    user.getName() != null
                            ? user.getName()
                            : ""
            );

            emailField.setText(
                    user.getEmail() != null
                            ? user.getEmail()
                            : ""
            );

            phoneField.setText(
                    user.getPhone() != null
                            ? user.getPhone()
                            : ""
            );
        }
    }

    /* ========================================= */
    @FXML
    public void initialize() {

        typeBox.getItems().addAll(
                "Argent",
                "Matériel"
        );

        toggleFields(null);

        typeBox.setOnAction(
                e -> toggleFields(typeBox.getValue())
        );

        montantField.setPromptText("Ex: 50");
        descriptionField.setPromptText("Décrire le matériel...");
        messageField.setPromptText("Votre message...");
    }

    /* ========================================= */
    private void toggleFields(String type) {

        boolean isArgent =
                "Argent".equals(type);

        boolean isMateriel =
                "Matériel".equals(type);

        montantField.setVisible(isArgent);
        montantField.setManaged(isArgent);

        descriptionField.setVisible(isMateriel);
        descriptionField.setManaged(isMateriel);

        clearErrors();
    }

    /* ========================================= */
    @FXML
    void saveDonation() {

        clearErrors();

        if (!validateForm()) {
            return;
        }

        try {

            /* ============================= */
            if (association == null) {
                showError(
                        "Erreur",
                        "Association introuvable."
                );
                return;
            }

            if (currentUser == null) {
                showError(
                        "Erreur",
                        "Utilisateur non connecté."
                );
                return;
            }

            Donation d = new Donation();

            d.setType(typeBox.getValue());
            d.setAssociation(association);
            d.setDonateur(currentUser);
            d.setMessageDon(
                    messageField.getText().trim()
            );

            /* ===================================== */
            /* MONEY DONATION => STRIPE PAYMENT      */
            /* INSERT ONLY AFTER SUCCESS             */
            /* ===================================== */
            if ("Argent".equals(typeBox.getValue())) {

                double amount =
                        Double.parseDouble(
                                montantField.getText().trim()
                        );

                d.setMontant(amount);
                d.setDescriptionMateriel(null);
                d.setStatut("Pending");

                StripePaymentService stripe =
                        new StripePaymentService(
                                service,
                                d,
                                currentUser
                        );

                String url =
                        stripe.createCheckoutSession(
                                amount,
                                currentUser,
                                association
                        );

                Alert info =
                        new Alert(
                                Alert.AlertType.INFORMATION
                        );

                info.setTitle("Stripe");
                info.setHeaderText(null);
                info.setContentText(
                        "Redirection vers Stripe..."
                );

                info.showAndWait();

                java.awt.Desktop
                        .getDesktop()
                        .browse(
                                new java.net.URI(url)
                        );

                close();
                return;
            }

            /* ===================================== */
            /* MATERIAL DONATION NORMAL              */
            /* ===================================== */
            d.setMontant(0.0);

            d.setDescriptionMateriel(
                    descriptionField
                            .getText()
                            .trim()
            );

            d.setStatut("En attente");

            service.add(d);

            DonationMailService mail =
                    new DonationMailService();

            mail.sendDonationPdf(
                    currentUser,
                    d
            );

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Donation envoyée avec succès !"
            );

            alert.showAndWait();

            close();

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Erreur",
                    "Erreur lors de la donation."
            );
        }
    }
    /* ========================================= */
    private boolean validateForm() {

        boolean valid = true;

        if (typeBox.getValue() == null) {
            errorType.setText("Choisir un type");
            valid = false;
        }

        return valid;
    }

    /* ========================================= */
    private void clearErrors() {

        errorType.setText("");
        errorMontant.setText("");
        errorDescription.setText("");
        errorNom.setText("");
        errorEmail.setText("");
        errorPhone.setText("");
    }

    /* ========================================= */
    private void showError(String title, String msg) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /* ========================================= */
    @FXML
    private void close() {

        Stage stage =
                (Stage) typeBox
                        .getScene()
                        .getWindow();

        stage.close();
    }
}