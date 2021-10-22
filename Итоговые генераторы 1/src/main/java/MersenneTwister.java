import java.math.BigInteger;
import java.util.ArrayList;

public class MersenneTwister {

    public static BigInteger or(BigInteger A, BigInteger B, int r, int w) {
        String ABit = A.toString(2);
        String BBit = B.toString(2);
        while (ABit.length() < w) ABit = '0' + ABit;
        while (BBit.length() < r) BBit = '0' + BBit;
        String resStr = ABit.substring(ABit.length() - w, ABit.length() - r);
        resStr += BBit.substring(BBit.length() - r);
        return new BigInteger(resStr, 2);
    }

    public static BigInteger Z(BigInteger X, int u, int w, int s, BigInteger b, BigInteger c, int t, int l) {
        BigInteger Y = (X.xor(X.shiftRight(u))).mod(BigInteger.TWO.pow(w));
        Y = Y.xor(Y.shiftLeft(s).and(b));
        Y = Y.xor(Y.shiftLeft(t).and(c));
        return (Y.xor(Y.shiftRight(l))).mod(BigInteger.TWO.pow(w));
    }

    public static void generate(int n, ArrayList<BigInteger> X, BigInteger mod) {
        int w = 32;
        int r = 31;
        int q = 397;
        BigInteger a = new BigInteger("2567483615");
        int u = 11;
        int s = 7;
        int t = 15;
        int l = 18;
        BigInteger b = new BigInteger("2636928640");
        BigInteger c = new BigInteger("4022730752");

        for (int i = 0; i < n; i++) {
            BigInteger curX = X.get(q).xor(or(X.get(0), X.get(1), r, w).shiftRight(1));
            curX = curX.xor(a.multiply(or(X.get(0), X.get(1), r, w).mod(BigInteger.TWO)))
                    .mod(BigInteger.TWO.pow(w));
            X.add(curX);
            X.remove(0);
            BigInteger res = Z(curX, u, w, s, b, c, t, l);
            if (!mod.equals(BigInteger.ZERO))
                res = res.mod(mod);
            System.out.println(res);
        }
    }
}
