package com.ssu.diploma.swing;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
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

public class SenderForm extends javax.swing.JFrame {
    private JPanel senderPanel;
    private JTextArea logConsole;
    private JButton startButton;
    private JButton stopButton;
    private JButton openFileButton;
    private JButton settingsButton;
    private JRadioButton LinkEncryptionRadio;
    private JRadioButton EndToEndRadio;
    private JComboBox modeComboBox;

    private final String[] modes =
            {"Нагрузочное тестирование", "Стресс-тестирование", "Бесконечная отправка"};
    private final SenderSettingsForm senderSettingsForm = new SenderSettingsForm();

    /*
    TODO:
     3) Добавить логику для кнопок старт, стоп.
     3.5) Логика старт, стоп в ресивере.
     4) Добавить нагрузочное тестирование, бесконечное тестирование, стресс тестирование
     */


    public SenderForm() {
        this.add(senderPanel);

        modeComboBox.setModel(new DefaultComboBoxModel(modes));

        LinkEncryptionRadio.addActionListener(e -> {
            if (EndToEndRadio.isSelected()) {
                ButtonGroup rb = new ButtonGroup();
                rb.add(EndToEndRadio);
                rb.clearSelection();
            }
        });

        EndToEndRadio.addActionListener(e -> {
            if (LinkEncryptionRadio.isSelected()) {
                ButtonGroup rb = new ButtonGroup();
                rb.add(LinkEncryptionRadio);
                rb.clearSelection();
            }
        });

        startButton.addActionListener(e -> {
            // попытка подключиться к серверу, уведомление об ошибке. (В принципе класс Sender можно юзать)
            // Отправка ключа шифрования, шифрсистемы, вектора инициализации.

            // 3 вида тестирования:
            // 1 нагрузочное - начни с него
            // Берем папку с файлами и отправляем эти файлы один за другим. Выводим время после ответа о том,
            // что оно было получено. Так же выводим размер отправленного файла и размер полученного файла, чтобы
            // показать потери.

            // 3 беск отправка

            // 2 стресс тест - указывается папка с файлами, которые надо отправить, а затем
            // включается нагрузка на роутер и коммутатор. (указывается уровень нагрузки)
        });

        stopButton.addActionListener(e -> {
            // Принудительно останавливает клиент, выводит об этом сообщение.
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
