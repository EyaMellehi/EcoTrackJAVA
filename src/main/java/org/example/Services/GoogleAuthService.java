package org.example.Services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import org.example.Entities.GoogleUserInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GoogleAuthService {

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of("openid", "email", "profile");

    public GoogleUserInfo authenticate() throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = getClass().getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new IllegalStateException("credentials.json not found in resources");
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(in, StandardCharsets.UTF_8)
        );

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        ).setAccessType("offline").build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setHost("localhost")
                .setPort(8888)
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                .authorize("user");

        var request = httpTransport.createRequestFactory(credential)
                .buildGetRequest(new GenericUrl("https://openidconnect.googleapis.com/v1/userinfo"));

        request.setParser(new JsonObjectParser(JSON_FACTORY));
        HttpResponse response = request.execute();

        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = response.parseAs(Map.class);

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Google email not returned");
        }

        return new GoogleUserInfo(email, name, picture);
    }
}