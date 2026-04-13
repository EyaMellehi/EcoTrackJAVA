package org.example.Utils;

import java.util.Set;
import java.util.regex.Pattern;

public final class InputValidationUtil {

    private static final Pattern URL_PATTERN = Pattern.compile("(?i)\\b(?:https?://|www\\.)\\S+");
    private static final Pattern REPEATED_CHAR_PATTERN = Pattern.compile("(?i)(.)\\1{4,}");
    private static final Pattern REPEATED_WORD_PATTERN = Pattern.compile("(?i)\\b([\\p{L}\\p{N}]{2,})\\b(?:\\s+\\1\\b){2,}");
    private static final Pattern COMMENT_ALLOWED_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s'’-]+$");
    private static final Pattern TITLE_ALLOWED_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s'’.,?!:-]+$");

    private static final Set<String> BANNED_WORDS = Set.of(
        "fuck", "shit", "merde", "pute", "salope", "idiot", "connard", "sale", "raciste", "nazis"
    );

    private InputValidationUtil() {
    }

    public static String validateAnnonceTitle(String title) {
        String value = normalize(title);
        if (value.isEmpty()) {
            return "Le titre est obligatoire.";
        }
        if (value.length() < 3 || value.length() > 100) {
            return "Le titre doit contenir entre 3 et 100 caractères.";
        }
        if (containsUrl(value)) {
            return "Le titre ne doit pas contenir de lien internet.";
        }
        if (!TITLE_ALLOWED_PATTERN.matcher(value).matches()) {
            return "Le titre contient des caractères non autorisés.";
        }
        if (hasTooManySymbols(value)) {
            return "Le titre ne doit pas contenir trop de symboles.";
        }
        if (containsRepeatedCharacters(value)) {
            return "Le titre ne doit pas contenir de lettres ou symboles répétés.";
        }
        if (containsBannedWords(value)) {
            return "Le titre contient des mots inappropriés.";
        }
        return null;
    }

    public static String validateAnnonceContent(String content) {
        String value = normalize(content);
        if (value.isEmpty()) {
            return "La description est obligatoire.";
        }
        if (value.length() < 10 || value.length() > 2000) {
            return "La description doit contenir entre 10 et 2000 caractères.";
        }
        if (containsUrl(value)) {
            return "La description ne doit pas contenir de lien internet.";
        }
        if (containsBannedWords(value)) {
            return "La description contient des mots interdits.";
        }
        if (isSpammy(value)) {
            return "La description semble répétitive ou spam.";
        }
        return null;
    }

    public static String validateRequiredSelection(String value, String fieldLabel) {
        if (normalize(value).isEmpty()) {
            return fieldLabel + " est obligatoire.";
        }
        return null;
    }

    public static String validateComment(String comment) {
        String value = normalize(comment);
        if (value.isEmpty()) {
            return "Le commentaire est obligatoire.";
        }
        if (value.length() < 5 || value.length() > 500) {
            return "Le commentaire doit contenir entre 5 et 500 caractères.";
        }
        if (containsUrl(value)) {
            return "Le commentaire ne doit pas contenir de lien internet.";
        }
        if (!COMMENT_ALLOWED_PATTERN.matcher(value).matches()) {
            return "Le commentaire ne doit contenir que des lettres, chiffres, espaces, apostrophes ou tirets.";
        }
        if (containsBannedWords(value)) {
            return "Le commentaire contient des mots interdits.";
        }
        if (isSpammy(value)) {
            return "Le commentaire semble répétitif ou spam.";
        }
        return null;
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private static boolean containsUrl(String value) {
        return URL_PATTERN.matcher(value).find();
    }

    private static boolean containsRepeatedCharacters(String value) {
        return REPEATED_CHAR_PATTERN.matcher(value).find();
    }

    private static boolean containsBannedWords(String value) {
        String lower = value.toLowerCase();
        for (String banned : BANNED_WORDS) {
            if (lower.contains(banned)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTooManySymbols(String value) {
        long symbolCount = value.chars()
            .filter(ch -> !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch))
            .count();
        int allowed = Math.max(3, value.length() / 5);
        return symbolCount > allowed;
    }

    private static boolean isSpammy(String value) {
        if (containsRepeatedCharacters(value) || REPEATED_WORD_PATTERN.matcher(value).find()) {
            return true;
        }

        String[] words = value.toLowerCase().split("\\s+");
        if (words.length < 6) {
            return false;
        }

        int distinct = (int) java.util.Arrays.stream(words)
            .filter(w -> !w.isBlank())
            .distinct()
            .count();

        return distinct <= Math.max(2, words.length / 3);
    }
}


