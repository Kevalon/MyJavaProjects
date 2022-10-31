package com.ssu.diploma.swing.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.commons.io.IOUtils;

public class Utils {
    public static final int RESOURCE_BUFFER_SIZE = 1024 * 1024;

    public static synchronized int browseDirAction(JTextField destination, JFrame frame) {
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
        return res;
    }

    public static synchronized void browseFileAction(JTextField destination, JFrame frame) {
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

    public static synchronized byte[] getBytesFromURL(URL resource) {
        try (InputStream in = resource.openStream()) {
            return IOUtils.toByteArray(in);
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static void log(JTextArea logConsole, String message) {
        logConsole.append(String.format(
                "%s%s\n",
                '[' + LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString() + "] ",
                message
        ));
    }

    public static byte[] receiveByteArray(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length > 0) {
            byte[] message = new byte[length];
            in.readFully(message, 0, message.length);
            return message;
        }
        throw new IOException();
    }

    public static void sendData(Object data, DataOutputStream out) throws IOException {
        if (data instanceof byte[]) {
            byte[] newData = (byte[]) data;
            out.writeInt(newData.length);
            out.flush();
            out.write(newData);
            out.flush();
        } else if (data instanceof Integer) {
            out.writeInt((int) data);
            out.flush();
        } else if (data instanceof Character) {
            out.writeInt((char) data);
            out.flush();
        }
    }
}
