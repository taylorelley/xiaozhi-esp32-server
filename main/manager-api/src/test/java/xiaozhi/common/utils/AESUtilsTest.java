package xiaozhi.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AESUtilsTest {

    @Test
    public void testEncryptAndDecrypt() {
        String key = "xiaozhi1234567890";
        String plainText = "Hello, smallintelligent!";

        System.out.println("originaltext: " + plainText);
        System.out.println("key: " + key);

        // encrypt
        String encrypted = AESUtils.encrypt(key, plainText);
        System.out.println("encryptresultresult: " + encrypted);

        // decrypt
        String decrypted = AESUtils.decrypt(key, encrypted);
        System.out.println("decryptresultresult: " + decrypted);

        // verification
        assertEquals(plainText, decrypted, "adddecryptresultresultshouldthisconsistent");
        System.out.println("adddecryptconsistent: " + plainText.equals(decrypted));
    }

    @Test
    public void testDifferentKeyLengths() {
        String[] keys = {
                "1234567890123456", // 16bit
                "123456789012345678901234", // 24bit
                "12345678901234567890123456789012", // 32bit
                "short", // shortkey
                "verylongkeythatwillbetruncatedto32bytes" // longkey
        };

        String plainText = "测trytext";

        for (String key : keys) {
            String encrypted = AESUtils.encrypt(key, plainText);
            String decrypted = AESUtils.decrypt(key, encrypted);
            assertEquals(plainText, decrypted, "keylength: " + key.length());
        }
    }

    @Test
    public void testSpecialCharacters() {
        String key = "xiaozhi1234567890";
        String[] testTexts = {
                "Hello World",
                "yougood世boundary",
                "Hello, smallintelligent!",
                "specialcharactersymbol: !@#$%^&*()",
                "number123andtextmixmerge",
                "Emoji: 😀🎉🚀",
                "emptystring测try",
                ""
        };

        for (String text : testTexts) {
            String encrypted = AESUtils.encrypt(key, text);
            String decrypted = AESUtils.decrypt(key, encrypted);
            assertEquals(text, decrypted, "测trytext: " + text);
        }
    }

    @Test
    public void testCrossLanguageCompatibility() {
        // theseYesPythonversionthisgenerate encryptresultresult，used for测trycrossLanguagecompatible
        String key = "xiaozhi1234567890";
        String plainText = "Hello, smallintelligent!";

        // Pythonversionthisgenerate encryptresultresult（needrunPython测tryafterget）
        // String pythonEncrypted = "fromPython测tryget encryptresultresult";
        // String decrypted = AESUtils.decrypt(key, pythonEncrypted);
        // assertEquals(plainText, decrypted, "JavashouldthiscandecryptPythonencrypt resultresult");

        // generateJavaencryptresultresultprovidePython测try
        String javaEncrypted = AESUtils.encrypt(key, plainText);
        System.out.println("JavaencryptresultresultprovidePython测try: " + javaEncrypted);
    }
}