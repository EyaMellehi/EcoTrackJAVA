package org.example.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PasswordResetStore {

    private static final Map<String, ResetData> resetCodes = new HashMap<>();

    public static String generateCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        long expireAt = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutes
        resetCodes.put(email, new ResetData(code, expireAt));
        return code;
    }

    public static boolean verifyCode(String email, String code) {
        ResetData data = resetCodes.get(email);

        if (data == null) {
            return false;
        }

        if (System.currentTimeMillis() > data.expireAt) {
            resetCodes.remove(email);
            return false;
        }

        boolean valid = data.code.equals(code);

        if (valid) {
            resetCodes.remove(email);
        }

        return valid;
    }

    private static class ResetData {
        String code;
        long expireAt;

        ResetData(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}