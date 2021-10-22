import java.math.BigInteger;
import java.util.ArrayList;

public class Additive {

    public static void generate(int n, ArrayList<BigInteger> a, ArrayList<BigInteger> x,
                                BigInteger c, BigInteger m, BigInteger module) {

        for (int i = 0; i < n ; i++) {
            BigInteger sum = BigInteger.valueOf(0);
            for (int j = 0; j < a.size(); j++) {
                BigInteger tmp1 = a.get(j);
                BigInteger tmp2 = x.get(i + j);
                BigInteger tmp3 = tmp1.multiply(tmp2);
                sum = sum.add(tmp3);
            }
            BigInteger res = sum.add(c).mod(m);
            if (!module.equals(BigInteger.ZERO)) {
                res = res.mod(module);
            }
            System.out.println(res);
            x.add(res);
        }
    }
}
