package org.example.Services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.Entities.Donation;
import org.example.Entities.User;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class DonationMailService {

    /* ===================================================== */
    /* SEND PDF RECEIPT TO CONNECTED USER                    */
    /* ===================================================== */
    public void sendDonationPdf(User user, Donation donation) {

        try {

            if (user == null || user.getEmail() == null) {
                return;
            }

            /* ================= SMTP ================= */
            final String username =
                    "akrem-jouini@longevityplus.store";

            final String password =
                    "akrem_jouini";

            Properties props = new Properties();

            props.put(
                    "mail.smtp.host",
                    "mail.longevityplus.store"
            );

            props.put(
                    "mail.smtp.port",
                    "465"
            );

            props.put(
                    "mail.smtp.auth",
                    "true"
            );

            props.put(
                    "mail.smtp.ssl.enable",
                    "true"
            );

            Session session =
                    Session.getInstance(
                            props,
                            new Authenticator() {
                                protected PasswordAuthentication
                                getPasswordAuthentication() {

                                    return new PasswordAuthentication(
                                            username,
                                            password
                                    );
                                }
                            }
                    );

            /* ================= EMAIL ================= */
            Message message =
                    new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(username)
            );

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(
                            user.getEmail()
                    )
            );

            message.setSubject(
                    "Confirmation de votre donation 💚"
            );

            /* ================= BODY ================= */
            MimeBodyPart textPart =
                    new MimeBodyPart();

            textPart.setText(
                    "Bonjour "
                            + user.getName()
                            + ",\n\n"
                            + "Merci pour votre donation sur EcoTrack.\n"
                            + "Veuillez trouver votre reçu PDF en pièce jointe.\n\n"
                            + "Cordialement,\nEcoTrack"
            );

            /* ================= PDF ================= */
            MimeBodyPart pdfPart =
                    new MimeBodyPart();

            byte[] pdfBytes =
                    generatePdf(user, donation);

            pdfPart.setFileName(
                    "donation_receipt.pdf"
            );

            pdfPart.setContent(
                    pdfBytes,
                    "application/pdf"
            );

            /* ================= MULTIPART ================= */
            Multipart multipart =
                    new MimeMultipart();

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(pdfPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println(
                    "EMAIL SENT TO "
                            + user.getEmail()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===================================================== */
    /* GENERATE PDF                                          */
    /* ===================================================== */
    private byte[] generatePdf(
            User user,
            Donation d
    ) throws Exception {

        PDDocument doc =
                new PDDocument();

        PDPage page =
                new PDPage();

        doc.addPage(page);

        PDPageContentStream cs =
                new PDPageContentStream(
                        doc,
                        page
                );

        cs.beginText();
        cs.setFont(
                PDType1Font.HELVETICA_BOLD,
                18
        );

        cs.newLineAtOffset(50, 750);
        cs.showText("Donation Receipt");
        cs.endText();

        cs.beginText();
        cs.setFont(
                PDType1Font.HELVETICA,
                12
        );

        cs.newLineAtOffset(50, 710);

        cs.showText("Name: " + user.getName());
        cs.newLineAtOffset(0, -20);

        cs.showText("Email: " + user.getEmail());
        cs.newLineAtOffset(0, -20);

        cs.showText("Type: " + d.getType());
        cs.newLineAtOffset(0, -20);

        cs.showText("Amount: " + d.getMontant());
        cs.newLineAtOffset(0, -20);



        cs.showText(
                "Date: "
                        + d.getDateDon().format(
                        DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy HH:mm"))
        );

        cs.endText();
        cs.close();

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        doc.save(out);
        doc.close();

        return out.toByteArray();
    }
}