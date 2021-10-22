import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Bob {
    private static final String IpAddr = "192.168.1.21";
    private static final String socket = "41134";
    private static final String MAC = "50:46:5D:6E:8C:20";

    private static String getKey(String keyPath) {
        String hexKey = "";
        try {
            FileReader in = new FileReader(keyPath);
            Scanner inScan = new Scanner(in);
            hexKey = inScan.next();
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
        return hexKey;
    }

    private static void writeBytes (byte[] bytes, String path) {
        File file = new File(path);
        try {
            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            os.close();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    public static void receiveBytes (ArrayList<byte[]> bytes, int channelType, String path, String keyPath) {
        if (channelType == 2) {
            bytes.remove(0);
        }

        int bytes_to_delete = (MAC + ':' + IpAddr + ':' + socket + " ").getBytes().length;
        int size = (bytes.size() - 1) * 8 + bytes.get(bytes.size() - 1).length;
        byte[] data = new byte[size];
        int p = 0;
        int idx = 0;
        while (p < size) {
            byte[] tmp = bytes.get(idx);
            for (int j = 0; j < 8; j++) {
                if (p > size) break;
                data[p] = tmp[j];
                p++;
            }
            idx++;
        }

        try {
            FileWriter out = new FileWriter("BobReceivedCryptogram.txt");
            StringBuilder res = new StringBuilder();
            for (byte b : data) {
                res.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            }
            out.write(res.toString());
            out.close();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        ArrayList<byte[]> decrypted = DES.decryptBytes(data, getKey(keyPath));
        size = (decrypted.size() - 1) * 8 + decrypted.get(decrypted.size() - 1).length;
        data = new byte[size];
        p = 0;
        for (byte[] tmp : decrypted) {
            for (byte b : tmp) {
                data[p] = b;
                p++;
            }
        }

        int newSize = channelType == 1 ? size - bytes_to_delete : size;
        byte[] newData = new byte[newSize];
        if (channelType == 1) {
            System.arraycopy(data, bytes_to_delete, newData, 0, newData.length);
        }
        else {
            System.arraycopy(data, 0, newData, 0, data.length);
        }

        writeBytes(newData, path);
    }
}
