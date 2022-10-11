package com.ssu.diploma;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.crypto.Cipher;

public class Sender {
    public static final int PORT = 8081;
    private static final String KEY_FILENAME = "key.txt";
    private static final String PUBLIC_FILE_FILENAME = "file.txt";
    private static final String ENCRYPTED_FILENAME = "encrypted.txt";
    private static final int RESOURCE_BUFFER_SIZE = 8 * 1024;

    public static void send(Scanner in) throws Exception {
        Encryptor encryptor = new EncryptorImpl("AES");

        System.out.println("input address");
        String ip = in.next();

        Socket socket = new Socket(ip, PORT);
        DataOutputStream dataOut =
                new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        BufferedReader inReader =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));

        long startTime = System.currentTimeMillis();
        int count;
        byte[] key = Sender.class.getClassLoader().getResourceAsStream(KEY_FILENAME).readAllBytes();
        Cipher cipher = encryptor.init(key, true);
        encryptor.encrypt(PUBLIC_FILE_FILENAME, ENCRYPTED_FILENAME, cipher);

        File file = new File(ENCRYPTED_FILENAME);
        InputStream fileInputStream = new FileInputStream(file);
        dataOut.writeLong(file.length());
        byte[] buffer = new byte[8 * 1024];
        while ((count = fileInputStream.read(buffer)) != -1) {
            dataOut.write(buffer, 0, count);
            dataOut.flush();
        }
        fileInputStream.close();
        System.out.println(inReader.readLine());
        long endTime = System.currentTimeMillis();
        System.out.println("time taken " + (endTime - startTime) + " ms");
    }
}
