import java.util.*;

public class DES {
    private static final int[] IP =
            { 58, 50, 42, 34, 26, 18, 10, 2,
              60, 52, 44, 36, 28, 20, 12, 4,
              62, 54, 46, 38, 30, 22, 14, 6,
              64, 56, 48, 40, 32, 24, 16, 8,
              57, 49, 41, 33, 25, 17,  9, 1,
              59, 51, 43, 35, 27, 19, 11, 3,
              61, 53, 45, 37, 29, 21, 13, 5,
              63, 55, 47, 39, 31, 23, 15, 7 };
    private static final int[] ExpansionTable =
            { 32,  1,  2,  3,  4,  5,
               4,  5,  6,  7,  8,  9,
               8,  9, 10, 11, 12, 13,
              12, 13, 14, 15, 16, 17,
              16, 17, 18, 19, 20, 21,
              20, 21, 22, 23, 24, 25,
              24, 25, 26, 27, 28, 29,
              28, 29, 30, 31, 32,  1 };
    private static final int[][] SBoxes =
            { { 14,  4, 13, 1,  2, 15, 11,  8,
            3, 10,  6, 12,  5,  9, 0,  7,
            0, 15,  7, 4, 14,  2, 13,  1,
            10,  6, 12, 11,  9,  5, 3,  8,
            4,  1, 14, 8, 13,  6,  2, 11,
            15, 12,  9,  7,  3, 10, 5,  0,
            15, 12,  8, 2,  4,  9,  1,  7,
            5, 11,  3, 14, 10,  0, 6, 13 },

            { 15,  1,  8, 14,  6, 11,  3,  4,
                    9, 7,  2, 13, 12, 0,  5, 10,
                    3, 13,  4,  7, 15,  2,  8, 14,
                    12, 0,  1, 10,  6, 9, 11,  5,
                    0, 14,  7, 11, 10,  4, 13,  1,
                    5, 8, 12,  6,  9, 3,  2, 15,
                    13,  8, 10,  1,  3, 15,  4,  2,
                    11, 6,  7, 12,  0, 5, 14,  9 },

            { 10,  0,  9, 14, 6,  3, 15,  5,
                    1, 13, 12,  7, 11,  4,  2,  8,
                    13,  7,  0,  9, 3,  4,  6, 10,
                    2,  8,  5, 14, 12, 11, 15,  1,
                    13,  6,  4,  9, 8, 15,  3,  0,
                    11,  1,  2, 12,  5, 10, 14,  7,
                    1, 10, 13,  0, 6,  9,  8,  7,
                    4, 15, 14,  3, 11,  5,  2, 12 },

            {  7, 13, 14, 3,  0,  6,  9, 10,
                    1, 2, 8,  5, 11, 12,  4, 15,
                    13,  8, 11, 5,  6, 15,  0,  3,
                    4, 7, 2, 12,  1, 10, 14,  9,
                    10,  6,  9, 0, 12, 11,  7, 13,
                    15, 1, 3, 14,  5,  2,  8,  4,
                    3, 15,  0, 6, 10,  1, 13,  8,
                    9, 4, 5, 11, 12,  7,  2, 14 },

            {  2, 12,  4,  1,  7, 10, 11,  6,
                    8,  5,  3, 15, 13, 0, 14,  9,
                    14, 11,  2, 12,  4,  7, 13,  1,
                    5,  0, 15, 10,  3, 9,  8,  6,
                    4,  2,  1, 11, 10, 13,  7,  8,
                    15,  9, 12,  5,  6, 3,  0, 14,
                    11,  8, 12,  7,  1, 14,  2, 13,
                    6, 15,  0,  9, 10, 4,  5,  3 },

            { 12,  1, 10, 15, 9,  2,  6,  8,
                    0, 13,  3,  4, 14,  7,  5, 11,
                    10, 15,  4,  2, 7, 12,  9,  5,
                    6,  1, 13, 14,  0, 11,  3,  8,
                    9, 14, 15,  5, 2,  8, 12,  3,
                    7,  0,  4, 10,  1, 13, 11,  6,
                    4,  3,  2, 12, 9,  5, 15, 10,
                    11, 14,  1,  7,  6,  0,  8, 13 },

            {  4, 11,  2, 14, 15, 0,  8, 13,
                    3, 12, 9,  7,  5, 10, 6,  1,
                    13,  0, 11,  7,  4, 9,  1, 10,
                    14,  3, 5, 12,  2, 15, 8,  6,
                    1,  4, 11, 13, 12, 3,  7, 14,
                    10, 15, 6,  8,  0,  5, 9,  2,
                    6, 11, 13,  8,  1, 4, 10,  7,
                    9,  5, 0, 15, 14,  2, 3, 12 },

            { 13,  2,  8, 4,  6, 15, 11,  1,
                    10,  9,  3, 14,  5,  0, 12,  7,
                    1, 15, 13, 8, 10,  3,  7,  4,
                    12,  5,  6, 11,  0, 14,  9,  2,
                    7, 11,  4, 1,  9, 12, 14,  2,
                    0,  6, 10, 13, 15,  3,  5,  8,
                    2,  1, 14, 7,  4, 10,  8, 13,
                    15, 12,  9,  0,  3,  5,  6, 11 } };

