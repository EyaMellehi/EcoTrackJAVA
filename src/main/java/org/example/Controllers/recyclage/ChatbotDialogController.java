package org.example.Controllers.recyclage;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.Services.ChatMessage;
import org.example.Services.ChatbotResult;
import org.example.Services.CohereChatbotHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotDialogController {

    @FXML private ScrollPane spChat;
    @FXML private VBox vbMessages;
    @FXML private TextArea taDraftPreview;
    @FXML private TextField tfUserMessage;

    private final CohereChatbotHelper chatbotHelper = new CohereChatbotHelper();

    private final List<ChatMessage> history = new ArrayList<>();
    private final Map<String, String> context = new HashMap<>();

    private TextArea targetDescriptionArea;
    private String mode = "POINT_DESC";

    public void setTargetDescriptionArea(TextArea targetDescriptionArea) {
        this.targetDescriptionArea = targetDescriptionArea;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setContext(Map<String, String> ctx) {
        context.clear();
        if (ctx != null) {
            context.putAll(ctx);
        }

        vbMessages.getChildren().clear();

        if ("RAPPORT_COMMENT".equalsIgnoreCase(mode)) {
            addMessageBubble("assistant",
                    "Bonjour 👋\nJe peux t’aider uniquement à rédiger un commentaire de rapport de recyclage.");
        } else {
            addMessageBubble("assistant",
                    "Bonjour 👋\nJe peux t’aider uniquement à rédiger une description liée au point de recyclage.");
        }
    }

    @FXML
    private void sendMessage() {
        String userMessage = tfUserMessage.getText() == null ? "" : tfUserMessage.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        addMessageBubble("user", userMessage);
        history.add(new ChatMessage("user", userMessage));

        if (history.size() > 12) {
            history.remove(0);
        }

        tfUserMessage.clear();

        try {
            ChatbotResult result = chatbotHelper.reply(mode, history, context);

            String reply = result.getReply() == null ? "" : result.getReply().trim();
            String draft = result.getDraft() == null ? "" : result.getDraft().trim();

            addMessageBubble("assistant", reply);
            history.add(new ChatMessage("assistant", reply));

            if (history.size() > 12) {
                history.remove(0);
            }

            taDraftPreview.setText(draft);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de contacter l’assistant IA.");
        }
    }

    @FXML
    private void insertDraft() {
        String draft = taDraftPreview.getText() == null ? "" : taDraftPreview.getText().trim();

        if (draft.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun brouillon", "Aucun brouillon disponible à insérer.");
            return;
        }

        if (targetDescriptionArea == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Champ cible introuvable.");
            return;
        }

        targetDescriptionArea.setText(draft);
        closeDialog();
    }

    @FXML
    private void resetChat() {
        history.clear();
        taDraftPreview.clear();
        vbMessages.getChildren().clear();

        if ("RAPPORT_COMMENT".equalsIgnoreCase(mode)) {
            addMessageBubble("assistant",
                    "Chat réinitialisé.\nJe peux t’aider uniquement à rédiger un commentaire de rapport de recyclage.");
        } else {
            addMessageBubble("assistant",
                    "Chat réinitialisé.\nJe peux t’aider uniquement à rédiger une description liée au point de recyclage.");
        }
    }

    @FXML
    private void closeDialog() {
        Stage stage = (Stage) vbMessages.getScene().getWindow();
        stage.close();
    }

    private void addMessageBubble(String role, String message) {
        HBox row = new HBox();
        row.setPadding(new Insets(2, 4, 2, 4));

        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(520);
        bubble.setPadding(new Insets(10, 14, 10, 14));

        if ("user".equalsIgnoreCase(role)) {
            row.setAlignment(Pos.CENTER_RIGHT);

            bubble.setStyle(
                    "-fx-background-color: #2f9e44;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18 18 4 18;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;"
            );

            row.getChildren().add(bubble);

        } else {
            row.setAlignment(Pos.CENTER_LEFT);

            Label avatar = new Label("🤖");
            avatar.setMinSize(34, 34);
            avatar.setPrefSize(34, 34);
            avatar.setAlignment(Pos.CENTER);
            avatar.setStyle(
                    "-fx-background-color: #eef7ee;" +
                            "-fx-background-radius: 999;" +
                            "-fx-font-size: 16px;"
            );

            bubble.setStyle(
                    "-fx-background-color: #f3f4f6;" +
                            "-fx-text-fill: #111827;" +
                            "-fx-background-radius: 18 18 18 4;" +
                            "-fx-font-size: 13px;"
            );

            HBox inner = new HBox(8);
            inner.setAlignment(Pos.BOTTOM_LEFT);
            inner.getChildren().addAll(avatar, bubble);

            row.getChildren().add(inner);
        }

        vbMessages.getChildren().add(row);

        if (spChat != null) {
            spChat.layout();
            spChat.setVvalue(1.0);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}