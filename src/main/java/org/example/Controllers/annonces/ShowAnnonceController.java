package org.example.Controllers.annonces;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Entities.Annonce;
import org.example.Entities.CommentaireAnnonce;
import org.example.Entities.User;
import org.example.Services.AnnonceReactionService;
import org.example.Services.AnnonceService;
import org.example.Services.AnnonceSummaryService;
import org.example.Services.AnnonceTranslationService;
import org.example.Services.CommentaireAnnonceService;
import org.example.Utils.InputValidationUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShowAnnonceController {

    private enum ReactionType {
        LIKE,
        DISLIKE
    }

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
    @FXML private Label lblResumeSimple;
    @FXML private Label lblResumeStatus;
    @FXML private TextArea taCommentaire;
    @FXML private Button btnPostComment;
    @FXML private Button btnLike;
    @FXML private Button btnDislike;
    @FXML private Button btnSummarize;
    @FXML private Button btnTranslateAr;
    @FXML private Button btnTranslateEn;
    @FXML private Label lblTranslationResult;
    @FXML private Label lblTranslationStatus;

    private final AnnonceService annonceService = new AnnonceService();
    private final AnnonceReactionService reactionService = new AnnonceReactionService();
    private final AnnonceSummaryService annonceSummaryService = new AnnonceSummaryService();
    private final AnnonceTranslationService translationService = new AnnonceTranslationService();
    private final CommentaireAnnonceService commentaireService = new CommentaireAnnonceService();
    private Annonce annonce;
    private User loggedUser;
    private CommentActionMode currentMode = CommentActionMode.NEW;
    private CommentaireAnnonce currentTargetComment;

    public void setAnnonce(Annonce a) {
        this.annonce = a;
        displayAnnonceData();
        clearSummaryState();
        clearTranslationState();
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
        if (btnSummarize != null) {
            btnSummarize.setOnAction(e -> handleSummarize());
        }
        if (btnTranslateAr != null) {
            btnTranslateAr.setOnAction(e -> handleTranslate("ar"));
        }
        if (btnTranslateEn != null) {
            btnTranslateEn.setOnAction(e -> handleTranslate("en"));
        }
        taCommentaire.setWrapText(true);
        setCommentFeedback("", true);
        resetCommentComposer();
        clearSummaryState();
        clearTranslationState();
        refreshReactionUi();
    }

    private void clearTranslationState() {
        if (lblTranslationResult != null) {
            lblTranslationResult.setText("Choisissez une langue pour traduire le titre et le contenu de l'annonce.");
        }
        if (lblTranslationStatus != null) {
            lblTranslationStatus.setText("");
        }
    }

    private void clearSummaryState() {
        if (lblResumeSimple != null) {
            lblResumeSimple.setText("Cliquez sur le bouton pour generer un resume.");
        }
        if (lblResumeStatus != null) {
            lblResumeStatus.setText("");
        }
    }

    @FXML
    private void handleSummarize() {
        if (annonce == null) {
            if (lblResumeStatus != null) {
                lblResumeStatus.setText("Annonce introuvable.");
            }
            return;
        }

        if (btnSummarize != null) {
            btnSummarize.setDisable(true);
        }
        if (lblResumeStatus != null) {
            lblResumeStatus.setText("Generation du resume...");
        }

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return annonceSummaryService.summarizeInThreeSimpleSentences(annonce);
            }
        };

        task.setOnSucceeded(event -> {
            String summary = task.getValue();
            if (lblResumeSimple != null) {
                lblResumeSimple.setText(summary == null || summary.isBlank()
                        ? "Resume indisponible pour le moment."
                        : summary);
            }
            if (lblResumeStatus != null) {
                lblResumeStatus.setText("Resume genere.");
            }
            if (btnSummarize != null) {
                btnSummarize.setDisable(false);
            }
        });

        task.setOnFailed(event -> {
            if (lblResumeSimple != null) {
                lblResumeSimple.setText("Resume indisponible pour le moment.");
            }
            if (lblResumeStatus != null) {
                Throwable ex = task.getException();
                lblResumeStatus.setText(ex != null ? ex.getMessage() : "Erreur de generation du resume.");
            }
            if (btnSummarize != null) {
                btnSummarize.setDisable(false);
            }
        });

        Thread thread = new Thread(task, "annonce-summary-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void handleTranslate(String targetLang) {
        if (annonce == null) {
            if (lblTranslationStatus != null) {
                lblTranslationStatus.setText("Annonce introuvable.");
            }
            return;
        }

        if (btnTranslateAr != null) {
            btnTranslateAr.setDisable(true);
        }
        if (btnTranslateEn != null) {
            btnTranslateEn.setDisable(true);
        }
        if (lblTranslationStatus != null) {
            lblTranslationStatus.setText("Traduction en cours...");
        }

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return translationService.translate(annonce, targetLang);
            }
        };

        task.setOnSucceeded(event -> {
            String result = task.getValue();
            if (lblTranslationResult != null) {
                lblTranslationResult.setText(result == null || result.isBlank()
                        ? "Traduction indisponible."
                        : result);
            }
            if (lblTranslationStatus != null) {
                lblTranslationStatus.setText("Traduction terminée.");
            }
            if (btnTranslateAr != null) {
                btnTranslateAr.setDisable(false);
            }
            if (btnTranslateEn != null) {
                btnTranslateEn.setDisable(false);
            }
        });

        task.setOnFailed(event -> {
            if (lblTranslationResult != null) {
                lblTranslationResult.setText("Traduction indisponible.");
            }
            if (lblTranslationStatus != null) {
                Throwable ex = task.getException();
                lblTranslationStatus.setText(ex != null ? ex.getMessage() : "Erreur lors de la traduction.");
            }
            if (btnTranslateAr != null) {
                btnTranslateAr.setDisable(false);
            }
            if (btnTranslateEn != null) {
                btnTranslateEn.setDisable(false);
            }
        });

        Thread thread = new Thread(task, "annonce-translation-task");
        thread.setDaemon(true);
        thread.start();
    }

    private boolean isCitizenUser() {
        return loggedUser != null
                && loggedUser.getRoles() != null
                && loggedUser.getRoles().contains("ROLE_CITOYEN");
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
        int annonceId = annonce.getId();

        try {
            int currentReaction = reactionService.getUserReaction(userId, annonceId);

            if (reactionType == ReactionType.LIKE) {
                if (currentReaction == 1) {
                    reactionService.removeReaction(userId, annonceId);
                    setCommentFeedback("Like retiré.", true);
                } else {
                    reactionService.setReaction(userId, annonceId, true);
                    setCommentFeedback("Annonce likée.", true);
                }
            } else {
                if (currentReaction == -1) {
                    reactionService.removeReaction(userId, annonceId);
                    setCommentFeedback("Dislike retiré.", true);
                } else {
                    reactionService.setReaction(userId, annonceId, false);
                    setCommentFeedback("Annonce dislikée.", true);
                }
            }
            refreshReactionUi();
        } catch (SQLException e) {
            setCommentFeedback("Impossible de sauvegarder la reaction: " + e.getMessage(), false);
        }
    }

    private void refreshReactionUi() {
        int likeCount = 0;
        int dislikeCount = 0;
        int currentReaction = 0;

        if (annonce != null) {
            try {
                AnnonceReactionService.ReactionCounts counts = reactionService.getReactionCounts(annonce.getId());
                likeCount = counts.likes;
                dislikeCount = counts.dislikes;
                if (loggedUser != null && loggedUser.getId() > 0) {
                    currentReaction = reactionService.getUserReaction(loggedUser.getId(), annonce.getId());
                }
            } catch (SQLException e) {
                likeCount = 0;
                dislikeCount = 0;
            }
        }

        if (lblLikeCount != null) {
            lblLikeCount.setText(likeCount + " like(s)");
        }
        if (lblDislikeCount != null) {
            lblDislikeCount.setText(dislikeCount + " dislike(s)");
        }

        boolean allowed = isCitizenUser() && annonce != null;
        if (btnLike != null) {
            btnLike.setDisable(!allowed);
            String likeBg = !allowed ? "#f3f4f6" : (currentReaction == 1 ? "#bbf7d0" : "#e8f5e9");
            btnLike.setStyle("-fx-background-color: " + likeBg + "; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 6 12;");
        }
        if (btnDislike != null) {
            btnDislike.setDisable(!allowed);
            String dislikeBg = !allowed ? "#f3f4f6" : (currentReaction == -1 ? "#fecaca" : "#fee2e2");
            btnDislike.setStyle("-fx-background-color: " + dislikeBg + "; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 6 12;");
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
            addReportButtonIfEligible(actions, comment);
        }

        box.getChildren().addAll(header, contentLabel, actions);
        return box;
    }

    private boolean canReportComment(CommentaireAnnonce c) {
        if (!isCitizenUser() || loggedUser == null || c == null) {
            return false;
        }
        Integer auteurId = c.getAuteurId();
        return auteurId == null || auteurId != loggedUser.getId();
    }

    private void addReportButtonIfEligible(HBox actions, CommentaireAnnonce comment) {
        if (!canReportComment(comment)) {
            return;
        }
        Button reportBtn = new Button("⚑ Signaler");
        reportBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1d4ed8; -fx-font-size: 12px; -fx-padding: 0;");
        reportBtn.setOnAction(e -> openSignalerPopup(comment));
        actions.getChildren().add(reportBtn);
    }

    private void openSignalerPopup(CommentaireAnnonce c) {
        if (!canReportComment(c)) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/annonces/signaler_commentaire.fxml"));
            Parent root = loader.load();
            SignalerCommentaireController ctrl = loader.getController();
            ctrl.setCommentaire(c);
            ctrl.setCitoyen(loggedUser);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Signalement");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException ex) {
            setCommentFeedback("Impossible d'ouvrir la fenêtre de signalement.", false);
        }
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
            HBox replyActions = new HBox(10);
            replyActions.setAlignment(Pos.CENTER_LEFT);
            addReportButtonIfEligible(replyActions, reply);
            if (!replyActions.getChildren().isEmpty()) {
                box.getChildren().add(replyActions);
            }
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



