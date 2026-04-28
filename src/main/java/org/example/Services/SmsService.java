package org.example.Services;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.example.Entities.Event;
import org.example.Entities.User;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsService {

    private static final DateTimeFormatter EVENT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static volatile boolean initialized = false;
    private static volatile String initializedSid;
    private static volatile String initializedToken;
    private static volatile Properties fileConfig;

    public boolean isConfigured() {
        return notBlank(readConfig("TWILIO_ACCOUNT_SID"))
                && notBlank(readConfig("TWILIO_AUTH_TOKEN"));
    }

    public int notifyCitizensForNewEvent(Event event, List<User> citizens) {
        if (event == null || citizens == null || citizens.isEmpty() || !isConfigured()) {
            if (event != null && (citizens == null || citizens.isEmpty())) {
                System.out.println("SMS: aucun citoyen cible pour la region: " + safe(event.getLieu()));
            }
            return 0;
        }

        ensureInitialized();
        int sent = 0;
        String message = buildEventMessage(event);
        String from = resolveFromNumber();
        Set<String> sentPhones = new HashSet<>();
        if (!notBlank(from)) {
            System.err.println("SMS: aucun numero expéditeur valide trouve pour ce compte Twilio.");
            return 0;
        }
        System.out.println("SMS: tentative d'envoi vers " + citizens.size() + " citoyens, depuis " + from + ".");

        for (User citizen : citizens) {
            if (citizen == null || !citizen.isActive()) {
                continue;
            }

            String phone = normalizePhone(citizen.getPhone());
            if (phone == null) {
                continue;
            }

            if (sentPhones.contains(phone)) {
                System.out.println("SMS: doublon ignore pour le numero " + phone);
                continue;
            }

            try {
                Message.creator(new PhoneNumber(phone), new PhoneNumber(from), message).create();
                sent++;
                sentPhones.add(phone);
            } catch (ApiException ex) {
                System.err.println("SMS Twilio non envoye a " + phone + " : " + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("Erreur SMS inattendue pour " + phone + " : " + ex.getMessage());
            }
        }

        return sent;
    }

    private void ensureInitialized() {
        String sid = readConfig("TWILIO_ACCOUNT_SID");
        String token = readConfig("TWILIO_AUTH_TOKEN");

        if (!notBlank(sid) || !notBlank(token)) {
            return;
        }

        if (initialized && sid.equals(initializedSid) && token.equals(initializedToken)) {
            return;
        }

        synchronized (SmsService.class) {
            if (!initialized || !sid.equals(initializedSid) || !token.equals(initializedToken)) {
                Twilio.init(sid, token);
                initialized = true;
                initializedSid = sid;
                initializedToken = token;
            }
        }
    }

    private String buildEventMessage(Event event) {
        String dateText = event.getDateDeb() == null ? "date a confirmer" : event.getDateDeb().format(EVENT_DATE_FORMAT);
        return "🌿 EcoTrack - Nouvel evenement\n"
                + "📌 " + safe(event.getTitre()) + "\n"
                + "📍 Region: " + safe(event.getLieu()) + "\n"
                + "📅 Date: " + dateText + "\n"
                + "✅ Ouvrez l'application pour vous inscrire.";
    }

    private String normalizePhone(String rawPhone) {
        if (!notBlank(rawPhone)) {
            return null;
        }

        String cleaned = rawPhone.trim().replaceAll("[^\\d+]", "");

        if (cleaned.matches("^\\+\\d{8,15}$")) {
            return cleaned;
        }

        if (cleaned.matches("^00\\d{8,15}$")) {
            return "+" + cleaned.substring(2);
        }

        // Format local tunisien a 8 chiffres -> +216XXXXXXXX
        if (cleaned.matches("^\\d{8}$")) {
            return "+216" + cleaned;
        }

        // Format deja avec +216 mais avec separateur nettoye
        if (cleaned.matches("^216\\d{8}$")) {
            return "+" + cleaned;
        }

        return null;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String resolveFromNumber() {
        String configured = readConfig("TWILIO_FROM_NUMBER");
        String sid = readConfig("TWILIO_ACCOUNT_SID");
        String token = readConfig("TWILIO_AUTH_TOKEN");

        List<String> accountNumbers = fetchIncomingNumbers(sid, token);
        if (accountNumbers.isEmpty()) {
            return configured;
        }

        if (notBlank(configured)) {
            for (String accountNumber : accountNumbers) {
                if (accountNumber.equals(configured.trim())) {
                    return accountNumber;
                }
            }
            System.out.println("SMS: numero FROM configure non associe au compte, bascule vers " + accountNumbers.get(0));
        }

        return accountNumbers.get(0);
    }

    private List<String> fetchIncomingNumbers(String sid, String token) {
        List<String> numbers = new ArrayList<>();
        if (!notBlank(sid) || !notBlank(token)) {
            return numbers;
        }

        try {
            URL url = new URL("https://api.twilio.com/2010-04-01/Accounts/" + sid + "/IncomingPhoneNumbers.json?PageSize=20");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            String auth = sid + ":" + token;
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.US_ASCII));
            connection.setRequestProperty("Authorization", "Basic " + encoded);

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                return numbers;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            Pattern pattern = Pattern.compile("\\\"phone_number\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
            Matcher matcher = pattern.matcher(response.toString());
            while (matcher.find()) {
                String phone = matcher.group(1);
                if (notBlank(phone)) {
                    numbers.add(phone.trim());
                }
            }
        } catch (Exception ex) {
            System.err.println("SMS: impossible de recuperer les numeros Twilio du compte: " + ex.getMessage());
        }

        return numbers;
    }

    private String readConfig(String key) {
        String value = System.getenv(key);
        if (notBlank(value)) {
            return value.trim();
        }
        value = System.getProperty(key);
        if (notBlank(value)) {
            return value.trim();
        }
        value = getFileConfig().getProperty(key);
        if (notBlank(value)) {
            return value.trim();
        }
        return null;
    }

    private Properties getFileConfig() {
        if (fileConfig != null) {
            return fileConfig;
        }

        synchronized (SmsService.class) {
            if (fileConfig != null) {
                return fileConfig;
            }

            Properties properties = new Properties();
            // Fichier local pour executer depuis IDE sans variables d'environnement.
            Path localPath = Path.of("twilio.properties");
            if (Files.exists(localPath)) {
                try (FileInputStream in = new FileInputStream(localPath.toFile())) {
                    properties.load(in);
                } catch (Exception ex) {
                    System.err.println("SMS: lecture twilio.properties impossible: " + ex.getMessage());
                }
            }

            fileConfig = properties;
            return fileConfig;
        }
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }
        return value.trim();
    }
}

