package com.Main;

import com.Utils.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;

public class Main {

    private static boolean belongsToCurve(BigInteger x, BigInteger y, BigInteger a1, BigInteger a2, BigInteger a3,
                                          BigInteger a4, BigInteger a6, BigInteger p) {
        BigInteger left = y.pow(2).add(a1.multiply(x).multiply(y)).add(a3.multiply(y)).mod(p);
        BigInteger right = x.pow(3).add(a2.multiply(x.pow(2))).add(a4.multiply(x)).add(a6).mod(p);
        return right.equals(left);
    }

    private static HashMap<Integer, BigInteger[]> getQs(BigInteger[] Q, BigInteger k, ArrayList<BigInteger> as,
                                                        BigInteger p) {
        HashMap<Integer, BigInteger[]> ans = new HashMap<>();
        ans.put(1, Q);
        BigInteger[] curQ = Q;
        BigInteger[] minusQ = EllipticCurveOperations.getNegative(Q[0], Q[1], as, p);
        ans.put(-1, minusQ);
        for (BigInteger i = BigInteger.ONE; i.compareTo(k) < 0; i = i.add(BigInteger.ONE)) {
            curQ = EllipticCurveOperations.pointsSum(curQ[0], curQ[1], Q[0], Q[1], as, p);
            minusQ = EllipticCurveOperations.getNegative(curQ[0], curQ[1], as, p);
            ans.put(i.intValue() + 1, curQ);
            ans.put(i.negate().intValue() - 1, minusQ);
        }
        return ans;
    }

    private static BigInteger[] getPoint(@NotNull BigInteger[] Q, BigInteger k, ArrayList<BigInteger> as, BigInteger p) {
        BigInteger[] ans = Q;
        for (BigInteger i = BigInteger.ONE; i.compareTo(k) < 0; i = i.add(BigInteger.ONE)) {
            ans = EllipticCurveOperations.pointsSum(ans[0], ans[1], Q[0], Q[1], as, p);
        }
        return ans;
    }

    public static int getKeyByValue(HashMap<Integer, BigInteger[]> map, BigInteger[] value) {
        for (Map.Entry<Integer, BigInteger[]> entry : map.entrySet()) {
            if (Arrays.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return 0;
    }

    private static BigInteger[] getResult(BigInteger[] R, BigInteger[] P, BigInteger k, ArrayList<BigInteger> coeffs,
                                          BigInteger p, HashMap<Integer, BigInteger[]> db, BigInteger q) {
        int check = getKeyByValue(db, R);
        if (check != 0) return new BigInteger[] { BigInteger.ZERO, BigInteger.valueOf(check) };
        else {
            BigInteger[] curP = P;
            BigInteger[] minusP;
            for (BigInteger i = BigInteger.ONE; i.compareTo(k) < 1; i = i.add(BigInteger.ONE)) {
                if (i.compareTo(BigInteger.ONE) > 0) {
                    curP = EllipticCurveOperations.pointsSum(curP[0], curP[1], P[0], P[1], coeffs, p);
                }
                minusP = EllipticCurveOperations.getNegative(curP[0], curP[1], coeffs, p);
                check = getKeyByValue(db, EllipticCurveOperations.pointsSum(R[0], R[1], minusP[0],
                        minusP[1], coeffs, p));
                if (check != 0) return new BigInteger[] {i.negate().multiply(BigInteger.TWO.multiply(k)
                        .add(BigInteger.ONE)), BigInteger.valueOf(check) };
                check = getKeyByValue(db, EllipticCurveOperations.pointsSum(R[0], R[1], curP[0], curP[1], coeffs, p));
                if (check != 0) return new BigInteger[] { i.multiply(BigInteger.TWO.multiply(k).add(BigInteger.ONE)),
                                                           BigInteger.valueOf(check) };
            }
        }
        return new BigInteger[]{BigInteger.ONE.negate(), BigInteger.ONE.negate(), BigInteger.ONE.negate()};
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        BigInteger[] curve;
        try {
            curve = EC1728.getCurve(12, 15);
        } catch (Exception ex) {
            curve = EC1728.getCurve(12, 15);
        }
        System.out.println("p = " + curve[0].toString() + " a4 = " + curve[1].toString());

        BigInteger p;
        System.out.println("Введите характеристику поля p (p - простое, p > 5)");
        try {
            p = in.nextBigInteger();
            if (p.compareTo(BigInteger.valueOf(6)) < 0 || !MillerRabinTest.test(p, 16)) {
                throw new Exception();
            }
        } catch (Exception ex) {
            System.out.println("p должно быть простым целым > 5");
            return;
        }

        int n;
        System.out.println("Введите n (n > 0)");
        try {
            n = in.nextInt();
            if (n < 1) {
                throw new Exception();
            }
        } catch (Exception ex) {
            System.out.println("n должно быть целым > 0");
            return;
        }

        BigInteger q = p.pow(n);
        System.out.println("q = " + q.toString());

        BigInteger a1, a2, a3, a4, a6;
        ArrayList<BigInteger> as = new ArrayList<>();
        System.out.println("Введите коэффициенты уравнения кривой в порядке: a1, a2, a3, a4, a6");
        try {
            a1 = in.nextBigInteger();
            a2 = in.nextBigInteger();
            a3 = in.nextBigInteger();
            a4 = in.nextBigInteger();
            a6 = in.nextBigInteger();
            as.add(a1);
            as.add(a2);
            as.add(a3);
            as.add(a4);
            as.add(a6);
            for (BigInteger ai : as) {
                if (ai.compareTo(BigInteger.ZERO) < 0 || ai.compareTo(p) > -1) {
                    throw new IllegalArgumentException();
                }
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Все коэффициенты должны быть в интервале от 0 до p - 1");
            return;
        }
        in.close();

        BigInteger x0 = MillerRabinTest.getRandomBigInteger(BigInteger.ZERO, p);
        BigInteger y0 = MillerRabinTest.getRandomBigInteger(BigInteger.ZERO, p);
        while (!belongsToCurve(x0, y0, a1, a2, a3, a4, a6, p)) {
            x0 = MillerRabinTest.getRandomBigInteger(BigInteger.ZERO, p);
            y0 = MillerRabinTest.getRandomBigInteger(BigInteger.ZERO, p);
        }

        BigInteger[] Q = new BigInteger[]{x0, y0};

        System.out.println("Q = (x0, y0) = (" + x0.toString() + ", " + y0.toString() + ")");

        MathContext mc = MathContext.DECIMAL128;
        BigDecimal sqrtTmp = new BigDecimal(q.multiply(BigInteger.TWO));
        BigInteger k = sqrtTmp.sqrt(mc).sqrt(mc).toBigInteger();
        System.out.println("k = " + k.toString());

        HashMap<Integer, BigInteger[]> Qs = getQs(Q, k, as, p);

        BigInteger[] P = getPoint(Q, k.multiply(BigInteger.TWO).add(BigInteger.ONE), as, p);
        BigInteger[] R = getPoint(Q, q.add(BigInteger.ONE), as, p);
        System.out.println("P = (" + P[0].toString() + ", " + P[1].toString() + ")");
        System.out.println("R = (" + R[0].toString() + ", " + R[1].toString() + ")");

        BigInteger[] dkE = getResult(R, P, k, as, p, Qs, q);

        System.out.println("dk = " + dkE[0].toString());
        System.out.println("e = " + dkE[1].toString());
        BigInteger N = q.add(BigInteger.ONE).add(dkE[0]).subtract(dkE[1]);
        System.out.println("N = " + N.toString());
    }
}
