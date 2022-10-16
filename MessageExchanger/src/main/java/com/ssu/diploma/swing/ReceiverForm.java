package com.ssu.diploma.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ReceiverForm extends javax.swing.JFrame {
    private JPanel receiverPanel;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logConsole;
    private JButton settingsButton;

    private final ReceiverSettingsForm receiverSettingsForm = new ReceiverSettingsForm();

    public ReceiverForm() {
        this.add(receiverPanel);

        startButton.addActionListener(e -> {

        });

        settingsButton.addActionListener(e -> {
            receiverSettingsForm.init();
        });

        stopButton.addActionListener(e -> {

        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 500);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - this.getWidth()) / 2;
        int y = (screenSize.height - this.getHeight()) / 2;
        this.setLocation(x, y);
        this.setVisible(true);
    }
}
