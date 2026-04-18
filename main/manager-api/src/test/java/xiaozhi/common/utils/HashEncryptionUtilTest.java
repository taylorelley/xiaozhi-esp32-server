package xiaozhi.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashEncryptionUtilTest {

    @ParameterizedTest(name = "MD5({0}) = {1}")
    @CsvSource({
            "'', d41d8cd98f00b204e9800998ecf8427e",
            "abc, 900150983cd24fb0d6963f7d28e17f72",
            "The quick brown fox jumps over the lazy dog, 9e107d9d372bb6826bd81d3542a419d6",
    })
    void md5KnownVectors(String input, String expected) {
        assertEquals(expected, HashEncryptionUtil.Md5hexDigest(input));
    }

    @Test
    void md5OutputIsLowercaseHex() {
        String hash = HashEncryptionUtil.Md5hexDigest("xiaozhi");
        assertEquals(32, hash.length());
        assertTrue(hash.matches("[0-9a-f]{32}"), "expected lowercase hex, got " + hash);
    }

    @Test
    void md5IsDeterministic() {
        assertEquals(HashEncryptionUtil.Md5hexDigest("same"),
                HashEncryptionUtil.Md5hexDigest("same"));
    }

    @Test
    void differentInputsYieldDifferentHashes() {
        assertNotEquals(HashEncryptionUtil.Md5hexDigest("a"),
                HashEncryptionUtil.Md5hexDigest("b"));
    }

    @ParameterizedTest(name = "SHA-256({0})")
    @CsvSource({
            "'', e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "abc, ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
    })
    void sha256KnownVectors(String input, String expected) {
        assertEquals(expected, HashEncryptionUtil.hexDigest(input, "SHA-256"));
    }

    @Test
    void unknownAlgorithmThrowsRuntimeException() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> HashEncryptionUtil.hexDigest("x", "NOT-A-REAL-ALGORITHM"));
        assertTrue(ex.getMessage().contains("NOT-A-REAL-ALGORITHM"));
    }

    @Test
    void leadingZeroBytesArePaddedInHex() {
        // The hash of this specific input happens to start with a 0-nibble byte;
        // the important behaviour is that every byte produces exactly two hex digits.
        String hash = HashEncryptionUtil.Md5hexDigest("xyzzy");
        assertEquals(32, hash.length());
    }
}
