package org.example.Controllers.association;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.Association;
import org.example.Entities.Donation;
import org.example.Services.DonationService;

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
    private final DonationService service = new DonationService();

    public void setAssociation(Association association) {
        this.association = association;
    }

    @FXML
    public void initialize() {

        // types donation
        typeBox.getItems().addAll("Argent", "Matériel");

        // hide dynamic fields
        toggleFields(null);

        // listener
        typeBox.setOnAction(e -> toggleFields(typeBox.getValue()));

        // placeholders
        montantField.setPromptText("Ex: 50");
        descriptionField.setPromptText("Décrire le matériel...");
        messageField.setPromptText("Votre message...");
        nomField.setPromptText("Votre nom");
        emailField.setPromptText("Votre email");
        phoneField.setPromptText("Votre téléphone");
    }

    // ====================================================
    // TOGGLE FIELDS
    // ====================================================
    private void toggleFields(String type) {

        boolean isArgent = "Argent".equals(type);
        boolean isMateriel = "Matériel".equals(type);

        // Montant
        montantField.setVisible(isArgent);
        montantField.setManaged(isArgent);

        // Description matériel
        descriptionField.setVisible(isMateriel);
        descriptionField.setManaged(isMateriel);

        clearErrors();
    }

    // ====================================================
    // SAVE DONATION
    // ====================================================
    @FXML
    void saveDonation() {

        clearErrors();

        if (!validateForm()) {
            return;
        }

        try {

            Donation d = new Donation();

            d.setType(typeBox.getValue());

            if ("Argent".equals(typeBox.getValue())) {
                d.setMontant(Double.parseDouble(montantField.getText().trim()));
                d.setDescriptionMateriel(null);
            } else {
                d.setMontant(0.0);
                d.setDescriptionMateriel(descriptionField.getText().trim());
            }

            d.setMessageDon(messageField.getText().trim());
          //  d.setDonateur();
            d.setStatut("En attente");
            d.setAssociation(association);

            service.add(d);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("✅ Donation envoyée avec succès !");
            alert.showAndWait();

            close();

        } catch (Exception e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("❌ Erreur lors de l'envoi.");
            alert.showAndWait();

            e.printStackTrace();
        }
    }

    // ====================================================
    // VALIDATION
    // ====================================================
    private boolean validateForm() {

        boolean valid = true;

        // type
        if (typeBox.getValue() == null) {
            errorType.setText("Choisir un type");
            valid = false;
        }

        // nom
        if (nomField.getText().trim().isEmpty()) {
            errorNom.setText("Nom obligatoire");
            valid = false;
        }

        // email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errorEmail.setText("Email obligatoire");
            valid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorEmail.setText("Email invalide");
            valid = false;
        }

        // phone
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            errorPhone.setText("Téléphone obligatoire");
            valid = false;
        } else if (!phone.matches("\\d{8,15}")) {
            errorPhone.setText("Numéro invalide");
            valid = false;
        }

        // argent
        if ("Argent".equals(typeBox.getValue())) {

            String montant = montantField.getText().trim();

            if (montant.isEmpty()) {
                errorMontant.setText("Montant obligatoire");
                valid = false;
            } else {
                try {
                    double m = Double.parseDouble(montant);

                    if (m <= 0) {
                        errorMontant.setText("Montant invalide");
                        valid = false;
                    }

                } catch (Exception e) {
                    errorMontant.setText("Nombre invalide");
                    valid = false;
                }
            }
        }

        // matériel
        if ("Matériel".equals(typeBox.getValue())) {

            if (descriptionField.getText().trim().isEmpty()) {
                errorDescription.setText("Décrire le matériel");
                valid = false;
            }
        }

        return valid;
    }

    // ====================================================
    // CLEAR ERRORS
    // ====================================================
    private void clearErrors() {

        errorType.setText("");
        errorMontant.setText("");
        errorDescription.setText("");
        errorNom.setText("");
        errorEmail.setText("");
        errorPhone.setText("");
    }

    // ====================================================
    // CLOSE
    // ====================================================
    @FXML
    private void close() {
        Stage stage = (Stage) typeBox.getScene().getWindow();
        stage.close();
    }
}