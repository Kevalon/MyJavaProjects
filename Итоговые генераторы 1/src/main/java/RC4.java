import java.math.BigInteger;

public class RC4 {

    public static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static void generate(int n, int w, int[] K, BigInteger module) {
        int[] S = new int[256];
        for (int i = 0; i < 256; i++) {
            S[i] = i;
        }
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + S[i] + K[i]) % 256;
            swap(S, i, j);
        }
        int i = 0;
        j = 0;
        int requiredAmountOfBits = n * w;
        int arSize = requiredAmountOfBits / 8 + 1;
        int[] X = new int[arSize];
        String bits = "";
        for (int t = 0; t < arSize; t++) {
            i = (i + 1) % 256;
            j = (j + S[i]) % 256;
            swap(S, i, j);
            X[t] = S[(S[i] + S[j]) % 256];
            String curBits = Integer.toBinaryString(X[t]);
            while (curBits.length() < 8) {
                curBits = "0" + curBits;
            }
            bits += curBits;
        }

        for (int it = 0; it < n; it++) {
            String curNum = bits.substring(0, w);
            bits = bits.substring(w);
            BigInteger res = new BigInteger(curNum, 2);
            if (!module.equals(BigInteger.ZERO))
                res = res.mod(module);
            System.out.println(res);
        }
    }
}
