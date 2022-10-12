package com.ssu.diploma.swing;

import com.ssu.diploma.EncryptorImpl;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.io.File;
import java.io.IOException;
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

public class SettingsForm extends javax.swing.JFrame {
    private JFrame f;
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

    private final JFileChooser in;
    private static final String[] SUPPORTED_CIPHERS = {"AES", "ГОСТ Р 34.12-2015 (Кузнечик)"};
    private static final String DEFAULT_KEY_LOCATION
            = "C:\\Users\\vbifu\\MyJavaProjects\\MessageExchanger\\src\\main\\resources\\key.txt";

    @Getter
    private final Map<String, String> settings = new HashMap<>();

    /*
    TODO:
     6) Генерация по кнопке нач вектора
     7) Продумать дефолтное расположение для закомменченных настроек и заменить дефолтный путь
     для генерации ключа.
     */

    public SettingsForm() {
        in = new JFileChooser();
        in.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        in.setDialogTitle("Выбор директории");

        cipherSystemComboBox.setModel(new DefaultComboBoxModel(SUPPORTED_CIPHERS));

        settings.put("receiverAddress", "localhost");
        settings.put("receiverPort", "8081");
//        settings.put("testFilesDirectory", testFilesDirectoryTextField.getText());
//        settings.put("reportsDirectory", reportsDirectoryTextField.getText());
        settings.put("cipherSystem", "AES");
        settings.put("keyPath", DEFAULT_KEY_LOCATION);

        choosePathButton1.addActionListener(e -> {browseDirAction(testFilesDirectoryTextField);});

        choosePathButton2.addActionListener(e -> {browseDirAction(reportsDirectoryTextField);});

        choosePathButton3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("выбор файла");
                int res = fileChooser.showOpenDialog(SettingsForm.this);
                if (res == 0) {
                    File file = fileChooser.getSelectedFile();
                    if (file.exists() && file.isFile())
                        keyPathTextField.setText(file.getAbsolutePath());
                    else {
                        JOptionPane.showMessageDialog(null,
                                "Директория не найдена.");
                        actionPerformed(e);
                    }
                }
            }
        });

        choosePathButton4.addActionListener(e -> browseDirAction(IVPathTextField));

        generateNewKeyButton.addActionListener(e -> {
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
                    if (Files.size(Path.of(keyPathTextField.getText()))
                            != EncryptorImpl.KEY_LENGTH / 8) {
                        errorLogConsole.append(String.format("Файл ключа имеет некорректную длину. " +
                                "Ключ должен быть = %d битам\n", EncryptorImpl.KEY_LENGTH));
                    } else {
                        settings.put("keyPath", keyPathTextField.getText());
                    }
                } catch (IOException ex) {
                    errorLogConsole.append("Не удалось проверить файл ключа. Убедитесь, что путь" +
                            " указан верно.\n");
                    success = false;
                }
            }

            if (success) {
                errorLogConsole.append("Все изменения были успешно применены.\n");
            }
        });
    }

    private void browseDirAction(JTextField destination) {
        int res = in.showOpenDialog(SettingsForm.this);
        if (res == 0) {
            File file = in.getSelectedFile();
            if (file.exists() && file.isDirectory())
                destination.setText(file.getAbsolutePath());
            else {
                JOptionPane.showMessageDialog(null,
                        "Директория не найдена.");
                browseDirAction(destination);
            }
        }
    }

    public void init() {
        f = new JFrame();
        f.add(settingsPanel);
        f.setSize(600, 500);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - f.getWidth()) / 2;
        int y = (screenSize.height - f.getHeight()) / 2;
        f.setLocation(x, y);

        receiverPortTextField.setText(settings.get("receiverPort"));
        receiverAddressTextField.setText(settings.get("receiverAddress"));
        testFilesDirectoryTextField.setText(settings.get("testFilesDirectory"));
        reportsDirectoryTextField.setText(settings.get("reportsDirectory"));
        cipherSystemComboBox.setSelectedItem(settings.get("cipherSystem"));
        keyPathTextField.setText(settings.get("keyPath"));

        errorLogConsole.setText("");

        f.setVisible(true);
    }
}
