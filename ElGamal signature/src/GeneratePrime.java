/*
import java.util.ArrayList;
import java.util.Scanner;

public class GeneratePrime {
    private static byte[] modifier = BigNumbersOperations.fromInt(16);
    private static long maxStartingPoint = (long) Math.pow(2., 16);
    private static final long minStartingPoint = 1;
    private static byte[] constanta = BigNumbersOperations.fromInt(19381);

    // Метод перевода больших чисел в бинарный вид
    private static byte[] toBinary(byte[] x) {
        // Инициализация
        ArrayList<Byte> res = new ArrayList<>();
        int l = 0;
        byte[] curPowOfTwo = BigNumbersOperations.fromInt(1);
        // Ищем Минимальную степень двойки, которая больше исходного числа
        while (BigNumbersOperations.compare(x, curPowOfTwo) >= 0) {
            curPowOfTwo = BigNumbersOperations.mult(curPowOfTwo, BigNumbersOperations.fromInt(2));
            l++;
        }
        curPowOfTwo = BigNumbersOperations.divide(curPowOfTwo, BigNumbersOperations.fromInt(2)).get(0);
        // Для каждой степени двойки начиная с той что мы нашли на предыдущем шаге выполняем следующее
        while (l > 0) {
            // Если число больше степени двойки, к ответу дописываем 1 и вычитаем степень двойки из числа
            if (BigNumbersOperations.compare(x, curPowOfTwo) >= 0) {
                res.add((byte) 1);
                x = BigNumbersOperations.posSubtract(x, curPowOfTwo);
            }
            // Иначе к ответу дописываем 0
            else
                res.add((byte) 0);
            curPowOfTwo = BigNumbersOperations.divide(curPowOfTwo, BigNumbersOperations.fromInt(2)).get(0);
            l--;
        }
        // Возвращение результата
        byte[] resb = new byte[res.size()];
        for (int i = 0; i < res.size(); i++)
            resb[i] = res.get(i);
        return resb;
    }

    // Метод, случайно генерирующий простое число заданой длины в битах
    public static byte[] getRandPrimeForBits(byte[] k) throws Exception {
        byte[] one = BigNumbersOperations.fromInt(1);

        byte[] start = BigNumbersOperations.modPow(
                BigNumbersOperations.fromInt(2),
                BigNumbersOperations.posSubtract(k, one),
                BigNumbersOperations.fromInt(0)
        );
        byte[] end = BigNumbersOperations.modPow(
                BigNumbersOperations.fromInt(2),
                k,
                BigNumbersOperations.fromInt(0)
        );

//        int start = (int) Math.pow(2, k - 1);
//        int end = (int) Math.pow(2, k);

        // Список простых чисел
        ArrayList<byte[]> ps = new ArrayList<>();
        byte[] i = start;
        // Перебираем все числа заданной длины
        while (BigNumbersOperations.compare(i, end) < 0) {
            // Если число простое, то добавляем его в список простых чисел
            if (MillerRabin.test(i))
                ps.add(i);
            i = BigNumbersOperations.posSum(i, one);
        }
        // Если простых чисел не найдено возвращаем исключение
        if (ps.size() == 0)
            throw new Exception();
        // Иначе возвращаем случайное число из списка простых чисел
        int idx = (int) (Math.random() * ps.size());
        return ps.get(idx);
    }

    // Метод генерации простого числа длины <= 16 бит
    public static byte[][] generateSmallP(int k) throws Exception {
        // Случайно генерируем простое q длины k / 2
        byte[] q = getRandPrimeForBits(BigNumbersOperations.fromInt(k / 2));
        // Устанавливаем s = 2
        byte[] s = BigNumbersOperations.fromInt((int) (Math.random() * (Math.pow(2., k - 1) - 2) + 2));
        if (!BigNumbersOperations.isZero(BigNumbersOperations.divide(s, BigNumbersOperations.fromInt(2)).get(1)))
            s = BigNumbersOperations.posSum(s, BigNumbersOperations.fromInt(1));
        byte[] two = BigNumbersOperations.fromInt(2);
        byte[] p;
        while (true) {
            // Высчитываем p = qs + 1
            p = BigNumbersOperations.posSum(
                    BigNumbersOperations.mult(q, s),
                    BigNumbersOperations.fromInt(1)
            );
            // Нижняя гранцица числа p = 2^(k - 1) (p должно быть не меньше k бит в длину)
            byte[] lowBoarder = BigNumbersOperations.modPow(
                    BigNumbersOperations.fromInt(2),
                    BigNumbersOperations.fromInt(k - 1),
                    BigNumbersOperations.fromInt(0)
            );
            // Верхняя граница числа p = 2^k (p должно быть не более k бит в длину)
            byte[] highBoarder = BigNumbersOperations.mult(lowBoarder, BigNumbersOperations.fromInt(2));
            // Елсли р больше k бит в длину, заного генерируем q и начинаем сначала
            if (BigNumbersOperations.compare(p, highBoarder) > 0) {
                q = getRandPrimeForBits(BigNumbersOperations.fromInt(k / 2));
                s = BigNumbersOperations.fromInt((int) (Math.random() * (Math.pow(2., k - 1) - 2) + 2));
                if (!BigNumbersOperations.isZero(BigNumbersOperations.divide(s, BigNumbersOperations.fromInt(2)).get(1)))
                    s = BigNumbersOperations.posSum(s, BigNumbersOperations.fromInt(1));
                continue;
            }

            // Если p меньше k бит в длину, то увеличиваем s на 2 и переходим в начало цикла
            if (BigNumbersOperations.compare(p, lowBoarder) > 0) {
                // Если длина p составляет k бит проверяем 3 условия
                // 1) p < (2q + 1)^2
                byte[] check1 = BigNumbersOperations.modPow(
                        BigNumbersOperations.posSum(
                                BigNumbersOperations.mult(two, q),
                                BigNumbersOperations.fromInt(1)
                        ),
                        two,
                        BigNumbersOperations.fromInt(0)
                );
                // 2) 2^(qs) = 1 (mod p)
                byte[] check2 = BigNumbersOperations.modPow(two, BigNumbersOperations.mult(q, s), p);
                // 3) 2^s != 1 (mod p)
                byte[] check3 = BigNumbersOperations.modPow(two, s, p);
                // Если все условия выплнены, то p - искомое простое число, иначе увеличиваем s на 2 и переходим к началу цикла
                if (BigNumbersOperations.compare(p, check1) < 0
                        && BigNumbersOperations.compare(check2, BigNumbersOperations.fromInt(1)) == 0
                        && BigNumbersOperations.compare(check3, BigNumbersOperations.fromInt(1)) != 0)
                    break;
            }
            s = BigNumbersOperations.posSum(s, two);
        }
        // Возвращение результата
        return new byte[][] {p, q};
    }

    // Метод генерации простого числа по процедуре А из ГОСТ 34.10-94
    public static byte[][] generateP(int k) throws Exception {
        // Если k <= 16 бит, воспользуеся методом для генерации простых чисел небольшой длины
        if (BigNumbersOperations.compare(BigNumbersOperations.fromInt(k), modifier) <= 0)
            return generateSmallP(k);
        // Устанавливаем случайные начальные значения в пределах от 1 до 2^16
        long x0 = (long) (Math.random() * (maxStartingPoint - minStartingPoint) + minStartingPoint);
        long c = (long) (Math.random() * (maxStartingPoint - minStartingPoint) + minStartingPoint);
        // Число c должно быть нечетным
        // Если c четно, добавляем к нему 1
        if (c % 2 == 0)
            c++;

        // Вывод промежуточных значений
        System.out.println("Сгенерированные начальные значения\n(они используются для генерации p длины больше 16 бит, согласно процедуре А из ГОСТ 34.10-94):");
        System.out.println("x0 = " + x0);
        System.out.println("c = " + c);

        // Шаг 1: устанавливваем y0 = x0
        byte[] y0 = BigNumbersOperations.fromInt(x0);
        // Шаг 2: вычисляем последовательность чисел (t0, t1, ..., ts)
        ArrayList<byte[]> t = new ArrayList<>();
        byte[] ti = BigNumbersOperations.fromInt(k);
        t.add(ti);
        byte[] two = BigNumbersOperations.fromInt(2);
        byte[] one = BigNumbersOperations.fromInt(1);

        while (BigNumbersOperations.compare(ti, modifier) > 0) {
            ti = BigNumbersOperations.divide(ti, two).get(0);
            t.add(ti);
        }
        int s = t.size() - 1;

        // Шаг 3: найдем случайное простое число ps длины ts битов
        byte[] ps = getRandPrimeForBits(t.get(s));
        // Шаг 4: устанавливаем m = s - 1
        int m = s - 1;

        // Массив, содержащий простые числа pi длины ti
        byte[][] p = new byte[s + 1][];
        p[s] = ps;

        byte[] pm;
        while (true) {
            // Шаг 5: установим rm = tm / 16 с округлением вверх
            ArrayList<byte[]> tmprm = BigNumbersOperations.divide(t.get(m), modifier);
            byte[] rm = tmprm.get(0);
            if (!BigNumbersOperations.isZero(tmprm.get(1)))
                rm = BigNumbersOperations.posSum(rm, one);

            while (true) {
                // Шаг 6:  рекурсивно вычисляем последовательность (y1, y2, ..., yrm)
                // y(i+1) = (19381*yi + c)
                ArrayList<byte[]> y = new ArrayList<>();
                byte[] yprev = y0;
                y.add(yprev);
                byte[] i = one.clone();
                while (BigNumbersOperations.compare(i, rm) <= 0){
                    byte[] yi = BigNumbersOperations.mult(yprev, constanta);
                    yi = BigNumbersOperations.posSum(
                            yi,
                            BigNumbersOperations.fromInt(c)
                    );
                    yi = BigNumbersOperations.divide(
                            yi,
                            BigNumbersOperations.fromInt(maxStartingPoint)
                    ).get(1);
                    y.add(yi);
                    yprev = yi;
                    i = BigNumbersOperations.posSum(i, one);
                }

                // Шаг 7: вычислим Ym как сумму по i = 0 - (rm - 1): yi*2^(16*i)
                byte[] Ym = BigNumbersOperations.fromInt(0);
                int j = 0;
                while (BigNumbersOperations.compare(BigNumbersOperations.fromInt(j), rm) < 0){
                    byte[] ytemp = BigNumbersOperations.mult(
                            y.get(j),
                            BigNumbersOperations.modPow(
                                    two,
                                    BigNumbersOperations.mult(modifier, BigNumbersOperations.fromInt(j)),
                                    BigNumbersOperations.fromInt(0)
                            )
                    );
                    Ym = BigNumbersOperations.posSum(
                            Ym,
                            ytemp
                    );
                    j++;
                }

                // Шаг 8: установим y0 = yrm
                y0 = y.get(BigNumbersOperations.toInt(rm));

                // Шаг 9: Вычислим N как сумму следующих двух слагаемых
                byte[] N;
                // Первое слагаемое вычисляется как 2^(tm - 1) / p(m + 1) с округлением вверх
                ArrayList<byte[]> Ntmp = BigNumbersOperations.divide(
                        BigNumbersOperations.modPow(
                                BigNumbersOperations.fromInt(2),
                                BigNumbersOperations.posSubtract(t.get(m), one),
                                BigNumbersOperations.fromInt(0)
                        ),
                        p[m + 1]
                );
                byte[] N1 = Ntmp.get(0);
                if (!BigNumbersOperations.isZero(Ntmp.get(1)))
                    N1 = BigNumbersOperations.posSum(N1, BigNumbersOperations.fromInt(1));
                // Второе слагаемое вычисляется как 2^(tm - 1) * Ym / (p(m + 1) * 2^(16*rm)) с округлением вниз
                byte[] N2 = BigNumbersOperations.divide(
                            BigNumbersOperations.mult(
                                    BigNumbersOperations.modPow(
                                            two,
                                            BigNumbersOperations.posSubtract(t.get(m), one),
                                            BigNumbersOperations.fromInt(0)
                                    ),
                                    Ym
                            ),
                            BigNumbersOperations.mult(
                                    p[m + 1],
                                    BigNumbersOperations.modPow(
                                            two,
                                            BigNumbersOperations.mult(modifier, rm),
                                            BigNumbersOperations.fromInt(0)
                                    )
                            )
                ).get(0);
                N = BigNumbersOperations.posSum(N1, N2);

                // Если N нечетно то N = N + 1
                if (!BigNumbersOperations.isZero(BigNumbersOperations.divide(N, BigNumbersOperations.fromInt(2)).get(1))) {
                    N = BigNumbersOperations.posSum(N, BigNumbersOperations.fromInt(1));
                }

                // Шаг 10: установим K = 0
                byte[] K = BigNumbersOperations.fromInt(0);

                byte[] test;
                while (true) {
                    // Шаг 11: вычислим pm = p(m + 1) * (N + K) + 1
                    pm = BigNumbersOperations.mult(
                            p[m + 1],
                            BigNumbersOperations.posSum(N, K)
                    );
                    pm = BigNumbersOperations.posSum(pm, BigNumbersOperations.fromInt(1));

                    // Шаг 12: если pm > 2^tm то переходим к шагу 6
                    test = BigNumbersOperations.modPow(
                                    two,
                                    t.get(m),
                                    BigNumbersOperations.fromInt(0)
                            );

                    if (BigNumbersOperations.compare(pm, test) > 0)
                        break;

                    // Шаг 13: проверяем 2 условия:
                    // 1) 2^(p(m + 1) * (N + K)) (mod pm) = 1
                    byte[] check1 = BigNumbersOperations.modPow(
                            two,
                            BigNumbersOperations.mult(
                                    p[m + 1],
                                    BigNumbersOperations.posSum(N, K)
                            ),
                            pm
                    );
                    // 2) 2^(N + K) (mod pm) != 1
                    byte[] check2 = BigNumbersOperations.modPow(
                            BigNumbersOperations.fromInt(2),
                            BigNumbersOperations.sum(N, K),
                            pm
                    );
                    // Если хотя бы одно из этих условий не выполняется, то K = K + 2 и переходим к шагу 11
                    if ((check1.length == 1 && check1[0] == 1) && !(check2.length == 1 && check2[0] == 1))
                        break;

                    K = BigNumbersOperations.posSum(K, two);
                }
                if (BigNumbersOperations.compare(pm, test) <= 0)
                    break;
            }
            // Записываем полученное pm в массив p
            p[m] = pm;
            // Шаг 14: уменьшаем m на 1. Если m < 0 то p0 - искомое p, p1 - искомое q, иначе перезодим к шагу 5
            m--;
            if (m < 0)
                break;
        }
        // Возвращение результата
        return p;
    }

    public static void main(String[] args) {
        // Считывание входных данных
        Scanner in = new Scanner(System.in);
        boolean flag = false;
        boolean err = false;
        int k = 0;
        while (!flag) {
            System.out.println("Введите длину простого числа в битах (целое неотрицательное число):");
            try {
                if (err)
                    in.next();
                k = in.nextInt();
                if (k < 0) {
                    System.out.println("Ошибка: введено отрицательное число");
                    continue;
                }

                flag = true;
            } catch (Exception e) {
                System.out.println("Ошибка: введено недопустимое значение");
                err = true;
                continue;
            }
        }
        System.out.println("k = " + k);
        // Запуск генератора простых чисел
        long t1 = System.currentTimeMillis();
        try {
            byte[][] p = generateP(k);
            long t2 = System.currentTimeMillis();
            // Перевод значений p и q в бинарный вид
            byte[] bp = toBinary(p[0]);
            byte[] bq = toBinary(p[1]);
            // Вывод результата
            System.out.println("\nРезультат работы:");
            System.out.println("s = " + BigNumbersOperations.toString(BigNumbersOperations.divide(p[0], p[1]).get(0)));
            System.out.println("q = " + BigNumbersOperations.toString(p[1]));
            System.out.println("q в двоичном представлении = " + BigNumbersOperations.toString(bq));
            System.out.println("Длина q в битах = " + bq.length);
            System.out.println("p = " + BigNumbersOperations.toString(p[0]));
            System.out.println("p в двоичном представлении = " + BigNumbersOperations.toString(bp));
            System.out.println("Длина p в битах = " + bp.length);
            System.out.println("Время работы: " + (t2 - t1) + "ms");
            System.out.println("\nТест Миллера-Рабина:");
            if (MillerRabin.test(p[0]))
                System.out.println("p - простое");
            else
                System.out.println("p - составное");
            if (MillerRabin.test(p[1]))
                System.out.println("q - простое");
            else
                System.out.println("q - составное");
        }
        catch (Exception e) {
            System.out.println("Для k = " + k + " невозможно найти подходящую пару простых чисел p и q");
            String cause = k >= 2 ? "q" : "p";
            int l = k >= 2 ? k / 2 : k;
            System.out.println("(Так как длина простого числа должна быть >= 2 бит, а длина "
                    + cause
                    + " равна "
                    + l
                    + " бит)");
        }
    }
}
*/
