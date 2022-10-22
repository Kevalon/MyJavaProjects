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
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
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
    private final int mode; // 1, 2, 3
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
                IV = Files.readAllBytes(Path.of(settings.get("ШМPath")));
            } catch (IOException e) {
                logConsole.append("Ошибка чтения файла начального вектора.\n");
                throw e;
            }
        }
        return new EncryptionParametersDto(key, IV, settings.get("cipherSystem"), mode);
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

    private void stressTesting() {
        // 2 стресс тест - указывается папка с файлами, которые надо отправить, а затем
        // пул потоков начинает подключаться к рандомным ip адресам и рандомным портам, с вероятностью
        // в 1 процент выбирается нормальное соединение в одном из потоков. Это соединение отправляет ряд
        // файлов на комп получателя, а тот в ответ шлет размер принятого файла, чтобы понять
        // были ли потери.
    }

    private void infiniteTexting() {
        // 3 беск отправка
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
            dataOut.write("ENC_PAR".getBytes(StandardCharsets.UTF_8));
            dataOut.flush();
            dataOut.write(rsaInstance.encrypt(parametersDto.getKey()));
            dataOut.flush();
            dataOut.write(rsaInstance.encrypt(parametersDto.getIV()));
            dataOut.flush();
            dataOut.write(
                    rsaInstance
                            .encrypt(parametersDto
                                    .getCipherSystem()
                                    .getBytes(StandardCharsets.UTF_8))
            );
            dataOut.flush();
            dataOut.write(rsaInstance.encrypt(
                    BigInteger.valueOf(parametersDto.getMode()).toByteArray()
            ));
            dataOut.flush();
        } catch (IOException exception) {
            logConsole.append("Не получилось отправить параметры шифрования получателю.\n");
            return;
        } catch (GeneralSecurityException e) {
            logConsole.append("Не удалось зашифровать данные с помощью RSA для отправки.\n");
        }
        System.out.println("Reached the while");
        while (!Thread.currentThread().isInterrupted()) {
            // 3 метода работы
            if (mode == 1) loadTesting();
            if (mode == 2) stressTesting();
            if (mode == 3) infiniteTexting();
        }
        try {
            close();
        } catch (IOException e) {
            logConsole.append("Ошибка закрытия соединения. Возможна потеря данных.\n");
        }
    }
}
