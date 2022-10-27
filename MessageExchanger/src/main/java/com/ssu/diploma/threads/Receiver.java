package com.ssu.diploma.threads;

import static com.ssu.diploma.swing.utils.SwingCommons.RESOURCE_BUFFER_SIZE;

import com.ssu.diploma.dto.EncryptionParametersDto;
import com.ssu.diploma.encryption.Encryptor;
import com.ssu.diploma.encryption.EncryptorImpl;
import com.ssu.diploma.encryption.RSA;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Map;
import javax.crypto.Cipher;
import javax.swing.JTextArea;
import org.apache.commons.codec.digest.DigestUtils;

public class Receiver implements Runnable {

    private final Map<String, String> settings;
    private boolean encrypt;
    private Encryptor encryptor;
    private final JTextArea logConsole;
    private final RSA rsaInstance = new RSA();
    private int mode; // 0, 1
    private ServerSocket ss;
    private Socket clientSocket;
    private PrintWriter out;
    private DataInputStream in;
    private EncryptionParametersDto encParameters;

    public Receiver(Map<String, String> settings, JTextArea logConsole) {
        this.settings = settings;
        this.logConsole = logConsole;
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
        logConsole.append(
                "Отправитель " + clientSocket.getInetAddress() + " успешно подключился.\n");

        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new PrintWriter(clientSocket.getOutputStream(), true);
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

    private void receiveOneFile(Cipher cipher, boolean infinite) throws IOException {
        String filename = new String(receiveByteArray());
        System.out.println("filename received");
        String receivePath = encrypt ? "./encryptedReceived/" + filename + ".enc" :
                settings.get("receivedFilesDirectory") + "/" + filename;
        long size = in.readLong();
        byte[] buffer = new byte[RESOURCE_BUFFER_SIZE];
        int count;
        try (FileOutputStream fileOutputStream = new FileOutputStream(receivePath)) {
            while (size > 0 &&
                    (count = in.read(buffer, 0, (int) Math.min(buffer.length, size))) > 0) {
                fileOutputStream.write(buffer, 0, count);
                size -= count;
            }
        }

        if (encrypt) {
            try {
                encryptor.encrypt(
                        receivePath,
                        settings.get("receivedFilesDirectory") + "/" + filename,
                        cipher);
            } catch (Exception e) {
                logConsole.append("Ошибка расшифрования файла " + filename + "\n");
            }
        }
        System.out.println("decrypted");

        out.println("Received");
        if (!infinite) {
            logConsole.append(String.format("Получен файл %s\n", filename));
        }

        if (encrypt) {
            out.println(DigestUtils.sha256Hex(Files.newInputStream(
                    Path.of(settings.get("receivedFilesDirectory") + "/" + filename))));
        }
    }

    private void loadTesting(boolean infinite) {
        while (!Thread.currentThread().isInterrupted()) {
            if (!settings.containsKey("receivedFilesDirectory")) {
                logConsole.append("Не найдена директория для получаемых файлов. " +
                        "Пожалуйста, укажите ее в настройках.\n");
                break;
            }

            Cipher cipher;
            if (encrypt) {
                try {
                    cipher = encryptor.init(
                            encParameters.getKey(),
                            encParameters.getIV(),
                            false);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                cipher = null;
            }
            do {
                try {
                    int fileCount = in.readInt();
                    for (int i = 0; i < fileCount; i++) {
                        receiveOneFile(cipher, infinite);
                    }
                } catch (IOException e) {
                    logConsole.append("Не удалось прочитать данные от отправителя.\n");
                }
            } while (infinite);
            break;
        }
    }

    private void infiniteTexting() {
        while (!Thread.currentThread().isInterrupted()) {
            logConsole.append("Установлен режим бесконечного обмена сообщениями.\n");
            loadTesting(true);
        }
    }

    @Override
    public void run() {
        try {
            init();
        } catch (IOException e) {
            return;
        }
        try {
            byte[] encData = receiveByteArray();
            if (!(new String(encData).equals("ENC_PAR"))) {
                throw new IOException();
            }
            mode = in.readInt();
            encrypt = in.readInt() == 1;
            if (encrypt) {
                Files.createDirectories(Path.of(".", "encryptedReceived"));
                encParameters = setUpEncParameters();
                encryptor = new EncryptorImpl(encParameters.getCipherSystem());
            }
            logConsole.append("Параметры работы и шифрования успешно получены.\n");
        } catch (IOException e) {
            e.printStackTrace();
            logConsole.append("Не удалось прочитать входные данные.\n");
            return;
        } catch (GeneralSecurityException e) {
            logConsole.append("Ошибка расшифрования параметров сквозного шифрования.\n");
            return;
        }

        if (mode == 0) {
            loadTesting(false);
        }
        if (mode == 1) {
            infiniteTexting();
        }

        try {
            close();
            logConsole.append("Получатель успешно закончил свою работу и остановился.\n");
        } catch (IOException e) {
            logConsole.append("Ошибка закрытия соединения. Возможна потеря данных.\n");
        }
    }
}
