package com.Novikov.task3;

import java.math.BigInteger;
import java.util.ArrayList;

public class PrimitiveNumber {

    public static BigInteger findPrimitive (BigInteger p, BigInteger start) {
        ArrayList<BigInteger> fact = new ArrayList<>();
        BigInteger phi = p.subtract(BigInteger.ONE);
        BigInteger n = phi;
        for (BigInteger i = BigInteger.TWO; n.compareTo(i.multiply(i)) > 0; i = i.add(BigInteger.ONE))
            if (n.mod(i).equals(BigInteger.ZERO)) {
                fact.add(i);
                while (n.mod(i).equals(BigInteger.ZERO))
                    n = n.divide(i);
            }
        if (n.compareTo(BigInteger.ONE) > 0)
            fact.add(n);

        for (BigInteger res = start; p.compareTo(res) > 0; res = res.add(BigInteger.ONE)) {
            boolean ok = true;
            for (int i = 0;  i < fact.size() && ok; ++i)
                ok = !res.modPow(phi.divide(fact.get(i)), p).equals(BigInteger.ONE);
            if (ok)  return res;
        }

        for (BigInteger res = BigInteger.TWO; p.compareTo(res) > 0; res = res.add(BigInteger.ONE)) {
            boolean ok = true;
            for (int i = 0;  i < fact.size() && ok; ++i)
                ok = !res.modPow(phi.divide(fact.get(i)), p).equals(BigInteger.ONE);
            if (ok)  return res;
        }
        return BigInteger.ONE.negate();
    }
}
