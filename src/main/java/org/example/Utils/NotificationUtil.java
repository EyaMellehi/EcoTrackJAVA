package org.example.Utils;

import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.util.Optional;

public class NotificationUtil {

    public static void showSuccess(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(4))
                .showInformation();
    }

    public static void showError(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(5))
                .showError();
    }

    public static void showWarning(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(5))
                .showWarning();
    }

    public static void showInfo(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(4))
                .showInformation();
    }

    public static boolean showConfirmation(String title, String message) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContentText(message);
        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Annuler", ButtonBar.ButtonData.NO);

        dialog.getDialogPane().getButtonTypes().addAll(yesButton, noButton);

        dialog.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        dialog.getDialogPane().lookupButton(yesButton).setStyle(
                "-fx-background-color: #2f9e44;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;"
        );

        dialog.getDialogPane().lookupButton(noButton).setStyle(
                "-fx-background-color: #ef4444;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;"
        );

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }
}