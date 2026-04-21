package org.example.Controllers.annonces;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.Entities.Annonce;
import org.example.Entities.CommentaireAnnonce;
import org.example.Entities.User;
import org.example.Services.AnnonceService;
import org.example.Services.CommentaireAnnonceService;
import org.example.Utils.InputValidationUtil;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShowAnnonceController {

    private enum ReactionType {
        LIKE,
        DISLIKE
    }

    // Store session (non persistant) des reactions utilisateur par annonce.
    private static final Map<Integer, Set<Integer>> LIKE_USERS_BY_ANNONCE = new HashMap<>();
    private static final Map<Integer, Set<Integer>> DISLIKE_USERS_BY_ANNONCE = new HashMap<>();

    private enum CommentActionMode {
        NEW,
        REPLY,
        EDIT
    }

    @FXML private Label lblTitre;
    @FXML private Label lblCategorie;
    @FXML private Label lblRegion;
    @FXML private Label lblDate;
    @FXML private Label lblAuteur;
    @FXML private Label lblContenu;
    @FXML private ImageView imgMedia;
    @FXML private VBox commentsContainer;
    @FXML private Label lblCommentCount;
    @FXML private Label lblCommentStatus;
    @FXML private Label lblLikeCount;
    @FXML private Label lblDislikeCount;
    @FXML private Label lblCommentFeedback;
    @FXML private TextArea taCommentaire;
    @FXML private Button btnPostComment;
    @FXML private Button btnLike;
    @FXML private Button btnDislike;

    private final AnnonceService annonceService = new AnnonceService();
    private final CommentaireAnnonceService commentaireService = new CommentaireAnnonceService();
    private Annonce annonce;
    private User loggedUser;
    private CommentActionMode currentMode = CommentActionMode.NEW;
    private CommentaireAnnonce currentTargetComment;

    public void setAnnonce(Annonce a) {
        this.annonce = a;
        displayAnnonceData();
        refreshReactionUi();
        loadComments();
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
        refreshReactionUi();
        if (annonce != null) {
            loadComments();
        }
    }

    @FXML
    public void initialize() {
        btnPostComment.setOnAction(e -> postComment());
        if (btnLike != null) {
            btnLike.setOnAction(e -> handleLike());
        }
        if (btnDislike != null) {
            btnDislike.setOnAction(e -> handleDislike());
        }
        taCommentaire.setWrapText(true);
        setCommentFeedback("", true);
        resetCommentComposer();
        refreshReactionUi();
    }

    private boolean isCitizenUser() {
        return loggedUser != null
                && loggedUser.getRoles() != null
                && loggedUser.getRoles().contains("ROLE_CITOYEN");
    }

    private Set<Integer> likeUsers() {
        if (annonce == null) {
            return java.util.Collections.emptySet();
        }
        return LIKE_USERS_BY_ANNONCE.computeIfAbsent(annonce.getId(), key -> new HashSet<>());
    }

    private Set<Integer> dislikeUsers() {
        if (annonce == null) {
            return java.util.Collections.emptySet();
        }
        return DISLIKE_USERS_BY_ANNONCE.computeIfAbsent(annonce.getId(), key -> new HashSet<>());
    }

    private void handleLike() {
        applyReactionToggle(ReactionType.LIKE);
    }

    private void handleDislike() {
        applyReactionToggle(ReactionType.DISLIKE);
    }

    private void applyReactionToggle(ReactionType reactionType) {
        if (!isCitizenUser()) {
            setCommentFeedback("Seul un citoyen connecté peut liker/disliker une annonce.", false);
            return;
        }
        if (annonce == null || loggedUser == null) {
            setCommentFeedback("Annonce introuvable ou utilisateur non connecté.", false);
            return;
        }

        int userId = loggedUser.getId();
        Set<Integer> likes = likeUsers();
        Set<Integer> dislikes = dislikeUsers();

        if (reactionType == ReactionType.LIKE) {
            if (likes.contains(userId)) {
                likes.remove(userId);
                setCommentFeedback("Like retiré.", true);
            } else {
                likes.add(userId);
                dislikes.remove(userId);
                setCommentFeedback("Annonce likée.", true);
            }
        } else {
            if (dislikes.contains(userId)) {
                dislikes.remove(userId);
                setCommentFeedback("Dislike retiré.", true);
            } else {
                dislikes.add(userId);
                likes.remove(userId);
                setCommentFeedback("Annonce dislikée.", true);
            }
        }

        refreshReactionUi();
    }

    private void refreshReactionUi() {
        int likeCount = annonce == null ? 0 : likeUsers().size();
        int dislikeCount = annonce == null ? 0 : dislikeUsers().size();

        if (lblLikeCount != null) {
            lblLikeCount.setText(likeCount + " like(s)");
        }
        if (lblDislikeCount != null) {
            lblDislikeCount.setText(dislikeCount + " dislike(s)");
        }

        boolean allowed = isCitizenUser() && annonce != null;
        if (btnLike != null) {
            btnLike.setDisable(!allowed);
            btnLike.setStyle("-fx-background-color: " + (allowed ? "#e8f5e9" : "#f3f4f6") + "; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 6 12;");
        }
        if (btnDislike != null) {
            btnDislike.setDisable(!allowed);
            btnDislike.setStyle("-fx-background-color: " + (allowed ? "#fee2e2" : "#f3f4f6") + "; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 6 12;");
        }
    }

    private void resetCommentComposer() {
        currentMode = CommentActionMode.NEW;
        currentTargetComment = null;
        taCommentaire.clear();
        taCommentaire.setPromptText("Écrivez votre commentaire ici...");
        btnPostComment.setText("Publier le commentaire");
    }

    private void displayAnnonceData() {
        if (annonce == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        lblTitre.setText(annonce.getTitre() != null ? annonce.getTitre() : "");
        lblCategorie.setText("Catégorie: " + (annonce.getCategorie() != null ? annonce.getCategorie() : ""));
        lblRegion.setText("Région: " + (annonce.getRegion() != null ? annonce.getRegion() : ""));
        lblDate.setText("Publiée: " + (annonce.getDatePub() != null ? annonce.getDatePub().format(formatter) : ""));
        lblAuteur.setText("Par: " + (annonce.getAuteurNom() != null ? annonce.getAuteurNom() : "Inconnu"));
        lblContenu.setText(annonce.getContenu() != null ? annonce.getContenu() : "");
        lblContenu.setWrapText(true);

        if (annonce.getMediaPath() != null && !annonce.getMediaPath().isEmpty()) {
            try {
                File imgFile = new File(annonce.getMediaPath());
                if (imgFile.exists()) {
                    imgMedia.setImage(new javafx.scene.image.Image(imgFile.toURI().toString()));
                    imgMedia.setVisible(true);
                }
            } catch (Exception e) {
                imgMedia.setVisible(false);
            }
        } else {
            imgMedia.setVisible(false);
        }
    }

    @FXML
    private void loadComments() {
        try {
            Integer viewerId = loggedUser != null ? loggedUser.getId() : null;
            List<CommentaireAnnonce> comments = commentaireService.getByAnnonceIdForViewer(annonce.getId(), viewerId);
            renderComments(comments);
            updateCommentSummary();
        } catch (SQLException e) {
            setCommentFeedback("Impossible de charger les commentaires: " + e.getMessage(), false);
        }
    }

    private void updateCommentSummary() {
        try {
            Integer viewerId = loggedUser != null ? loggedUser.getId() : null;
            int count = commentaireService.countTotalVisibleComments(annonce.getId(), viewerId);
            if (lblCommentCount != null) {
                lblCommentCount.setText(count + " commentaire(s) visible(s)");
            }
            if (lblCommentStatus != null) {
                lblCommentStatus.setText(count == 0 ? "Aucun commentaire visible" : "Commentaires visibles mis à jour");
            }
        } catch (SQLException e) {
            if (lblCommentCount != null) {
                lblCommentCount.setText("Commentaires indisponibles");
            }
        }
    }

    private void setCommentFeedback(String message, boolean success) {
        if (lblCommentFeedback == null) {
            return;
        }

        lblCommentFeedback.setText(message == null ? "" : message);
        if (message == null || message.isBlank()) {
            lblCommentFeedback.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            return;
        }

        if (success) {
            lblCommentFeedback.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #166534; -fx-background-color: #dcfce7; -fx-padding: 8 12; -fx-background-radius: 8;");
        } else {
            lblCommentFeedback.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b91c1c; -fx-background-color: #fee2e2; -fx-padding: 8 12; -fx-background-radius: 8;");
        }
    }

    private void renderComments(List<CommentaireAnnonce> comments) {
        commentsContainer.getChildren().clear();

        if (comments.isEmpty()) {
            Label noComments = new Label("Aucun commentaire pour le moment. Soyez le premier!");
            noComments.setStyle("-fx-text-fill: #7a8087; -fx-font-size: 13px;");
            commentsContainer.getChildren().add(noComments);
            return;
        }

        for (CommentaireAnnonce comment : comments) {
            VBox commentBox = createCommentItem(comment);

            try {
                Integer viewerId = loggedUser != null ? loggedUser.getId() : null;
                List<CommentaireAnnonce> replies = commentaireService.getByParentIdForViewer(comment.getId(), viewerId);
                if (!replies.isEmpty()) {
                    VBox repliesContainer = new VBox(8);
                    repliesContainer.setStyle("-fx-padding: 8 0 0 28;");
                    for (CommentaireAnnonce reply : replies) {
                        repliesContainer.getChildren().add(createReplyItem(reply));
                    }
                    commentBox.getChildren().add(repliesContainer);
                }
            } catch (SQLException e) {
                setCommentFeedback("Impossible de charger une réponse: " + e.getMessage(), false);
            }

            commentsContainer.getChildren().add(commentBox);
        }
    }

    private VBox createCommentItem(CommentaireAnnonce comment) {
        VBox box = new VBox(5);
        box.setStyle("-fx-padding: 15; -fx-border-color: #e5e7eb; -fx-background-color: #f9fafb; " +
                     "-fx-background-radius: 6; -fx-border-radius: 6;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(resolveAuthorName(comment));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(comment.getDateComm().format(formatter));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7a8087;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(nameLabel, dateLabel, spacer);

        if (comment.getModerationStatus() != null && !"Approuvé".equalsIgnoreCase(comment.getModerationStatus())) {
            Label statusLabel = new Label("EN ATTENTE");
            statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #92400e; -fx-background-color: #fef3c7; -fx-padding: 4 10; -fx-background-radius: 999;");
            header.getChildren().add(statusLabel);
        } else {
            Label statusLabel = new Label("APPROUVÉ");
            statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #16a34a; -fx-padding: 4 10; -fx-background-radius: 999;");
            header.getChildren().add(statusLabel);
        }

        Label contentLabel = new Label(comment.getDescription() != null ? comment.getDescription() : "");
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        contentLabel.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button replyBtn = new Button("↩️ Répondre");
        replyBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2e7d32; -fx-font-size: 12px; -fx-padding: 0;");
        replyBtn.setOnAction(e -> replyToComment(comment));

        if (loggedUser != null && loggedUser.getId() == comment.getAuteurId()) {
            Button editBtn = new Button("✏️ Modifier");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0066cc; -fx-font-size: 12px; -fx-padding: 0;");
            editBtn.setOnAction(e -> editComment(comment));

            Button deleteBtn = new Button("🗑️ Supprimer");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-font-size: 12px; -fx-padding: 0;");
            deleteBtn.setOnAction(e -> deleteComment(comment));

            actions.getChildren().addAll(replyBtn, editBtn, deleteBtn);
        } else {
            actions.getChildren().add(replyBtn);
        }

        box.getChildren().addAll(header, contentLabel, actions);
        return box;
    }

    private VBox createReplyItem(CommentaireAnnonce reply) {
        VBox box = new VBox(5);
        box.setStyle("-fx-padding: 12 15; -fx-border-color: #e5e7eb; -fx-background-color: #ffffff; " +
                     "-fx-background-radius: 4; -fx-border-radius: 4;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(resolveAuthorName(reply));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-font-size: 12px;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(reply.getDateComm().format(formatter));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7a8087;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(nameLabel, dateLabel, spacer);

        if (reply.getModerationStatus() != null && !"Approuvé".equalsIgnoreCase(reply.getModerationStatus())) {
            Label statusLabel = new Label("EN ATTENTE");
            statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #92400e; -fx-background-color: #fef3c7; -fx-padding: 4 10; -fx-background-radius: 999;");
            header.getChildren().add(statusLabel);
        } else {
            Label statusLabel = new Label("APPROUVÉ");
            statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #16a34a; -fx-padding: 4 10; -fx-background-radius: 999;");
            header.getChildren().add(statusLabel);
        }

        Label contentLabel = new Label(reply.getDescription() != null ? reply.getDescription() : "");
        contentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4b5563;");
        contentLabel.setWrapText(true);

        if (loggedUser != null && loggedUser.getId() == reply.getAuteurId()) {
            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_LEFT);

            Button editBtn = new Button("✏️ Modifier");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0066cc; -fx-font-size: 11px; -fx-padding: 0;");
            editBtn.setOnAction(e -> editComment(reply));

            Button deleteBtn = new Button("🗑️ Supprimer");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-font-size: 11px; -fx-padding: 0;");
            deleteBtn.setOnAction(e -> deleteComment(reply));

            actions.getChildren().addAll(editBtn, deleteBtn);
            box.getChildren().addAll(header, contentLabel, actions);
        } else {
            box.getChildren().addAll(header, contentLabel);
        }

        return box;
    }

    @FXML
    private void postComment() {
        String content = InputValidationUtil.normalize(taCommentaire.getText());
        String error = InputValidationUtil.validateComment(content);
        if (error != null) {
            setCommentFeedback(error, false);
            return;
        }

        if (loggedUser == null) {
            setCommentFeedback("Vous devez être connecté pour commenter.", false);
            return;
        }

        try {
            if (currentMode == CommentActionMode.EDIT && currentTargetComment != null) {
                currentTargetComment.setDescription(content);
                commentaireService.update(currentTargetComment);
                setCommentFeedback("Commentaire modifié avec succès.", true);
            } else {
                CommentaireAnnonce comment = new CommentaireAnnonce(content, annonce.getId(), loggedUser.getId());
                if (currentMode == CommentActionMode.REPLY && currentTargetComment != null) {
                    comment.setParentId(currentTargetComment.getId());
                }
                comment.setModerationStatus("Approuvé");
                commentaireService.add(comment);
                if (currentMode == CommentActionMode.REPLY) {
                    setCommentFeedback("Votre réponse est valide et a été publiée.", true);
                } else {
                    setCommentFeedback("Votre commentaire est valide et a été publié.", true);
                }
            }

            resetCommentComposer();
            loadComments();
        } catch (SQLException e) {
            setCommentFeedback("Impossible de poster le commentaire: " + e.getMessage(), false);
        }
    }

    private void replyToComment(CommentaireAnnonce parentComment) {
        if (loggedUser == null) {
            setCommentFeedback("Vous devez être connecté pour répondre.", false);
            return;
        }

        currentMode = CommentActionMode.REPLY;
        currentTargetComment = parentComment;
        taCommentaire.clear();
        taCommentaire.setPromptText("Réponse à " + resolveAuthorName(parentComment) + "...");
        btnPostComment.setText("Publier la réponse");
        taCommentaire.requestFocus();
        setCommentFeedback("Mode réponse activé. Écrivez votre réponse puis cliquez sur Publier la réponse.", true);
    }

    private void editComment(CommentaireAnnonce comment) {
        currentMode = CommentActionMode.EDIT;
        currentTargetComment = comment;
        taCommentaire.setText(comment.getDescription() != null ? comment.getDescription() : "");
        taCommentaire.setPromptText("Modifier votre commentaire...");
        btnPostComment.setText("Enregistrer la modification");
        taCommentaire.requestFocus();
        taCommentaire.positionCaret(taCommentaire.getText().length());
        setCommentFeedback("Mode modification activé. Modifiez le texte puis cliquez sur Enregistrer la modification.", true);
    }

    private void deleteComment(CommentaireAnnonce comment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Êtes-vous sûr?");
        confirm.setContentText("Voulez-vous supprimer ce commentaire? Cette action est irréversible.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                commentaireService.delete(comment.getId(), loggedUser.getId());
                if (currentTargetComment != null && currentTargetComment.getId() == comment.getId()) {
                    resetCommentComposer();
                }
                loadComments();
                setCommentFeedback("Commentaire supprimé.", true);
            } catch (SQLException e) {
                setCommentFeedback("Impossible de supprimer le commentaire: " + e.getMessage(), false);
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String resolveAuthorName(CommentaireAnnonce comment) {
        if (comment == null) {
            return "Anonyme";
        }
        if (loggedUser != null && comment.getAuteurId() != null && comment.getAuteurId() == loggedUser.getId()) {
            String connectedName = loggedUser.getName();
            if (connectedName != null && !connectedName.isBlank()) {
                return connectedName;
            }
        }
        if (comment.getAuteurNom() != null && !comment.getAuteurNom().isBlank()) {
            return comment.getAuteurNom();
        }
        return "Anonyme";
    }
}



