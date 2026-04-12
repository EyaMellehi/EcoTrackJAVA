# 🔧 Implémentation Optionnelle: Édition et Suppression des Commentaires

Ce guide montre comment ajouter la fonctionnalité d'édition et suppression des commentaires.

## 🎯 Objectif

Permettre à l'utilisateur de:
1. **Éditer** un commentaire existant
2. **Supprimer** un commentaire

## 📋 Architecture

### 1. Modification de AfficherDetailsAnnonce.java

Ajouter une classe interne pour les commentaires affichables:

```java
// Ajouter cette classe interne dans AfficherDetailsAnnonce.java
private class CommentaireItem {
    Commentaire commentaire;
    String texte;
    
    public CommentaireItem(Commentaire c) {
        this.commentaire = c;
        this.texte = c.getAuteur() + " (" + c.getDateCreation() + "):\n" + c.getTexte() + "\n---";
    }
    
    @Override
    public String toString() {
        return texte;
    }
}
```

### 2. Modification du ListView des Commentaires

Remplacer le ListView simple par une ListView avec cellules personnalisées:

```fxml
<!-- Dans AfficherDetailsAnnonce.fxml -->
<ListView fx:id="commentairesListView" prefHeight="150" VBox.vgrow="ALWAYS">
    <!-- Les cellules seront créées en Java -->
</ListView>
```

### 3. Méthode de Chargement des Commentaires

```java
private void chargerCommentaires() {
    try {
        List<Commentaire> commentaires = commentaireService.readByAnnonceId(annonce.getId());
        ObservableList<CommentaireItem> items = FXCollections.observableArrayList();
        
        for (Commentaire c : commentaires) {
            items.add(new CommentaireItem(c));
        }
        
        commentairesListView.setItems(items);
        
        // Ajouter le menu contextuel
        commentairesListView.setCellFactory(param -> new CommentaireCellule());
        
    } catch (SQLException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText("Erreur lors du chargement des commentaires: " + e.getMessage());
        alert.show();
    }
}
```

### 4. Classe Cellule Personnalisée

```java
// Ajouter cette classe interne dans AfficherDetailsAnnonce.java
private class CommentaireCellule extends ListCell<CommentaireItem> {
    @Override
    protected void updateItem(CommentaireItem item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            setText(null);
            setContextMenu(null);
        } else {
            setText(item.toString());
            
            // Créer un menu contextuel
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem editer = new MenuItem("✏️ Éditer");
            editer.setOnAction(e -> editerCommentaire(item.commentaire));
            
            MenuItem supprimer = new MenuItem("🗑️ Supprimer");
            supprimer.setOnAction(e -> supprimerCommentaire(item.commentaire));
            
            contextMenu.getItems().addAll(editer, supprimer);
            setContextMenu(contextMenu);
        }
    }
}
```

### 5. Méthode pour Éditer un Commentaire

```java
private void editerCommentaire(Commentaire commentaire) {
    // Afficher un dialogue d'édition
    Dialog<Commentaire> dialog = new Dialog<>();
    dialog.setTitle("Éditer le Commentaire");
    dialog.setHeaderText("Modifiez le commentaire:");
    
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20));
    
    TextField auteurField = new TextField();
    auteurField.setText(commentaire.getAuteur());
    
    TextArea texteField = new TextArea();
    texteField.setText(commentaire.getTexte());
    texteField.setPrefRowCount(5);
    texteField.setWrapText(true);
    
    grid.add(new Label("Auteur:"), 0, 0);
    grid.add(auteurField, 1, 0);
    grid.add(new Label("Texte:"), 0, 1);
    grid.add(texteField, 1, 1);
    
    dialog.getDialogPane().setContent(grid);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    
    dialog.setResultConverter(buttonType -> {
        if (buttonType == ButtonType.OK) {
            commentaire.setAuteur(auteurField.getText());
            commentaire.setTexte(texteField.getText());
            return commentaire;
        }
        return null;
    });
    
    dialog.showAndWait().ifPresent(c -> {
        try {
            commentaireService.update(c);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Commentaire modifié avec succès!");
            alert.show();
            chargerCommentaires();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la modification: " + e.getMessage());
            alert.show();
        }
    });
}
```

### 6. Méthode pour Supprimer un Commentaire

```java
private void supprimerCommentaire(Commentaire commentaire) {
    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Confirmation");
    confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce commentaire?");
    
    if (confirmation.showAndWait().get() == ButtonType.OK) {
        try {
            commentaireService.delete(commentaire);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Commentaire supprimé avec succès!");
            alert.show();
            chargerCommentaires();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la suppression: " + e.getMessage());
            alert.show();
        }
    }
}
```

### 7. Imports Requis

Ajouter ces imports à AfficherDetailsAnnonce.java:

```java
import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.GridPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
```

## 📝 Résumé des Modifications

| Fichier | Changement |
|---------|-----------|
| AfficherDetailsAnnonce.java | Ajout méthodes édition/suppression |
| AfficherDetailsAnnonce.java | Ajout classe interne CommentaireCellule |
| AfficherDetailsAnnonce.java | Ajout classe interne CommentaireItem |
| AfficherDetailsAnnonce.fxml | Aucun changement (utilise déjà ListView) |

## 🧪 Test

1. Afficher une annonce avec commentaires
2. **Clic droit** sur un commentaire
3. Sélectionner "✏️ Éditer"
4. Modifier le texte
5. Cliquer "OK"
6. Vérifier que le commentaire est modifié

Ou:

1. **Clic droit** sur un commentaire
2. Sélectionner "🗑️ Supprimer"
3. Confirmer la suppression
4. Vérifier que le commentaire est supprimé

## ✨ Résultat Attendu

- Menu contextuel au clic droit sur chaque commentaire
- Dialogue d'édition avec auteur et texte modifiables
- Confirmation avant suppression
- Messages de succès/erreur

## 🔐 Sécurité

- PreparedStatement utilisé par CommentaireService.update()
- Validation en base de données
- Confirmation avant suppression
- Gestion des exceptions

---

**Coût d'implémentation**: ~2-3 heures de travail
**Complexité**: Moyenne
**Dépendances**: Aucune nouvelle


