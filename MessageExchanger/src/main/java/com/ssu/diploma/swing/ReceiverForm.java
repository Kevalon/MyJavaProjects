package com.ssu.diploma.swing;

import com.ssu.diploma.swing.utils.Utils;
import com.ssu.diploma.threads.Receiver;
import java.awt.Dimension;
import java.awt.Toolkit;
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
    private Thread receiverThread;

    public ReceiverForm() {
        this.add(receiverPanel);

        startButton.addActionListener(e -> {
            // clean all trash from previous launch
            String test = receiverSettingsForm.getSettings().get("receivedFilesDirectory");
            if (test == null || test.equals("")) {
                Utils.log(
                        logConsole,
                        "Пожалуйста, выберите директорию для принимаемых файлов в настройках.");
                return;
            }
            receiverThread = new Thread(
                    new Receiver(receiverSettingsForm.getSettings(), logConsole));
            receiverThread.start();
        });

        settingsButton.addActionListener(e -> {
            receiverSettingsForm.init();
        });

        stopButton.addActionListener(e -> {
            if (receiverThread == null) {
                Utils.log(logConsole, "Получатель не запущен.");
                return;
            }
            receiverThread.interrupt();
            Utils.log(logConsole, "Получатель успешно остановлен.");
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