    private static final int[] P =
            { 16,  7, 20, 21, 29, 12, 28, 17,
              1, 15, 23, 26,  5, 18, 31, 10,
              2,  8, 24, 14, 32, 27,  3,  9,
              19, 13, 30,  6, 22, 11,  4, 25 };

    private static final int[] finalPerm =
            { 40, 8, 48, 16, 56, 24, 64, 32,
              39, 7, 47, 15, 55, 23, 63, 31,
              38, 6, 46, 14, 54, 22, 62, 30,
              37, 5, 45, 13, 53, 21, 61, 29,
              36, 4, 44, 12, 52, 20, 60, 28,
              35, 3, 43, 11, 51, 19, 59, 27,
              34, 2, 42, 10, 50, 18, 58, 26,
              33, 1, 41,  9, 49, 17, 57, 25 };

    private static final int [] keyPerm =
            { 57, 49, 41, 33, 25, 17,  9,  8,
               1, 58, 50, 42, 34, 26, 18, 16,
              10,  2, 59, 51, 43, 35, 27, 24,
              19, 11,  3, 60, 52, 44, 36, 32,
              63, 55, 47, 39, 31, 23, 15, 40,
               7, 62, 54, 46, 38, 30, 22, 48,
              14,  6, 61, 53, 45, 37, 29, 56,
              21, 13,  5, 28, 20, 12,  4, 64 };

    private static final int[] shiftTable =
            { 1, 1, 2, 2, 2, 2, 2, 2,
              1, 2, 2, 2, 2, 2, 2, 1 };

    private static final int[] KIPerm =
            { 14, 17, 11, 24,  1,  5,  3, 28,
              15,  6, 21, 10, 23, 19, 12,  4,
              26,  8, 16,  7, 27, 20, 13,  2,
              41, 52, 31, 37, 47, 55, 30, 40,
              51, 45, 33, 48, 44, 49, 39, 56,
              34, 53, 46, 42, 50, 36, 29, 32 };

