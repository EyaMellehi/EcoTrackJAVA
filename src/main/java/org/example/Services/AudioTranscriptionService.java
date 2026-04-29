package org.example.Services;

import org.example.Entities.AudioTranscriptionResult;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.File;

public class AudioTranscriptionService {

    private static final String API_KEY = "c25bb51f32a44c70aeb439de75a094b4";
    private static final String BASE_URL = "https://api.assemblyai.com/v2";

    public AudioTranscriptionResult transcribe(File audioFile) throws IOException, InterruptedException {
        if (audioFile == null || !audioFile.exists()) {
            throw new IOException("Audio file not found.");
        }

        String uploadUrl = uploadAudio(audioFile);
        String transcriptId = startTranscript(uploadUrl);
        return pollTranscript(transcriptId);
    }

    private String uploadAudio(File audioFile) throws IOException {
        URL url = new URL(BASE_URL + "/upload");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("authorization", API_KEY);
        connection.setRequestProperty("content-type", "application/octet-stream");

        try (OutputStream out = connection.getOutputStream();
             FileInputStream fis = new FileInputStream(audioFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        int code = connection.getResponseCode();
        String response = readAll(
                code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream()
        );

        if (code < 200 || code >= 300) {
            throw new IOException("Upload failed: HTTP " + code + " - " + response);
        }

        String uploadUrl = extractString(response, "upload_url");
        if (uploadUrl == null || uploadUrl.isBlank()) {
            throw new IOException("Upload URL not found.");
        }

        return uploadUrl;
    }

    private String startTranscript(String uploadUrl) throws IOException {
        URL url = new URL(BASE_URL + "/transcript");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("authorization", API_KEY);
        connection.setRequestProperty("content-type", "application/json");

        String body = "{"
                + "\"audio_url\":\"" + escapeJson(uploadUrl) + "\","
                + "\"speech_models\":[\"universal-3-pro\",\"universal-2\"],"
                + "\"language_detection\":true"
                + "}";
        try (OutputStream out = connection.getOutputStream()) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = connection.getResponseCode();
        String response = readAll(
                code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream()
        );

        if (code < 200 || code >= 300) {
            throw new IOException("Start transcript failed: HTTP " + code + " - " + response);
        }

        String id = extractString(response, "id");
        if (id == null || id.isBlank()) {
            throw new IOException("Transcript id not found.");
        }

        return id;
    }

    private AudioTranscriptionResult pollTranscript(String transcriptId) throws IOException, InterruptedException {
        int maxAttempts = 60;

        for (int i = 0; i < maxAttempts; i++) {
            URL url = new URL(BASE_URL + "/transcript/" + transcriptId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("authorization", API_KEY);

            int code = connection.getResponseCode();
            String response = readAll(
                    code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream()
            );

            if (code < 200 || code >= 300) {
                throw new IOException("Polling transcript failed: HTTP " + code + " - " + response);
            }

            String status = extractString(response, "status");

            if ("completed".equalsIgnoreCase(status)) {
                AudioTranscriptionResult result = new AudioTranscriptionResult();
                result.setSuccess(true);
                result.setId(transcriptId);
                result.setStatus(status);
                result.setText(extractString(response, "text"));
                return result;
            }

            if ("error".equalsIgnoreCase(status)) {
                AudioTranscriptionResult result = new AudioTranscriptionResult();
                result.setSuccess(false);
                result.setId(transcriptId);
                result.setStatus(status);
                result.setError(extractString(response, "error"));
                return result;
            }

            Thread.sleep(2000);
        }

        throw new IOException("Transcription timeout.");
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
                switch (c) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append("\\u").append(hex);
                                i += 4;
                            }
                        }
                    }
                    default -> sb.append(c);
                }
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                break;
            }

            sb.append(c);
        }

        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}