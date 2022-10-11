package com.ssu.diploma;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

@Getter
public class EncryptorImpl implements Encryptor {

    private static final Path KEY_PATH = Path.of("./src/main/resources/key.txt");
    private static final Path IV_PATH = Path.of("./src/main/resources/IV.txt");
    private static final int KEY_LENGTH = 256;
    //    "AES" or "GOST3412-2015"
    private final String systemName;
    private final byte pIVLen = 8;
    private final int buffSize = 8 * 1024;

    static {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
    }

    public EncryptorImpl(String system) {
        systemName = system;
    }

    @Override
    public void generateKey() throws NoSuchAlgorithmException, IOException,
            NoSuchProviderException {
        KeyGenerator keyGen = KeyGenerator.getInstance(systemName, "BC");
        keyGen.init(KEY_LENGTH);
        SecretKey key = keyGen.generateKey();
        byte[] byteKey = key.getEncoded();
        Files.write(KEY_PATH, byteKey);

        byte[] IV = new byte[pIVLen];
        CryptoServicesRegistrar.getSecureRandom().nextBytes(IV);
        Files.write(IV_PATH, IV);
    }

    @Override
    public Cipher init(byte[] byteKey, boolean encrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(systemName, "BC");
        byte[] IV = Files.readAllBytes(IV_PATH);
        SecretKey key = new SecretKeySpec(byteKey, systemName);

        if (encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
        }
        return cipher;
    }

    @Override
    public void encrypt(String source, String destination, Cipher cipher)
            throws IllegalBlockSizeException, BadPaddingException, IOException {
        try (
                FileInputStream input = new FileInputStream(source);
                FileOutputStream output = new FileOutputStream(destination);
        ) {
            byte[] buffer = new byte[buffSize];
            int count = input.read(buffer);
            while (count >= 0) {
                output.write(cipher.update(buffer, 0, count));
                count = input.read(buffer);
            }
            output.write(cipher.doFinal());
            output.flush();
        }
    }
}
