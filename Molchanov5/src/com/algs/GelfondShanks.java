package com.algs;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class GelfondShanks {
    private static List<BigInteger[]> getPairs(BigInteger a, BigInteger r, BigInteger m) {
        List<BigInteger[]> res = new ArrayList<>();
        for (BigInteger i = BigInteger.ZERO; i.compareTo(r) < 0; i = i.add(BigInteger.ONE)) {
            BigInteger[] tmp = new BigInteger[2];
            tmp[0] = i;
            tmp[1] = a.modPow(i, m);
            res.add(tmp);
        }

        res.sort(Comparator.comparing(o -> o[1]));
        return res;
    }

    private static BigInteger contains(List<BigInteger[]> pairs, BigInteger el) {
        for (BigInteger[] pair : pairs) {
            if (pair[1].equals(el)) {
                return pair[0];
            }
        }
        return BigInteger.ONE.negate();
    }

    public static BigInteger getLog(BigInteger a, BigInteger B, BigInteger b) {
        BigInteger r = new BigDecimal(B).sqrt(new MathContext(10)).add(BigDecimal.ONE).toBigInteger();
        List<BigInteger[]> pairs = getPairs(a, r, B);

        BigInteger a1 = a.modInverse(B).modPow(r, B);
        BigInteger a1i, k;
        List<BigInteger> kri = new ArrayList<>();
        for (BigInteger i = BigInteger.ZERO; i.compareTo(r) < 0; i = i.add(BigInteger.ONE)){
            a1i = a1.modPow(i, B);
            k = contains(pairs, a1i.multiply(b).mod(B));
            if (!k.equals(BigInteger.ONE.negate())) {
                kri.add(k.add(r.multiply(i)));
            }
        }

        BigInteger min = kri.get(0);

        for (int i = 1; i < kri.size(); i++) {
            if (kri.get(i).compareTo(min) < 0) {
                min = kri.get(i);
            }
        }

        return min;
    }
}
