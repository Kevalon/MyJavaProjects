package com.ssu.diploma.encryption;

import javax.crypto.Cipher;

public interface Encryptor {
    byte[] generateKey() throws Exception;

    byte[] generateIV() throws Exception;

    Cipher init(String keyPath, String IVPath, boolean encrypt) throws Exception;

    Cipher init(byte[] key, byte[] IV, boolean encrypt) throws Exception;

    void encrypt(String source, String destination, Cipher cipher) throws Exception;
}
