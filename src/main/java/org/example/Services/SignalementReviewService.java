package org.example.Services;

import org.example.Entities.SignalementReviewResult;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SignalementReviewService {

    private static final String REVIEW_URL = "http://127.0.0.1:8000/review";

    public SignalementReviewResult review(String titre, String description, String type, List<File> photos) throws IOException {
        String boundary = "----EcoTrackBoundary" + UUID.randomUUID();
        HttpURLConnection connection = (HttpURLConnection) new URL(REVIEW_URL).openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(60000);

        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            writeTextPart(out, boundary, "titre", titre);
            writeTextPart(out, boundary, "description", description);
            writeTextPart(out, boundary, "type", type);

            if (photos != null) {
                for (File photo : photos) {
                    if (photo != null && photo.exists()) {
                        writeFilePart(out, boundary, "photos", photo);
                    }
                }
            }

            out.writeBytes("--" + boundary + "--\r\n");
            out.flush();
        }

        int status = connection.getResponseCode();
        InputStream responseStream = status >= 200 && status < 300
                ? connection.getInputStream()
                : connection.getErrorStream();

        String response = readAll(responseStream);

        if (status < 200 || status >= 300) {
            throw new IOException("Review service error: HTTP " + status + " - " + response);
        }

        return parseReviewResponse(response);
    }

    private void writeTextPart(DataOutputStream out, String boundary, String fieldName, String value) throws IOException {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n");
        out.write(value != null ? value.getBytes(StandardCharsets.UTF_8) : new byte[0]);
        out.writeBytes("\r\n");
    }

    private void writeFilePart(DataOutputStream out, String boundary, String fieldName, File file) throws IOException {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + file.getName() + "\"\r\n");
        out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        out.writeBytes("\r\n");
    }

    private String readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private SignalementReviewResult parseReviewResponse(String json) {
        SignalementReviewResult result = new SignalementReviewResult();

        result.setDecision(extractString(json, "decision"));
        result.setModel(extractString(json, "model"));
        result.setReasons(extractStringArray(json, "reasons"));

        result.setToxicity(extractDouble(json, "toxicity", 0.0));
        result.setInsult(extractDouble(json, "insult", 0.0));
        result.setThreat(extractDouble(json, "threat", 0.0));
        result.setImagesCount(extractInteger(json, "images_count"));
        result.setClipScoreAvg(extractNullableDouble(json, "clip_score_avg"));
        result.setClipScoreMin(extractNullableDouble(json, "clip_score_min"));

        return result;
    }

    private String extractString(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start == -1) return "";
        start += marker.length();

        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

        if (start >= json.length() || json.charAt(start) != '"') return "";
        start++;

        StringBuilder sb = new StringBuilder();
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                sb.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') break;
            sb.append(c);
        }
        return sb.toString();
    }

    private List<String> extractStringArray(String json, String key) {
        List<String> values = new ArrayList<>();
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start == -1) return values;

        start = json.indexOf('[', start);
        if (start == -1) return values;

        int end = json.indexOf(']', start);
        if (end == -1) return values;

        String content = json.substring(start + 1, end).trim();
        if (content.isEmpty()) return values;

        String[] parts = content.split(",");
        for (String part : parts) {
            String cleaned = part.trim();
            if (cleaned.startsWith("\"")) cleaned = cleaned.substring(1);
            if (cleaned.endsWith("\"")) cleaned = cleaned.substring(0, cleaned.length() - 1);
            if (!cleaned.isBlank()) values.add(cleaned);
        }
        return values;
    }

    private double extractDouble(String json, String key, double defaultValue) {
        Double value = extractNullableDouble(json, key);
        return value != null ? value : defaultValue;
    }

    private Double extractNullableDouble(String json, String key) {
        String raw = extractRawValue(json, key);
        if (raw == null || raw.equals("null")) return null;
        try {
            return Double.parseDouble(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer extractInteger(String json, String key) {
        String raw = extractRawValue(json, key);
        if (raw == null || raw.equals("null")) return null;
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractRawValue(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start == -1) return null;
        start += marker.length();

        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

        int end = start;
        while (end < json.length() && ",}]".indexOf(json.charAt(end)) == -1) end++;

        return json.substring(start, end).trim();
    }
}