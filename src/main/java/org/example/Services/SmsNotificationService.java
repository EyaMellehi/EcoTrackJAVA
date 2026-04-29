package org.example.Services;

import org.example.Entities.Annonce;
import org.example.Entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmsNotificationService {

    private static final String TWILIO_BASE_URL = "https://api.twilio.com/2010-04-01/Accounts";
    private static final Path TWILIO_FILE = Path.of("twilio_sms.local.txt");
    private static final String DEFAULT_COUNTRY_CODE_FALLBACK = "+216";

    private final AnnonceSmsLocalStore localStore = new AnnonceSmsLocalStore();

    public SmsDispatchReport notifyCitizensForAnnonce(Annonce annonce, List<User> citizens) {
        SmsDispatchReport report = new SmsDispatchReport();
        if (annonce == null || citizens == null || citizens.isEmpty()) {
            return report;
        }

        for (User user : citizens) {
            if (user == null || user.getId() <= 0) {
                continue;
            }

            String phone = normalizePhone(user.getPhone());
            if (!isValidE164(phone)) {
                report.failed++;
                report.errors.add("Utilisateur #" + user.getId() + ": numero invalide (format attendu: +216XXXXXXXX). Valeur: " + safe(user.getPhone()));
                continue;
            }

            if (localStore.alreadySent(annonce.getId(), user.getId(), phone)) {
                report.skipped++;
                continue;
            }

            try {
                String body = buildSmsBody(annonce);
                sendSms(phone, body);
                localStore.markSent(annonce.getId(), user.getId(), phone, "SENT", "");
                report.sent++;
            } catch (Exception e) {
                String error = e.getMessage() == null ? "Erreur inconnue" : e.getMessage();
                localStore.markSent(annonce.getId(), user.getId(), phone, "FAILED", error);
                report.failed++;
                report.errors.add("Utilisateur #" + user.getId() + " (" + phone + "): " + error);
            }
        }

        return report;
    }

    public void sendSms(String toPhone, String message) throws IOException {
        TwilioConfig cfg = resolveTwilioConfig();
        if (cfg.accountSid.isBlank() || cfg.authToken.isBlank() || cfg.fromNumber.isBlank()) {
            throw new IOException("Configuration Twilio manquante (variables d'environnement ou twilio_sms.local.txt).");
        }

        String normalizedFrom = normalizePhone(cfg.fromNumber, cfg.defaultCountryCode);
        if (!isValidE164(normalizedFrom)) {
            throw new IOException("Configuration Twilio invalide: TWILIO_FROM_NUMBER doit etre au format E.164 (ex: +12183192973).");
        }

        String normalizedTo = normalizePhone(toPhone, cfg.defaultCountryCode);
        if (!isValidE164(normalizedTo)) {
            throw new IOException("Numero destinataire invalide: format E.164 requis (ex: +21626106036). Valeur recue: " + safe(toPhone));
        }

        String endpoint = TWILIO_BASE_URL + "/" + cfg.accountSid + "/Messages.json";
        HttpURLConnection con = (HttpURLConnection) URI.create(endpoint).toURL().openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setConnectTimeout(10000);
        con.setReadTimeout(20000);
        con.setRequestProperty("Authorization", basicAuth(cfg.accountSid, cfg.authToken));
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "From=" + urlEncode(normalizedFrom)
                + "&To=" + urlEncode(normalizedTo)
                + "&Body=" + urlEncode(message);

        try (OutputStream os = con.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = con.getResponseCode();
        String response = readAll(code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream());
        con.disconnect();

        if (code < 200 || code >= 300) {
            throw new IOException(parseTwilioError(code, response, normalizedTo));
        }
    }

    private TwilioConfig resolveTwilioConfig() {
        String sid = safe(System.getenv("TWILIO_ACCOUNT_SID"));
        String token = safe(System.getenv("TWILIO_AUTH_TOKEN"));
        String from = safe(System.getenv("TWILIO_FROM_NUMBER"));
        String defaultCountryCode = safe(System.getenv("TWILIO_DEFAULT_COUNTRY_CODE"));

        Map<String, String> local = readTwilioFile();
        if (sid.isBlank()) {
            sid = safe(local.get("TWILIO_ACCOUNT_SID"));
        }
        if (token.isBlank()) {
            token = safe(local.get("TWILIO_AUTH_TOKEN"));
        }
        if (from.isBlank()) {
            from = safe(local.get("TWILIO_FROM_NUMBER"));
        }
        if (defaultCountryCode.isBlank()) {
            defaultCountryCode = safe(local.get("TWILIO_DEFAULT_COUNTRY_CODE"));
        }

        if (defaultCountryCode.isBlank()) {
            defaultCountryCode = DEFAULT_COUNTRY_CODE_FALLBACK;
        }

        return new TwilioConfig(sid, token, normalizePhone(from, defaultCountryCode), normalizeCountryCode(defaultCountryCode));
    }

    private Map<String, String> readTwilioFile() {
        Map<String, String> values = new HashMap<>();
        if (!Files.exists(TWILIO_FILE)) {
            return values;
        }

        try {
            List<String> lines = Files.readAllLines(TWILIO_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null) {
                    continue;
                }
                String trimmed = line.replace("\uFEFF", "").trim();
                if (trimmed.isBlank() || trimmed.startsWith("#")) {
                    continue;
                }

                int idx = trimmed.indexOf('=');
                if (idx <= 0) {
                    continue;
                }

                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();
                values.put(key, value);
            }
        } catch (IOException ignored) {
            return values;
        }

        return values;
    }

    private String buildSmsBody(Annonce annonce) {
        String titre = safe(annonce.getTitre());
        if (titre.length() > 40) {
            titre = titre.substring(0, 40) + "...";
        }

        return "EcoTrack: Nouvelle annonce pour " + safe(annonce.getRegion())
                + " - " + titre + ". Ouvrez l'application pour voir les details.";
    }

    private String basicAuth(String sid, String token) {
        String raw = sid + ":" + token;
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(safe(value), StandardCharsets.UTF_8);
    }

    private String readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String normalizePhone(String phone) {
        return normalizePhone(phone, normalizeCountryCode(resolveDefaultCountryCode()));
    }

    private String normalizePhone(String phone, String defaultCountryCode) {
        String cleaned = safe(phone)
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
                .replace(".", "");
        if (cleaned.isBlank()) {
            return "";
        }

        if (cleaned.startsWith("00")) {
            cleaned = "+" + cleaned.substring(2);
        }

        if (cleaned.startsWith("+")) {
            String digits = digitsOnly(cleaned.substring(1));
            return digits.isBlank() ? "" : "+" + digits;
        }

        String digits = digitsOnly(cleaned);
        if (digits.isBlank()) {
            return "";
        }

        // Format local 8 chiffres -> ajoute automatiquement l'indicatif (par defaut +216)
        if (digits.length() == 8) {
            return normalizeCountryCode(defaultCountryCode) + digits;
        }

        // Format local avec 0 initial (ex: 026106036)
        if (digits.length() == 9 && digits.startsWith("0")) {
            return normalizeCountryCode(defaultCountryCode) + digits.substring(1);
        }

        // Numero international sans +
        return "+" + digits;
    }

    private String resolveDefaultCountryCode() {
        String fromEnv = safe(System.getenv("TWILIO_DEFAULT_COUNTRY_CODE"));
        if (!fromEnv.isBlank()) {
            return fromEnv;
        }
        Map<String, String> local = readTwilioFile();
        String fromFile = safe(local.get("TWILIO_DEFAULT_COUNTRY_CODE"));
        return fromFile.isBlank() ? DEFAULT_COUNTRY_CODE_FALLBACK : fromFile;
    }

    private String normalizeCountryCode(String code) {
        String digits = digitsOnly(safe(code));
        if (digits.isBlank()) {
            return DEFAULT_COUNTRY_CODE_FALLBACK;
        }
        return "+" + digits;
    }

    private String digitsOnly(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isDigit(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private boolean isValidE164(String phone) {
        if (phone == null || !phone.startsWith("+")) {
            return false;
        }
        String digits = digitsOnly(phone.substring(1));
        return digits.length() >= 8 && digits.length() <= 15 && digits.charAt(0) != '0';
    }

    private String parseTwilioError(int httpCode, String response, String toPhone) {
        if (response != null && response.contains("\"code\":21211")) {
            return "Numero destinataire invalide pour Twilio: " + toPhone
                    + " (format E.164 requis, ex: +21626106036).";
        }
        if (response != null && response.contains("\"code\":21608")) {
            return "Compte Twilio en mode trial: le numero " + toPhone
                    + " n'est pas verifie. Verifiez ce numero dans Twilio Console (Phone Numbers > Verified Caller IDs) "
                    + "ou passez le compte en payant.";
        }
        return "Twilio HTTP " + httpCode + ": " + safe(response);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static class TwilioConfig {
        private final String accountSid;
        private final String authToken;
        private final String fromNumber;
        private final String defaultCountryCode;

        private TwilioConfig(String accountSid, String authToken, String fromNumber, String defaultCountryCode) {
            this.accountSid = accountSid;
            this.authToken = authToken;
            this.fromNumber = fromNumber;
            this.defaultCountryCode = defaultCountryCode;
        }
    }

    public static class SmsDispatchReport {
        public int sent;
        public int skipped;
        public int failed;
        public final List<String> errors = new ArrayList<>();
    }
}


