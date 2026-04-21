package org.example.Services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.samstevens.totp.secret.DefaultSecretGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;

public class TwoFactorService {

    public String generateSecret() {
        return new DefaultSecretGenerator().generate();
    }

    public String buildOtpAuthUrl(String email, String secret) {
        return "otpauth://totp/EcoTrack:" + email + "?secret=" + secret + "&issuer=EcoTrack";
    }

    public ByteArrayInputStream generateQrCodeImage(String otpAuthUrl) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 250, 250);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        return new ByteArrayInputStream(pngOutputStream.toByteArray());
    }
    public boolean verifyCode(String secret, String code) {
        DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
        SystemTimeProvider timeProvider = new SystemTimeProvider();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        return verifier.isValidCode(secret, code);
    }
}