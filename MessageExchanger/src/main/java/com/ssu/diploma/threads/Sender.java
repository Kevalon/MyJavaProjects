package com.ssu.diploma.threads;

import static com.ssu.diploma.swing.utils.SwingCommons.RESOURCE_BUFFER_SIZE;

import com.ssu.diploma.dto.EncryptionParametersDto;
import com.ssu.diploma.encryption.Encryptor;
import com.ssu.diploma.encryption.EncryptorImpl;
import com.ssu.diploma.encryption.RSA;
import com.ssu.diploma.swing.utils.SwingCommons;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.swing.JTextArea;
import org.apache.commons.codec.digest.DigestUtils;

public class Sender implements Runnable {
    private final Map<String, String> settings;
    private final Encryptor encryptor;
    private final JTextArea logConsole;
    private Socket clientSocket;
    private DataOutputStream out;
    private BufferedReader in;
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
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
        } catch (IOException e) {
            logConsole.append("Не удалось установить поточное соединение с получателем.\n");
            throw e;
        }

        if (encrypt) {
            Files.createDirectories(Path.of(".", "encryptedSent"));
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

    private void sendData(Object data) throws IOException {
        if (data instanceof byte[]) {
            byte[] newData = (byte[]) data;
            out.writeInt(newData.length);
            out.flush();
            out.write(newData);
            out.flush();
        } else if (data instanceof Integer) {
            out.writeInt((int) data);
            out.flush();
        } else if (data instanceof Character) {
            out.writeInt((char) data);
            out.flush();
        }
    }

    private void encryptAndSend(Path filePath, Cipher cipher, boolean infinite) {
        Instant start, end;
        String checkSumBefore = "";
        Path fileToSendPath = filePath;
        if (encrypt) {
            try (InputStream is = Files.newInputStream(filePath)) {
                checkSumBefore = DigestUtils.md5Hex(is);
            } catch (IOException e) {
                logConsole.append(
                        "Не удалось открыть поток на чтение для " + filePath.getFileName() + "\n");
                return;
            }
            start = Instant.now();
            fileToSendPath =
                    Path.of("./encryptedSent/" + filePath.getFileName().toString() + ".enc");
            try {
                encryptor.encrypt(
                        filePath.toString(),
                        fileToSendPath.toString(),
                        cipher);
            } catch (Exception e) {
                logConsole.append("Не удалось зашифровать файл " + filePath.getFileName() + "\n");
                return;
            }
        } else {
            start = Instant.now();
        }

        File file = fileToSendPath.toFile();
        String filename = filePath.getFileName().toString();
        if (!infinite) {
            logConsole.append("Отправляю файл " + filename + "\n");
        }
        try (InputStream fileInputStream = new FileInputStream(file)) {
            out.writeInt(filename.getBytes(StandardCharsets.UTF_8).length);
            out.flush();
            out.write(filename.getBytes(StandardCharsets.UTF_8));
            out.flush();

            int count;
            out.writeLong(file.length());
            out.flush();
            byte[] buffer = new byte[RESOURCE_BUFFER_SIZE];
            while ((count = fileInputStream.read(buffer)) > 0) {
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        if (!infinite) {
            try {
                in.readLine();
                end = Instant.now();
                logConsole.append("Файл доставлен до получателя. Время: " +
                        Duration.between(start, end).toMillis() + " мс.\n");
                if (encrypt) {
                    if (checkSumBefore.equals(in.readLine())) {
                        logConsole.append("Хэш-сумма файлов совпала. Потерь нет.\n");
                    } else {
                        logConsole.append(
                                "Хэш-сумма файлов не совпала. Были потери при отправке.\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadTesting(boolean infinite) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<Path> filesToSend =
                        Files.walk(Path.of(settings.get("testFilesDirectory")))
                                .filter(Files::isRegularFile)
                                .collect(Collectors.toList());
                Cipher cipher;
                if (encrypt) {
                    cipher = encryptor.init(
                            settings.get("keyPath"),
                            settings.get("IVPath"),
                            true
                    );
                } else {
                    cipher = null;
                }
                do {
                    sendData(filesToSend.size());
                    filesToSend.forEach(p -> encryptAndSend(p, cipher, infinite));
                } while (infinite);
                break;
            } catch (IOException e) {
                logConsole.append("Не удалось прочитать директорию с файлами для отправки.\n");
                break;
            } catch (Exception ex) {
                logConsole.append("Произошла внутренняя ошибка отправки файла.\n");
                break;
            }
        }
    }

    private void infiniteTexting() {
        while (!Thread.currentThread().isInterrupted()) {
            logConsole.append("Бесконечная отправка началась. Для отмены нажмите 'Стоп'.\n");
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
        EncryptionParametersDto parametersDto;
        try {
            parametersDto = getDto();
        } catch (IOException e) {
            return;
        }

        try {
            sendData("ENC_PAR".getBytes(StandardCharsets.UTF_8));
            sendData(mode);
            if (encrypt) {
                sendData(1);
            } else {
                sendData(0);
            }
            if (encrypt) {
                sendData(rsaInstance.encrypt(parametersDto.getKey()));
                sendData(rsaInstance.encrypt(parametersDto.getIV()));
                sendData(parametersDto.getCipherSystem().charAt(0));
            }
        } catch (IOException exception) {
            logConsole.append("Не получилось отправить параметры шифрования получателю.\n");
            return;
        } catch (GeneralSecurityException e) {
            logConsole.append("Не удалось зашифровать данные с помощью RSA для отправки.\n");
        }

        if (mode == 0) {
            loadTesting(false);
        }
        if (mode == 1) {
            infiniteTexting();
        }

        try {
            close();
            logConsole.append("Отправитель закончил свою работу и отключился от получателя.\n");
        } catch (IOException e) {
            logConsole.append("Ошибка закрытия соединения. Возможна потеря данных.\n");
        }
    }
}
