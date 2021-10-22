import java.math.BigInteger;
import java.util.ArrayList;

public class RSLOS {

    public static ArrayList<Integer> getX(BigInteger Yp, int p) {
        ArrayList<Integer> res = new ArrayList<>();
        String binNum = Yp.toString(2);
        while (binNum.length() < p) {
            binNum = "0" + binNum;
        }
        for (int i = 0; i < p; i++) {
            res.add(binNum.charAt(i) - '0');
        }
        return res;
    }

    public static void generate(int n, BigInteger Yp, int p, int[] j, int s, BigInteger module) {
        int maxsize = Math.max(p, s);
        ArrayList<Integer> x = getX(Yp, p);
        int[] a = new int[p];
        for (int el : j) {
            a[el - 1] = 1;
        }
        for (int i = 0; i < n; i++) {
            BigInteger res = BigInteger.valueOf(0);
            for (int k = 0; k < s; k++) {
                int curX = 0;
                int start = x.size() - p;
                for (int it = start; it < x.size(); it++)
                    curX = (curX ^ (a[it - start] & x.get(it))) % 2;
                x.add(curX);
                if (x.size() > maxsize) x.remove(0);
                if (curX == 1) res = res.setBit(s - 1 - k);
            }
            if (!module.equals(BigInteger.ZERO))
                res = res.mod(module);
            System.out.println(res.toString());
        }
    }
}
