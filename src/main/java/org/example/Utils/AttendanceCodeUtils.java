package org.example.Utils;

import org.example.Entities.Event;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public final class AttendanceCodeUtils {

    private static final String SECRET = System.getenv().getOrDefault(
            "ECOTRACK_ATTENDANCE_SECRET",
            "ecotrack-attendance-secret-v1"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private AttendanceCodeUtils() {
    }

    public static String generateFiveDigitCode(Event event, int userId) {
        if (event == null) {
            return null;
        }
        LocalDate referenceDate = resolveReferenceDate(event.getDateDeb());
        String payload = event.getId() + ":" + userId + ":" + referenceDate.format(DATE_FORMATTER) + ":" + SECRET;
        byte[] digest = sha256(payload);
        if (digest == null || digest.length < 4) {
            return null;
        }

        int raw = ((digest[0] & 0xFF) << 24)
                | ((digest[1] & 0xFF) << 16)
                | ((digest[2] & 0xFF) << 8)
                | (digest[3] & 0xFF);

        int value = Math.floorMod(raw, 100000);
        return String.format("%05d", value);
    }

    public static boolean matches(Event event, int userId, String providedCode) {
        if (providedCode == null || providedCode.isBlank()) {
            return false;
        }
        String expected = generateFiveDigitCode(event, userId);
        return expected != null && expected.equals(providedCode.trim());
    }

    public static String buildQrPayload(Event event, int userId) {
        return generateFiveDigitCode(event, userId);
    }

    public static String extractFiveDigitCodeFromPayload(String payload, Event expectedEvent, int expectedUserId) {
        if (payload == null || payload.isBlank() || expectedEvent == null) {
            return null;
        }

        Map<String, String> parsed = parsePayload(payload.trim());
        if (parsed.isEmpty()) {
            return null;
        }

        int eventId;
        int userId;
        LocalDate payloadDate;
        try {
            eventId = Integer.parseInt(parsed.getOrDefault("E", "-1"));
            userId = Integer.parseInt(parsed.getOrDefault("U", "-1"));
            payloadDate = LocalDate.parse(parsed.getOrDefault("D", ""), DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }

        if (eventId != expectedEvent.getId() || userId != expectedUserId) {
            return null;
        }

        String expectedSignature = buildSignature(eventId, userId, payloadDate);
        String receivedSignature = parsed.get("S");
        if (receivedSignature == null || !receivedSignature.equals(expectedSignature)) {
            return null;
        }

        return generateFiveDigitCode(expectedEvent, expectedUserId);
    }

    private static LocalDate resolveReferenceDate(LocalDateTime dateDeb) {
        if (dateDeb != null) {
            return dateDeb.toLocalDate();
        }
        return LocalDate.now();
    }

    private static byte[] sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static String buildSignature(int eventId, int userId, LocalDate referenceDate) {
        String signatureSeed = eventId + ":" + userId + ":" + referenceDate.format(DATE_FORMATTER) + ":" + SECRET;
        byte[] digest = sha256(signatureSeed);
        if (digest == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8 && i < digest.length; i++) {
            builder.append(String.format("%02x", digest[i]));
        }
        return builder.toString();
    }

    private static Map<String, String> parsePayload(String payload) {
        Map<String, String> values = new HashMap<>();
        String[] parts = payload.split("\\|");
        if (parts.length < 5 || !"ECOTRACK_CHECKIN".equals(parts[0])) {
            return values;
        }

        for (int i = 1; i < parts.length; i++) {
            String[] entry = parts[i].split("=", 2);
            if (entry.length == 2) {
                values.put(entry[0], entry[1]);
            }
        }
        return values;
    }
}

