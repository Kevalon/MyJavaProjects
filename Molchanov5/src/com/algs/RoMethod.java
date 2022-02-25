package com.algs;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

public abstract class RoMethod {
    private static final Set<BigInteger> U1 = new HashSet<>();
    private static final Set<BigInteger> U2 = new HashSet<>();
    private static final Map<BigInteger, BigInteger[]> triplets = new HashMap<>();

    private static void prepareU(BigInteger m) {
        BigInteger oneThird = m.divide(BigInteger.valueOf(3));
        BigInteger twoThirds = oneThird.multiply(BigInteger.TWO);
        for (BigInteger i = BigInteger.ONE; i.compareTo(m) < 0; i = i.add(BigInteger.ONE)) {
            if (i.compareTo(oneThird) < 0) {
                U1.add(i);
            } else if (i.compareTo(twoThirds) < 0) {
                U2.add(i);
            }
        }
    }

    private static BigInteger[] f(BigInteger a, BigInteger b, BigInteger m, BigInteger i) {
        BigInteger[] tripletPrev = triplets.get(i);
        BigInteger originalMod = m.add(BigInteger.ONE);
        BigInteger alpha, beta, y;
        if (U1.contains(tripletPrev[0])) {
            y = b.multiply(tripletPrev[0]).mod(originalMod);
            alpha = tripletPrev[1].mod(m);
            beta = tripletPrev[2].add(BigInteger.ONE).mod(m);
        } else if (U2.contains(tripletPrev[0])) {
            y = tripletPrev[0].pow(2).mod(originalMod);
            alpha = tripletPrev[1].multiply(BigInteger.TWO).mod(m);
            beta = tripletPrev[2].multiply(BigInteger.TWO).mod(m);
        } else {
            y = a.multiply(tripletPrev[0]).mod(originalMod);
            alpha = tripletPrev[1].add(BigInteger.ONE).mod(m);
            beta = tripletPrev[2].mod(m);
        }
        return new BigInteger[] {y, alpha, beta};
    }

    public static BigInteger getLog(BigInteger m, BigInteger a, BigInteger b, double eps) {
        MathContext mc = new MathContext(20);
        prepareU(m);
        BigInteger mSqrt = new BigDecimal(m).sqrt(mc).toBigInteger();
        BigInteger newMod = m.subtract(BigInteger.ONE);

        BigInteger k =
                BigDecimal.valueOf(2)
                        .multiply(new BigDecimal(mSqrt))
                        .multiply(BigDecimal.valueOf(
                                Math.log(BigDecimal.ONE.divide(new BigDecimal(eps), mc).doubleValue())))
                .sqrt(mc).toBigInteger()
                .add(BigInteger.ONE);

        BigInteger s = BigInteger.ZERO;
        //s = BigInteger.valueOf(6);
        while (true) {
            BigInteger i = BigInteger.ONE;
            s = s.add(BigInteger.ONE);
            //System.out.println(s.toString());
            triplets.clear();

            BigInteger[] y0 = new BigInteger[] {a.modPow(s, m), s, BigInteger.ZERO};
            triplets.put(BigInteger.ZERO, y0);
            BigInteger[] y1 = f(a, b, newMod, BigInteger.ZERO);
            triplets.put(BigInteger.ONE, y1);

            while (i.compareTo(k) < 0) {
                i = i.add(BigInteger.ONE);
                BigInteger[] yi = f(a, b, newMod, i.subtract(BigInteger.ONE));
                if (!triplets.containsKey(i)) {
                    triplets.put(i, yi);
                }
                BigInteger twoIMinusTwo = i.multiply(BigInteger.TWO).subtract(BigInteger.TWO);
                BigInteger twoIMinusOne = i.multiply(BigInteger.TWO).subtract(BigInteger.ONE);
                if (!triplets.containsKey(twoIMinusOne)) {
                    triplets.put(twoIMinusOne, f(a, b, newMod, twoIMinusTwo));
                }
                BigInteger[] y2i = f(a, b, newMod, twoIMinusOne);
                if (!triplets.containsKey(i.multiply(BigInteger.TWO))) {
                    triplets.put(i.multiply(BigInteger.TWO), y2i);
                }

                if (triplets.get(i)[0].equals(triplets.get(i.multiply(BigInteger.TWO))[0])) {
                    yi = triplets.get(i);
                    y2i = triplets.get(i.multiply(BigInteger.TWO));
                    BigInteger d = newMod.gcd(yi[2].subtract(y2i[2]).mod(newMod));
                    if (d.compareTo(mSqrt) > 0) break;
                    BigInteger left = y2i[1].subtract(yi[1]).mod(newMod);
                    BigInteger right = yi[2].subtract(y2i[2]).mod(newMod);
                    if (d.equals(BigInteger.ONE)) {
                        return left.multiply(right.modInverse(newMod)).mod(newMod);
                    } else {
                        for (BigInteger x = BigInteger.ONE;
                             d.compareTo(BigInteger.ZERO) > 0 && x.compareTo(m) < 0;
                             x = x.add(BigInteger.ONE)) {
                            if (left.equals(right.multiply(x).mod(newMod))) {
                                if (a.modPow(x, m).equals(b)) {
                                    return x;
                                }
                                d = d.subtract(BigInteger.ONE);
                            }
                        }
                    }
                }
            }
            if (s.equals(newMod)) {
                System.out.println("Значение x вычислить не удалось");
                return BigInteger.ONE.negate();
            }
        }
    }
}
