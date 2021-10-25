import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static BigInteger modPolynomial(ArrayList<BigInteger> first, ArrayList<BigInteger> second, BigInteger q) {
        while (first.size() >= second.size()) {
            BigInteger coeff = first.get(first.size() - 1);
            first.remove(first.size() - 1);
            for (int i = 0; i < 2; i++) {
                int ind = first.size() - 1 - i;
                first.set(ind, first.get(ind).subtract(second.get(second.size() - 2 - i).multiply(coeff)));
            }
        }

        while (first.get(0).compareTo(BigInteger.ZERO) < 0) {
            BigInteger tmp = first.get(0).negate().divide(q).add(BigInteger.ONE);
            first.set(0, first.get(0).add(q.multiply(tmp)).mod(q));
        }
        return first.get(0).mod(q);
    }

    private static boolean nonResidue(BigInteger a, BigInteger p) {
        BigInteger tmp = p.subtract(BigInteger.ONE);
        return a.modPow(tmp.divide(BigInteger.TWO), p).equals(tmp);
    }

    private static BigInteger step22(BigInteger a, BigInteger q) {
        BigInteger b = BigInteger.ZERO;
        BigInteger mayBeNegative;
        do {
            mayBeNegative = b.pow(2).subtract(BigInteger.valueOf(4).multiply(a));
            while (mayBeNegative.compareTo(BigInteger.ZERO) < 0) {
                mayBeNegative = mayBeNegative.add(q);
            }
            b = b.add(BigInteger.ONE);
        } while (!nonResidue(mayBeNegative, q));

        b = b.subtract(BigInteger.ONE);

        ArrayList<BigInteger> fy = new ArrayList<>();
        fy.add(a);
        fy.add(b.negate());
        fy.add(BigInteger.ONE);
        BigInteger limit = q.add(BigInteger.ONE).divide(BigInteger.TWO);
        ArrayList<BigInteger> x = new ArrayList<>();
        for (BigInteger i = BigInteger.ZERO; i.compareTo(limit) < 0; i = i.add(BigInteger.ONE)) {
            x.add(BigInteger.ZERO);
        }
        x.add(BigInteger.ONE);

        return modPolynomial(x, fy, q);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        BigInteger p, a;

        System.out.print("a = ");
        a = in.nextBigInteger();
        System.out.print("p = ");
        p = in.nextBigInteger();

        System.out.println("x = " + step22(a, p));
    }
}
