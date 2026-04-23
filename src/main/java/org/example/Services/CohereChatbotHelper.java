package org.example.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CohereChatbotHelper {

    private static final String API_URL = "https://api.cohere.com/v1/chat";
    private static final String API_KEY = "QuKwC6BrtrK5sN0E7Jj9D0b5LYwO80zaadsYW99J";
    private static final String MODEL = "command-a-03-2025";

    public ChatbotResult reply(String mode, List<ChatMessage> history, Map<String, String> context) throws IOException {
        String systemPrompt;

        if ("POINT_DESC".equalsIgnoreCase(mode)) {
            systemPrompt = buildSystemPromptForPointDescription(context);
        } else if ("RAPPORT_COMMENT".equalsIgnoreCase(mode)) {
            systemPrompt = buildSystemPromptForRapportComment(context);
        } else {
            throw new IOException("Mode non supporté.");
        }

        String conversation = buildConversation(history);
        String prompt = systemPrompt + "\n\nConversation:\n" + conversation;

        String payload = """
                {
                  "model": "%s",
                  "message": %s,
                  "temperature": 0.2,
                  "response_format": { "type": "json_object" }
                }
                """.formatted(
                escapeJson(MODEL),
                quoteJson(prompt)
        );

        HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setConnectTimeout(10000);
        con.setReadTimeout(25000);
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
            throw new IOException("Réponse AI vide.");
        }

        String reply = parseStringField(text, "reply", null);
        String draft = parseStringField(text, "draft", null);

        if (reply == null || reply.isBlank()) {
            reply = "Je n'ai pas pu générer une réponse correcte.";
        }

        if (draft == null) {
            draft = "";
        }

        return new ChatbotResult(reply.trim(), draft.trim());
    }

    private String buildSystemPromptForPointDescription(Map<String, String> context) {
        String categorie = safe(context.get("categorie"));
        String quantite = safe(context.get("quantite"));

        return """
                Tu es un assistant de rédaction pour EcoTrack.

                Rôle:
                - Aider uniquement un citoyen à rédiger une description claire et professionnelle d’un point de recyclage.
                - Le sujet doit rester strictement lié au point de recyclage.

                Sujet autorisé:
                - catégorie
                - quantité
                - état
                - emballage (sacs, vrac, cartons, etc.)
                - repère de lieu
                - niveau de danger éventuel
                - détails utiles pour la collecte

                Sujet interdit:
                - politique
                - religion
                - finance
                - voitures
                - sport
                - santé hors contexte du point
                - tout autre sujet non lié au point de recyclage

                Si l’utilisateur parle d’un sujet hors contexte:
                - réponds poliment que tu peux seulement aider à rédiger une description de point de recyclage
                - ne réponds pas à la question hors sujet
                - draft doit être vide

                Règles:
                - Ne jamais inventer de faits.
                - Utiliser uniquement les informations fournies par l’utilisateur.
                - Si des infos utiles manquent, poser une ou deux questions courtes.
                - Si les infos sont suffisantes, produire un brouillon final.
                - Le brouillon doit être factuel, simple, propre, professionnel.
                - Le brouillon doit faire entre 2 et 4 phrases maximum.
                - Réponds UNIQUEMENT en JSON valide avec exactement:
                  {"reply":"...","draft":"..."}

                Contexte actuel:
                - Catégorie: %s
                - Quantité: %s
                """.formatted(
                categorie.isBlank() ? "Non précisée" : categorie,
                quantite.isBlank() ? "Non précisée" : quantite
        );
    }

    private String buildSystemPromptForRapportComment(Map<String, String> context) {
        String categorie = safe(context.get("categorie"));
        String quantiteDeclaree = safe(context.get("quantiteDeclaree"));
        String quantiteCollectee = safe(context.get("quantiteCollectee"));
        String adresse = safe(context.get("adresse"));

        return """
                Tu es un assistant de rédaction pour EcoTrack.

                Rôle:
                - Aider uniquement un agent de terrain à rédiger un commentaire de rapport de recyclage.
                - Le sujet doit rester strictement lié à l’opération de collecte d’un point de recyclage.

                Sujet autorisé:
                - quantité collectée
                - état du point
                - état des déchets
                - sacs, vrac, cartons, tri
                - accès au lieu
                - difficulté rencontrée
                - observations utiles pour la suite
                - anomalies liées à la collecte

                Sujet interdit:
                - politique
                - religion
                - finance
                - voitures
                - sport
                - discussions générales
                - tout autre sujet non lié au rapport de recyclage

                Si l’utilisateur parle d’un sujet hors contexte:
                - réponds poliment que tu peux seulement aider à rédiger un commentaire de rapport de recyclage
                - ne réponds pas à la question hors sujet
                - draft doit être vide

                Règles:
                - Ne jamais inventer de faits.
                - Utiliser uniquement les informations fournies par l’utilisateur.
                - Si des détails utiles manquent, poser une ou deux questions courtes.
                - Si les infos sont suffisantes, produire un brouillon final.
                - Le brouillon doit être formel, clair, structuré, et professionnel.
                - Le brouillon doit faire entre 2 et 5 phrases maximum.
                - Réponds UNIQUEMENT en JSON valide avec exactement:
                  {"reply":"...","draft":"..."}

                Contexte actuel:
                - Catégorie: %s
                - Quantité déclarée: %s
                - Quantité collectée: %s
                - Adresse: %s
                """.formatted(
                categorie.isBlank() ? "Non précisée" : categorie,
                quantiteDeclaree.isBlank() ? "Non précisée" : quantiteDeclaree,
                quantiteCollectee.isBlank() ? "Non précisée" : quantiteCollectee,
                adresse.isBlank() ? "Non précisée" : adresse
        );
    }

    private String buildConversation(List<ChatMessage> history) {
        StringBuilder sb = new StringBuilder();

        if (history != null) {
            for (ChatMessage msg : history) {
                if (msg == null) continue;
                String role = safe(msg.getRole()).toLowerCase(Locale.ROOT);
                String content = safe(msg.getContent());
                if (content.isBlank()) continue;

                if ("assistant".equals(role)) {
                    sb.append("Assistant: ").append(content).append("\n");
                } else {
                    sb.append("User: ").append(content).append("\n");
                }
            }
        }

        return sb.toString().trim();
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

    private String safe(String value) {
        return value == null ? "" : value;
    }
}