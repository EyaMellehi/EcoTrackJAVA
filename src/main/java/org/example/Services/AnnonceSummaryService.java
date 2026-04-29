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

public class AnnonceSummaryService {

    private static final String API_URL = "https://api.cohere.com/v1/chat";
    private static final String MODEL = "command-a-03-2025";

    public String summarizeInThreeSimpleSentences(Annonce annonce) throws IOException {
        if (annonce == null) {
            throw new IOException("Annonce introuvable.");
        }

        String apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("Variable d'environnement COHERE_API_KEY manquante (et fichier cohere_api_key.local.txt introuvable ou vide).");
        }

        String prompt = buildPrompt(annonce);
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
            throw new IOException("La reponse IA ne contient pas de resume.");
        }

        return text.trim();
    }

    private String buildPrompt(Annonce annonce) {
        return """
                Tu es un assistant citoyen EcoTrack.
                Resume l'annonce suivante en francais simple.

                Contraintes strictes:
                - exactement 3 phrases
                - phrases courtes
                - vocabulaire non technique
                - ne rien inventer
                - rester fidele au contenu

                Titre: %s
                Categorie: %s
                Region: %s
                Contenu: %s
                """.formatted(
                safe(annonce.getTitre()),
                safe(annonce.getCategorie()),
                safe(annonce.getRegion()),
                safe(annonce.getContenu())
        );
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

