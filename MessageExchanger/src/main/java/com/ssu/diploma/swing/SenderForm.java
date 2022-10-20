package com.ssu.diploma.swing;

import com.ssu.diploma.threads.Sender;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

public class SenderForm extends javax.swing.JFrame {
    private JPanel senderPanel;
    private JTextArea logConsole;
    private JButton startButton;
    private JButton stopButton;
    private JButton openFileButton;
    private JButton settingsButton;
    private JRadioButton linkEncryptionRadio;
    private JRadioButton endToEndRadio;
    private JComboBox modeComboBox;

    private final String[] modes =
            {"Нагрузочное тестирование", "Стресс-тестирование", "Бесконечная отправка"};
    private final SenderSettingsForm senderSettingsForm = new SenderSettingsForm();
    private Thread senderThread;

    public SenderForm() {
        this.add(senderPanel);

        modeComboBox.setModel(new DefaultComboBoxModel(modes));

        endToEndRadio.setSelected(true);

        linkEncryptionRadio.addActionListener(e -> {
            if (endToEndRadio.isSelected()) {
                ButtonGroup rb = new ButtonGroup();
                rb.add(endToEndRadio);
                rb.clearSelection();
            }
        });

        endToEndRadio.addActionListener(e -> {
            if (linkEncryptionRadio.isSelected()) {
                ButtonGroup rb = new ButtonGroup();
                rb.add(linkEncryptionRadio);
                rb.clearSelection();
            }
        });

        startButton.addActionListener(e -> {
            senderThread = new Thread(
                    new Sender(
                            senderSettingsForm.getSettings(),
                            logConsole,
                            IntStream.range(0, modes.length)
                                    .filter(i -> modes[i].equals(modeComboBox.getSelectedItem()))
                                    .findFirst()
                                    .getAsInt(),
                            endToEndRadio.isSelected()
                    )
            );
            senderThread.start();
        });

        stopButton.addActionListener(e -> {
            senderThread.interrupt();
            logConsole.append("Отправитель успешно остановлен.\n");
            // чистит все внутренние каталоги с зашифрованными файлами.

        });

        settingsButton.addActionListener(e -> {
            senderSettingsForm.init();
        });

        openFileButton.addActionListener(e -> {
            File file = new File(System.getProperty("user.dir"));
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(null, "Файл не найден.");
            }
        });

        this.setSize(800, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - this.getWidth()) / 2;
        int y = (screenSize.height - this.getHeight()) / 2;

        this.setLocation(x, y);
        this.setVisible(true);
    }
}
