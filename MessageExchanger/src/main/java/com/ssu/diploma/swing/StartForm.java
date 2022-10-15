package com.ssu.diploma.swing;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class StartForm extends javax.swing.JFrame {
    private JButton senderButton;
    private JButton receiverButton;
    private JPanel mainPanel;

    public StartForm() {
        //mainPanel.setLayout(new BoxLayout(mainPanel, ));
        this.add(mainPanel);
        senderButton.addActionListener(e -> {
            new SenderForm();
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });


        receiverButton.addActionListener(e -> {
            new ReceiverForm();
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });


        this.setSize(400, 100);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - this.getWidth()) / 2;
        int y = (screenSize.height - this.getHeight()) / 2;

        this.setLocation(x, y);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new StartForm();
    }
}
