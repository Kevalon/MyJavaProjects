package com.ssu.diploma.ecnryption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class RSA {

    private static final Path PUBLIC_KEY_PATH = Path.of("./src/main/resources/publicKey.txt");
    private static final Path PRIVATE_KEY_PATH =
            Path.of("./src/main/resources/privateKey.txt");

    static {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
    }

    public void generateKeyPair() throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        KeyPair pair = keyGen.generateKeyPair();
        Files.write(PRIVATE_KEY_PATH, pair.getPrivate().getEncoded());
        Files.write(PUBLIC_KEY_PATH, pair.getPublic().getEncoded());
    }

    public byte[] encrypt(byte[] data)
            throws NoSuchPaddingException, NoSuchAlgorithmException, IOException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException,
            InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE,
                KeyFactory.getInstance("RSA")
                        .generatePublic(
                                new X509EncodedKeySpec(Files.readAllBytes(PUBLIC_KEY_PATH))));
        return cipher.doFinal(data);
    }

    public byte[] decrypt(byte[] data)
            throws NoSuchPaddingException, NoSuchAlgorithmException, IOException,
            InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE,
                KeyFactory.getInstance("RSA")
                        .generatePrivate(
                                new X509EncodedKeySpec(Files.readAllBytes(PRIVATE_KEY_PATH))));
        return cipher.doFinal(data);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        new RSA().generateKeyPair();
    }
}
