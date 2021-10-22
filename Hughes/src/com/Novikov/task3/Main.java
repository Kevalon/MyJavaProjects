package com.Novikov.task3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        int response;

        while (true) {
            System.out.println("Кто вы?");
            System.out.println("1. Алиса.");
            System.out.println("2. Боб.");
            response = in.nextInt();

            if (response == 1) {
                while (true) {
                    System.out.println("Выберите желаемое действие:");
                    System.out.println("1. Сгенерировать g и p.");
                    System.out.println("2. Выбрать x и сгенерировать k.");
                    System.out.println("3. Вычислить X.");
                    response = in.nextInt();

                    if (response == 1) {
                        System.out.println("Введите длину в битах чисел g и n");
                        int l = in.nextInt();
                        BigInteger n = GeneratePrime.generate(l);
                        BigInteger start, g;
                        do {
                            start = MillerRabinTest.getRandomBigInteger(BigInteger.ZERO, n.subtract(BigInteger.TWO));
                            g = PrimitiveNumber.findPrimitive(n, start);
                        } while (!MillerRabinTest.test(g, 16));

                        try (FileWriter out = new FileWriter("AliceGN.txt")) {
                            out.write(g.toString());
                            out.append("\n");
                            out.append(n.toString());
                        }
                        System.out.println("g и n были успешно записаны в файл AliceGN.txt.");
                    }

                    else if (response == 2) {
                        BigInteger g, n;
                        File gnInput = new File("AliceGN.txt");
                        if (!gnInput.exists() || gnInput.isDirectory()) {
                            System.out.println("Файл с g и n не найден.");
                            in.close();
                            return;
                        }
                        if (gnInput.length() == 0) {
                            System.out.println("Файл с g и n пуст.");
                            in.close();
                            return;
                        }

                        Scanner gnScan = new Scanner(gnInput);
                        g = gnScan.nextBigInteger();
                        n = gnScan.nextBigInteger();
                        gnScan.close();

                        System.out.println("Введите длину в битах числа x");
                        int l = in.nextInt();
                        BigInteger x = MillerRabinTest.getRandomBigInteger(BigInteger.TWO.pow(l - 1),
                                BigInteger.TWO.pow(l).subtract(BigInteger.ONE));
                        BigInteger k = g.modPow(x, n);

                        try (FileWriter out = new FileWriter("x.txt")) {
                            out.write(x.toString());
                        }
                        System.out.println("x было успешно записано в файл x.txt.");
                        try (FileWriter out = new FileWriter("k.txt")) {
                            out.write(k.toString());
                        }
                        System.out.println("k было успешно записано в файл k.txt.");
                        in.close();
                        return;
                    }

                    else if (response == 3) {
                        BigInteger g, n, Y, x;
                        File gnInput = new File("AliceGN.txt");
                        if (!gnInput.exists() || gnInput.isDirectory()) {
                            System.out.println("Файл с g и n не найден.");
                            in.close();
                            return;
                        }
                        if (gnInput.length() == 0) {
                            System.out.println("Файл с g и n пуст.");
                            in.close();
                            return;
                        }
                        File xInput = new File("x.txt");
                        if (!xInput.exists() || xInput.isDirectory()) {
                            System.out.println("Файл с x не найден.");
                            in.close();
                            return;
                        }
                        if (xInput.length() == 0) {
                            System.out.println("Файл с x пуст.");
                            in.close();
                            return;
                        }
                        File YInput = new File("AliceY.txt");
                        if (!YInput.exists() || YInput.isDirectory()) {
                            System.out.println("Файл с Y не найден.");
                            in.close();
                            return;
                        }
                        if (YInput.length() == 0) {
                            System.out.println("Файл с Y пуст.");
                            in.close();
                            return;
                        }

                        Scanner gnScan = new Scanner(gnInput);
                        g = gnScan.nextBigInteger();
                        n = gnScan.nextBigInteger();
                        gnScan.close();
                        Scanner YScan = new Scanner(YInput);
                        Y = YScan.nextBigInteger();
                        YScan.close();
                        Scanner xScan = new Scanner(xInput);
                        x = xScan.nextBigInteger();
                        xScan.close();

                        BigInteger X = Y.modPow(x, n);

                        try (FileWriter out = new FileWriter("AliceX.txt")) {
                            out.write(X.toString());
                        }
                        System.out.println("X было успешно записано в файл AliceX.txt.");
                        in.close();
                        return;
                    }

                    else {
                        System.out.println("Такой команды не существует.");
                    }
                }
            }

            else if (response == 2) {
                while (true) {
                    System.out.println("Выберите желаемое действие:");
                    System.out.println("1. Выбрать y и сгенерировать Y.");
                    System.out.println("2. Вычислить k'.");
                    response = in.nextInt();

                    if (response == 1) {
                        BigInteger g, n;
                        File gnInput = new File("BobGN.txt");
                        if (!gnInput.exists() || gnInput.isDirectory()) {
                            System.out.println("Файл с g и n не найден.");
                            in.close();
                            return;
                        }
                        if (gnInput.length() == 0) {
                            System.out.println("Файл с g и n пуст.");
                            in.close();
                            return;
                        }

                        Scanner gnScan = new Scanner(gnInput);
                        g = gnScan.nextBigInteger();
                        n = gnScan.nextBigInteger();
                        gnScan.close();

                        System.out.println("Введите длину в битах числа y");
                        int l = in.nextInt();
                        BigInteger y = MillerRabinTest.getRandomBigInteger(BigInteger.TWO.pow(l - 1),
                                BigInteger.TWO.pow(l).subtract(BigInteger.ONE));
                        BigInteger Y = g.modPow(y, n);

                        try (FileWriter out = new FileWriter("y.txt")) {
                            out.write(y.toString());
                        }
                        System.out.println("y было успешно записано в файл y.txt.");
                        try (FileWriter out = new FileWriter("BobY.txt")) {
                            out.write(Y.toString());
                        }
                        System.out.println("Y было успешно записано в файл BobY.txt.");
                        in.close();
                        return;
                    }

                    else if (response == 2) {
                        BigInteger g, n, y, X;
                        File gnInput = new File("BobGN.txt");
                        if (!gnInput.exists() || gnInput.isDirectory()) {
                            System.out.println("Файл с g и n не найден.");
                            in.close();
                            return;
                        }
                        if (gnInput.length() == 0) {
                            System.out.println("Файл с g и n пуст.");
                            in.close();
                            return;
                        }
                        File yInput = new File("y.txt");
                        if (!yInput.exists() || yInput.isDirectory()) {
                            System.out.println("Файл с y не найден.");
                            in.close();
                            return;
                        }
                        if (yInput.length() == 0) {
                            System.out.println("Файл с y пуст.");
                            in.close();
                            return;
                        }
                        File XInput = new File("BobX.txt");
                        if (!XInput.exists() || XInput.isDirectory()) {
                            System.out.println("Файл с X не найден.");
                            in.close();
                            return;
                        }
                        if (XInput.length() == 0) {
                            System.out.println("Файл с X пуст.");
                            in.close();
                            return;
                        }

                        Scanner gnScan = new Scanner(gnInput);
                        Scanner XScan = new Scanner(XInput);
                        Scanner yScan = new Scanner(yInput);
                        g = gnScan.nextBigInteger();
                        n = gnScan.nextBigInteger();
                        y = yScan.nextBigInteger();
                        X = XScan.nextBigInteger();
                        gnScan.close();
                        XScan.close();
                        yScan.close();

                        BigInteger z = y.modInverse(n.subtract(BigInteger.ONE));
                        BigInteger kHatch = X.modPow(z, n);

                        try (FileWriter out = new FileWriter("kHatch.txt")) {
                            out.write(kHatch.toString());
                        }
                        System.out.println("k' было успешно записано в файл kHatch.txt.");
                        in.close();
                        return;
                    }

                    else {
                        System.out.println("Такой команды не существует.");
                    }
                }
            }

            else {
                System.out.println("Такой команды не существует.");
            }
        }
    }
}
