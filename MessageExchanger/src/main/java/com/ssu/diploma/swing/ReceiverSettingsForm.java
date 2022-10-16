package com.ssu.diploma.swing;

import com.ssu.diploma.swing.utils.SwingCommons;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
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

    @Getter
    private final Map<String, String> settings = new HashMap<>();

    public ReceiverSettingsForm() {

        settings.put("serverPort", "8081");

        choosePathButton1.addActionListener(e -> {
            SwingCommons.browseDirAction(receivedFilesDirectoryTextField, this);
        });

        applyButton.addActionListener(e -> {
            settings.put("receiverPort", receiverPortTextField.getText());
            settings.put("receivedFilesDirectory", receivedFilesDirectoryTextField.getText());

            errorLogConsole.append("Все изменения были успешно применены.\n");
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
