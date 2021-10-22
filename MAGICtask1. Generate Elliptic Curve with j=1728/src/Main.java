import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static ArrayList<Double> Qx = new ArrayList<>();
    private static ArrayList<Double> Qy = new ArrayList<>();

    private static BigInteger modPolynomial (ArrayList<BigInteger> first, ArrayList<BigInteger> second, BigInteger q) {
        while (first.size() >= second.size()) {
            BigInteger coeff = first.get(first.size() - 1);
            first.remove(first.size() - 1);
            for (int i = 0; i < 2; i++) {
                int ind = first.size() - 1 - i;
                first.set(ind, first.get(ind).subtract(second.get(second.size() - 2 - i).multiply(coeff)));
            }
        }

        return first.get(0).mod(q);
    }

    private static boolean nonResidue(BigInteger a, BigInteger p) {
        BigInteger tmp = p.subtract(BigInteger.ONE);
        return a.modPow(tmp.divide(BigInteger.TWO), p).equals(tmp);
    }

    private static boolean residue(BigInteger a, BigInteger p) {
        return a.modPow(p.subtract(BigInteger.ONE).divide(BigInteger.TWO), p).equals(BigInteger.ONE);
    }

    private static BigInteger step22(BigInteger q) {
        BigInteger b = BigInteger.ZERO;
        while (!nonResidue(b.pow(2).add(BigInteger.valueOf(4)), q)) {
            b = b.add(BigInteger.ONE);
        }

        ArrayList<BigInteger> fy = new ArrayList<>();
        fy.add(BigInteger.ONE.negate());
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

    private static BigInteger[] step2(BigInteger p) {
        BigInteger u = step22(p);
        if (u.equals(BigInteger.ZERO))
            u = p;

        int i = 0;
        ArrayList<BigInteger> us = new ArrayList<>();
        us.add(u);
        ArrayList<BigInteger> ms = new ArrayList<>();
        ms.add(p);

        while (true) {
            ms.add(us.get(i).pow(2).add(BigInteger.ONE).divide(ms.get(i)));
            us.add(us.get(i).mod(ms.get(i + 1)).min(ms.get(i + 1).subtract(us.get(i).mod(ms.get(i + 1)))));
            if (us.get(us.size() - 1).equals(BigInteger.ZERO))
                us.set(us.size() - 1, p);

            if (ms.get(i + 1).equals(BigInteger.ONE)) {
                break;
            }
            i++;
        }

        BigInteger[] as = new BigInteger[i + 1];
        BigInteger[] bs = new BigInteger[i + 1];
        as[i] = us.get(i);
        bs[i] = BigInteger.ONE;

        while (i != 0) {
            BigInteger tmp = as[i].pow(2).add(bs[i].pow(2)).mod(p);
            if (us.get(i - 1).multiply(as[i]).add(bs[i]).mod(tmp).equals(BigInteger.ZERO))
                as[i - 1] = us.get(i - 1).multiply(as[i]).add(bs[i]).divide(tmp);
            else
                as[i - 1] = us.get(i - 1).negate().multiply(as[i]).add(bs[i]).divide(tmp);
            if (as[i].negate().add(us.get(i - 1).multiply(bs[i])).mod(tmp).equals(BigInteger.ZERO))
                bs[i - 1] = as[i].negate().add(us.get(i - 1).multiply(bs[i])).divide(tmp);
            else
                bs[i - 1] = as[i].negate().add(us.get(i - 1).negate().multiply(bs[i])).divide(tmp);
            i--;
        }

        as[0] = as[0].mod(p);
        as[1] = as[1].mod(p);

        return new BigInteger[] {as[i].mod(p), bs[i].mod(p)};
    }

    private static boolean step6(BigInteger N, BigInteger x1, BigInteger y1, BigInteger p, BigInteger A) {
        BigInteger lambda = x1.pow(2).multiply(BigInteger.valueOf(3)).add(A)
                .multiply(BigInteger.TWO.multiply(y1).modInverse(p)).mod(p);
        BigInteger x2 = lambda.pow(2).subtract(BigInteger.TWO.multiply(x1));
        if (x2.compareTo(BigInteger.ZERO) < 0) {
            BigInteger tmp = x2.negate().divide(p).add(BigInteger.ONE);
            x2 = x2.add(p.multiply(tmp)).mod(p);
        }
        x2 = x2.mod(p);
        BigInteger y2 = lambda.multiply(x1.subtract(x2)).subtract(y1).mod(p);
        BigInteger count = N.subtract(BigInteger.TWO);

        while (count.compareTo(BigInteger.ZERO) > 0) {
            BigInteger check = x2.subtract(x1).mod(p);
            if (check.mod(p).equals(BigInteger.ZERO)) {
                return count.compareTo(BigInteger.ONE) <= 0;
            }

            lambda = y2.subtract(y1).mod(p);
            lambda = lambda.multiply(check.modInverse(p)).mod(p);
            x2 = lambda.pow(2).subtract(x2).subtract(x1).mod(p);
            y2 = lambda.multiply(x1.subtract(x2)).subtract(y1).mod(p);

            count = count.subtract(BigInteger.ONE);
        }
        
        return false;
    }

    private static BigInteger[] step7(BigInteger Nr, BigInteger x1, BigInteger y1, BigInteger A, BigInteger p) {
        BigInteger lambda = x1.pow(2).multiply(BigInteger.valueOf(3)).add(A)
                .multiply(BigInteger.TWO.multiply(y1).modInverse(p)).mod(p);
        BigInteger x2 = lambda.pow(2).subtract(BigInteger.TWO.multiply(x1));
        if (x2.compareTo(BigInteger.ZERO) < 0) {
            BigInteger tmp = x2.negate().divide(p).add(BigInteger.ONE);
            x2 = x2.add(p.multiply(tmp)).mod(p);
        }
        x2 = x2.mod(p);
        BigInteger y2 = lambda.multiply(x1.subtract(x2)).subtract(y1);
        if (y2.compareTo(BigInteger.ZERO) < 0) {
            BigInteger tmp = y2.negate().divide(p).add(BigInteger.ONE);
            y2 = y2.add(p.multiply(tmp)).mod(p);
        }
        y2 = y2.mod(p);

        if (Nr.equals(BigInteger.TWO))
            return new BigInteger[] { x2, y2 };

        return step7(BigInteger.TWO, x2, y2, A, p);
    }

    private static void getQ(BigInteger N, BigInteger x1, BigInteger y1, BigInteger A, BigInteger p) {
        BigInteger lambda = x1.pow(2).multiply(BigInteger.valueOf(3)).add(A)
                .multiply(BigInteger.TWO.multiply(y1).modInverse(p)).mod(p);
        BigInteger x2 = lambda.pow(2).subtract(BigInteger.TWO.multiply(x1)).mod(p);
        Qx.add(x2.doubleValue() / p.longValue());
        BigInteger y2 = lambda.multiply(x1.subtract(x2)).subtract(y1).mod(p);
        Qy.add(y2.doubleValue() / p.longValue());
        BigInteger count = N.subtract(BigInteger.TWO);

        while (count.compareTo(BigInteger.ZERO) > 0) {
            BigInteger check = x2.subtract(x1);
            if (check.mod(p).equals(BigInteger.ZERO)) {
                return;
            }

            lambda = y2.subtract(y1).mod(p);
            lambda = lambda.multiply(check.modInverse(p)).mod(p);
            x2 = lambda.pow(2).subtract(x2).subtract(x1).mod(p);
            Qx.add(x2.doubleValue() / p.longValue());
            y2 = lambda.multiply(x1.subtract(x2)).subtract(y1).mod(p);
            y2 = y2.mod(p);
            Qy.add(y2.doubleValue() / p.longValue());

            count = count.subtract(BigInteger.ONE);
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Введите длину характеристики поля: l = ");
        int l;
        try {
            l = in.nextInt();
        }
        catch (Exception ex) {
            System.out.println("Вы ввели не целое число.");
            return;
        }
        if (l < 5) {
            System.out.println("l не может быть < 5.");
            return;
        }
        System.out.print("Введите максимальную степень расширения: m = ");
        int m;
        try {
            m = in.nextInt();
        }
        catch (Exception ex) {
            System.out.println("Вы ввели не целое число.");
            return;
        }
        if (m < 1) {
            System.out.println("m не может быть < 1.");
            return;
        }

        BigInteger p;
        BigInteger N;
        BigInteger r;
        BigInteger[] ab;

        do {
            do {
                p = GeneratePrime.generate(l);
            } while (!p.mod(BigInteger.valueOf(4)).equals(BigInteger.ONE));

            ab = step2(p);

            N = p.add(BigInteger.ONE);
            r = BigInteger.ZERO;
            BigInteger[] T = { ab[0].multiply(BigInteger.TWO).mod(p), ab[1].multiply(BigInteger.TWO).mod(p),
                    ab[0].multiply(BigInteger.TWO).negate(), ab[1].multiply(BigInteger.TWO).negate() };
            for (int i = 0; i < 4; i++) {
                BigInteger Ntmp = N.add(T[i]).mod(p);

                if (Ntmp.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                    r = Ntmp.divide(BigInteger.TWO);
                    if (MillerRabinTest.test(r, 14)) {
                        N = Ntmp;
                        break;
                    }
                    if (r.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                        r = r.divide(BigInteger.TWO);
                        if (MillerRabinTest.test(r, 14)) {
                            N = Ntmp;
                            break;
                        }
                    }
                }
            }

            if (p.equals(r) || r.equals(BigInteger.ZERO)) {
                r = BigInteger.ZERO;
                continue;
            }

            for (int i = 1; i <= m; i++) {
                if (p.modPow(BigInteger.valueOf(i), r).equals(BigInteger.ONE)) {
                    r = BigInteger.ZERO;
                    break;
                }
            }
        } while (r.equals(BigInteger.ZERO));

        System.out.println("p = " + p.toString());
        System.out.println("a = " + ab[0].toString() + ", b = " + ab[1].toString());
        System.out.println("N = " + N.toString());
        System.out.println("r = " + r.toString());

        BigInteger x0;
        BigInteger y0;
        BigInteger A;
        boolean working = true;
        boolean two = N.divide(r).equals(BigInteger.TWO);
        while (true) {
            x0 = MillerRabinTest.getRandomBigInteger(BigInteger.ONE, p);
            y0 = MillerRabinTest.getRandomBigInteger(BigInteger.ONE, p);
            A = y0.pow(2).subtract(x0.pow(3)).multiply(x0.modInverse(p)).mod(p);
            BigInteger minusA = A.negate().mod(p);
            if (two && nonResidue(minusA, p)) {
                working = false;
            }
            if (!two && residue(minusA, p)) {
                working = false;
            }

            if (!working && step6(N, x0, y0, p, A)) {
                break;
            }
            else working = true;
        }

        System.out.println("x0 = " + x0.toString());
        System.out.println("y0 = " + y0.toString());
        System.out.println("A = " + A.toString());


        BigInteger[] Q = step7(N.divide(r), x0, y0, A, p);
        System.out.println("Q = (" + Q[0].toString() + ", " + Q[1].toString() + ")");

        System.out.println("\nОтвет:");
        System.out.println("(p=" + p.toString() +", A=" + A.toString() +", Q=(" + Q[0].toString()
        + ", " + Q[1].toString() + "), r=" + r.toString() + ").");


        Qx.add(Q[0].doubleValue() / p.longValue());
        Qy.add(Q[1].doubleValue() / p.longValue());
        getQ(N, Q[0], Q[1], A, p);
        Graphic g = new Graphic(Qx, Qy);
        g.setVisible(true);
    }
}
