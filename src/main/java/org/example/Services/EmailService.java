package org.example.Services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "eyamellehi@gmail.com";

    private static final String APP_PASSWORD = "lizjrxxtcufehrkc";

    public void sendResetCode(String toEmail, String code) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("EcoTrack - Password Reset Code");

        String body = "Hello,\n\n"
                + "Your EcoTrack password reset code is: " + code + "\n\n"
                + "This code expires in 5 minutes.\n\n"
                + "EcoTrack Team";

        message.setText(body);

        Transport.send(message);
    }
    public void sendFieldAgentCredentials(String toEmail, String fullName, String password) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL, false));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("EcoTrack - Your Field Agent Account");

        String body =
                "Hello " + fullName + ",\n\n" +
                        "Your EcoTrack Field Agent account has been created.\n\n" +
                        "Login email: " + toEmail + "\n" +
                        "Password: " + password + "\n\n" +
                        "Please login and change your password after first access.\n\n" +
                        "EcoTrack Team";

        message.setText(body);

        Transport.send(message);
    }
    public void sendMunicipalAgentCredentials(String toEmail, String fullName, String password) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL, false));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("EcoTrack - Your Municipal Agent Account");

        String body =
                "Hello " + fullName + ",\n\n" +
                        "Your EcoTrack Municipal Agent account has been created.\n\n" +
                        "Login email: " + toEmail + "\n" +
                        "Password: " + password + "\n\n" +
                        "Please login and change your password after first access.\n\n" +
                        "EcoTrack Team";

        message.setText(body);

        Transport.send(message);
    }

    public void sendEventCheckinQr(String toEmail, String fullName, String eventTitle, String qrPayload) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        String safePayload = qrPayload == null ? "" : qrPayload;
        String qrUrl = "https://quickchart.io/qr?size=260&text=" +
                URLEncoder.encode(safePayload, StandardCharsets.UTF_8);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL, false));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("EcoTrack - Code de presence evenement");

        String safeName = fullName == null || fullName.isBlank() ? "Citoyen" : fullName;
        String safeEvent = eventTitle == null || eventTitle.isBlank() ? "Evenement EcoTrack" : eventTitle;
        String htmlBody = "<html><body style='font-family:Arial,sans-serif;'>"
                + "<h3>Bonjour " + safeName + ",</h3>"
                + "<p>Votre participation a <b>" + safeEvent + "</b> est confirmee.</p>"
                + "<p>Le jour J, montrez uniquement ce QR a l'agent municipal.</p>"
                + "<p>Apres scan, le PIN de presence sera genere pour validation.</p>"
                + "<p><img src='" + qrUrl + "' alt='QR check-in'/></p>"
                + "<p>Si l'image ne s'affiche pas, utilisez ce lien QR: <a href='" + qrUrl + "'>Ouvrir QR</a></p>"
                + "<p>Ne partagez pas ce QR avec d'autres personnes.</p>"
                + "<p>EcoTrack Team</p>"
                + "</body></html>";

        message.setContent(htmlBody, "text/html; charset=UTF-8");
        Transport.send(message);
    }
}