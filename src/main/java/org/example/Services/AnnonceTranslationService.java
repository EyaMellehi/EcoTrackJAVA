package org.example.Services;

import org.example.Entities.Annonce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class AnnonceTranslationService {

    private static final String API_URL = "https://api.cohere.com/v1/chat";
    private static final String MODEL = "command-a-03-2025";

    /**
     * Traduit le titre et le contenu de l'annonce vers la langue cible.
     *
     * @param targetLang {@code "ar"} (arabe) ou {@code "en"} (anglais)
     * @return texte formaté {@code === TITRE ===\n...\n\n=== CONTENU ===\n...}
     */
    public String translate(Annonce annonce, String targetLang) throws IOException {
        if (annonce == null) {
            throw new IOException("Annonce introuvable.");
        }
        if (targetLang == null || (!"ar".equalsIgnoreCase(targetLang) && !"en".equalsIgnoreCase(targetLang))) {
            throw new IOException("Langue cible invalide (utilisez ar ou en).");
        }

        String apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("Variable d'environnement COHERE_API_KEY manquante (et fichier cohere_api_key.local.txt introuvable ou vide).");
        }

        String lang = targetLang.toLowerCase();
        String prompt = buildPrompt(annonce, lang);
        String payload = """
                {
                  "model": "%s",
                  "message": %s,
                  "temperature": 0.2
                }
                """.formatted(escapeJson(MODEL), quoteJson(prompt));

        HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setConnectTimeout(10000);
        con.setReadTimeout(25000);
        con.setRequestProperty("Authorization", "Bearer " + apiKey.trim());
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");

        try (OutputStream os = con.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int code = con.getResponseCode();
        String response = readAll(code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream());
        con.disconnect();

        if (code < 200 || code >= 300) {
            throw new IOException("Erreur IA HTTP " + code + ": " + response);
        }

        String text = parseStringField(response, "text", null);
        if (text == null || text.isBlank()) {
            throw new IOException("La reponse IA ne contient pas de traduction.");
        }

        return formatTranslationOutput(text.trim());
    }

    private String formatTranslationOutput(String rawModelText) throws IOException {
        ParsedParts parts = parseTitreContenu(rawModelText);
        if (parts.contenu.isBlank()) {
            throw new IOException("Impossible d'extraire CONTENU de la reponse IA.");
        }
        String titreOut = parts.titre.isBlank() ? "—" : parts.titre;
        return "=== TITRE ===\n" + titreOut + "\n\n=== CONTENU ===\n" + parts.contenu;
    }

    private record ParsedParts(String titre, String contenu) {}

    private ParsedParts parseTitreContenu(String raw) {
        String normalized = raw.replace("\r\n", "\n").trim();
        int idxContenu = indexOfIgnoreCase(normalized, "CONTENU:");
        if (idxContenu >= 0) {
            String head = normalized.substring(0, idxContenu).trim();
            String tail = normalized.substring(idxContenu + "CONTENU:".length()).trim();

            int idxTitre = indexOfIgnoreCase(head, "TITRE:");
            String titre;
            if (idxTitre >= 0) {
                titre = head.substring(idxTitre + "TITRE:".length()).trim();
            } else {
                titre = head;
            }
            return new ParsedParts(titre, tail);
        }

        int idxTitre = indexOfIgnoreCase(normalized, "TITRE:");
        if (idxTitre >= 0) {
            String titre = normalized.substring(idxTitre + "TITRE:".length()).trim();
            return new ParsedParts(titre, "");
        }

        return new ParsedParts("", normalized);
    }

    private static int indexOfIgnoreCase(String haystack, String needle) {
        return haystack.toLowerCase().indexOf(needle.toLowerCase());
    }

    private String buildPrompt(Annonce annonce, String lang) {
        String titre = safe(annonce.getTitre());
        String contenu = safe(annonce.getContenu());

        if ("ar".equals(lang)) {
            return """
                    Traduis le titre et le contenu de cette annonce municipale tunisienne en arabe (dialecte standard).
                    Réponds UNIQUEMENT avec ce format exact :
                    TITRE: {titre traduit}
                    CONTENU: {contenu traduit}

                    Titre original: %s
                    Contenu original: %s
                    """.formatted(titre, contenu);
        }

        return """
                Traduis le titre et le contenu de cette annonce municipale tunisienne en anglais (English).
                Réponds UNIQUEMENT avec ce format exact :
                TITRE: {titre traduit}
                CONTENU: {contenu traduit}

                Titre original: %s
                Contenu original: %s
                """.formatted(titre, contenu);
    }

    private String readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private String parseStringField(String json, String field, String defaultValue) {
        String marker = "\"" + field + "\":";
        int idx = json.indexOf(marker);
        if (idx == -1) {
            return defaultValue;
        }

        idx += marker.length();
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) {
            idx++;
        }

        if (idx >= json.length() || json.charAt(idx) != '"') {
            return defaultValue;
        }

        idx++;
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

        for (int i = idx; i < json.length(); i++) {
            char ch = json.charAt(i);

            if (escaped) {
                switch (ch) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    default -> sb.append(ch);
                }
                escaped = false;
                continue;
            }

            if (ch == '\\') {
                escaped = true;
                continue;
            }

            if (ch == '"') {
                return sb.toString().trim();
            }

            sb.append(ch);
        }

        return defaultValue;
    }

    private String resolveApiKey() {
        String env = System.getenv("COHERE_API_KEY");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }

        Path keyFile = Path.of("cohere_api_key.local.txt");
        if (Files.exists(keyFile)) {
            String fileValue = readFirstNonEmptyLine(keyFile);
            if (fileValue != null && !fileValue.isBlank()) {
                return fileValue;
            }
        }

        return null;
    }

    private String readFirstNonEmptyLine(Path path) {
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line == null) {
                    continue;
                }
                String trimmed = line.replace("\uFEFF", "").trim();
                if (!trimmed.isBlank() && !trimmed.startsWith("#")) {
                    return trimmed;
                }
            }
        } catch (IOException ignored) {
            return null;
        }
        return null;
    }

    private String quoteJson(String value) {
        return "\"" + escapeJson(value) + "\"";
    }

    private String escapeJson(String value) {
        return safe(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
