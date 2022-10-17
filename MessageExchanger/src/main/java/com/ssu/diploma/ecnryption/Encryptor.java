package com.ssu.diploma.ecnryption;

import javax.crypto.Cipher;

public interface Encryptor {
    byte[] generateKey() throws Exception;

    byte[] generateIV() throws Exception;

    Cipher init(byte[] key, boolean encrypt) throws Exception;

    void encrypt(String source, String destination, Cipher cipher) throws Exception;
}
