package com.ssu.diploma.threads;

import com.ssu.diploma.ecnryption.Encryptor;
import com.ssu.diploma.ecnryption.EncryptorImpl;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import javax.swing.JTextArea;

public class Sender implements Runnable{
    private static final int RESOURCE_BUFFER_SIZE = 8 * 1024;

    private final Map<String, String> settings;
    private final Encryptor encryptor;
    private final JTextArea logConsole;
    private Socket clientSocket;
    private DataOutputStream dataOut;
    private BufferedReader inReader;

    public Sender(Map<String, String> settings, JTextArea logConsole) {
        this.settings = settings;
        encryptor = new EncryptorImpl(settings.get("cipherSystem"));
        this.logConsole = logConsole;
    }

//    public static void send() {
//        DataOutputStream dataOut =
//                new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//        BufferedReader inReader =
//                new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//        long startTime = System.currentTimeMillis();
//        int count;
//        byte[] key = Sender.class.getClassLoader().getResourceAsStream(KEY_FILENAME).readAllBytes();
//        Cipher cipher = encryptor.init(key, true);
//        encryptor.encrypt(PUBLIC_FILE_FILENAME, ENCRYPTED_FILENAME, cipher);
//
//        File file = new File(ENCRYPTED_FILENAME);
//        InputStream fileInputStream = new FileInputStream(file);
//        dataOut.writeLong(file.length());
//        byte[] buffer = new byte[8 * 1024];
//        while ((count = fileInputStream.read(buffer)) != -1) {
//            dataOut.write(buffer, 0, count);
//            dataOut.flush();
//        }
//        fileInputStream.close();
//        System.out.println(inReader.readLine());
//        long endTime = System.currentTimeMillis();
//        System.out.println("time taken " + (endTime - startTime) + " ms");
//    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                clientSocket = new Socket(
                        settings.get("receiverAddress"),
                        Integer.parseInt(settings.get("receiverPort"))
                );
            } catch (IOException e) {
                logConsole.append("Не удалось подключиться к получателю. Проверь адрес и порт.\n");
                break;
            }

            try {
                dataOut =
                        new DataOutputStream(
                                new BufferedOutputStream(clientSocket.getOutputStream()));
                inReader =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                logConsole.append("Не удалось установить поточное соединение с получателем.\n");
                break;
            }
        }
    }
}
