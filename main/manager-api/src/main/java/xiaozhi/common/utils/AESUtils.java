package xiaozhi.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * AESencrypt
     * 
     * @param key       key（16bit、24bitor32bit）
     * @param plainText 待encryptstring
     * @return encrypt后 Base64string
     */
    public static String encrypt(String key, String plainText) {
        try {
            // ensurekey长度as16、24or32bit
            byte[] keyBytes = padKey(key.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("AESencryptfailed", e);
        }
    }

    /**
     * AESdecrypt
     * 
     * @param key           key（16bit、24bitor32bit）
     * @param encryptedText 待decrypt Base64string
     * @return decrypt后 string
     */
    public static String decrypt(String key, String encryptedText) {
        try {
            // ensurekey长度as16、24or32bit
            byte[] keyBytes = padKey(key.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AESdecryptfailed", e);
        }
    }

    /**
     * 填充keytospecified长度（16、24or32bit）
     * 
     * @param keyBytes 原始keybytearray
     * @return 填充后 keybytearray
     */
    private static byte[] padKey(byte[] keyBytes) {
        int keyLength = keyBytes.length;
        if (keyLength == 16 || keyLength == 24 || keyLength == 32) {
            return keyBytes;
        }

        // ifkey长度not 足，用0填充；if超，截取前32bit
        byte[] paddedKey = new byte[32];
        System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyLength, 32));
        return paddedKey;
    }
}
