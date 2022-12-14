package com.ssu.diploma.threads;

import static com.ssu.diploma.swing.utils.Utils.RESOURCE_BUFFER_SIZE;
import static com.ssu.diploma.swing.utils.Utils.receiveByteArray;

import com.ssu.diploma.dto.EncryptionParametersDto;
import com.ssu.diploma.encryption.Encryptor;
import com.ssu.diploma.encryption.EncryptorImpl;
import com.ssu.diploma.encryption.RSA;
import com.ssu.diploma.swing.utils.Utils;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.swing.JTextArea;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

public class Sender extends Thread {
    private final Map<String, String> settings;
    private final Encryptor encryptor;
    private final JTextArea logConsole;
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private final RSA rsaInstance = new RSA();
    private final int testingMode; // 0, 1, 2
    private final int encryptionMode; // 0, 1, 2, 3
    private final boolean encrypt;
    private Path[] pathsToEncrypt;

    @Setter
    @Getter
    private boolean stop = false;

    public Sender(
            Map<String, String> settings,
            JTextArea logConsole,
            int testingMode,
            int encryptionMode) {
        this.settings = settings;
        encryptor = new EncryptorImpl(settings.get("cipherSystem"));
        this.logConsole = logConsole;
        this.testingMode = testingMode;
        this.encryptionMode = encryptionMode;
        encrypt = encryptionMode != 2;
    }

    public Sender(
            Map<String, String> settings,
            JTextArea logConsole,
            int testingMode,
            int encryptionMode,
            Path[] pathsToEncrypt
    ) {
        this.settings = settings;
        encryptor = new EncryptorImpl(settings.get("cipherSystem"));
        this.logConsole = logConsole;
        this.testingMode = testingMode;
        this.encryptionMode = encryptionMode;
        encrypt = encryptionMode != 2;
        this.pathsToEncrypt = pathsToEncrypt;
    }

    private void init() throws IOException {
        try {
            clientSocket = new Socket(
                    settings.get("receiverAddress"),
                    Integer.parseInt(settings.get("receiverPort"))
            );
            Utils.log(logConsole, "Соединение с получателем установлено.");
        } catch (IOException e) {
            Utils.log(
                    logConsole,
                    "Не удалось подключиться к получателю. Проверьте адрес и порт.");
            throw e;
        }

        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
        } catch (IOException e) {
            Utils.log(logConsole, "Не удалось установить поточное соединение с получателем.");
            throw e;
        }

