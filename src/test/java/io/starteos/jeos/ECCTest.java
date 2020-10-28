package io.starteos.jeos;

import org.junit.Test;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import io.starteos.jeos.utils.Base58;

public class ECCTest {
    static {
//        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void test() throws Exception {
        try {
            // 1.初始化密钥
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(112);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
            ECPrivateKey ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
            System.out.println("私钥：" + Base58.encode(ecPrivateKey.getEncoded()));
            // 2.执行签名[私钥签名]
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(ecPrivateKey.getEncoded());

            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            Signature signature = Signature.getInstance("SHA1withECDSA");
            signature.initSign(privateKey);
            signature.update(ECCTest.src.getBytes());
            byte[] res = signature.sign();
            System.out.println("签名：" + Base58.encode(res));

            // 3.验证签名[公钥验签]
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(ecPublicKey.getEncoded());
            keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            signature = Signature.getInstance("SHA1withECDSA");
            signature.initVerify(publicKey);
            signature.update(ECCTest.src.getBytes());
            boolean bool = signature.verify(res);
            System.out.println("验证：" + bool);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String src = "ecdsa security";

}
