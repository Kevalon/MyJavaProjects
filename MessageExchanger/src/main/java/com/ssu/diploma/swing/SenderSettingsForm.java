package com.ssu.diploma.swing;

import com.ssu.diploma.EncryptorImpl;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
    private JTextField reportsDirectoryTextField;
    private JTextArea errorLogConsole;
    private JTextField keyPathTextField;
    private JComboBox cipherSystemComboBox;
    private JButton generateNewKeyButton;
    private JButton choosePathButton1;
    private JButton choosePathButton2;
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

        choosePathButton1.addActionListener(e -> browseDirAction(testFilesDirectoryTextField));
        choosePathButton2.addActionListener(e -> browseDirAction(reportsDirectoryTextField));
        choosePathButton3.addActionListener(e -> browseFileAction(keyPathTextField));
        choosePathButton4.addActionListener(e -> browseFileAction(IVPathTextField));

        generateNewKeyButton.addActionListener(e -> {
            browseDirAction(keyPathTextField);
            keyPathTextField.setText(keyPathTextField.getText() + "\\key.txt");

            EncryptorImpl encryptor
                    = new EncryptorImpl((String) cipherSystemComboBox.getSelectedItem());
            try {
                byte[] key = encryptor.generateKey();
                Files.write(Path.of(keyPathTextField.getText()), key);
            } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
                errorLogConsole.append("Ошибка генерации ключа.\n");
            } catch (IOException ex) {
                errorLogConsole.append("Не удалось найти указанный для генерации путь.\n");
            }
        });

        generateNewIVButton.addActionListener(e -> {
            browseDirAction(IVPathTextField);
            keyPathTextField.setText(IVPathTextField.getText() + "\\IV.txt");

            EncryptorImpl encryptor
                    = new EncryptorImpl((String) cipherSystemComboBox.getSelectedItem());
            try {
                byte[] IV = encryptor.generateIV();
                Files.write(Path.of(IVPathTextField.getText()), IV);
            } catch (IOException ex) {
                errorLogConsole.append("Не удалось найти указанный для генерации путь.\n");
            }
        });

        applyButton.addActionListener(e -> {
            boolean success = true;
            settings.put("receiverAddress", receiverAddressTextField.getText());
            settings.put("receiverPort", receiverPortTextField.getText());
            settings.put("testFilesDirectory", testFilesDirectoryTextField.getText());
            settings.put("reportsDirectory", reportsDirectoryTextField.getText());
            settings.put("cipherSystem", (String) cipherSystemComboBox.getSelectedItem());

            if (!keyPathTextField.getText().equals("")) {
                try {
                    long size;
                    try {
                        size = getBytesFromURL(new URL(keyPathTextField.getText())).length;
                    } catch (MalformedURLException exception) {
                        size = Files.size(Path.of(keyPathTextField.getText()));
                    }

                    if (size != EncryptorImpl.KEY_LENGTH / 8) {
                        errorLogConsole.append(
                                String.format("Файл ключа имеет некорректную длину. " +
                                                "Ключ должен быть = %d битам\n",
                                        EncryptorImpl.KEY_LENGTH));
                        success = false;
                    } else {
                        settings.put("keyPath", keyPathTextField.getText());
                    }
                } catch (IOException ex) {
                    errorLogConsole.append("Не удалось проверить файл ключа. " +
                            "Убедитесь, что путь указан верно.\n");
                    success = false;
                }
            }

            if (!IVPathTextField.getText().equals("")) {
                try {
                    long size;
                    try {
                        size = getBytesFromURL(new URL(IVPathTextField.getText())).length;
                    } catch (MalformedURLException exception) {
                        size = Files.size(Path.of(IVPathTextField.getText()));
                    }

                    if (size != EncryptorImpl.pIVLen) {
                        errorLogConsole.append(
                                String.format("Файл вектора имеет некорректную " +
                                                "длину. Вектор должен быть = %d битам\n",
                                        EncryptorImpl.pIVLen));
                        success = false;
                    } else {
                        settings.put("IVPath", IVPathTextField.getText());
                    }
                } catch (IOException ex) {
                    errorLogConsole.append("Не удалось проверить файл начального вектора. " +
                            "Убедитесь, что путь указан верно.\n");
                    success = false;
                }
            }

            if (success) {
                errorLogConsole.append("Все изменения были успешно применены.\n");
            }
        });
    }

    private void browseDirAction(JTextField destination) {
        JFileChooser dirFileChooser = new JFileChooser();
        dirFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirFileChooser.setDialogTitle("Выбор директории");
        int res = dirFileChooser.showOpenDialog(SenderSettingsForm.this);
        if (res == 0) {
            File file = dirFileChooser.getSelectedFile();
            if (file.exists() && file.isDirectory()) {
                destination.setText(file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(null,
                        "Директория не найдена.");
                browseDirAction(destination);
            }
        }
    }

    private void browseFileAction(JTextField destination) {
        JFileChooser fileFileChooser = new JFileChooser();
        fileFileChooser.setDialogTitle("Выбор файла");
        int res = fileFileChooser.showOpenDialog(SenderSettingsForm.this);
        if (res == 0) {
            File file = fileFileChooser.getSelectedFile();
            if (file.exists() && file.isFile()) {
                destination.setText(file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(null,
                        "Файл не найден.");
                browseFileAction(destination);
            }
        }
    }

    private byte[] getBytesFromURL(URL resource) {
        try (InputStream in = resource.openStream()) {
            return in.readAllBytes();
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
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
        reportsDirectoryTextField.setText(settings.get("reportsDirectory"));
        cipherSystemComboBox.setSelectedItem(settings.get("cipherSystem"));
        keyPathTextField.setText(settings.get("keyPath"));
        IVPathTextField.setText(settings.get("IVPath"));

        errorLogConsole.setText("");

        this.setVisible(true);
    }
}
