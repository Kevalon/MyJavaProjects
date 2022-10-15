package com.ssu.diploma.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import lombok.Getter;

public class ReceiverSettingsForm extends javax.swing.JFrame {
    private JPanel receiverSettingsPanel;
    private JTextArea errorLogConsole;

    @Getter
    private final Map<String, String> settings = new HashMap<>();

    public ReceiverSettingsForm() {

    }

    public void init() {
        this.add(receiverSettingsPanel);
        this.setSize(600, 500);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - this.getWidth()) / 2;
        int y = (screenSize.height - this.getHeight()) / 2;
        this.setLocation(x, y);

        errorLogConsole.setText("");

        this.setVisible(true);
    }
}
