package com.ssu.diploma.threads;

import com.ssu.diploma.ecnryption.Encryptor;
import com.ssu.diploma.ecnryption.EncryptorImpl;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.crypto.Cipher;

public class Receiver implements Runnable{
    public static final int PORT = 8081;
    public static final Path KEY_PATH = Path.of("./key.txt");
    public static final Path FILE_PATH = Path.of("./fileEncReally.txt");

    public static void receive() throws Exception {
        ServerSocket ss = new ServerSocket(PORT);
        System.out.println("Server Started");
        Encryptor encryptor = new EncryptorImpl("AES");

        Socket socket = ss.accept();
        if (socket != null) {
            System.out.println("Got a connection");
        } else {
            System.out.println("Нет подключения");
            return;
        }
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(FILE_PATH.toString());

        long size = dataInputStream.readLong();
        byte[] buffer = new byte[8 * 1024];
        byte[] key = Files.readAllBytes(KEY_PATH);
        Cipher cipher = encryptor.init(key, false);

        int bytes;
        while (size > 0 &&
                (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) !=
                        -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
        fileOutputStream.close();

        //encryptor.encrypt(FILE_PATH.toFile(), cipher);

        out.println("received");
        dataInputStream.close();
        socket.close();
        ss.close();
    }

    @Override
    public void run() {

    }
}
