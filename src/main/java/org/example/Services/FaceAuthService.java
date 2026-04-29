package org.example.Services;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaceAuthService {

    public Integer identifyUserByFace(File imageFile) throws Exception {
        String boundary = "----Boundary" + System.currentTimeMillis();
        URL url = new URL("http://127.0.0.1:5000/identify");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Accept", "application/json");

        try (OutputStream output = connection.getOutputStream()) {
            String fileName = imageFile.getName();

            String partHeader =
                    "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                            "Content-Type: image/jpeg\r\n\r\n";

            output.write(partHeader.getBytes(StandardCharsets.UTF_8));
            Files.copy(imageFile.toPath(), output);
            output.write("\r\n".getBytes(StandardCharsets.UTF_8));

            String closing = "--" + boundary + "--\r\n";
            output.write(closing.getBytes(StandardCharsets.UTF_8));
            output.flush();
        }

        int status = connection.getResponseCode();
        InputStream inputStream = (status >= 200 && status < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        if (!response.contains("\"matched\": true") && !response.contains("\"matched\":true")) {
            return null;
        }

        Matcher matcher = Pattern.compile("\"user_id\"\\s*:\\s*(\\d+)").matcher(response);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }
}