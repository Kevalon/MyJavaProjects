package com.ssu.diploma.threads;

import static com.ssu.diploma.swing.utils.Utils.RESOURCE_BUFFER_SIZE;

import com.ssu.diploma.dto.EncryptionParametersDto;
import com.ssu.diploma.encryption.Encryptor;
import com.ssu.diploma.encryption.EncryptorImpl;
import com.ssu.diploma.encryption.RSA;
import com.ssu.diploma.swing.utils.Utils;
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
            Utils.log(logConsole, "Получатель успешно запущен. Ожидаю отправителя.");
        } catch (IOException e) {
            Utils.log(logConsole, String.format("Не удалось запустить сервер на порте %s.",
                    settings.get("serverPort")));
            throw e;
        }
        clientSocket = ss.accept();
        Utils.log(
                logConsole,
                "Отправитель " + clientSocket.getInetAddress() + " успешно подключился.");

        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException exception) {
            Utils.log(
                    logConsole,
                    "Не удалось открыть потоки на чтение и запись для отправителя.");
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
            Utils.log(logConsole, "Не получилось прочитать параметры шифрования.");
            throw exception;
        } catch (GeneralSecurityException exception) {
            Utils.log(
                    logConsole,
                    "Не удалось зашифровать данные с помощью RSA для отправки.");
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
                Utils.log(logConsole, String.format("Ошибка расшифрования файла %s", filename));
            }
        }

        out.println("Received");
        if (!infinite) {
            Utils.log(logConsole, String.format("Получен файл %s", filename));
        }
        out.println(DigestUtils.sha256Hex(Files.newInputStream(
                Path.of(settings.get("receivedFilesDirectory") + "/" + filename))));
    }

    private void loadTesting(boolean infinite) throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            if (!settings.containsKey("receivedFilesDirectory")) {
                Utils.log(logConsole, "Не найдена директория для получаемых файлов. " +
                        "Пожалуйста, укажите ее в настройках.");
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
                    Utils.log(logConsole, "Не удалось прочитать данные от отправителя.");
                }
            } while (infinite);

            if (encrypt) {
                Files.walk(Path.of("./encryptedReceived/"))
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }

            break;
        }
    }

    private void infiniteTexting() throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            Utils.log(logConsole, "Установлен режим бесконечного обмена сообщениями.");
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
            Utils.log(logConsole, "Параметры работы и шифрования успешно получены.");
        } catch (IOException e) {
            e.printStackTrace();
            Utils.log(logConsole, "Не удалось прочитать входные данные.");
            return;
        } catch (GeneralSecurityException e) {
            Utils.log(logConsole, "Ошибка расшифрования параметров сквозного шифрования.");
            return;
        }

        try {
            if (mode == 0) {
                loadTesting(false);
            }
            if (mode == 1) {
                infiniteTexting();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }


        try {
            close();
            Utils.log(logConsole, "Получатель успешно закончил свою работу и остановился.");
        } catch (IOException e) {
            Utils.log(logConsole, "Ошибка закрытия соединения. Возможна потеря данных.");
        }
    }
}
