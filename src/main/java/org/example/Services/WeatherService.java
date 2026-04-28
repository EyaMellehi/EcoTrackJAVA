package org.example.Services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherService {

    private static final String OPEN_WEATHER_API_KEY = "bd458492c23e14f99f2c468e70016cb7";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final DateTimeFormatter API_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Pattern ITEM_PATTERN = Pattern.compile(
            "\\{\\s*\\\"dt\\\"\\s*:\\s*\\d+.*?\\\"temp\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?).*?"
                    + "\\\"weather\\\"\\s*:\\s*\\[\\s*\\{.*?\\\"description\\\"\\s*:\\s*\\\"([^\\\"]+)\\\".*?\\}\\s*\\].*?"
                    + "\\\"pop\\\"\\s*:\\s*(\\d+(?:\\.\\d+)?).*?\\\"dt_txt\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"",
            Pattern.DOTALL
    );

    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private String lastError;

    public String getLastError() {
        return lastError;
    }

    public WeatherSuggestion suggestBestSlot(String location, LocalDateTime minDateTime) {
        lastError = null;
        if (location == null || location.isBlank()) {
            lastError = "Lieu vide.";
            return null;
        }

        String body;
        try {
            body = fetchForecast(location.trim());
        } catch (IOException e) {
            lastError = e.getMessage();
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lastError = "Requete meteo interrompue.";
            return null;
        }

        Matcher matcher = ITEM_PATTERN.matcher(body);
        Candidate best = null;

        while (matcher.find()) {
            double temp;
            double pop;
            LocalDateTime dateTime;
            String description;

            try {
                temp = Double.parseDouble(matcher.group(1));
                description = matcher.group(2);
                pop = Double.parseDouble(matcher.group(3));
                dateTime = LocalDateTime.parse(matcher.group(4), API_TIME_FORMAT);
            } catch (Exception ex) {
                continue;
            }

            if (dateTime.isBefore(minDateTime)) {
                continue;
            }

            int score = scoreCandidate(dateTime, temp, pop, description);
            Candidate candidate = new Candidate(dateTime, temp, pop, description, score);

            if (best == null || candidate.score > best.score) {
                best = candidate;
            }
        }

        if (best == null) {
            if (lastError == null) {
                lastError = "Aucun creneau exploitable retourne par OpenWeatherMap.";
            }
            return null;
        }

        LocalDateTime start = best.dateTime;
        LocalDateTime end = start.plusHours(2);

        String summary = String.format(
                Locale.US,
                "%s | %.0fC | pluie %.0f%%",
                capitalize(best.description),
                best.temp,
                best.pop * 100.0
        );

        String recommendation = best.pop <= 0.2 && best.temp >= 18 && best.temp <= 30
                ? "Conditions meteo ideales"
                : "Conditions acceptables";

        return new WeatherSuggestion(start, end, summary, recommendation);
    }

    private String fetchForecast(String location) throws IOException, InterruptedException {
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String url = FORECAST_URL
                + "?q=" + encodedLocation
                + "&appid=" + OPEN_WEATHER_API_KEY
                + "&units=metric&lang=fr";

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            if (response.statusCode() == 401) {
                throw new IOException("Cle API OpenWeatherMap invalide/inactive (HTTP 401). Active la cle dans le dashboard OpenWeather puis attends quelques minutes.");
            }
            if (response.statusCode() == 404) {
                throw new IOException("Ville introuvable: " + location + ".");
            }
            throw new IOException("Erreur OpenWeatherMap HTTP " + response.statusCode());
        }
        return response.body();
    }

    private int scoreCandidate(LocalDateTime dateTime, double temp, double pop, String description) {
        int score = 0;

        int hour = dateTime.getHour();
        if (hour >= 8 && hour <= 18) {
            score += 30;
        }

        if (temp >= 20 && temp <= 28) {
            score += 45;
        } else if (temp >= 16 && temp <= 32) {
            score += 25;
        } else {
            score -= 20;
        }

        int rainPenalty = (int) Math.round(pop * 100);
        score -= rainPenalty;

        String lowerDescription = description == null ? "" : description.toLowerCase(Locale.ROOT);
        if (lowerDescription.contains("orage") || lowerDescription.contains("forte pluie")) {
            score -= 35;
        }

        return score;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "Meteo";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private static class Candidate {
        private final LocalDateTime dateTime;
        private final double temp;
        private final double pop;
        private final String description;
        private final int score;

        private Candidate(LocalDateTime dateTime, double temp, double pop, String description, int score) {
            this.dateTime = dateTime;
            this.temp = temp;
            this.pop = pop;
            this.description = description;
            this.score = score;
        }
    }

    public static class WeatherSuggestion {
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final String summary;
        private final String recommendation;

        public WeatherSuggestion(LocalDateTime start, LocalDateTime end, String summary, String recommendation) {
            this.start = start;
            this.end = end;
            this.summary = summary;
            this.recommendation = recommendation;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public String getSummary() {
            return summary;
        }

        public String getRecommendation() {
            return recommendation;
        }
    }
}
