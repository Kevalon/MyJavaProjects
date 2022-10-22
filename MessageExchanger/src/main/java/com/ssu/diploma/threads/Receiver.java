package com.ssu.diploma.threads;

import com.ssu.diploma.dto.EncryptionParametersDto;
import com.ssu.diploma.encryption.Encryptor;
import com.ssu.diploma.encryption.EncryptorImpl;
import com.ssu.diploma.encryption.RSA;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;
import javax.swing.JTextArea;

public class Receiver implements Runnable {
    private static final int RESOURCE_BUFFER_SIZE = 8 * 1024;

    private final Map<String, String> settings;
    private boolean encrypt;
    private final Encryptor encryptor;
    private final JTextArea logConsole;
    private final RSA rsaInstance = new RSA();
    private int mode; // 1, 2, 3
    private ServerSocket ss;
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    public Receiver(Map<String, String> settings, JTextArea logConsole) {
        this.settings = settings;
        this.logConsole = logConsole;
        encryptor = new EncryptorImpl(settings.get("cipherSystem"));
    }

    private void init() throws IOException {
        try {
            ss = new ServerSocket(Integer.parseInt(settings.get("serverPort")));
        } catch (IOException e) {
            logConsole.append(String.format("Не удалось запустить сервер на порте %s.\n",
                    settings.get("serverPort")));
            throw e;
        }
        clientSocket = ss.accept();

        try {
            out = new DataOutputStream(
                    new BufferedOutputStream(clientSocket.getOutputStream()));;
            in = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException exception) {
            logConsole.append("Не удалось открыть потоки на чтение и запись для отправителя.\n");
            throw exception;
        }
    }

    private void close() throws IOException {
        if (out != null) {
            out.close();
            out = null;
        }
        if (in != null) {
            in.close();
            in = null;
        }
        if (clientSocket != null) {
            clientSocket.close();
            clientSocket = null;
        }
        if (ss != null) {
            ss.close();
            ss = null;
        }
    }

    private EncryptionParametersDto setUpEncParameters() throws IOException,
            GeneralSecurityException {
        byte[] encData = new byte["ENC_PAR".getBytes(StandardCharsets.UTF_8).length];
        try {
            System.out.println("prepared to read");
            in.read(encData);
            System.out.println("all read");
        } catch (IOException e) {
            logConsole.append("Не удалось прочитать входные данные.\n");
            throw e;
        }
        try {
            if (!(new String(encData).equals("ENC_PAR"))) {
                throw new IOException();
            } else {
                byte[] key = new byte[32];
                byte[] IV = new byte[8];
                byte[] cipherSystem = new byte[RESOURCE_BUFFER_SIZE];
                byte[] mode = new byte[RESOURCE_BUFFER_SIZE];
                in.read(key);
                in.read(IV);
                in.read(cipherSystem);
                in.read(mode);
                key = rsaInstance.decrypt(key);
                IV = rsaInstance.decrypt(IV);
                return new EncryptionParametersDto(
                        rsaInstance.decrypt(key),
                        rsaInstance.decrypt(IV),
                        new String(rsaInstance.decrypt(cipherSystem)),
                        new BigInteger(rsaInstance.decrypt(mode)).intValue()
                );
            }
        } catch (IOException exception) {
            logConsole.append("Не получилось прочитать параметры шифрования.\n");
            throw exception;
        } catch (GeneralSecurityException exception) {
            logConsole.append("Не удалось зашифровать данные с помощью RSA для отправки.");
            throw exception;
        }
    }

    public static void receive() {
//
//        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
//        FileOutputStream fileOutputStream = new FileOutputStream(FILE_PATH.toString());
//
//        long size = dataInputStream.readLong();
//        byte[] buffer = new byte[8 * 1024];
//        byte[] key = Files.readAllBytes(KEY_PATH);
//        Cipher cipher = encryptor.init(key, false);
//
//        int bytes;
//        while (size > 0 &&
//                (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) !=
//                        -1) {
//            fileOutputStream.write(buffer, 0, bytes);
//            size -= bytes;
//        }
//        fileOutputStream.close();
//
//        //encryptor.encrypt(FILE_PATH.toFile(), cipher);
//
//        out.println("received");
    }

    @Override
    public void run() {
        try {
            init();
        } catch (IOException e) {
            return;
        }
        EncryptionParametersDto dto;
        try {
            dto = setUpEncParameters();
        } catch (Exception e) {
            return;
        }
        System.out.println("All is good");
        while (!Thread.currentThread().isInterrupted()) {


            // 3 метода работы
//            if (mode == 1) {
//                loadTesting();
//            }
//            if (mode == 2) {
//                stressTesting();
//            }
//            if (mode == 3) {
//                infiniteTexting();
//            }
        }
        try {
            close();
        } catch (IOException e) {
            logConsole.append("Ошибка закрытия соединения. Возможна потеря данных.\n");
        }
    }
}
