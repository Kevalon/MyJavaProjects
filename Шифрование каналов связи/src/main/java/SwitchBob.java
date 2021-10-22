import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class SwitchBob {

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

    public static ArrayList<byte[]> decryptBytes(ArrayList<byte[]> bytes, String keyPath) {
        byte[] tmp = new byte[8 * bytes.size()];
        int p = 0;
        for (byte[] bs : bytes) {
            for (byte b : bs) {
                tmp[p] = b;
                p++;
            }
        }
        return DES.decryptBytes(tmp, getKey(keyPath));
    }

    public static ArrayList<byte[]> encryptBytes(ArrayList<byte[]> bytes, String keyPath) {
        byte[] tmp = new byte[8 * (bytes.size() - 1) + bytes.get(bytes.size() - 1).length];
        int p = 0;
        for (byte[] bs : bytes) {
            for (byte b : bs) {
                tmp[p] = b;
                p++;
            }
        }
        return DES.encryptBytes(tmp, getKey(keyPath));
    }
}
