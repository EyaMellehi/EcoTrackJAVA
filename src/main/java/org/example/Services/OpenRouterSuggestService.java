package org.example.Services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

public class OpenRouterSuggestService {

    private static final String API_KEY =""
 
    private static final String MODEL =
            "openai/gpt-4o-mini";

    private static final String SITE_URL =
            "https://localhost:8000";

    private static final String APP_NAME =
            "DonationApp";

    private final HttpClient client =
            HttpClient.newHttpClient();

    /* ========================================= */
    /* PUBLIC RESULT TEXT FOR UI                 */
    /* ========================================= */
    private String lastMessage =
            "";

    public String getLastMessage() {
        return lastMessage;
    }

    /* ========================================= */
    public List<Integer> suggest(
            String donationText,
            List<JSONObject> associations,
            int topK
    ) {

        List<Integer> result =
                new ArrayList<>();

        lastMessage = "";

        try {

            if (donationText == null ||
                    donationText.trim().length() < 5) {

                lastMessage =
                        "Please describe your need in more detail.";
                return result;
            }

            JSONArray assocs =
                    new JSONArray();

            for (JSONObject a : associations) {

                if (a.optBoolean("isActive")) {
                    assocs.put(a);
                }

                if (assocs.length() >= 60)
                    break;
            }

            JSONObject user =
                    new JSONObject();

            user.put(
                    "donation_text",
                    donationText
            );

            user.put(
                    "associations",
                    assocs
            );

            String system =
                    "Tu es un assistant expert qui recommande des associations. "
                            + "Analyse la demande utilisateur et recommande les meilleures associations. "
                            + "Retourne TOUJOURS uniquement JSON valide sous forme : "
                            + "{\"ids\":[1,2],"
                            + "\"message\":\"texte court utile pour utilisateur\"}. "
                            + "Même si la demande est imprécise, propose une aide utile. "
                            + "Ne jamais répondre hors sujet.";

            JSONArray messages =
                    new JSONArray();

            messages.put(
                    new JSONObject()
                            .put("role","system")
                            .put("content",system)
            );

            messages.put(
                    new JSONObject()
                            .put("role","user")
                            .put("content",user.toString())
            );

            JSONObject payload =
                    new JSONObject();

            payload.put("model", MODEL);
            payload.put("messages", messages);
            payload.put("temperature", 0.4);

            HttpRequest request =
                    HttpRequest.newBuilder()

                            .uri(
                                    URI.create(
                                            "https://openrouter.ai/api/v1/chat/completions"
                                    )
                            )

                            .header(
                                    "Authorization",
                                    "Bearer " + API_KEY
                            )

                            .header(
                                    "Content-Type",
                                    "application/json"
                            )

                            .header(
                                    "HTTP-Referer",
                                    SITE_URL
                            )

                            .header(
                                    "X-Title",
                                    APP_NAME
                            )

                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(
                                                    payload.toString()
                                            )
                            )

                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            JSONObject json =
                    new JSONObject(
                            response.body()
                    );

            String content =
                    json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

            content =
                    content.replace("```json", "")
                            .replace("```", "")
                            .trim();

            JSONObject parsed =
                    new JSONObject(content);

            if (parsed.has("message")) {

                lastMessage =
                        parsed.getString("message");

            } else {

                lastMessage =
                        "AI found matching associations.";
            }

            if (parsed.has("ids")) {

                JSONArray ids =
                        parsed.getJSONArray("ids");

                for (int i = 0;
                     i < ids.length() && i < topK;
                     i++) {

                    result.add(
                            ids.getInt(i)
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            lastMessage =
                    "AI unavailable right now. Showing standard results.";
        }

        return result;
    }
}