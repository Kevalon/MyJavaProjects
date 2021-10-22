import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Eva {

    private static final String IpAddr = "192.168.1.11";
    private static final String socket = "52974";
    private static final String MAC = "0F:01:DD:66:82:AA";

    public static void intercept (String bitSeq) {
        try {
            FileWriter out = new FileWriter("EvaIntercepted.txt", true);
            out.append("Перехваченная битовая последовательность:\n");
            out.append(bitSeq);
            out.append("\n\n");

            String[] letters = bitSeq.split("(?<=\\G.{8})");
            int bytes_to_delete = (MAC + ':' + IpAddr + ':' + socket + " ").getBytes().length;
            byte[] ASCIIConvert = new byte[bytes_to_delete];
            for (int i = 0; i < bytes_to_delete; i++) {
                int x = Integer.parseInt(letters[i], 2);
                ASCIIConvert[i] = (byte) x;
            }
            ArrayList<Byte> byteSeq = new ArrayList<>();
            for (int i = bytes_to_delete; i < letters.length; i++) {
                int x = Integer.parseInt(letters[i], 2);
                byteSeq.add((byte) x);
            }

            out.append("Перехваченная служебная информация:\n");
            out.append(new String(ASCIIConvert, StandardCharsets.UTF_8));
            out.append("\n");

            out.append("Перехваченная байтовая последовательность:\n");
            int cnt = 0;
            for (byte b : byteSeq) {
                if (cnt > 80) {
                    out.append("\n");
                    cnt = 0;
                }
                out.append((char) b);
                cnt++;
            }
            out.append("\n\n");
            out.close();
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void clearFile () {
        try {
            FileWriter out = new FileWriter("EvaIntercepted.txt");
            out.write("");
            out.close();
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void interceptBytes (ArrayList<byte[]> bytes) {
        StringBuilder res = new StringBuilder();
        for (byte[] bs : bytes) {
            for (byte b : bs) {
                String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF))
                        .replace(' ', '0');
                res.append(s1);
            }
        }
        intercept(res.toString());
    }
}
