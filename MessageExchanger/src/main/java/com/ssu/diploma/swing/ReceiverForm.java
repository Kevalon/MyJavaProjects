package com.ssu.diploma.swing;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ReceiverForm {
    private JFrame f;
    private JPanel receiverPanel;

    public ReceiverForm() {
        f = new JFrame();
        f.add(receiverPanel);
        f.setSize(800, 500);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
