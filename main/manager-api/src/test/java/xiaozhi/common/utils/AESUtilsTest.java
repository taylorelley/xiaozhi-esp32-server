package xiaozhi.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AESUtilsTest {

    @Test
    void encryptDecryptRoundtripWith16ByteKey() {
        String key = "0123456789abcdef"; // 16 bytes
        String plain = "hello xiaozhi";

        String encrypted = AESUtils.encrypt(key, plain);
        assertNotNull(encrypted);
        assertNotEquals(plain, encrypted);

        String decrypted = AESUtils.decrypt(key, encrypted);
        assertEquals(plain, decrypted);
    }

    @ParameterizedTest
    @ValueSource(ints = {16, 24, 32})
    void roundtripWithSupportedKeyLengths(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append((char) ('a' + (i % 26)));
        String key = sb.toString();
        String plain = "msg-of-len-" + len;

        String encrypted = AESUtils.encrypt(key, plain);
        assertEquals(plain, AESUtils.decrypt(key, encrypted));
    }

    @Test
    void shortKeyIsZeroPaddedToBlockSize() {
        // A 5-byte key is padded out to 32 bytes (AES-256) with trailing zeros.
        String key = "short";
        String plain = "padding test";

        String encrypted = AESUtils.encrypt(key, plain);
        assertEquals(plain, AESUtils.decrypt(key, encrypted));
    }

    @Test
    void longKeyIsTruncatedTo32Bytes() {
        // Keys longer than 32 bytes are truncated. Two keys that share their
        // first 32 bytes must therefore produce identical ciphertext.
        String base = "0123456789abcdef0123456789abcdef"; // exactly 32 bytes
        String plain = "truncation test";

        String encA = AESUtils.encrypt(base, plain);
        String encB = AESUtils.encrypt(base + "extra-bytes-ignored", plain);
        assertEquals(encA, encB);
    }

    @Test
    void differentKeysProduceDifferentCiphertext() {
        String plain = "consistent plaintext";
        String a = AESUtils.encrypt("0123456789abcdef", plain);
        String b = AESUtils.encrypt("fedcba9876543210", plain);
        assertNotEquals(a, b);
    }

    @Test
    void sameKeyAndPlaintextAreDeterministicUnderEcb() {
        // AES/ECB/PKCS5Padding is deterministic: identical inputs yield identical outputs.
        String key = "0123456789abcdef";
        String plain = "repeat me";
        assertEquals(AESUtils.encrypt(key, plain), AESUtils.encrypt(key, plain));
    }

    @Test
    void decryptGarbageThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> AESUtils.decrypt("0123456789abcdef", "not-valid-base64-&-not-ciphertext"));
        assertTrue(ex.getMessage().contains("decrypt") || ex.getCause() != null);
    }

    @Test
    void multibyteUnicodeRoundtrips() {
        String key = "secret-key-1234";
        String plain = "你好，xiaozhi 👋";
        assertEquals(plain, AESUtils.decrypt(key, AESUtils.encrypt(key, plain)));
    }
}
