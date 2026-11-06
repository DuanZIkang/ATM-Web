package com.example.atm.Utils;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class PasswordUtils {
    /**
     * 使用 AES/ECB/PKCS5Padding 解密 Base64 编码的密码字符串。
     * 如果 encryptedBase64 为 null 或空字符串，则返回空字符串。
     *
     * @param encryptedBase64 Base64 编码的加密密码字符串
     * @param secretKey AES 解密密钥，UTF-8 字节长度必须为 16、24 或 32
     * @return 解密后的 UTF-8 明文密码
     * @throws IllegalArgumentException secretKey 长度不符合 AES 密钥要求时抛出
     * @throws Exception Base64 解码、Cipher 初始化或解密过程失败时抛出
     */
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