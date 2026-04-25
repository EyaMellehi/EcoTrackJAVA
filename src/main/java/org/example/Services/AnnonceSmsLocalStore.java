package org.example.Services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnnonceSmsLocalStore {

    private static final Path LOG_PATH = Path.of("var", "sms", "annonce_sms_dispatch.csv");
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Set<String> sentKeys = new HashSet<>();
    private boolean initialized;

    public synchronized boolean alreadySent(int annonceId, int userId, String phone) {
        ensureLoaded();
        return sentKeys.contains(buildKey(annonceId, userId, phone));
    }

    public synchronized void markSent(int annonceId, int userId, String phone, String status, String error) {
        ensureLoaded();

        String timestamp = LocalDateTime.now().format(TS_FORMAT);
        String line = toCsv(timestamp, Integer.toString(annonceId), Integer.toString(userId),
                safe(phone), safe(status), safe(error));

        try {
            Files.createDirectories(LOG_PATH.getParent());
            if (!Files.exists(LOG_PATH)) {
                String header = "sent_at,annonce_id,user_id,phone,status,error" + System.lineSeparator();
                Files.writeString(LOG_PATH, header, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            Files.writeString(LOG_PATH, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            if ("SENT".equalsIgnoreCase(status)) {
                sentKeys.add(buildKey(annonceId, userId, phone));
            }
        } catch (IOException ignored) {
            // Non bloquant: on ne casse pas le flux d'envoi si l'ecriture locale echoue.
        }
    }

    private void ensureLoaded() {
        if (initialized) {
            return;
        }
        initialized = true;

        if (!Files.exists(LOG_PATH)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(LOG_PATH, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.isBlank()) {
                    continue;
                }
                String[] parts = splitCsv(line);
                if (parts.length < 5) {
                    continue;
                }
                String status = unquote(parts[4]);
                if (!"SENT".equalsIgnoreCase(status)) {
                    continue;
                }
                int annonceId = parseInt(unquote(parts[1]));
                int userId = parseInt(unquote(parts[2]));
                String phone = unquote(parts[3]);
                if (annonceId > 0 && userId > 0 && !phone.isBlank()) {
                    sentKeys.add(buildKey(annonceId, userId, phone));
                }
            }
        } catch (IOException ignored) {
            // Non bloquant: si le log est illisible, on continue sans historique local.
        }
    }

    private String[] splitCsv(String line) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // Guillemet echappe dans CSV: ""
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                    continue;
                }
                inQuotes = !inQuotes;
                continue;
            }

            if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(ch);
        }

        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    private String toCsv(String... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            String value = values[i] == null ? "" : values[i];
            sb.append('"').append(value.replace("\"", "\"\"")).append('"');
        }
        return sb.toString();
    }

    private String unquote(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed.replace("\"\"", "\"");
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }
    }

    private String buildKey(int annonceId, int userId, String phone) {
        return annonceId + "|" + userId + "|" + normalizePhone(phone);
    }

    private String normalizePhone(String phone) {
        return safe(phone).replace(" ", "").replace("-", "");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}


