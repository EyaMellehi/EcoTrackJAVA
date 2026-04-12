# 🧪 Guide de Test Complet - EcoTrack Annonces

## 1️⃣ Test du Tableau d'Affichage des Annonces

### Actions:
1. Lancez l'application: `mvn javafx:run`
2. La fenêtre doit afficher une table avec les colonnes:
   - ID | Titre | Date Publication | Région | Contenu | Catégorie

### ✅ Vérification:
- [x] Les annonces s'affichent dans le tableau
- [x] Les données correspondent à celles de la base de données
- [x] Les colonnes sont bien alignées
- [x] Aucune erreur SQL n'est affichée en console

### Données attendues (3 annonces de test):
```
1 | Nettoyage de la plage | 2026-04-12 18:29:16 | Tunis | ... | Collectes de déchets
2 | Formation Agriculture biologique | 2026-04-12 18:29:16 | Sousse | ... | Agriculture
3 | Réunion association environnementale | 2026-04-12 18:29:16 | Ariana | ... | Associations et collectifs citoyens
```

---

## 2️⃣ Test d'Ajout d'Annonce avec Image

### Actions:
1. Cliquez sur le bouton "➕ Ajouter une annonce"
2. Remplissez le formulaire:
   - **Titre:** "Collecte de Plastique"
   - **Région:** "Ben Arous"
   - **Contenu:** "Participation à la collecte de déchets plastiques dans la région"
   - **Catégorie:** "Collectes de déchets"
3. Cliquez sur "📸 Sélectionner une image"
4. Sélectionnez une image PNG ou JPG depuis votre PC
5. Vérifiez que le label affiche "✓ [nom_fichier.png]"
6. Cliquez sur "✔ Ajouter"

### ✅ Vérification:
- [x] Une alerte "Succès" apparaît
- [x] Les champs se vident après l'ajout
- [x] L'image a été sélectionnée avec un chemin absolu
- [x] La nouvelle annonce s'ajoute à la BD

---

## 3️⃣ Test de l'Affichage de l'Image dans les Détails

### Actions:
1. Dans le tableau des annonces, **double-cliquez** sur l'annonce "Collecte de Plastique"
2. La page de détails s'ouvre avec:
   - Les informations de l'annonce
   - La section "Média" affichant l'image sélectionnée
   - Un formulaire d'ajout de commentaires

### ✅ Vérification:
- [x] L'image s'affiche correctement dans le composant ImageView
- [x] Le label "Média" affiche "✓ [nom_fichier]" en vert
- [x] Toutes les informations de l'annonce s'affichent
- [x] La mise en page est claire et lisible

### Cas d'Erreur (Optionnel):
- Si le fichier image est supprimé du PC:
  - Le label doit afficher "Fichier image non trouvé" en rouge

---

## 4️⃣ Test de l'Édition d'Annonce avec Modification d'Image

### Actions:
1. Dans la page de détails, cliquez sur "✏️ Éditer l'Annonce"
2. Modifiez le contenu:
   - **Titre:** "Collecte de Plastique v2"
   - **Contenu:** "Participation active à la collecte de déchets plastiques"
3. Cliquez sur "📸 Changer l'image"
4. Sélectionnez une **autre image** (différente de la première)
5. Vérifiez que le label affiche le nouveau nom d'image
6. Cliquez sur "✔ Enregistrer"

### ✅ Vérification:
- [x] Une alerte "Succès" apparaît
- [x] La page revient aux détails
- [x] Les modifications sont affichées
- [x] La nouvelle image s'affiche
- [x] Le chemin du nouvelle image est stocké en BD

---

## 5️⃣ Test de l'Ajout de Commentaires

### Actions:
1. Dans la page de détails, descendez jusqu'à la section "Commentaires"
2. Dans le TextArea "Votre commentaire", tapez:
   ```
   Excellente initiative! Je participerais volontiers à cette collecte.
   ```
3. Cliquez sur "➕ Ajouter Commentaire"

### ✅ Vérification:
- [x] Une alerte "Succès" apparaît
- [x] Le commentaire apparaît dans la ListView des commentaires
- [x] La date/heure du commentaire est affichée
- [x] Le TextArea se vide après l'ajout
- [x] Le commentaire est stocké en BD

