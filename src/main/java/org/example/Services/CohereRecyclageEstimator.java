package org.example.Services;

import org.example.Entities.PointRecyclage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class CohereRecyclageEstimator {

    private static final String API_URL = "https://api.cohere.com/v1/chat";

    // Remplace par ta vraie clé
    private static final String API_KEY = "QuKwC6BrtrK5sN0E7Jj9D0b5LYwO80zaadsYW99J";
    private static final String MODEL = "command-a-03-2025";

    public AiEstimateResult estimate(PointRecyclage p) throws IOException {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IOException("COHERE_API_KEY manquant.");
        }

        int ageDays = 0;
        if (p.getDateDec() != null) {
            ageDays = (int) Math.max(0, ChronoUnit.DAYS.between(p.getDateDec(), LocalDate.now()));
        }

        String cat = (p.getCategorie() != null && p.getCategorie().getNom() != null)
                ? p.getCategorie().getNom()
                : "Non spécifiée";

        String desc = p.getDescription() != null ? p.getDescription().trim() : "";
        if (desc.isEmpty()) {
            desc = "Aucune description";
        }

        double q = p.getQuantite();

        String prompt = """
                Tu es un système de priorisation pour des points de recyclage en Tunisie.

                Objectif:
                Donner une estimation de priorité à partir de:
                - description
                - ancienneté
                - quantité
                - catégorie

                Contraintes:
                - Réponds UNIQUEMENT en JSON valide avec ces clés EXACTES:
                  {"score":0-100,"priority":"LOW|MEDIUM|HIGH|URGENT","explanation":"..."}
                - explanation: une seule phrase courte et concrète.

                Données:
                - Catégorie: %s
                - Quantité (kg): %s
                - Ancienneté (jours): %d
                - Description: "%s"

                Règles:
                - Plus ancien => plus prioritaire.
                - Danger/risque (toxique, verre cassé, seringue, incendie, déchets médicaux, odeur forte, chimique) => priorité plus élevée.
                """
                .formatted(
                        escapeForPrompt(cat),
                        String.format(Locale.US, "%.2f", q),
                        ageDays,
                        escapeForPrompt(desc)
                );

        String payload = """
                {
                  "model": "%s",
                  "message": %s,
                  "temperature": 0.2,
                  "response_format": { "type": "json_object" }
                }
                """
                .formatted(escapeJson(MODEL), quoteJson(prompt));

        HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setConnectTimeout(15000);
        con.setReadTimeout(60000);
        con.setRequestProperty("Authorization", "Bearer " + API_KEY.trim());
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");

        try (OutputStream os = con.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int code = con.getResponseCode();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream(),
                        StandardCharsets.UTF_8
                )
        )) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        } finally {
            con.disconnect();
        }

        if (code < 200 || code >= 300) {
            throw new IOException("Erreur Cohere HTTP " + code + ": " + response);
        }

        String text = parseStringField(response.toString(), "text", null);
        if (text == null || text.isBlank()) {
            throw new IOException("Réponse Cohere vide.");
        }

        AiEstimateResult result = new AiEstimateResult();
        result.setScore(parseIntField(text, "score", 50));
        result.setPriority(parseStringField(text, "priority", "MEDIUM"));
        result.setExplanation(parseStringField(text, "explanation", "Estimation automatique."));
        result.setAgeDays(ageDays);

        return normalize(result);
    }

    private AiEstimateResult normalize(AiEstimateResult result) {
        int score = result.getScore();
        if (score < 0 || score > 100) {
            score = 50;
        }

        String priority = result.getPriority() == null ? "" : result.getPriority().trim().toUpperCase(Locale.ROOT);
        if (!priority.equals("LOW") && !priority.equals("MEDIUM") && !priority.equals("HIGH") && !priority.equals("URGENT")) {
            if (score >= 80) priority = "URGENT";
            else if (score >= 60) priority = "HIGH";
            else if (score >= 35) priority = "MEDIUM";
            else priority = "LOW";
        }

        String explanation = result.getExplanation();
        if (explanation == null || explanation.isBlank()) {
            explanation = "Estimation automatique.";
        }

        result.setScore(score);
        result.setPriority(priority);
        result.setExplanation(explanation.trim());
        return result;
    }

    private int parseIntField(String json, String field, int defaultValue) {
        String marker = "\"" + field + "\":";
        int idx = json.indexOf(marker);
        if (idx == -1) return defaultValue;

        idx += marker.length();
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) idx++;

        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }

        try {
            return Integer.parseInt(json.substring(idx, end).trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String parseStringField(String json, String field, String defaultValue) {
        String marker = "\"" + field + "\":";
        int idx = json.indexOf(marker);
        if (idx == -1) return defaultValue;

        idx += marker.length();
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) idx++;

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

    private String quoteJson(String value) {
        return "\"" + escapeJson(value) + "\"";
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String escapeForPrompt(String value) {
        return value.replace("\"", "\\\"");
    }
}