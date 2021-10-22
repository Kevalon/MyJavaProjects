import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.*;

public class Alice {

    private static final String IpAddr = "192.168.1.12";
    private static final String socket = "42398";
    private static final String MAC = "00:26:57:00:1F:02";

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

    public static ArrayList<byte[]> sendBytes (int channelType, String filepath, String keyPath) {
        byte[] data = null;
        try {
            Path path = Paths.get(filepath);
            data = Files.readAllBytes(path);
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        ArrayList<byte[]> res = new ArrayList<>();
        if (channelType == 1) {
            byte[] tmp = (MAC + ':' + IpAddr + ':' + socket + " ").getBytes();
            byte[] cipher = new byte[data.length + tmp.length];
            System.arraycopy(tmp, 0, cipher, 0, tmp.length);
            System.arraycopy(data, 0, cipher, tmp.length, data.length);
            res = DES.encryptBytes(cipher, getKey(keyPath));
        }
        if (channelType == 2) {
            ArrayList<byte[]> data1 = DES.encryptBytes(data, getKey(keyPath));
            byte[] tmp = (MAC + ':' + IpAddr + ':' + socket + " ").getBytes();
            res = data1;
            res.add(0, tmp);
        }
        return res;
    }
}
