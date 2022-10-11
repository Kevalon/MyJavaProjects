package com.ssu.diploma.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SettingsForm extends javax.swing.JFrame {
    private JFrame f;
    private JPanel settingsPanel;
    private JTextField receiverAddressTextField;
    private JTextField receiverPortTextField;
    private JButton applyButton;
    private JTextField testFilesDirectoryTextField;
    private JTextField reportsDirectoryTextField;
    private JTextArea errorLogConsole;
    private JTextField keyPathTextField;
    private JComboBox cipherSystemComboBox;
    private JButton generateNewKeyButton;
    private JButton choosePathButton1;
    private JButton choosePathButton2;
    private JButton choosePathButton3;

    private final JFileChooser in;
    private final String[] cipherNames = {"AES", "ГОСТ Р 34.12-2015 (Кузнечик)"};
    private final Map<String, String> settings = new HashMap<>();

    /*
    TODO:
     2) настройки должны хранить в себе все необходимые настройки (их вытащит форма сендера)
     3) Применить изменения - валидирует данные, какие может, иначе выводит ошибку в лог поле
     4) При генерации нового ключа - он пишется либо в выбранный путь, либо если путь не выбран -
     то он генерится где-то и выставляется путь до него.
     5) Ключ будет улетать перед отправкой файла в сквозном шифровании.
     6) При открытии окошка настроек соотв. поля должны заполняться данными готовыми, иначе становиться
     пустыми. При закрытии окна, не сохранив данные, при повторном открытии пишутся старые данные.
     */

    public SettingsForm() {
        in = new JFileChooser();
        in.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        in.setDialogTitle("выбор директории");



        cipherSystemComboBox.setModel(new DefaultComboBoxModel(cipherNames));

        choosePathButton1.addActionListener(e -> {
            browseDirAction(testFilesDirectoryTextField);
        });

        choosePathButton2.addActionListener(e -> {
            browseDirAction(reportsDirectoryTextField);
        });

        choosePathButton3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("выбор файла");
                int res = fileChooser.showOpenDialog(SettingsForm.this);
                if (res == 0) {
                    File file = fileChooser.getSelectedFile();
                    if (file.exists() && file.isFile())
                        keyPathTextField.setText(file.getAbsolutePath());
                    else {
                        JOptionPane.showMessageDialog(null,
                                "Директория не найдена.");
                        actionPerformed(e);
                    }
                }
            }
        });

        generateNewKeyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    private void browseDirAction(JTextField destination) {
        int res = in.showOpenDialog(SettingsForm.this);
        if (res == 0) {
            File file = in.getSelectedFile();
            if (file.exists() && file.isDirectory())
                destination.setText(file.getAbsolutePath());
            else {
                JOptionPane.showMessageDialog(null,
                        "Директория не найдена.");
                browseDirAction(destination);
            }
        }
    }

    public void init() {
        f = new JFrame();
        f.add(settingsPanel);
        f.setSize(600, 400);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - f.getWidth()) / 2;
        int y = (screenSize.height - f.getHeight()) / 2;

        f.setLocation(x, y);
        f.setVisible(true);
    }
}
