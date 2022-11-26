package com.ssu.diploma.encryption;

import static com.ssu.diploma.swing.utils.Utils.RESOURCE_BUFFER_SIZE;

import com.ssu.diploma.swing.utils.Utils;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public static final int KEY_LENGTH = 256;
    //    "AES" or "GOST3412-2015"
    public final String systemName;
    public static final byte pIVLen = 16;

    static {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
    }

    public EncryptorImpl(String system) {
        systemName = system.charAt(0) == 'A' ? "AES/CFB/NoPadding" : "GOST3412-2015/CFB/NoPadding";
    }

    @Override
    public byte[] generateKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator keyGen =
                KeyGenerator.getInstance(systemName.substring(0, systemName.indexOf('/')), "BC");
        keyGen.init(KEY_LENGTH);
        SecretKey key = keyGen.generateKey();
        return key.getEncoded();
    }

    @Override
    public byte[] generateIV() {
        byte[] IV = new byte[pIVLen];
        CryptoServicesRegistrar.getSecureRandom().nextBytes(IV);
        return IV;
    }

    @Override
    public Cipher init(String keyPath, String IVPath, boolean encrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(systemName, "BC");
        byte[] IV, keyByte;
        try {
            keyByte = Utils.getBytesFromURL(new URL(keyPath));
        } catch (MalformedURLException exception) {
            keyByte = Files.readAllBytes(Paths.get(keyPath));
        }
        try {
            IV = Utils.getBytesFromURL(new URL(IVPath));
        } catch (MalformedURLException exception) {
            IV = Files.readAllBytes(Paths.get(IVPath));
        }
        SecretKey key = new SecretKeySpec(keyByte, systemName);

        if (encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
        }
        return cipher;
    }

    @Override
    public Cipher init(byte[] keyByte, byte[] IV, boolean encrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(systemName, "BC");
        SecretKey key = new SecretKeySpec(keyByte, systemName);
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
                FileOutputStream output = new FileOutputStream(destination)
        ) {
            long filesize = Files.size(Paths.get(source));
            byte[] buffer = new byte[RESOURCE_BUFFER_SIZE];
            int count;
            while (filesize > 0 &&
                    (count = input.read(
                            buffer,
                            0,
                            (int) Math.min(buffer.length, filesize)
                    )) > 0) {
                output.write(cipher.update(buffer, 0, count));
                filesize -= count;
            }
            output.write(cipher.doFinal());
        }
    }

    public static void main(String[] args) throws IOException {
        byte[] IV = new EncryptorImpl("AES").generateIV();
        Files.write(Paths.get("./src/main/resources/IV.txt"), IV);
    }
}
