package com.example.atm.Utils;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class PasswordUtils {



    public static String decryptPassword(String encryptedBase64, String secretKey) throws Exception {
        if (encryptedBase64 == null || encryptedBase64.isEmpty()) return "";

        byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new IllegalArgumentException("SECRET_KEY length must be 16, 24, or 32 bytes.");
        }

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}