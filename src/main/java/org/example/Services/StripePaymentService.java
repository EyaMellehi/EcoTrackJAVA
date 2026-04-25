package org.example.Services;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.sun.net.httpserver.HttpServer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import org.example.Entities.Association;
import org.example.Entities.Donation;
import org.example.Entities.User;

import java.awt.Desktop;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class StripePaymentService {

    /* ===================================================== */
    /* STRIPE SECRET KEY                                     */
    /* ===================================================== */
     /* ===================================================== */
    private static HttpServer server;
    private static int PORT = 5053;

    /* CURRENT PAYMENT DATA */
    private static DonationService donationServiceRef;
    private static Donation donationRef;
    private static User userRef;
    private static DonationMailService donationMailServiceRef;

    /* ===================================================== */
    public StripePaymentService(
            DonationService service,
            Donation donation,
            User user
    ) {

        Stripe.apiKey = SECRET_KEY;

        donationServiceRef = service;
        donationRef = donation;
        userRef = user;
        donationMailServiceRef =
                new DonationMailService();
        startServer();
    }

    /* ===================================================== */
    /* OPEN STRIPE CHECKOUT                                  */
    /* ===================================================== */
    public String createCheckoutSession(
            double amount,
            User user,
            Association association
    ) throws Exception {

        long cents =
                Math.round(amount * 100);

        SessionCreateParams params =

                SessionCreateParams.builder()

                        .setMode(
                                SessionCreateParams.Mode.PAYMENT
                        )

                        .setSuccessUrl(
                                "http://localhost:"
                                        + PORT
                                        + "/success"
                        )

                        .setCancelUrl(
                                "http://localhost:"
                                        + PORT
                                        + "/cancel"
                        )

                        .setCustomerEmail(
                                user.getEmail()
                        )

                        .addLineItem(

                                SessionCreateParams.LineItem
                                        .builder()

                                        .setQuantity(1L)

                                        .setPriceData(

                                                SessionCreateParams
                                                        .LineItem
                                                        .PriceData
                                                        .builder()

                                                        .setCurrency("eur")

                                                        .setUnitAmount(cents)

                                                        .setProductData(

                                                                SessionCreateParams
                                                                        .LineItem
                                                                        .PriceData
                                                                        .ProductData
                                                                        .builder()

                                                                        .setName(
                                                                                "Donation - "
                                                                                        + association.getNom()
                                                                        )

                                                                        .build()
                                                        )

                                                        .build()
                                        )

                                        .build()
                        )

                        .build();

        Session session =
                Session.create(params);

        return session.getUrl();
    }

    /* ===================================================== */
    /* AUTO START CALLBACK SERVER                            */
    /* ===================================================== */
    private void startServer() {

        try {

            if (server != null) {
                return;
            }

            server =
                    HttpServer.create(
                            new InetSocketAddress(PORT),
                            0
                    );

            /* ============================================= */
            /* SUCCESS                                       */
            /* ============================================= */
            server.createContext("/success", exchange -> {

                try {

                    /* INSERT DB */
                    if (donationRef != null &&
                            donationServiceRef != null) {

                        donationRef.setStatut("Success");

                        donationServiceRef.add(
                                donationRef
                        );
                        donationMailServiceRef.sendDonationPdf(userRef , donationRef);
                    }

                    String html =
                            "<html>" +
                                    "<head>" +
                                    "<meta charset='UTF-8'>" +
                                    "<script>" +
                                    "setTimeout(function(){window.close();},1200);" +
                                    "</script>" +
                                    "</head>" +
                                    "<body style='font-family:Arial;text-align:center;padding:50px'>" +
                                    "<h1>Paiement réussi ✅</h1>" +
                                    "<p>Donation enregistrée.</p>" +
                                    "<p>Retour automatique...</p>" +
                                    "</body></html>";

                    byte[] bytes =
                            html.getBytes(
                                    StandardCharsets.UTF_8
                            );

                    exchange.getResponseHeaders().set(
                            "Content-Type",
                            "text/html; charset=UTF-8"
                    );

                    exchange.sendResponseHeaders(
                            200,
                            bytes.length
                    );

                    OutputStream os =
                            exchange.getResponseBody();

                    os.write(bytes);
                    os.close();

                    Platform.runLater(() -> {

                        try {

                            for (Stage s :
                                    Stage.getWindows()
                                            .filtered(w -> w instanceof Stage)
                                            .toArray(Stage[]::new)) {

                                s.toFront();
                                s.requestFocus();
                                break;
                            }

                        } catch (Exception ignored) {
                        }

                        Alert alert =
                                new Alert(
                                        Alert.AlertType.INFORMATION
                                );

                        alert.setTitle("Stripe");
                        alert.setHeaderText(null);
                        alert.setContentText(
                                "Paiement confirmé et donation ajoutée ✅"
                        );

                        alert.show();
                    });

                    stopServer();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            /* ============================================= */
            /* CANCEL                                        */
            /* ============================================= */
            server.createContext("/cancel", exchange -> {

                try {

                    String html =
                            "<html>" +
                                    "<head><meta charset='UTF-8'></head>" +
                                    "<body style='font-family:Arial;text-align:center;padding:50px'>" +
                                    "<h1>Paiement annulé ❌</h1>" +
                                    "<p>Aucune donation ajoutée.</p>" +
                                    "</body></html>";

                    byte[] bytes =
                            html.getBytes(
                                    StandardCharsets.UTF_8
                            );

                    exchange.getResponseHeaders().set(
                            "Content-Type",
                            "text/html; charset=UTF-8"
                    );

                    exchange.sendResponseHeaders(
                            200,
                            bytes.length
                    );

                    OutputStream os =
                            exchange.getResponseBody();

                    os.write(bytes);
                    os.close();

                    Platform.runLater(() -> {

                        Alert alert =
                                new Alert(
                                        Alert.AlertType.WARNING
                                );

                        alert.setTitle("Stripe");
                        alert.setHeaderText(null);
                        alert.setContentText(
                                "Paiement annulé."
                        );

                        alert.show();
                    });

                    stopServer();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            server.start();

            System.out.println(
                    "Stripe callback started on port "
                            + PORT
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===================================================== */
    /* STOP SERVER AFTER PAYMENT                             */
    /* ===================================================== */
    private void stopServer() {

        try {

            if (server != null) {

                server.stop(0);
                server = null;

                System.out.println(
                        "Stripe callback stopped."
                );
            }

        } catch (Exception ignored) {
        }
    }

    /* ===================================================== */
    /* DIRECT OPEN CHECKOUT                                  */
    /* ===================================================== */
    public void pay(
            double amount,
            User user,
            Association association
    ) {

        try {

            String url =
                    createCheckoutSession(
                            amount,
                            user,
                            association
                    );

            Desktop.getDesktop()
                    .browse(
                            new URI(url)
                    );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}