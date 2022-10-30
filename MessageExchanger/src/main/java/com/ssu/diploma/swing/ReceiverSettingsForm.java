package com.ssu.diploma.swing;

import com.ssu.diploma.swing.utils.Utils;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import lombok.Getter;

public class ReceiverSettingsForm extends javax.swing.JFrame {
    private JPanel receiverSettingsPanel;
    private JTextArea errorLogConsole;
    private JTextField receiverPortTextField;
    private JTextField receivedFilesDirectoryTextField;
    private JButton choosePathButton1;
    private JButton applyButton;
    private JScrollPane errorLogConsoleScrollPane;

    @Getter
    private final Map<String, String> settings = new HashMap<>();

    public ReceiverSettingsForm() {

        settings.put("serverPort", "8081");
        // todo: remove after fix
        settings.put("receivedFilesDirectory", "C:\\Users\\vbifu\\OneDrive\\Документы\\receive");

        choosePathButton1.addActionListener(e -> {
            Utils.browseDirAction(receivedFilesDirectoryTextField, this);
        });

        applyButton.addActionListener(e -> {
            settings.put("serverPort", receiverPortTextField.getText());

            if (!receivedFilesDirectoryTextField.getText().equals("")) {
                if (Files.isDirectory(Path.of(receivedFilesDirectoryTextField.getText()))) {
                    settings.put("receivedFilesDirectory",
                            receivedFilesDirectoryTextField.getText());
                } else {
                    Utils.log(
                            errorLogConsole,
                            "Не получилось проверить указанную директорию для файлов."
                    );
                    return;
                }
            }
            Utils.log(
                    errorLogConsole,
                    "Все изменения были успешно применены."
            );
        });
    }

    public void init() {
        this.add(receiverSettingsPanel);
        this.setSize(600, 400);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - this.getWidth()) / 2;
        int y = (screenSize.height - this.getHeight()) / 2;
        this.setLocation(x, y);

        receiverPortTextField.setText(settings.get("serverPort"));
        receivedFilesDirectoryTextField.setText(settings.get("receivedFilesDirectory"));

        errorLogConsole.setText("");

        this.setVisible(true);
    }
}
