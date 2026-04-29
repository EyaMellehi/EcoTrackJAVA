package org.example.Utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class ModernNotification {

    public static void showSuccess(Stage owner, String title, String message) {
        show(owner, "✔", title, message, "#16a34a", "#ecfdf3");
    }

    public static void showError(Stage owner, String title, String message) {
        show(owner, "✖", title, message, "#dc2626", "#fef2f2");
    }

    public static void showWarning(Stage owner, String title, String message) {
        show(owner, "⚠", title, message, "#d97706", "#fff7ed");
    }

    public static void showInfo(Stage owner, String title, String message) {
        show(owner, "ℹ", title, message, "#2563eb", "#eff6ff");
    }
    public static boolean showConfirmation(String title, String message) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(yesButton, cancelButton);

        Label icon = new Label("?");
        icon.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2563eb;" +
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 10 14;" +
                        "-fx-border-color: #2563eb;" +
                        "-fx-border-radius: 999;"
        );

        Label lblTitle = new Label(title);
        lblTitle.setWrapText(true);
        lblTitle.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );

        Label lblMessage = new Label(message);
        lblMessage.setWrapText(true);
        lblMessage.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #4b5563;"
        );

        VBox textBox = new VBox(6, lblTitle, lblMessage);
        HBox content = new HBox(14, icon, textBox);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        content.setPadding(new javafx.geometry.Insets(18));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #dbe5dc;" +
                        "-fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 18, 0.2, 0, 6);"
        );

        Button yes = (Button) dialog.getDialogPane().lookupButton(yesButton);
        Button cancel = (Button) dialog.getDialogPane().lookupButton(cancelButton);

        yes.setStyle(
                "-fx-background-color: #16a34a;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 18;"
        );

        cancel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-text-fill: #374151;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 10 18;"
        );

        java.util.Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    private static void show(Stage owner, String iconText, String title, String message, String accent, String bg) {
        if (owner == null) return;

        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        Label icon = new Label(iconText);
        icon.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + accent + ";" +
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 10 12;" +
                        "-fx-border-color: " + accent + ";" +
                        "-fx-border-radius: 999;"
        );

        Label lblTitle = new Label(title);
        lblTitle.setWrapText(true);
        lblTitle.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );

        Label lblMessage = new Label(message);
        lblMessage.setWrapText(true);
        lblMessage.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #4b5563;"
        );

        VBox textBox = new VBox(4, lblTitle, lblMessage);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox root = new HBox(14, icon, textBox);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(16));
        root.setPrefWidth(360);
        root.setMaxWidth(360);
        root.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: " + accent + ";" +
                        "-fx-border-width: 0 0 0 6;" +
                        "-fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 18, 0.2, 0, 6);"
        );

        Scene ownerScene = owner.getScene();
        if (ownerScene != null) {
            root.setOpacity(0);
            root.setTranslateX(40);
        }

        popup.getContent().add(root);
        popup.show(owner);

        double x = owner.getX() + owner.getWidth() - 390;
        double y = owner.getY() + 60;

        popup.setX(x);
        popup.setY(y);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), root);
        slideIn.setFromX(40);
        slideIn.setToX(0);

        PauseTransition stay = new PauseTransition(Duration.seconds(3.2));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(220), root);
        slideOut.setFromX(0);
        slideOut.setToX(30);

        SequentialTransition sequence = new SequentialTransition(
                new javafx.animation.ParallelTransition(fadeIn, slideIn),
                stay,
                new javafx.animation.ParallelTransition(fadeOut, slideOut)
        );

        sequence.setOnFinished(e -> popup.hide());
        sequence.play();
    }
}