import java.math.BigInteger;

public class BBS {

    public static void generate(int c, BigInteger x0, int l, BigInteger n, BigInteger mod) {
        int maxBitLength = c * l;

        String resultBitStr = "";
        BigInteger xCur = x0;
        while (resultBitStr.length() < maxBitLength) {
            xCur = xCur.modPow(BigInteger.TWO, n);
            String xCurStr = xCur.toString(2);
            resultBitStr += xCurStr.substring(xCurStr.length() - 1);
        }
        for (int i = 0; i < c; i++) {
            String curStr = resultBitStr.substring(0, l);
            resultBitStr = resultBitStr.substring(l);
            BigInteger res = new BigInteger(curStr, 2);
            if (!mod.equals(BigInteger.ZERO))
                res = res.mod(mod);
            System.out.println(res);
        }
    }
}
