package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour la validation des données
 */
public class ValidationUtil {
    
    // Mauvais mots à interdire
    private static final List<String> BAD_WORDS = new ArrayList<>();
    
    static {
        // Ajouter des mauvais mots (exemple - adapté selon vos besoins)
        BAD_WORDS.add("spam");
        BAD_WORDS.add("abuse");
        BAD_WORDS.add("scam");
        BAD_WORDS.add("porn");
        BAD_WORDS.add("hate");
        BAD_WORDS.add("violence");
        BAD_WORDS.add("insult");
    }
    
    /**
     * Valider le titre d'une annonce
     * Règles: 3-100 caractères, pas d'URL, pas trop de caractères spéciaux
     */
    public static ValidationResult validerTitre(String titre) {
        if (titre == null || titre.trim().isEmpty()) {
            return new ValidationResult(false, "❌ Le titre est obligatoire");
        }
        
        titre = titre.trim();
        
        if (titre.length() < 3) {
            return new ValidationResult(false, "❌ Le titre doit contenir au moins 3 caractères");
        }
        
        if (titre.length() > 100) {
            return new ValidationResult(false, "❌ Le titre ne peut pas dépasser 100 caractères");
        }
        
        if (contientURL(titre)) {
            return new ValidationResult(false, "❌ Les URLs ne sont pas autorisées dans le titre");
        }
        
        if (contientCaracteresSpéciaux(titre)) {
            return new ValidationResult(false, "❌ Trop de caractères spéciaux dans le titre");
        }
        
        if (contientSpam(titre)) {
            return new ValidationResult(false, "❌ Le titre contient du contenu suspect (spam)");
        }
        
        if (contientMauvaisMots(titre)) {
            return new ValidationResult(false, "❌ Le titre contient des mots non autorisés");
        }
        
        return new ValidationResult(true, "✅ Titre valide");
    }
    
    /**
     * Valider le contenu/description d'une annonce
     * Règles: 10-2000 caractères
     */
    public static ValidationResult validerContenu(String contenu) {
        if (contenu == null || contenu.trim().isEmpty()) {
            return new ValidationResult(false, "❌ Le contenu est obligatoire");
        }
        
        contenu = contenu.trim();
        
        if (contenu.length() < 10) {
            return new ValidationResult(false, "❌ Le contenu doit contenir au moins 10 caractères");
        }
        
        if (contenu.length() > 2000) {
            return new ValidationResult(false, "❌ Le contenu ne peut pas dépasser 2000 caractères");
        }
        
        if (contientSpam(contenu)) {
            return new ValidationResult(false, "❌ Le contenu contient du spam ou du contenu répétitif");
        }
        
        if (contientMauvaisMots(contenu)) {
            return new ValidationResult(false, "❌ Le contenu contient des mots non autorisés");
        }
        
        return new ValidationResult(true, "✅ Contenu valide");
    }
    
    /**
     * Valider la région
     * Règles: non vide, existe dans la liste
     */
    public static ValidationResult validerRegion(String region) {
        if (region == null || region.trim().isEmpty()) {
            return new ValidationResult(false, "❌ La région est obligatoire");
        }
        return new ValidationResult(true, "✅ Région valide");
    }
    
    /**
     * Valider la catégorie
     * Règles: non vide
     */
    public static ValidationResult validerCategorie(String categorie) {
        if (categorie == null || categorie.trim().isEmpty()) {
            return new ValidationResult(false, "❌ La catégorie est obligatoire");
        }
        return new ValidationResult(true, "✅ Catégorie valide");
    }
    
    /**
     * Valider un commentaire
     * Règles: 5-500 caractères, pas de mauvais mots, pas de spam
     */
    public static ValidationResult validerCommentaire(String commentaire) {
        if (commentaire == null || commentaire.trim().isEmpty()) {
            return new ValidationResult(false, "❌ Le commentaire est obligatoire");
        }
        
        commentaire = commentaire.trim();
        
        if (commentaire.length() < 5) {
            return new ValidationResult(false, "❌ Le commentaire doit contenir au moins 5 caractères");
        }
        
        if (commentaire.length() > 500) {
            return new ValidationResult(false, "❌ Le commentaire ne peut pas dépasser 500 caractères");
        }
        
        if (contientSpam(commentaire)) {
            return new ValidationResult(false, "❌ Le commentaire contient du spam ou du contenu répétitif");
        }
        
        if (contientMauvaisMots(commentaire)) {
            return new ValidationResult(false, "❌ Le commentaire contient des mots non autorisés");
        }
        
        if (contientURL(commentaire)) {
            return new ValidationResult(false, "❌ Les URLs ne sont pas autorisées dans les commentaires");
        }
        
        return new ValidationResult(true, "✅ Commentaire valide");
    }
    
    /**
     * Déterminer si le texte contient une URL
     */
    private static boolean contientURL(String texte) {
        return texte.toLowerCase().contains("http://") || 
               texte.toLowerCase().contains("https://") ||
               texte.toLowerCase().contains("www.") ||
               texte.contains("@");
    }
    
    /**
     * Déterminer si le texte contient trop de caractères spéciaux
     */
    private static boolean contientCaracteresSpéciaux(String texte) {
        long count = texte.chars().filter(c -> !Character.isLetterOrDigit(c) && 
                                               !Character.isWhitespace(c) && 
                                               c != '\'' && c != '-' && c != '.').count();
        // Plus de 5 caractères spéciaux suspects
        return count > 5;
    }
    
    /**
     * Déterminer si le texte contient du spam (caractères très répétés)
     */
    private static boolean contientSpam(String texte) {
        // Vérifier les caractères répétés (ex: "aaaaaaa", "!!!!!!!")
        Pattern spamPattern = Pattern.compile("(.)\\1{4,}");
        return spamPattern.matcher(texte).find();
    }
    
    /**
     * Déterminer si le texte contient des mauvais mots
     */
    private static boolean contientMauvaisMots(String texte) {
        String texteLower = texte.toLowerCase();
        for (String badWord : BAD_WORDS) {
            if (texteLower.contains(badWord.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtenir le nombre de caractères disponibles pour un champ
     */
    public static int getCaracteresDisponibles(String texte, int max) {
        if (texte == null) return max;
        return Math.max(0, max - texte.length());
    }
    
    /**
     * Nettoyer le texte (supprimer les espaces inutiles)
     */
    public static String nettoyer(String texte) {
        if (texte == null) return "";
        return texte.trim();
    }
    
    /**
     * Classe pour retourner les résultats de validation
     */
    public static class ValidationResult {
        private boolean valide;
        private String message;
        
        public ValidationResult(boolean valide, String message) {
            this.valide = valide;
            this.message = message;
        }
        
        public boolean isValide() {
            return valide;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

