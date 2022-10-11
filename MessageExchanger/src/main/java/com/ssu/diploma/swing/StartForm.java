package com.ssu.diploma.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class StartForm {
    private JButton senderButton;
    private JButton receiverButton;
    private JPanel mainPanel;
    private final JFrame f;

    public StartForm() {
        f = new JFrame();
        f.add(mainPanel);
        f.setSize(400, 100);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - f.getWidth()) / 2;
        int y = (screenSize.height - f.getHeight()) / 2;

        f.setLocation(x, y);
        f.setVisible(true);

        senderButton.addActionListener(e -> {
            new SenderForm();
            f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
        });

        receiverButton.addActionListener(e -> {
            new ReceiverForm();
            f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
        });
    }

    public static void main(String[] args) {
        new StartForm();
    }
}
