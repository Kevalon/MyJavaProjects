package com.Utils;

import java.math.BigInteger;
import java.util.ArrayList;

public abstract class EllipticCurveOperations {

    private static final BigInteger[] INFINITY = {BigInteger.TWO.negate(), BigInteger.TWO.negate()};

    public static BigInteger[] pointsSum (BigInteger x1, BigInteger y1, BigInteger x2, BigInteger y2,
                                          ArrayList<BigInteger> as, BigInteger p) {
        if (x1.equals(INFINITY[0]) && y1.equals(INFINITY[1])) {
            return new BigInteger[] { x2, y2 };
        }
        if (x2.equals(INFINITY[0]) && y2.equals(INFINITY[1])) {
            return new BigInteger[] { x1, y1 };
        }

        BigInteger lambda, v, denominator;
        try {
            if (x1.equals(x2) && y1.equals(y2)) {
                denominator = BigInteger.TWO.multiply(y1).add(as.get(0).multiply(x1)).add(as.get(2)).mod(p)
                        .modInverse(p);
                lambda = BigInteger.valueOf(3).multiply(x1.pow(2)).add(BigInteger.TWO.multiply(as.get(1)).multiply(x1))
                        .add(as.get(3)).subtract(as.get(0).multiply(y1)).mod(p);
                lambda = lambda.multiply(denominator).mod(p);
                v = x1.pow(3).negate().add(as.get(3).multiply(x1)).add(BigInteger.TWO.multiply(as.get(4)))
                        .subtract(as.get(2).multiply(y1)).mod(p);
                v = v.multiply(denominator).mod(p);
            }
            else {
                BigInteger check = x2.subtract(x1).mod(p);
                if (check.equals(BigInteger.ZERO)) {
                    return INFINITY;
                }

                denominator = check.modInverse(p);
                lambda = y2.subtract(y1).mod(p).multiply(denominator).mod(p);
                v = y1.multiply(x2).subtract(y2.multiply(x1)).mod(p).multiply(denominator).mod(p);
            }
        }
        catch (ArithmeticException ex) {
            System.out.println(ex.getMessage());
            return null;
        }

        BigInteger x3 = lambda.pow(2).add(as.get(0).multiply(lambda)).subtract(as.get(1)).subtract(x1).subtract(x2)
                .mod(p);
        BigInteger y3 = x3.multiply(lambda.add(as.get(0))).negate().subtract(v).subtract(as.get(2)).mod(p);

        return new BigInteger[] {x3, y3};
    }

    public static BigInteger[] getNegative(BigInteger x0, BigInteger y0, ArrayList<BigInteger> as, BigInteger p) {
        return new BigInteger[]{x0, y0.negate().subtract(as.get(0).multiply(x0)).subtract(as.get(2)).mod(p)};
    }
}
