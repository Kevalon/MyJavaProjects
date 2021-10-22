package com.Novikov.task3;

import java.math.BigInteger;
import java.util.ArrayList;

public class GeneratePrime {

    private static final long[] p16 = {-1, 1, 2, 5, 11, 17, 37, 67, 131, 257, 521, 1031,
            2053, 4099, 8209, 16411, 32771, 65537};

    private static BigInteger generatePrime16(int t) {
        long upperLimit = (long) Math.pow(2, t) - 1;
        long lowerLimit = (long) Math.pow(2, t - 1);
        long res;
        BigInteger ans = BigInteger.TEN;
        while (!MillerRabinTest.test(ans, 16)) {
            res = (long) ((Math.random() * (upperLimit - lowerLimit)) + lowerLimit);
            ans = BigInteger.valueOf(res);
        }
        return ans;
    }

    // Генерация p с t >= 17 битов
    public static BigInteger generate (int t) {

        if (t < 17) return generatePrime16(t);

        // Начальные значения
        long x0 = (long) ((Math.random() * (((long) Math.pow(2, 16)) - 1)) + 1);
        long c = (long) ((Math.random() * (((long) Math.pow(2, 16)) - 1)) + 1);
        if (c % 2 == 0) c++;

        // Шаг 1
        BigInteger y0 = BigInteger.valueOf(x0);

        // Шаг 2
        ArrayList<Integer> ts = new ArrayList<>();
        ts.add(t);
        while (ts.get(ts.size() - 1) >= 17) {
            ts.add(ts.get(ts.size() - 1) / 2);
        }
        int s = ts.size() - 1;

        // Шаг 3
        BigInteger ps = BigInteger.valueOf(p16[ts.get(s)]);

        // Шаг 4
        int m = s - 1;

        // Массив, содержащий числа pi длины ti
        BigInteger[] p = new BigInteger[s + 1];
        p[s] = ps;

        BigInteger pm;
        do {
            // Шаг 5
            int rm = ts.get(m + 1) / 16;
            if (ts.get(m + 1) % 16 != 0) {
                rm++;
            }

            do {
                // Шаг 6
                ArrayList<BigInteger> Y = new ArrayList<>();
                Y.add(y0);
                for (int i = 0; i < rm; i++) {
                    Y.add(BigInteger.valueOf(19381).multiply(Y.get(i)).add(BigInteger.valueOf(c))
                            .mod(BigInteger.valueOf((long) Math.pow(2, 16))));
                }

                // Шаг 7
                BigInteger Ym = BigInteger.ZERO;
                for (int i = 0; i < rm; i++) {
                    Ym = Ym.add(Y.get(i).multiply(BigInteger.TWO.pow(16 * i)));
                }

                // Шаг 8
                y0 = Y.get(rm);

                // Шаг 9
                BigInteger N = BigInteger.valueOf((long) Math.pow(2, ts.get(m) - 1)).divide(p[m + 1]);
                if (!BigInteger.valueOf((long) Math.pow(2, ts.get(m) - 1)).mod(p[m + 1]).equals(BigInteger.ZERO)) {
                    N = N.add(BigInteger.ONE);
                }
                N = N.add(BigInteger.valueOf((long) Math.pow(2, ts.get(m) - 1)).multiply(Ym)
                        .divide(p[m + 1].multiply(BigInteger.valueOf((long) Math.pow(2, 16 * rm)))));

                if (N.mod(BigInteger.TWO).equals(BigInteger.ONE)) {
                    N = N.add(BigInteger.ONE);
                }

                // Шаг 10
                int k = 0;

                while (true) {
                    // Шаг 11
                    BigInteger tmp = p[m + 1].multiply(N.add(BigInteger.valueOf(k)));
                    pm = tmp.add(BigInteger.ONE);

                    // Шаг 12
                    if (pm.compareTo(BigInteger.valueOf((long) Math.pow(2, ts.get(m)))) > 0) {
                        break;
                    }

                    // Шаг 13
                    boolean first = BigInteger.TWO.modPow(tmp, pm).equals(BigInteger.ONE);
                    boolean second = !BigInteger.valueOf((long) Math.pow(2, N.intValue() + k)).mod(pm)
                            .equals(BigInteger.ONE);

                    if (first && second) {
                        break;
                    }

                    k += 2;
                }
            } while (pm.compareTo(BigInteger.valueOf((long) Math.pow(2, ts.get(m)))) >= 1);
            p[m] = pm;

            // Шаг 14
            m--;
        } while (m >= 0);
        return p[0];
    }
}
