import java.math.BigInteger;

public class RSAGen {

    public static void generate(int c, BigInteger x0, int l, int w, BigInteger e, BigInteger n, BigInteger mod) {
        int requiredBitAmount = l * c;

        BigInteger xCur = x0;
        String bitStr = "";
        while (bitStr.length() < requiredBitAmount) {
            xCur = xCur.modPow(e, n);
            String xCurStr = xCur.toString(2);
            while (xCurStr.length() < w) {
                xCurStr = "0" + xCurStr;
            }
            if (xCurStr.length() > w) {
                xCurStr = new StringBuilder(xCurStr).reverse().toString();
                xCurStr = xCurStr.substring(0, w);
                xCurStr = new StringBuilder(xCurStr).reverse().toString();
            }
            bitStr += xCurStr;
        }
        for (int i = 0; i < c; i++) {
            String curNumbStr = bitStr.substring(0, l);
            bitStr = bitStr.substring(l);
            BigInteger res = new BigInteger(curNumbStr, 2);
            if (!mod.equals(BigInteger.ZERO))
                res = res.mod(mod);
            System.out.println(res);
        }
    }
}