    private static ArrayList<byte[]> getByteBlocks(byte[] bytes) {
        int size = bytes.length % 8 == 0 ? bytes.length / 8 : bytes.length / 8 + 1;
        int capacity = bytes.length % 8 == 0 ? size + 1 : size;
        ArrayList<byte[]> res = new ArrayList<>(capacity);
        int p = 0;
        for (int i = 0; i < capacity - 1; i++) {
            byte[] tmp = new byte[8];
            System.arraycopy(bytes, p, tmp, 0, 8);
            res.add(tmp);
            p += 8;
        }
        int difference = bytes.length - p;
        if (difference == 0) {
            byte[] tmp = {(byte) 128, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
            res.add(tmp);
        }
        else {
            byte[] tmp = new byte[8];
            System.arraycopy(bytes, p, tmp, 0, difference);
            tmp[difference] = (byte)128;
            for (int i = difference + 1; i < 8; i++) {
                tmp[i] = (byte)0;
            }
            res.add(tmp);
        }
        return res;
    }

    private static String hexToBin(String hex) {
        HashMap<Character, String> conversionTable = new HashMap<>();
        conversionTable.put('0', "0000");        conversionTable.put('1', "0001");
        conversionTable.put('2', "0010");        conversionTable.put('3', "0011");
        conversionTable.put('4', "0100");        conversionTable.put('5', "0101");
        conversionTable.put('6', "0110");        conversionTable.put('7', "0111");
        conversionTable.put('8', "1000");        conversionTable.put('9', "1001");
        conversionTable.put('A', "1010");        conversionTable.put('B', "1011");
        conversionTable.put('C', "1100");        conversionTable.put('D', "1101");
        conversionTable.put('E', "1110");        conversionTable.put('F', "1111");
        conversionTable.put('a', "1010");        conversionTable.put('b', "1011");
        conversionTable.put('c', "1100");        conversionTable.put('d', "1101");
        conversionTable.put('e', "1110");        conversionTable.put('f', "1111");

        StringBuilder ans = new StringBuilder();
        int p = 0;
        while (p < hex.length()) {
            ans.append(conversionTable.get(hex.charAt(p)));
            p++;
        }
        return ans.toString();
    }

    private static String permutation(String bin, int[] arr, int n) {
        StringBuilder per = new StringBuilder();
        for (int i = 0; i < n; i++) {
            per.append(bin.charAt(arr[i] - 1));
        }
        return per.toString();
    }

    private static String shift(String shiftMe, int cnt) {
        return shiftMe.substring(cnt) + shiftMe.substring(0, cnt);
    }

    private static String xor(String a, String b) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == b.charAt(i))
                ans.append("0");
            else ans.append("1");
        }
        return ans.toString();
    }

    private static String expandKey(String key) {
        StringBuilder expanded = new StringBuilder(key);
        int p = 0;
        int sum;
        for (int i = 0; i < 8; i++) {
            sum = 0;
            for (int j = 0; j < 7; j++) {
                sum += expanded.charAt(p) - '0';
                p++;
            }
            if (i != 7) {
                if (sum % 2 == 0)
                    expanded = new StringBuilder(expanded.substring(0, p) + "1" + expanded.substring(p));
                else expanded = new StringBuilder(expanded.substring(0, p) + "0" + expanded.substring(p));
            }
            else {
                if (sum % 2 == 0)
                    expanded.append("1");
                else expanded.append("0");
            }
            p++;
        }
        return expanded.toString();
    }

    private static String shortenKey(String key) {
        String shortened = key;
        for (int i = 0; i < 7; i++) {
            shortened = shortened.substring(0, i * 7 + 7) + shortened.substring(i * 7 + 8);
        }
        return shortened.substring(0, shortened.length() - 1);
    }

    private static String[] getKI(String hexKey) {
        String binKey = hexToBin(hexKey);
        binKey = expandKey(binKey);
        binKey = permutation(binKey, keyPerm, 64);
        binKey = shortenKey(binKey);
        String Ci = binKey.substring(0, 28);
        String Di = binKey.substring(28);
        String[] ki = new String[16];
        for (int i = 0; i < 16; i++) {
            Ci = shift(Ci, shiftTable[i]);
            Di = shift(Di, shiftTable[i]);
            String k = permutation(Ci + Di, KIPerm, 48);
            ki[i] = k;
        }
        return ki;
    }

    private static byte[] encryptByteBlock(byte[] bytes, String[] keys) {
        StringBuilder tmp = new StringBuilder();
        for (byte b : bytes) {
            tmp.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        String encrypted = encryptBlock(tmp.toString(), keys);
        byte[] res = new byte[8];
        for (int i = 0; i < 8; i++) {
            if (encrypted.length() > 8) {
                int x = Integer.parseInt(encrypted.substring(0, 8), 2);
                res[i] = (byte) x;
                encrypted = encrypted.substring(8);
            }
            else {
                int x = Integer.parseInt(encrypted, 2);
                res[i] = (byte) x;
            }
        }
        return res;
    }

    private static String encryptBlock(String text, String[] keys) {
        String binText = permutation(text, IP, 64);
        String L = binText.substring(0, 32);
        String R = binText.substring(32);

        for (int i = 0; i < 16; i++) {
            String Ri = permutation(R, ExpansionTable, 48);
            String xored = xor(keys[i], Ri);
            StringBuilder res = new StringBuilder();
            int rowInd, colInd, val;
            for (int j = 0; j < 8; j++) {
                rowInd = 2 * (xored.charAt(j * 6) - '0') + (xored.charAt(j * 6 + 5) - '0');
                colInd = 8 * (xored.charAt(j * 6 + 1) - '0') + 4 * (xored.charAt(j * 6 + 2) - '0')
                        + 2 * (xored.charAt(j * 6 + 3) - '0') + (xored.charAt(j * 6 + 4) - '0');
                val = SBoxes[j][rowInd * 16 + colInd];
                String cur = Integer.toBinaryString(val);
                cur = String.format("%4s", cur).replaceAll(" ", "0");
                res.append(cur);
            }
            res = new StringBuilder(permutation(res.toString(), P, 32));
            L = xor(res.toString(), L);
            if (i < 15) {
                String tmp = R;
                R = L;
                L = tmp;
            }
        }
        return permutation(L + R, finalPerm, 64);
    }

    public static ArrayList<byte[]> encryptBytes (byte[] bytes, String hexKey) {
        ArrayList<byte[]> blocks = getByteBlocks(bytes);
        String[] ki = getKI(hexKey);
        ArrayList<byte[]> cipher = new ArrayList<>();
        for (byte[] block : blocks) {
            cipher.add(encryptByteBlock(block, ki));
        }
        return cipher;
    }

    public static ArrayList<byte[]> decryptBytes(byte[] bytes, String hexKey) {
        ArrayList<byte[]> blocks = new ArrayList<>();
        int p = 0;
        for (int i = 0; i < bytes.length / 8; i++) {
            byte[] tmp = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmp[j] = bytes[p];
                p++;
            }
            blocks.add(tmp);
        }

        String[] ki = getKI(hexKey);
        for (int i = 0; i < ki.length / 2; i++) {
            String temp = ki[i];
            ki[i] = ki[ki.length - 1 - i];
            ki[ki.length - 1 - i] = temp;
        }

        ArrayList <byte[]> res = new ArrayList<>();
        for (byte[] block : blocks) {
            res.add(encryptByteBlock(block, ki));
        }
        boolean entire = true;
        int idx = -1;
        byte[] check = res.get(res.size() - 1);
        for (int i = 7; i >= 0; i--) {
            if (Byte.toUnsignedInt(check[i]) != 0) {
                entire = false;
                idx = i;
                break;
            }
        }
        if (entire) {
            res.remove(res.size() - 1);
        }
        else {
            byte[] tmp = new byte[idx];
            byte[] copy_me = res.get(res.size() - 1);
            System.arraycopy(copy_me, 0, tmp, 0, idx);
            res.remove(res.size() - 1);
            res.add(tmp);
        }

        return res;
    }
}
