package com.ssu.diploma.swing;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

public class SenderForm {
    private JFrame f;
    private JPanel senderPanel;
    private JTextArea logConsole;
    private JButton startButton;
    private JButton stopButton;
    private JButton openFileButton;
    private JButton settingsButton;
    private JRadioButton ChannelEncryptionRadio;
    private JRadioButton EndToEndRadio;
    private JComboBox modeComboBox;

    private final String[] modes = {"Нагрузочное тестирование", "Стресс-тестирование", "Бесконечная отправка"};
    private final SettingsForm settingsForm = new SettingsForm();

    /*
    TODO:
     1) закончить форму настроек. Данные берутся из объекта формы внутри класса
     2) Добавить логику для кнопок старт, стоп.
     3) Написать форму получателя (тоже там настройки будут)
     4) Добавить нагрузочное тестирование, бесконечное тестирование, стресс тестирование
     */


    public SenderForm() {
        f = new JFrame();
        f.add(senderPanel);
        f.setSize(800, 500);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - f.getWidth()) / 2;
        int y = (screenSize.height - f.getHeight()) / 2;

        f.setLocation(x, y);
        f.setVisible(true);

        modeComboBox.setModel(new DefaultComboBoxModel(modes));

        ChannelEncryptionRadio.addActionListener(e -> {
            if (EndToEndRadio.isSelected()) {
                ButtonGroup rb = new ButtonGroup();
                rb.add(EndToEndRadio);
                rb.clearSelection();
            }
        });

        EndToEndRadio.addActionListener(e -> {
            if (ChannelEncryptionRadio.isSelected()) {
                ButtonGroup rb = new ButtonGroup();
                rb.add(ChannelEncryptionRadio);
                rb.clearSelection();
            }
        });

        startButton.addActionListener(e -> {

        });

        stopButton.addActionListener(e -> {

        });

        settingsButton.addActionListener(e -> {
            settingsForm.init();
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
    }
}
