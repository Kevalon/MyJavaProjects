package com.ssu.diploma.swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.ssu.diploma.encryption.EncryptorImpl;
import com.ssu.diploma.swing.utils.Utils;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import lombok.Getter;

public class SenderSettingsForm extends JFrame {
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
    private JScrollPane errorLogConsoleScrollPane;

    private static final String[] SUPPORTED_CIPHERS = {"AES", "ГОСТ Р 34.12-2015 (Кузнечик)"};
    private static final URL DEFAULT_KEY_LOCATION = ClassLoader.getSystemResource("key.txt");
    private static final URL DEFAULT_IV_LOCATION = ClassLoader.getSystemResource("IV.txt");

    @Getter
    private final Map<String, String> settings = new HashMap<>();

    public SenderSettingsForm() {
        cipherSystemComboBox.setModel(new DefaultComboBoxModel(SUPPORTED_CIPHERS));

        settings.put("receiverAddress", "localhost");
        settings.put("receiverPort", "8081");
        settings.put("cipherSystem", "AES");
        settings.put("keyPath", DEFAULT_KEY_LOCATION.toString());
        settings.put("IVPath", DEFAULT_IV_LOCATION.toString());
        if (Files.exists(Paths.get("C:\\Users\\vbifu\\OneDrive\\Документы\\send"))) {
            settings.put("testFilesDirectory", "C:\\Users\\vbifu\\OneDrive\\Документы\\send");
        } else {
            settings.put("testFilesDirectory", "");
        }

        choosePathButton1.addActionListener(e ->
                Utils.browseDirAction(testFilesDirectoryTextField, this));
        choosePathButton3.addActionListener(e ->
                Utils.browseFileAction(keyPathTextField, this));
        choosePathButton4.addActionListener(e ->
                Utils.browseFileAction(IVPathTextField, this));

        generateNewKeyButton.addActionListener(e -> {
            EncryptorImpl encryptor
                    = new EncryptorImpl((String) cipherSystemComboBox.getSelectedItem());
            try {
                byte[] key = encryptor.generateKey();
                Path keyFolder = Paths.get("generatedKey");
                if (!Files.exists(keyFolder)) {
                    Files.createDirectory(keyFolder);
                }
                Path newPath = Paths.get("generatedKey", "key.txt");
                Files.write(newPath, key);
                Utils.log(errorLogConsole, "Новый ключ успешно сгенерирован.");
                keyPathTextField.setText(newPath.toAbsolutePath().toString());
            } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
                Utils.log(errorLogConsole, "Ошибка генерации ключа.");
            } catch (IOException ex) {
                Utils.log(
                        errorLogConsole,
                        "Не удалось найти указанный для генерации путь.");
            }
        });

        generateNewIVButton.addActionListener(e -> {
            EncryptorImpl encryptor
                    = new EncryptorImpl((String) cipherSystemComboBox.getSelectedItem());
            try {
                byte[] IV = encryptor.generateIV();
                Path IVFolder = Paths.get("generatedIV");
                if (!Files.exists(IVFolder)) {
                    Files.createDirectory(IVFolder);
                }
                Path newPath = Paths.get("generatedIV", "IV.txt");
                Files.write(newPath, IV);
                Utils.log(errorLogConsole, "Новый начальный вектор успешно сгенерирован.");
                IVPathTextField.setText(newPath.toAbsolutePath().toString());
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
                        size = Utils.getBytesFromURL(new URL(keyPathTextField.getText())).length;
                    } catch (MalformedURLException exception) {
                        size = Files.size(Paths.get(keyPathTextField.getText()));
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
                        size = Utils.getBytesFromURL(new URL(IVPathTextField.getText())).length;
                    } catch (MalformedURLException exception) {
                        size = Files.size(Paths.get(IVPathTextField.getText()));
                    }

                    if (size != EncryptorImpl.pIVLen) {
                        Utils.log(
                                errorLogConsole,
                                String.format("Файл вектора имеет некорректную " +
                                                "длину. Вектор должен быть = %d битам.",
                                        EncryptorImpl.pIVLen * 8));
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
                    if (Files.walk(Paths.get(testFilesDirectoryTextField.getText()))
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayoutManager(11, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Шифрсистема сквозного шифрования");
        settingsPanel.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cipherSystemComboBox = new JComboBox();
        settingsPanel.add(cipherSystemComboBox,
                new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Адрес получателя");
        settingsPanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        receiverAddressTextField = new JTextField();
        settingsPanel.add(receiverAddressTextField,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
                        false));
        final JLabel label3 = new JLabel();
        label3.setText("Порт получателя");
        settingsPanel.add(label3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        receiverPortTextField = new JTextField();
        receiverPortTextField.setHorizontalAlignment(10);
        receiverPortTextField.setText("");
        settingsPanel.add(receiverPortTextField,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
                        false));
        final JLabel label4 = new JLabel();
        label4.setText("Путь до файлов тестирования");
        settingsPanel.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        testFilesDirectoryTextField = new JTextField();
        settingsPanel.add(testFilesDirectoryTextField,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
                        false));
        final JLabel label5 = new JLabel();
        label5.setText("Ключ сквозного шифрования");
        settingsPanel.add(label5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keyPathTextField = new JTextField();
        settingsPanel.add(keyPathTextField,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
                        false));
        choosePathButton3 = new JButton();
        choosePathButton3.setText("Выбрать");
        settingsPanel.add(choosePathButton3,
                new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generateNewKeyButton = new JButton();
        generateNewKeyButton.setText("Сгенерировать новый ключ");
        settingsPanel.add(generateNewKeyButton,
                new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        applyButton = new JButton();
        applyButton.setText("Применить изменения");
        settingsPanel.add(applyButton,
                new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Файл начального вектора");
        settingsPanel.add(label6, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generateNewIVButton = new JButton();
        generateNewIVButton.setText("Сгенерировать новый вектор");
        settingsPanel.add(generateNewIVButton,
                new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        IVPathTextField = new JTextField();
        settingsPanel.add(IVPathTextField,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
                        false));
        choosePathButton4 = new JButton();
        choosePathButton4.setText("Выбрать");
        settingsPanel.add(choosePathButton4,
                new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        choosePathButton1 = new JButton();
        choosePathButton1.setText("Выбрать");
        settingsPanel.add(choosePathButton1,
                new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        errorLogConsoleScrollPane = new JScrollPane();
        settingsPanel.add(errorLogConsoleScrollPane,
                new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK |
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        errorLogConsole = new JTextArea();
        errorLogConsoleScrollPane.setViewportView(errorLogConsole);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return settingsPanel;
    }

}
