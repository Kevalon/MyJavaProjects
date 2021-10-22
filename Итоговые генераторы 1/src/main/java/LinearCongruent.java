import java.math.BigInteger;

public class LinearCongruent {

    public static void generate(int n, BigInteger a, BigInteger c, BigInteger m, BigInteger x0, BigInteger mod) {
        BigInteger xi = x0;

        for (int i = 0; i < n ; i++) {
            xi = ((a.multiply(xi)).add(c)).mod(m);
            if (!mod.equals(BigInteger.ZERO))
                xi = xi.mod(mod);
            System.out.println(xi);
        }
    }
}
