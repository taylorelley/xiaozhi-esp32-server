package xiaozhi.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 哈希encrypt算法 toolclass
 * @author zjy
 */
@Slf4j
public class HashEncryptionUtil {
    /**
     * usemd5performencrypt
     * @param context isencrypt content
     * @return 哈希value
     */
    public static String Md5hexDigest(String context){
        return hexDigest(context,"MD5");
    }

    /**
     * specified哈希算法performencrypt
     * @param context isencrypt content
     * @param algorithm 哈希算法
     * @return 哈希value
     */
   public static String hexDigest(String context,String algorithm ){
       // getMD5算法实example
       MessageDigest md = null;
       try {
           md = MessageDigest.getInstance(algorithm);
       } catch (NoSuchAlgorithmException e) {
           log.error("encryptfailed 算法：{}",algorithm);
           throw new RuntimeException("encryptfailed，"+ algorithm +"哈希算法systemnot support");
       }
       // 计算agentid MD5value
       byte[] messageDigest = md.digest(context.getBytes());
       // willbytearrayconvert to十六进制string
       StringBuilder hexString = new StringBuilder();
       for (byte b : messageDigest) {
           String hex = Integer.toHexString(0xFF & b);
           if (hex.length() == 1) {
               hexString.append('0');
           }
           hexString.append(hex);
       }
       return hexString.toString();
   }

}
