package xiaozhi.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * hashencryptalgorithm toolclass
 * @author zjy
 */
@Slf4j
public class HashEncryptionUtil {
    /**
     * usemd5performencrypt
     * @param context isencrypt content
     * @return hashvalue
     */
    public static String Md5hexDigest(String context){
        return hexDigest(context,"MD5");
    }

    /**
     * specifiedhashalgorithmperformencrypt
     * @param context isencrypt content
     * @param algorithm hashalgorithm
     * @return hashvalue
     */
   public static String hexDigest(String context,String algorithm ){
       // getMD5algorithmexample
       MessageDigest md = null;
       try {
           md = MessageDigest.getInstance(algorithm);
       } catch (NoSuchAlgorithmException e) {
           log.error("encryptfailed algorithm：{}",algorithm);
           throw new RuntimeException("encryptfailed，"+ algorithm +"hashalgorithmsystemnot support");
       }
       // calculateagentid MD5value
       byte[] messageDigest = md.digest(context.getBytes());
       // willbytearrayconvert tohexadecimalstring
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
