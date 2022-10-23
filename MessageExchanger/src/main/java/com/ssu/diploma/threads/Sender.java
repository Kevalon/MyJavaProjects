package com.ssu.diploma.threads;

import com.ssu.diploma.dto.EncryptionParametersDto;
import com.ssu.diploma.encryption.Encryptor;
import com.ssu.diploma.encryption.EncryptorImpl;
import com.ssu.diploma.encryption.RSA;
import com.ssu.diploma.swing.utils.SwingCommons;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Map;
import javax.swing.JTextArea;

public class Sender implements Runnable {
    private static final int RESOURCE_BUFFER_SIZE = 8 * 1024;

    private final Map<String, String> settings;
    private final Encryptor encryptor;
    private final JTextArea logConsole;
    private Socket clientSocket;
    private DataOutputStream dataOut;
    private BufferedReader inReader;
    private final RSA rsaInstance = new RSA();
    private final int mode; // 0, 1
    private final boolean encrypt;

    public Sender(Map<String, String> settings, JTextArea logConsole, int mode, boolean encrypt) {
        this.settings = settings;
        encryptor = new EncryptorImpl(settings.get("cipherSystem"));
        this.logConsole = logConsole;
        this.mode = mode;
        this.encrypt = encrypt;
    }

    private void init() throws IOException {
        try {
            clientSocket = new Socket(
                    settings.get("receiverAddress"),
                    Integer.parseInt(settings.get("receiverPort"))
            );
            logConsole.append("Соединение с получателем установлено.\n");
        } catch (IOException e) {
            logConsole.append("Не удалось подключиться к получателю. Проверь адрес и порт.\n");
            throw e;
        }

        try {
            dataOut =
                    new DataOutputStream(
                            new BufferedOutputStream(clientSocket.getOutputStream()));
            inReader =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            logConsole.append("Не удалось установить поточное соединение с получателем.\n");
            throw e;
        }
    }

    private void close() throws IOException {
        if (dataOut != null) {
            dataOut.close();
            dataOut = null;
        }
        if (inReader != null) {
            inReader.close();
            inReader = null;
        }
        if (clientSocket != null) {
            clientSocket.close();
            clientSocket = null;
        }
    }

    private EncryptionParametersDto getDto() throws IOException {
        byte[] key;
        byte[] IV;
        try {
            key = SwingCommons.getBytesFromURL(new URL(settings.get("keyPath")));
        } catch (MalformedURLException exception) {
            try {
                key = Files.readAllBytes(Path.of(settings.get("keyPath")));
            } catch (IOException e) {
                logConsole.append("Ошибка чтения файла ключа.\n");
                throw e;
            }
        }
        try {
            IV = SwingCommons.getBytesFromURL(new URL(settings.get("IVPath")));
        } catch (MalformedURLException exception) {
            try {
                IV = Files.readAllBytes(Path.of(settings.get("IVPath")));
            } catch (IOException e) {
                logConsole.append("Ошибка чтения файла начального вектора.\n");
                throw e;
            }
        }
        return EncryptionParametersDto.builder()
                .IV(IV)
                .key(key)
                .cipherSystem(settings.get("cipherSystem"))
                .build();
    }

    private void send(Object data) throws IOException {
        if (data instanceof byte[]) {
            byte[] newData = (byte[]) data;
            dataOut.writeInt(newData.length);
            dataOut.flush();
            dataOut.write(newData);
            dataOut.flush();
        } else if (data instanceof Integer) {
            dataOut.writeInt((int) data);
            dataOut.flush();
        } else if (data instanceof Character) {
            dataOut.writeInt((char) data);
            dataOut.flush();
        }
    }

//    public static void send() {
//
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
//    }

    private void loadTesting() {
        // 1 нагрузочное - начни с него
        // Берем папку с файлами и отправляем эти файлы один за другим. Выводим время после ответа о том,
        // что оно было получено. Так же выводим размер отправленного файла и размер полученного файла, чтобы
        // показать потери.
    }

    private void infiniteTexting() {
        // 2 беск отправка
    }

    @Override
    public void run() {
        try {
            init();
        } catch (IOException e) {
            return;
        }
        EncryptionParametersDto parametersDto;
        try {
            parametersDto = getDto();
        } catch (IOException e) {
            return;
        }

        try {
            send("ENC_PAR".getBytes(StandardCharsets.UTF_8));
            System.out.println("ENC_PAR: " +
                    Arrays.toString("ENC_PAR".getBytes(StandardCharsets.UTF_8)));
            send(mode);
            if (encrypt) {
                send(1);
            } else send(0);

            System.out.println(Arrays.toString(parametersDto.getKey()));
            System.out.println(Arrays.toString(parametersDto.getIV()));
            System.out.println((int) parametersDto.getCipherSystem().charAt(0));
            if (encrypt) {
                send(rsaInstance.encrypt(parametersDto.getKey()));
                send(rsaInstance.encrypt(parametersDto.getIV()));
                send(parametersDto.getCipherSystem().charAt(0));
            }
        } catch (IOException exception) {
            logConsole.append("Не получилось отправить параметры шифрования получателю.\n");
            return;
        } catch (GeneralSecurityException e) {
            logConsole.append("Не удалось зашифровать данные с помощью RSA для отправки.\n");
        }
        logConsole.append("");

        while (!Thread.currentThread().isInterrupted()) {
            // 2 метода работы
            if (mode == 0) loadTesting();
            if (mode == 1) infiniteTexting();
        }
        try {
            close();
        } catch (IOException e) {
            logConsole.append("Ошибка закрытия соединения. Возможна потеря данных.\n");
        }
    }
}
