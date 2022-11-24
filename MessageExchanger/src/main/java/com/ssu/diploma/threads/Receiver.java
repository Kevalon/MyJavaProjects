package com.ssu.diploma.threads;

import static com.ssu.diploma.swing.utils.Utils.RESOURCE_BUFFER_SIZE;

import com.ssu.diploma.dto.EncryptionParametersDto;
import com.ssu.diploma.encryption.Encryptor;
import com.ssu.diploma.encryption.EncryptorImpl;
import com.ssu.diploma.encryption.RSA;
import com.ssu.diploma.swing.utils.Utils;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Map;
import javax.crypto.Cipher;
import javax.swing.JTextArea;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

public class Receiver extends Thread {

    private final Map<String, String> settings;
    private boolean encrypt;
    private Encryptor encryptor;
    private final JTextArea logConsole;
    private final RSA rsaInstance = new RSA();
    private int mode; // 0, 1
    public ServerSocket ss;
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private EncryptionParametersDto encParameters;

    @Setter
    private boolean stop = false;

    public Receiver(Map<String, String> settings, JTextArea logConsole) {
        this.settings = settings;
        this.logConsole = logConsole;
    }

    private void init() throws IOException {
        try {
            ss = new ServerSocket(Integer.parseInt(settings.get("serverPort")));
            Utils.log(logConsole, "Ожидается отправитель.");
        } catch (BindException exception) {
            Utils.log(logConsole, "Невозможно повторно запустить сервер. " +
                    "Пожалуйста, перезапустите программу.");
            throw exception;
        }
        catch (IOException e) {
            Utils.log(logConsole, String.format("Не удалось запустить сервер на порте %s.",
                    settings.get("serverPort")));
            throw e;
        }
        ss.setSoTimeout(1000);
        while (true) {
            try {
                clientSocket = ss.accept();
                break;
            } catch (SocketTimeoutException e) {
                if (stop) {
                    throw e;
                }
            }
        }

        Utils.log(
                logConsole,
                "Отправитель " + clientSocket.getInetAddress() + " успешно подключился.");

        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
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

    private EncryptionParametersDto setUpEncParameters()
            throws IOException, GeneralSecurityException {
        try {
            byte[] key = Utils.receiveByteArray(in);
            byte[] IV = Utils.receiveByteArray(in);
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

    private void receiveOneFile(Cipher cipher, boolean infinite) throws IOException {
        String filename = new String(Utils.receiveByteArray(in), StandardCharsets.UTF_8);
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

        Utils.sendData(1, out);
        if (!infinite) {
            Utils.log(logConsole, String.format("Получен файл %s", filename));
        }
        Utils.sendData(
                DigestUtils.sha256Hex(
                        Files.newInputStream(
                                Paths.get(
                                        settings.get("receivedFilesDirectory")
                                                + "/"
                                                + filename)))
                        .getBytes(StandardCharsets.UTF_8),
                out
        );
    }

    private void loadTesting(boolean infinite) throws IOException {
        if (!settings.containsKey("receivedFilesDirectory")) {
            Utils.log(logConsole, "Не найдена директория для получаемых файлов. " +
                    "Пожалуйста, укажите ее в настройках.");
            return;
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
                    if (stop) {
                        break;
                    }
                    receiveOneFile(cipher, infinite);
                }
            } catch (SocketException exception) {
                break;
            } catch (IOException e) {
                Utils.log(logConsole, "Не удалось прочитать данные от отправителя.");
            }
        } while (infinite);

        if (encrypt) {
            Files.walk(Paths.get("./encryptedReceived/"))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            Files.deleteIfExists(Paths.get("./encryptedReceived/"));
        }
    }

    private void infiniteTexting() throws IOException {
        Utils.log(logConsole, "Установлен режим бесконечного обмена сообщениями.");
        loadTesting(true);
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                init();
            } catch (SocketTimeoutException exception) {
                Utils.log(logConsole, "Получатель успешно остановлен.");
                return;
            }
            catch (IOException e) {
                return;
            }

            try {
                byte[] encData = Utils.receiveByteArray(in);
                if (!(new String(encData).equals("ENC_PAR"))) {
                    throw new IOException();
                }
                mode = in.readInt();
                encrypt = in.readInt() == 1;
                if (encrypt) {
                    Utils.sendData(Utils.getBytesFromURL(RSA.PUBLIC_KEY_PATH), out);
                    Files.createDirectories(Paths.get(".", "encryptedReceived"));
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
}
