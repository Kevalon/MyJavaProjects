package com.ssu.diploma;

import java.io.File;
import javax.crypto.Cipher;

public interface Encryptor {
    void generateKey() throws Exception;

    Cipher init(byte[] key, boolean encrypt) throws Exception;

    void encrypt(String source, String destination, Cipher cipher) throws Exception;
}
