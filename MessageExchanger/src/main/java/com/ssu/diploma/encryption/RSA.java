package com.ssu.diploma.encryption;

import com.ssu.diploma.swing.utils.Utils;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class RSA {

    public static final URL PUBLIC_KEY_PATH = ClassLoader.getSystemResource("publicKey.txt");
    public static final URL PRIVATE_KEY_PATH = ClassLoader.getSystemResource("privateKey.txt");

    static {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
    }

    public void generateKeyPair()
            throws NoSuchAlgorithmException, IOException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(512);
        KeyPair pair = keyGen.generateKeyPair();
//        Files.write(PRIVATE_KEY_PATH, pair.getPrivate().getEncoded());
//        Files.write(PUBLIC_KEY_PATH, pair.getPublic().getEncoded());
    }

    public byte[] encrypt(byte[] data)
            throws NoSuchPaddingException, NoSuchAlgorithmException, IOException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
            InvalidKeyException, NoSuchProviderException, ShortBufferException {
        Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE,
                KeyFactory.getInstance("RSA", "BC")
                        .generatePublic(
                                new X509EncodedKeySpec(Utils.getBytesFromURL(PUBLIC_KEY_PATH))));
        if (data.length > 64) {
            byte[] res = new byte[data.length];
            byte[] temp;
            for (int i = 0; i < res.length; i += 64) {
                temp = new byte[64];
                cipher.doFinal(data, i, 64, temp);
                System.arraycopy(temp, 0, res, i, temp.length);
            }
            return res;
        } else return cipher.doFinal(data);
    }

    public byte[] decrypt(byte[] data)
            throws NoSuchPaddingException, NoSuchAlgorithmException, IOException,
            InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, NoSuchProviderException, ShortBufferException {
        Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE,
                KeyFactory.getInstance("RSA", "BC")
                        .generatePrivate(
                                new PKCS8EncodedKeySpec(Utils.getBytesFromURL(PRIVATE_KEY_PATH))));
        if (data.length > 64) {
            byte[] res = new byte[data.length];
            byte[] temp;
            for (int i = 0; i < res.length; i += 64) {
                temp = new byte[64];
                cipher.doFinal(data, i, 64, temp);
                System.arraycopy(temp, 0, res, i, temp.length);
            }
            return res;
        } else return cipher.doFinal(data);
    }
}
