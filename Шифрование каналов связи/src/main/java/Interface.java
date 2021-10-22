import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Desktop;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Interface extends javax.swing.JFrame {
    private JButton ChooseInputFileButton;
    private JPanel panel;
    private JTextField InputAddressText;
    private JButton ChooseKeyFileButton;
    private JRadioButton ChannelEncryptionRadio;
    private JRadioButton EndToEndRadio;
    private JTextField KeyAddressText;
    private JButton SendButton;
    private JButton ExitButton;
    private JLabel InputLabel;
    private JLabel KeyLabel;
    private JTextArea OutputTextArea;
    private JButton OpenFileButton;
    private JCheckBox EnableEva;
    private JFrame f;
    private JFileChooser in, key;

    int channelType;

    private static boolean checkKey(String keyPath) {
        String hexKey = "";
        try {
            FileReader in = new FileReader(keyPath);
            Scanner inScan = new Scanner(in);
            hexKey = inScan.next();
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
        if (hexKey.length() != 14) return false;
        return hexKey.matches("[0-9A-Fa-f]+");
    }

    public Interface() {


        in = new JFileChooser();
        in.setDialogTitle("выбор файла");
        if (new File("C:\\Users\\vbifu\\IdeaProjects\\Шифрование каналов связи").exists())
            in.setCurrentDirectory(new File ("C:\\Users\\vbifu\\IdeaProjects\\Шифрование каналов связи\\"));

        key = new JFileChooser("C:\\Users\\vbifu\\IdeaProjects\\Шифрование каналов связи");
        key.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
        key.setAcceptAllFileFilterUsed(false);
        key.setDialogTitle("выбор файла");
        if (new File("C:\\Users\\vbifu\\IdeaProjects\\Шифрование каналов связи").exists())
            key.setCurrentDirectory(new File ("C:\\Users\\vbifu\\IdeaProjects\\Шифрование каналов связи\\"));

        try {
            ChooseInputFileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int res = in.showOpenDialog(Interface.this);
                    if (res == 0) {
                        File file = in.getSelectedFile();
                        if (file.exists() && file.isFile())
                            InputAddressText.setText(file.getAbsolutePath());
                        else {
                            JOptionPane.showMessageDialog(null, "Файл не найден.");
                            actionPerformed(e);
                        }
                    }
                }
            });
        }
        catch (NullPointerException ex) {
            System.out.println(ex.getMessage());
        }


        ChooseKeyFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int res = in.showOpenDialog(Interface.this);
                if (res == 0) {
                    File file = in.getSelectedFile();
                    if (file.exists() && file.isFile())
                        if (checkKey(file.getAbsolutePath()))
                            KeyAddressText.setText(file.getAbsolutePath());
                    else {
                        JOptionPane.showMessageDialog(null,
                                "Файл не найден или представлен в неправильной форме.");
                        actionPerformed(e);
                    }
                }
            }
        });

        OpenFileButton.addActionListener(e -> {
            File file = new File(System.getProperty("user.dir"));
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException exception) {
                exception.printStackTrace();
                JOptionPane.showMessageDialog(null, "Файл не найден.");
            }
        });

        ExitButton.addActionListener(e -> System.exit(0));

        ChannelEncryptionRadio.addActionListener(e -> {
            if (EndToEndRadio.isSelected()) {
                ButtonGroup rb = new ButtonGroup();
                rb.add(EndToEndRadio);
                rb.clearSelection();
            }
            channelType = 1;
        });

        EndToEndRadio.addActionListener(e -> {
            if (ChannelEncryptionRadio.isSelected()) {
                ButtonGroup rb = new ButtonGroup();
                rb.add(ChannelEncryptionRadio);
                rb.clearSelection();
            }
            channelType = 2;
        });

        SendButton.addActionListener(e -> {
            if (InputAddressText.getText().isEmpty()) {
                OutputTextArea.setText("Ошибка. Сначала нужно выбрать входной файл.");
                return;
            }
            if (KeyAddressText.getText().isEmpty()) {
                OutputTextArea.setText("Ошибка. Сначала нужно выбрать файл ключа.");
                return;
            }
            if (!ChannelEncryptionRadio.isSelected() && !EndToEndRadio.isSelected()) {
                OutputTextArea.setText("Ошибка. Выберите тип шифрования.");
                return;
            }
            double time = (double)System.currentTimeMillis();

            ArrayList<byte[]> AliceBinMessage;
            AliceBinMessage = Alice.sendBytes(channelType, InputAddressText.getText(), KeyAddressText.getText());
            if (EnableEva.isSelected())
                Eva.interceptBytes(AliceBinMessage);
            String str = InputAddressText.getText();
            String reversed = new StringBuilder(str).reverse().toString();
            int index = str.length() - 1 - reversed.indexOf(".");
            String format = InputAddressText.getText().substring(index);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss:ms");
            LocalDateTime now;
            if (channelType == 1) {
                now = LocalDateTime.now();
                OutputTextArea.setText(dtf.format(now) + ": " + "Сообщение успешно попало на первый коммутатор.\n");
                ArrayList<byte[]> tmp = SwitchAlice.decryptBytes(AliceBinMessage, KeyAddressText.getText());
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Сообщение успешно расшифровано на первом коммутаторе.\n");
                tmp = SwitchAlice.encryptBytes(tmp, KeyAddressText.getText());
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Сообщение успешно зашифровано на первом коммутаторе.\n");
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Сообщение успешно попало на роутер.\n");
                tmp = Router.decryptBytes(tmp, KeyAddressText.getText());
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Сообщение успешно расшифровано на роутере.\n");
                tmp = Router.encryptBytes(tmp, KeyAddressText.getText());
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Сообщение успешно зашифровано на роутере.\n");
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Сообщение успешно попало на второй коммутатор.\n");
                tmp = SwitchBob.decryptBytes(tmp, KeyAddressText.getText());
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Сообщение успешно расшифровано на втором коммутаторе.\n");
                tmp = SwitchBob.encryptBytes(tmp, KeyAddressText.getText());
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Сообщение успешно зашифровано на втором коммутаторе.\n");
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Криптограмма успешно попала на компьютер получателя.\n" +
                            "Полученная криптограмма расшифрована и записана в файл BobReceivedCryptogram.txt.\n");
                Bob.receiveBytes(tmp, channelType, "BobReceived" + format, "BobKey.txt");
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Сообщение успешно расшифровано и записано в файл BobReceived" + format + ".\n\n");
            }
            else {
                now = LocalDateTime.now();
                OutputTextArea.setText(dtf.format(now) + ": " + "Сообщение успешно попало на первый коммутатор.\n");
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " + "Сообщение успешно попало на роутер.\n");
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " + "Сообщение успешно попало на второй коммутатор.\n");
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Криптограмма успешно попала на компьютер получателя.\n" +
                            "Полученная криптограмма расшифрована и записана в файл BobReceivedCryptogram.txt.\n");
                Bob.receiveBytes(AliceBinMessage, channelType, "BobReceived" + format, "BobKey.txt");
                now = LocalDateTime.now();
                OutputTextArea.append(dtf.format(now) + ": " +
                        "Сообщение успешно расшифровано и записано в файл BobReceived" + format + ".\n\n");
            }

            OutputTextArea.append("Сообщение успешно доставлено.\n");
            OutputTextArea.append("Время доставки: " + ((double)System.currentTimeMillis() - time) / 1000.0 + " с\n");
        });

        f = new JFrame();
        f.add(panel);
        f.setSize(800, 500);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    public static void main(String[] args) {

        new Interface();
        Eva.clearFile();
    }
}
