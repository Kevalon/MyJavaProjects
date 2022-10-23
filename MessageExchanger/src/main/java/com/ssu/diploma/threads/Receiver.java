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
            logConsole.append("Получатель успешно запущен. Ожидаю отправителя.\n");
        } catch (IOException e) {
            logConsole.append(String.format("Не удалось запустить сервер на порте %s.\n",
                    settings.get("serverPort")));
            throw e;
        }
        clientSocket = ss.accept();
        logConsole.append("Клиент " + clientSocket.getInetAddress() + " успешно подключился.\n");

        try {
            out = new DataOutputStream(
                    new BufferedOutputStream(clientSocket.getOutputStream()));
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
        try {

            byte[] key = receiveByteArray();
            byte[] IV = receiveByteArray();
            char cipherFirstLetter = (char) in.readInt();
            System.out.println(cipherFirstLetter);
            key = rsaInstance.decrypt(key);
            IV = rsaInstance.decrypt(IV);
            return new EncryptionParametersDto(key, IV,
                    cipherFirstLetter == 'A' ? "AES" : "GOST3412-2015");
        } catch (IOException exception) {
            logConsole.append("Не получилось прочитать параметры шифрования.\n");
            throw exception;
        } catch (GeneralSecurityException exception) {
            logConsole.append("Не удалось зашифровать данные с помощью RSA для отправки.");
            throw exception;
        }
    }

    private byte[] receiveByteArray() throws IOException {
        int length = in.readInt();
        if (length > 0) {
            byte[] message = new byte[length];
            in.readFully(message, 0, message.length);
            return message;
        }
        throw new IOException();
    }

    public static void smack() {
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

        EncryptionParametersDto encParameters = null;
        try {
            byte[] encData = receiveByteArray();
            if (!(new String(encData).equals("ENC_PAR"))) {
                throw new IOException();
            }
            mode = in.readInt();
            System.out.println(mode);
            encrypt = in.readInt() == 1;
            System.out.println(encrypt);
            if (encrypt) {
                encParameters = setUpEncParameters();
            }
            logConsole.append("Параметры работы и шифрования успешно получены.\n");
        } catch (IOException e) {
            logConsole.append("Не удалось прочитать входные данные.\n");
            return;
        } catch (GeneralSecurityException e) {
            logConsole.append("Ошибка расшифрования параметров сквозного шифрования.\n");
            return;
        }
        System.out.println(encParameters);


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
