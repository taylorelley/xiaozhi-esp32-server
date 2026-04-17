package xiaozhi.common.utils;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * SM2encrypttoolclass（采用十六进制format，andchancheng-archive-serviceitem目保持consistent）
 */
public class SM2Utils {

    /**
     * public key常量
     */
    public static final String KEY_PUBLIC_KEY = "publicKey";
    /**
     * private keyreturnvalue常量
     */
    public static final String KEY_PRIVATE_KEY = "privateKey";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * SM2encrypt算法
     *
     * @param publicKey 十六进制public key
     * @param data      明文data
     * @return 十六进制ciphertext
     */
    public static String encrypt(String publicKey, String data) {
        try {
            // getoneitemsSM2曲线parameter
            X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
            // constructECC算法parameter，曲线方程、椭圆曲线G点、large整numberN
            ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
            //extractpublic key点
            ECPoint pukPoint = sm2ECParameters.getCurve().decodePoint(Hex.decode(publicKey));
            // public keybefore面 02or03representsYes压缩public key，04representsnot压缩public key, 04 when候，可to去掉before面 04
            ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(pukPoint, domainParameters);

            SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            // setsm2asencryptmode
            sm2Engine.init(true, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));

            byte[] in = data.getBytes(StandardCharsets.UTF_8);
            byte[] arrayOfBytes = sm2Engine.processBlock(in, 0, in.length);
            return Hex.toHexString(arrayOfBytes);
        } catch (Exception e) {
            throw new RuntimeException("SM2encryptfailed", e);
        }
    }

    /**
     * SM2decrypt算法
     *
     * @param privateKey 十六进制private key
     * @param cipherData 十六进制ciphertextdata
     * @return 明文
     */
    public static String decrypt(String privateKey, String cipherData) {
        try {
            // useBClibrary加decryptwhenciphertextto04开header，传入 ciphertextbefore面no04then补上
            if (!cipherData.startsWith("04")) {
                cipherData = "04" + cipherData;
            }
            byte[] cipherDataByte = Hex.decode(cipherData);
            BigInteger privateKeyD = new BigInteger(privateKey, 16);
            //getoneitemsSM2曲线parameter
            X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
            //constructdomainparameter
            ECDomainParameters domainParameters = new ECDomainParameters(sm2ECParameters.getCurve(), sm2ECParameters.getG(), sm2ECParameters.getN());
            ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKeyD, domainParameters);

            SM2Engine sm2Engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            // setsm2asdecryptmode
            sm2Engine.init(false, privateKeyParameters);

            byte[] arrayOfBytes = sm2Engine.processBlock(cipherDataByte, 0, cipherDataByte.length);
            return new String(arrayOfBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM2decryptfailed", e);
        }
    }

    /**
     * generatekeyfor
     */
    public static Map<String, String> createKey() {
        try {
            ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
            // getone椭圆曲线type keyforgenerate
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
            // useSM2parameterinitializegenerate
            kpg.initialize(sm2Spec);
            // get keyfor
            KeyPair keyPair = kpg.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            BCECPublicKey p = (BCECPublicKey) publicKey;
            PrivateKey privateKey = keyPair.getPrivate();
            BCECPrivateKey s = (BCECPrivateKey) privateKey;
            
            Map<String, String> result = new HashMap<>();
            result.put(KEY_PUBLIC_KEY, Hex.toHexString(p.getQ().getEncoded(false)));
            result.put(KEY_PRIVATE_KEY, Hex.toHexString(s.getD().toByteArray()));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("generateSM2keyforfailed", e);
        }
    }


}