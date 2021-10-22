import java.math.BigInteger;
import java.util.ArrayList;

public class FiveParams {

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

    public static void generate(int n, BigInteger Yp, int p, int q1, int q2, int q3, int w, BigInteger mod) {
        int maxsize = Math.max(p, w);
        ArrayList<Integer> x = getX(Yp, p);
        for (int i = 0; i < n; i++) {
            BigInteger res = BigInteger.valueOf(0);
            for (int j = 0; j < w; j++) {
                int curX = 0;
                curX = (curX ^ x.get(q1) ^ x.get(q2) ^ x.get(q3) ^ x.get(0)) % 2;
                x.add(curX);
                if (x.size() > maxsize) x.remove(0);
                if (curX == 1) res = res.setBit(w - 1 - j);
            }
            if (!mod.equals(BigInteger.ZERO))
                res = res.mod(mod);
            System.out.println(res.toString());
        }
    }
}