        if (encrypt) {
            Files.createDirectories(Paths.get(".", "encryptedSent"));
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
            key = Utils.getBytesFromURL(new URL(settings.get("keyPath")));
        } catch (MalformedURLException exception) {
            try {
                key = Files.readAllBytes(Paths.get(settings.get("keyPath")));
            } catch (IOException e) {
                Utils.log(logConsole, "Ошибка чтения файла ключа.");
                throw e;
            }
        }
        try {
            IV = Utils.getBytesFromURL(new URL(settings.get("IVPath")));
        } catch (MalformedURLException exception) {
            try {
                IV = Files.readAllBytes(Paths.get(settings.get("IVPath")));
            } catch (IOException e) {
                Utils.log(logConsole, "Ошибка чтения файла начального вектора.");
                throw e;
            }
        }
        return EncryptionParametersDto.builder()
                .IV(IV)
                .key(key)
                .cipherSystem(settings.get("cipherSystem"))
                .build();
    }

    private void encryptAndSend(Path filePath, Cipher cipher, boolean infinite) {
        if (stop) {
            return;
        }
        Instant start, end;
        String checkSumBefore;
        Path fileToSendPath = filePath;

        try (InputStream is = Files.newInputStream(filePath)) {
            checkSumBefore = DigestUtils.sha256Hex(is);
        } catch (IOException e) {
            Utils.log(
                    logConsole,
                    String.format(
                            "Не удалось открыть поток на чтение для %s",
                            filePath.getFileName()
                    )
            );
            return;
        }

        if (encrypt) {
            start = Instant.now();
            fileToSendPath =
                    Paths.get("./encryptedSent/" + filePath.getFileName().toString() + ".enc");
            try {
                if (!infinite) {
                    Utils.log(
                            logConsole,
                            String.format("Шифрование файла %s", filePath.getFileName()));
                }
                int cnt = encryptionMode == 3 ? 1 : 0;
                for (int i = 0; i <= cnt; i++) {
                    encryptor.encrypt(
                            filePath.toString(),
                            fileToSendPath.toString(),
                            cipher);
                }
            } catch (Exception e) {
                Utils.log(
                        logConsole,
                        String.format("Не удалось зашифровать файл %s", filePath.getFileName()));
                return;
            }
        } else {
            start = Instant.now();
        }

        File file = fileToSendPath.toFile();
        String filename = filePath.getFileName().toString();
        Utils.log(logConsole, String.format("Отправление файла %s", filename));
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
                in.readInt();
                end = Instant.now();
                Utils.log(logConsole, "Файл доставлен до получателя. Время: " +
                        Duration.between(start, end).toMillis() + " мс.");
                if (encryptionMode != 2) {
                    if (checkSumBefore.equals(new String(receiveByteArray(in),
                            StandardCharsets.UTF_8))) {
                        Utils.log(logConsole, "Хеш-сумма файлов совпала. Потерь нет.");
                    } else {
                        Utils.log(
                                logConsole,
                                "Хеш-сумма файлов не совпала. Были потери при отправке.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadTesting(boolean infinite) {
        try {
            List<Path> filesToSend;
            if (testingMode == 2) {
                filesToSend = Arrays.asList(pathsToEncrypt);
            } else {
                filesToSend = Files.walk(Paths.get(settings.get("testFilesDirectory")))
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());
            }

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
                if (stop) {
                    break;
                }
                Utils.sendData(filesToSend.size(), out);
                filesToSend.forEach(p -> encryptAndSend(p, cipher, infinite));
            } while (infinite);

            if (encrypt) {
                Files.walk(Paths.get("./encryptedSent/"))
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                Files.deleteIfExists(Paths.get("./encryptedSent/"));
            }
        } catch (IOException e) {
            Utils.log(
                    logConsole,
                    "Не удалось прочитать директорию с файлами для отправки.");
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.log(logConsole, "Произошла внутренняя ошибка отправки файла.");
        }
    }

    private void infiniteTexting() {
        Utils.log(logConsole, "Бесконечная отправка началась. Для отмены нажмите 'Стоп'.");
        loadTesting(true);
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
            Utils.sendData("ENC_PAR".getBytes(StandardCharsets.UTF_8), out);
            Utils.sendData(testingMode, out);
            Utils.sendData(encryptionMode, out);
            if (encryptionMode == 1 || encryptionMode == 3) {
                Utils.sendData(Integer.parseInt(settings.get("nodesAmount")), out);
            }

            if (encrypt) {
                Utils.log(logConsole, "Отправлен запрос на публичный ключ RSA.");
                byte[] rsaKey = Utils.receiveByteArray(in);
                Utils.log(logConsole, "Публичный ключ RSA успешно получен.");
                Utils.sendData(rsaInstance.encrypt(parametersDto.getKey(), rsaKey), out);
                Utils.sendData(rsaInstance.encrypt(parametersDto.getIV(), rsaKey), out);
                Utils.sendData(parametersDto.getCipherSystem().charAt(0), out);
            }
        } catch (IOException exception) {
            Utils.log(logConsole,
                    "Не получилось отправить параметры шифрования получателю.");
            exception.printStackTrace();
            return;
        } catch (GeneralSecurityException e) {
            Utils.log(logConsole,
                    "Не удалось зашифровать данные с помощью RSA для отправки.");
        }

        if (testingMode == 0 || testingMode == 2) {
            loadTesting(false);
        }
        if (testingMode == 1) {
            infiniteTexting();
        }

        try {
            close();
            Utils.log(logConsole,
                    "Отправитель закончил свою работу и отключился от получателя.");
        } catch (IOException e) {
            Utils.log(logConsole, "Ошибка закрытия соединения. Возможна потеря данных.");
        } finally {
            stop = true;
        }
    }
}
