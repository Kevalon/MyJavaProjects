package com.ssu.diploma.swing.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class SwingCommons {

    public static void browseDirAction(JTextField destination, JFrame frame) {
        JFileChooser dirFileChooser = new JFileChooser();
        dirFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirFileChooser.setDialogTitle("Выбор директории");
        int res = dirFileChooser.showOpenDialog(frame);
        if (res == 0) {
            File file = dirFileChooser.getSelectedFile();
            if (file.exists() && file.isDirectory()) {
                destination.setText(file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(null,
                        "Директория не найдена.");
                browseDirAction(destination, frame);
            }
        }
    }

    public static void browseFileAction(JTextField destination, JFrame frame) {
        JFileChooser fileFileChooser = new JFileChooser();
        fileFileChooser.setDialogTitle("Выбор файла");
        int res = fileFileChooser.showOpenDialog(frame);
        if (res == 0) {
            File file = fileFileChooser.getSelectedFile();
            if (file.exists() && file.isFile()) {
                destination.setText(file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(null,
                        "Файл не найден.");
                browseFileAction(destination, frame);
            }
        }
    }

    public static byte[] getBytesFromURL(URL resource) {
        try (InputStream in = resource.openStream()) {
            return in.readAllBytes();
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
