package com.ssu.diploma.swing;

import com.ssu.diploma.encryption.EncryptorImpl;
import com.ssu.diploma.swing.utils.Utils;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import lombok.Getter;

public class SenderSettingsForm extends javax.swing.JFrame {
    private JPanel settingsPanel;
    private JTextField receiverAddressTextField;
    private JTextField receiverPortTextField;
    private JButton applyButton;
    private JTextField testFilesDirectoryTextField;
    private JTextArea errorLogConsole;
    private JTextField keyPathTextField;
    private JComboBox cipherSystemComboBox;
    private JButton generateNewKeyButton;
    private JButton choosePathButton1;
    private JButton choosePathButton3;
    private JButton generateNewIVButton;
    private JTextField IVPathTextField;
    private JButton choosePathButton4;

    private static final String[] SUPPORTED_CIPHERS = {"AES", "ГОСТ Р 34.12-2015 (Кузнечик)"};
    private static final URL DEFAULT_KEY_LOCATION
            = ClassLoader.getSystemResource("key.txt");
    private static final URL DEFAULT_IV_LOCATION
            = ClassLoader.getSystemResource("IV.txt");

    @Getter
    private final Map<String, String> settings = new HashMap<>();

    //TODO: разобраться с подписью jar файлов и BC

    public SenderSettingsForm() {
        cipherSystemComboBox.setModel(new DefaultComboBoxModel(SUPPORTED_CIPHERS));

        settings.put("receiverAddress", "localhost");
        settings.put("receiverPort", "8081");
        settings.put("cipherSystem", "AES");
        settings.put("keyPath", DEFAULT_KEY_LOCATION.toString());
        settings.put("IVPath", DEFAULT_IV_LOCATION.toString());
        // todo: remove after fix
        settings.put("testFilesDirectory", "C:\\Users\\vbifu\\OneDrive\\Документы\\send");

        choosePathButton1.addActionListener(e ->
                Utils.browseDirAction(testFilesDirectoryTextField, this));
        choosePathButton3.addActionListener(e ->
                Utils.browseFileAction(keyPathTextField, this));
        choosePathButton4.addActionListener(e ->
                Utils.browseFileAction(IVPathTextField, this));

        generateNewKeyButton.addActionListener(e -> {
            int res = Utils.browseDirAction(keyPathTextField, this);
            if (res == 1) return;
            keyPathTextField.setText(keyPathTextField.getText() + "\\key.txt");

            EncryptorImpl encryptor
                    = new EncryptorImpl((String) cipherSystemComboBox.getSelectedItem());
            try {
                byte[] key = encryptor.generateKey();
                Files.write(Path.of(keyPathTextField.getText()), key);
            } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
                Utils.log(errorLogConsole, "Ошибка генерации ключа.");
            } catch (IOException ex) {
                Utils.log(
                        errorLogConsole,
                        "Не удалось найти указанный для генерации путь.");
            }
        });

        generateNewIVButton.addActionListener(e -> {
            int res = Utils.browseDirAction(IVPathTextField, this);
            if (res == 1) return;
            IVPathTextField.setText(IVPathTextField.getText() + "\\IV.txt");

            EncryptorImpl encryptor
                    = new EncryptorImpl((String) cipherSystemComboBox.getSelectedItem());
            try {
                byte[] IV = encryptor.generateIV();
                Files.write(Path.of(IVPathTextField.getText()), IV);
            } catch (IOException ex) {
                Utils.log(errorLogConsole, "Не удалось найти указанный для генерации путь.");
            }
        });

        applyButton.addActionListener(e -> {
            boolean success = true;
            settings.put("receiverAddress", receiverAddressTextField.getText());
            settings.put("receiverPort", receiverPortTextField.getText());
            settings.put("testFilesDirectory", testFilesDirectoryTextField.getText());
            settings.put("cipherSystem", (String) cipherSystemComboBox.getSelectedItem());

            if (!keyPathTextField.getText().equals("")) {
                try {
                    long size;
                    try {
                        size = Utils.getBytesFromURL(
                                new URL(keyPathTextField.getText())).length;
                    } catch (MalformedURLException exception) {
                        size = Files.size(Path.of(keyPathTextField.getText()));
                    }

                    if (size != EncryptorImpl.KEY_LENGTH / 8) {
                        Utils.log(
                                errorLogConsole,
                                String.format("Файл ключа имеет некорректную длину. " +
                                                "Ключ должен быть = %d битам.",
                                        EncryptorImpl.KEY_LENGTH));
                        success = false;
                    } else {
                        settings.put("keyPath", keyPathTextField.getText());
                    }
                } catch (IOException ex) {
                    Utils.log(
                            errorLogConsole,
                            "Не удалось проверить файл ключа. " +
                                    "Убедитесь, что путь указан верно.");
                    success = false;
                }
            }

            if (!IVPathTextField.getText().equals("")) {
                try {
                    long size;
                    try {
                        size = Utils.getBytesFromURL(
                                new URL(IVPathTextField.getText())).length;
                    } catch (MalformedURLException exception) {
                        size = Files.size(Path.of(IVPathTextField.getText()));
                    }

                    if (size != EncryptorImpl.pIVLen) {
                        Utils.log(
                                errorLogConsole,
                                String.format("Файл вектора имеет некорректную " +
                                                "длину. Вектор должен быть = %d битам.",
                                        EncryptorImpl.pIVLen));
                        success = false;
                    } else {
                        settings.put("IVPath", IVPathTextField.getText());
                    }
                } catch (IOException ex) {
                    Utils.log(
                            errorLogConsole,
                            "Не удалось проверить файл начального вектора. " +
                                    "Убедитесь, что путь указан верно.");
                    success = false;
                }
            }

            if (!testFilesDirectoryTextField.getText().equals("")) {
                try {
                    if (Files.walk(Path.of(testFilesDirectoryTextField.getText()))
                            .noneMatch(Files::isRegularFile)) {
                        Utils.log(
                                errorLogConsole,
                                "Указанная директория с файлами для отправки " +
                                        "не содержит ни одного файла.");
                        success = false;
                    }
                } catch (IOException ex) {
                    Utils.log(
                            errorLogConsole,
                            "Не удалось открыть директорию с файлами для отправки.");
                    success = false;
                }
            }

            if (success) {
                Utils.log(errorLogConsole, "Все изменения были успешно применены.");
            }
        });
    }

    public void init() {
        this.add(settingsPanel);
        this.setSize(600, 500);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - this.getWidth()) / 2;
        int y = (screenSize.height - this.getHeight()) / 2;
        this.setLocation(x, y);

        receiverPortTextField.setText(settings.get("receiverPort"));
        receiverAddressTextField.setText(settings.get("receiverAddress"));
        testFilesDirectoryTextField.setText(settings.get("testFilesDirectory"));
        cipherSystemComboBox.setSelectedItem(settings.get("cipherSystem"));
        keyPathTextField.setText(settings.get("keyPath"));
        IVPathTextField.setText(settings.get("IVPath"));

        errorLogConsole.setText("");

        this.setVisible(true);
    }
}
