package com.main;

import com.algs.GelfondShanks;
import com.algs.RoMethod;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("Выберите алгоритм:");
        System.out.println("1. Метод Гельфонда-Шенкса.");
        System.out.println("2. ро-метод Полларда.");
        int response;
        Scanner in = new Scanner(System.in);

        Set<BigInteger> set = new HashSet<>();
        for (int i = 0; i < 17; i++) {
            BigInteger res = BigInteger.valueOf(3).modPow(BigInteger.valueOf(i), BigInteger.valueOf(17));
            set.add(res);
            System.out.println(i + ": " + res.toString());
        }
        System.out.println(set.size());

        response = in.nextInt();
        System.out.println("Введите количество элементов в группе");
        if (response == 1) {
            BigInteger B = in.nextBigInteger();
            System.out.println("Введите а:");
            BigInteger a = in.nextBigInteger();
            System.out.println("Введите b:");
            BigInteger b = in.nextBigInteger();
            System.out.println("x = " + GelfondShanks.getLog(a, B, b).toString());
        } else if (response == 2) {
            BigInteger m = in.nextBigInteger();
            System.out.println("Введите а:");
            BigInteger a = in.nextBigInteger();
            System.out.println("Введите b:");
            BigInteger b = in.nextBigInteger();
            System.out.println("Введите epsilon:");
            double eps = in.nextDouble();
            BigInteger test = RoMethod.getLog(m, a, b, eps);
            if (!test.equals(BigInteger.ONE.negate()))
                System.out.println("x = " + test.toString());
        }
    }
}


