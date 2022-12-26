package com.ssu.diploma.swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.ssu.diploma.swing.utils.Utils;
import com.ssu.diploma.threads.Sender;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.IntStream;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// Интерфейс окна отправителя
public class SenderForm extends JFrame {
    private JPanel senderPanel;
    private JTextArea logConsole;
    private JButton startButton;
    private JButton stopButton;
    private JButton settingsButton;
    private JRadioButton tunnelingRadio;
    private JRadioButton endToEndRadio;
    private JComboBox modeComboBox;
    private JScrollPane logConsoleScrollPane;
    private JRadioButton linkEncryptionRadio;
    private JRadioButton mixedEncryptionRadioButton;

    private final String[] testingModes =
            {"Нагрузочное тестирование", "Бесконечная отправка", "Выборочная отправка файлов"};
    private final SenderSettingsForm senderSettingsForm = new SenderSettingsForm();
    private Sender senderThread;

    public SenderForm() {
        this.add(senderPanel);

        modeComboBox.setModel(new DefaultComboBoxModel(testingModes));

        endToEndRadio.setSelected(true);

        tunnelingRadio.addActionListener(e -> {
            linkEncryptionRadio.setSelected(false);
            endToEndRadio.setSelected(false);
            mixedEncryptionRadioButton.setSelected(false);
        });

        linkEncryptionRadio.addActionListener(e -> {
            endToEndRadio.setSelected(false);
            tunnelingRadio.setSelected(false);
            mixedEncryptionRadioButton.setSelected(false);
        });

        endToEndRadio.addActionListener(e -> {
            tunnelingRadio.setSelected(false);
            linkEncryptionRadio.setSelected(false);
            mixedEncryptionRadioButton.setSelected(false);
        });

        mixedEncryptionRadioButton.addActionListener(e -> {
            tunnelingRadio.setSelected(false);
            linkEncryptionRadio.setSelected(false);
            endToEndRadio.setSelected(false);
        });

        // Запуск отправителя
        startButton.addActionListener(e -> {
            // 0 - stress, 1 - infinite, 2 - choose files
            int testingMode = IntStream.range(0, testingModes.length)
                    .filter(i -> testingModes[i].equals(modeComboBox.getSelectedItem()))
                    .findFirst()
                    .getAsInt();
            // 0 - end-to-end, 1 - link, 2 - tunnel, 3 - mixed
            int encryptionMode;
            if (endToEndRadio.isSelected()) {
                encryptionMode = 0;
            } else if (linkEncryptionRadio.isSelected()) {
                encryptionMode = 1;
            } else if (tunnelingRadio.isSelected()) {
                encryptionMode = 2;
            } else {
                encryptionMode = 3;
            }
            Map<String, String> settings = senderSettingsForm.getSettings();

            if (encryptionMode == 1 || encryptionMode == 3) {
                int nodesAmount;
                boolean repeat = false;
                String message = "Введите количество промежуточных узлов";
                do {
                    if (repeat) {
                        message = "Пожалуйста, укажите неотрицательное целое число";
                    }
                    String name = JOptionPane.showInputDialog(this,
                            message, null);
                    if (name == null) {
                        return;
                    }
                    try {
                        nodesAmount = Integer.parseInt(name);
                    } catch (NumberFormatException exception) {
                        nodesAmount = -1;
                    }
                    repeat = true;
                } while (nodesAmount < 0);
                settings.put("nodesAmount", String.valueOf(nodesAmount));
            }

            if (testingMode == 2) {
                Path[] paths = Utils.browseSeveralFiles(this);
                if (paths == null) {
                    return;
                }
                senderThread = new Sender(
                        settings,
                        logConsole,
                        testingMode,
                        encryptionMode,
                        paths
                );
            } else {
                String test = settings.get("testFilesDirectory");
                if (test == null || test.equals("")) {
                    Utils.log(
                            logConsole,
                            "Пожалуйста, укажите в настройках директорию с отправляемыми файлами."
                    );
                    return;
                }
                senderThread = new Sender(
                        settings,
                        logConsole,
                        testingMode,
                        encryptionMode
                );
            }
            senderThread.start();
        });

        // Остановка отправителя
        stopButton.addActionListener(e -> {
            if (senderThread == null || senderThread.isStop()) {
                Utils.log(logConsole, "Отправитель не запущен.");
                return;
            }
            Utils.log(
                    logConsole,
                    "Остановка отправителя началась. Это может занять некоторое время.");
            senderThread.setStop(true);
        });

        settingsButton.addActionListener(e -> {
            senderSettingsForm.init();
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        senderPanel = new JPanel();
        senderPanel.setLayout(new GridLayoutManager(5, 9, new Insets(0, 0, 0, 0), -1, -1));
        startButton = new JButton();
        startButton.setText("Старт");
        senderPanel.add(startButton, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(4);
        label1.setText("Вид шифрования");
        senderPanel.add(label1, new GridConstraints(0, 0, 1, 6, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logConsoleScrollPane = new JScrollPane();
        senderPanel.add(logConsoleScrollPane,
                new GridConstraints(3, 0, 1, 9, GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK |
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        logConsole = new JTextArea();
        logConsoleScrollPane.setViewportView(logConsole);
        endToEndRadio = new JRadioButton();
        endToEndRadio.setText("Сквозное шифрование");
        senderPanel.add(endToEndRadio, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stopButton = new JButton();
        stopButton.setText("Стоп");
        senderPanel.add(stopButton, new GridConstraints(4, 8, 1, 1, GridConstraints.ANCHOR_EAST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        senderPanel.add(spacer1, new GridConstraints(4, 3, 1, 3, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null,
                null, null, 0, false));
        linkEncryptionRadio = new JRadioButton();
        linkEncryptionRadio.setText("Канальное шифрование");
        senderPanel.add(linkEncryptionRadio,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tunnelingRadio = new JRadioButton();
        tunnelingRadio.setText("Туннелирование");
        senderPanel.add(tunnelingRadio, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mixedEncryptionRadioButton = new JRadioButton();
        mixedEncryptionRadioButton.setText("Комбинированное шифрование");
        senderPanel.add(mixedEncryptionRadioButton,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        settingsButton = new JButton();
        settingsButton.setText("Настройки");
        senderPanel.add(settingsButton,
                new GridConstraints(2, 8, 1, 1, GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        modeComboBox = new JComboBox();
        senderPanel.add(modeComboBox, new GridConstraints(1, 8, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return senderPanel;
    }

}
