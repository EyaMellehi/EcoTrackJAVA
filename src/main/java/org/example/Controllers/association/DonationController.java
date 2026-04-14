package org.example.Controllers.association;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Entities.Association;
import org.example.Entities.Donation;
import org.example.Entities.User;
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
    private User loggedUser;

    private final DonationService service = new DonationService();

    public void setAssociation(Association association) {
        this.association = association;
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        if (user != null) {
            if (nomField != null) {
                nomField.setText(user.getName() != null ? user.getName() : "");
            }

            if (emailField != null) {
                emailField.setText(user.getEmail() != null ? user.getEmail() : "");
            }

            if (phoneField != null) {
                phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
            }
        }
    }

    @FXML
    public void initialize() {
        typeBox.getItems().addAll("Argent", "Matériel");

        toggleFields(null);
        typeBox.setOnAction(e -> toggleFields(typeBox.getValue()));

        montantField.setPromptText("Ex: 50");
        descriptionField.setPromptText("Décrire le matériel...");
        messageField.setPromptText("Votre message...");
        nomField.setPromptText("Votre nom");
        emailField.setPromptText("Votre email");
        phoneField.setPromptText("Votre téléphone");
    }

    private void toggleFields(String type) {
        boolean isArgent = "Argent".equals(type);
        boolean isMateriel = "Matériel".equals(type);

        montantField.setVisible(isArgent);
        montantField.setManaged(isArgent);

        descriptionField.setVisible(isMateriel);
        descriptionField.setManaged(isMateriel);

        clearErrors();
    }

    @FXML
    void saveDonation() {
        clearErrors();

        if (!validateForm()) {
            return;
        }

        try {
            if (association == null) {
                showError("Erreur", "Association introuvable.");
                return;
            }

            if (loggedUser == null) {
                showError("Erreur", "Aucun utilisateur connecté.");
                return;
            }

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
            d.setStatut("En attente");
            d.setAssociation(association);

            // correction principale
            d.setDonateur(loggedUser);

            service.add(d);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("✅ Donation envoyée avec succès !");
            alert.showAndWait();

            close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "❌ Erreur lors de l'envoi.");
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if (typeBox.getValue() == null) {
            errorType.setText("Choisir un type");
            valid = false;
        }

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorNom.setText("Nom obligatoire");
            valid = false;
        }

        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        if (email.isEmpty()) {
            errorEmail.setText("Email obligatoire");
            valid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorEmail.setText("Email invalide");
            valid = false;
        }

        String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
        if (phone.isEmpty()) {
            errorPhone.setText("Téléphone obligatoire");
            valid = false;
        } else if (!phone.matches("\\d{8,15}")) {
            errorPhone.setText("Numéro invalide");
            valid = false;
        }

        if ("Argent".equals(typeBox.getValue())) {
            String montant = montantField.getText() != null ? montantField.getText().trim() : "";

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

        if ("Matériel".equals(typeBox.getValue())) {
            String desc = descriptionField.getText() != null ? descriptionField.getText().trim() : "";
            if (desc.isEmpty()) {
                errorDescription.setText("Décrire le matériel");
                valid = false;
            }
        }

        return valid;
    }

    private void clearErrors() {
        errorType.setText("");
        errorMontant.setText("");
        errorDescription.setText("");
        errorNom.setText("");
        errorEmail.setText("");
        errorPhone.setText("");
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) typeBox.getScene().getWindow();
        stage.close();
    }
}