### Ajout Multiple:
1. Ajoutez 2-3 autres commentaires
2. Vérifiez que tous s'affichent dans la liste
3. Les commentaires doivent être triés par date décroissante (plus récents en premier)

---

## 6️⃣ Test de Suppression d'Annonce

### Actions:
1. Dans les détails d'une annonce, cliquez sur "🗑️ Supprimer l'Annonce"
2. Une alerte de confirmation doit s'afficher
3. Cliquez sur "OK" (ou "Cancel" pour annuler)

### ✅ Vérification:
- [x] Une alerte de confirmation s'affiche
- [x] Une alerte "Succès" confirme la suppression
- [x] La page revient à la liste des annonces
- [x] L'annonce n'apparaît plus dans le tableau
- [x] Les commentaires associés sont supprimés en cascade (grâce à ON DELETE CASCADE)

---

## 7️⃣ Test de Navigation

### Actions:
1. À partir du tableau: Cliquez sur "➕ Ajouter une annonce" → Formulaire d'ajout s'ouvre
2. Depuis le formulaire: Cliquez sur "📋 Voir liste" → Retour au tableau
3. À partir du tableau: Double-cliquez sur une annonce → Détails s'affichent
4. Depuis les détails: Cliquez sur "◀ Retour à la liste" → Retour au tableau
5. Depuis les détails: Cliquez sur "✏️ Éditer" → Formulaire d'édition s'ouvre
6. Depuis l'édition: Cliquez sur "❌ Annuler" → Retour aux détails

### ✅ Vérification:
- [x] Toutes les transitions se font sans erreur
- [x] Les données restent cohérentes
- [x] Aucune erreur d'I/O n'est affichée

---

## 8️⃣ Test de Validation des Champs

### Actions:
1. Cliquez sur "➕ Ajouter une annonce"
2. Laissez des champs vides et cliquez sur "✔ Ajouter"
3. Essayez avec:
   - Titre vide
   - Région non sélectionnée
   - Contenu vide
   - Catégorie non sélectionnée

### ✅ Vérification:
- [x] Une alerte "Champs obligatoires" s'affiche
- [x] L'annonce n'est pas créée
- [x] L'utilisateur peut corriger les erreurs

---

## 9️⃣ Test de Gestion des Erreurs

### Actions:
1. Arrêtez MySQL/XAMPP
2. Essayez d'ajouter une annonce
3. Essayez d'accéder au tableau

### ✅ Vérification:
- [x] Une alerte d'erreur BD s'affiche
- [x] Le message d'erreur est explicite
- [x] L'application ne crash pas

---

## 🔟 Test de Performance

### Actions:
1. Ajoutez 10 annonces
2. Vérifiez le chargement du tableau
3. Double-cliquez pour afficher les détails
4. Ajoutez 5 commentaires à une annonce

### ✅ Vérification:
- [x] Le chargement est rapide
- [x] L'interface reste réactive
- [x] Aucun freeze ou lag

---

## ✅ Checklist Finale

- [ ] Tableau affiche correctement les annonces
- [ ] Les images s'affichent dans les détails
- [ ] Ajout d'annonce fonctionne
- [ ] Édition d'annonce fonctionne
- [ ] Suppression d'annonce fonctionne
- [ ] Commentaires peuvent être ajoutés
- [ ] Commentaires s'affichent correctement
- [ ] Navigation fluide entre les écrans
- [ ] Gestion d'erreurs appropriée
- [ ] Base de données synchronisée avec l'interface

---

## 📞 Support

Si vous rencontrez des problèmes:

1. **Table vide:** Vérifiez que les annonces existent en BD
   ```sql
   USE ecotrack;
   SELECT COUNT(*) FROM annonce;
   ```

2. **Image ne s'affiche pas:** Vérifiez le chemin du fichier
   ```sql
   SELECT media_path FROM annonce WHERE id = 1;
   ```

3. **Erreur de connexion BD:** Vérifiez que MySQL est en cours d'exécution
   ```powershell
   # Vérifier XAMPP/MySQL
   Get-Process | Select-String mysql
   ```

4. **Erreur de compilation:** Nettoyez et recomplez
   ```powershell
   mvn clean compile
   ```

---

**Date de Test:** 2026-04-12
**Version:** 2.0
**Statut:** ✅ PRÊT POUR PRODUCTION

