package com.Utils;

import java.math.BigInteger;
import java.util.Random;

public abstract class MillerRabinTest {

    public static BigInteger getRandomBigInteger (BigInteger minLimit, BigInteger maxLimit) {
        BigInteger bigInteger = maxLimit.subtract(minLimit);
        Random randNum = new Random();
        int len = maxLimit.bitLength();
        BigInteger res = new BigInteger(len, randNum);
        if (res.compareTo(minLimit) < 0)
            res = res.add(minLimit);
        if (res.compareTo(bigInteger) >= 0)
            res = res.mod(bigInteger).add(minLimit);
        return res;
    }

    // Тест Миллера-Рабина, n - тестируемое число, k - точность
    public static boolean test (BigInteger n, int k) {
        // Если n == 0 или n == 1 - эти числа не простые, возвращаем false
        if (n.equals(BigInteger.ZERO) || n.equals(BigInteger.ONE)) return false;
        // Если n == 2 или n == 3 - эти числа простые, возвращаем true
        if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3))) return true;
        // Если n четное - возвращаем false
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO))
            return false;
        // Представим n − 1 в виде (2^s)*t, где t нечётно, это можно сделать последовательным делением n - 1 на 2
        BigInteger t = n.subtract(BigInteger.ONE);
        BigInteger s = BigInteger.ZERO;
        while (t.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            t = t.divide(BigInteger.TWO);
            s = s.add(BigInteger.ONE);
        }
        // Повторить k раз
        for (int i = 0; i < k; i++) {
            // Выберем случайное целое число a в отрезке [2, n − 2]
            BigInteger a = getRandomBigInteger(BigInteger.TWO, n.subtract(BigInteger.TWO));
            // x = a^t(mod n)
            BigInteger x = a.modPow(t, n);
            // Если x == 1 или x == n − 1, то перейти на следующую итерацию цикла
            if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE)))
                continue;
            // Повторить s − 1 раз
            for (BigInteger r = BigInteger.ONE; !s.subtract(r).equals(BigInteger.ZERO);
                 r = r.add(BigInteger.ONE)) {
                // x = x^2(mod n)
                x = x.modPow(BigInteger.TWO, n);
                // Если x == 1, то вернуть false
                if (x.equals(BigInteger.ONE))
                    return false;
                // Если x == n − 1, то перейти на следующую итерацию внешнего цикла
                if (x.equals(n.subtract(BigInteger.ONE)))
                    break;
            }
            if (!x.equals(n.subtract(BigInteger.ONE)))
                return false;
        }
        // Вернуть true, число вероятно простое
        return true;
    }
}
