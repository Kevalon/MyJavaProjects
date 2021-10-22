import java.io.*;
import java.util.ArrayList;

public class Main {

    public final static int MAXN = 5000;
    public static ArrayList<ArrayList<ArrayList<Integer>>> order = new ArrayList<>();
    public static ArrayList<ArrayList<Integer>> reverseG = new ArrayList<>();
    public static ArrayList<ArrayList<Integer>> g = new ArrayList<>();
    public static int[] cl = new int[MAXN];
    public static ArrayList<Integer> sources = new ArrayList<>();
    public static int cycleStart = -1;
    public static StringBuilder neuroNet2Result = new StringBuilder();
    public static StringBuilder neuroNet3Result = new StringBuilder();
    public static String[] operations = new String[MAXN];
    public static String[] allowedOperations = { "+", "*", "exp" };

    public static boolean dfsCycles(int v) {
        cl[v] = 1;
        for (int i = 0; i < reverseG.get(v).size(); ++i) {
            int to = reverseG.get(v).get(i);
            if (cl[to] == 0) {
                if (dfsCycles(to)) return true;
            }
            else if (cl[to] == 1) {
                cycleStart = to;
                return true;
            }
        }
        cl[v] = 2;
        return false;
    }

    public static void dfs (int v) {
        boolean some = false;
        boolean first = true;
        for (int i = 0; i < reverseG.get(v).size(); ++i) {
            if (!first) {
                neuroNet2Result.append(", ");
                neuroNet3Result.append(", ");
            }
            int to = order.get(v).get(i).get(0);
            some = true;
            if (first) {
                neuroNet2Result.append(v).append('(');
                neuroNet3Result.append(operations[v]).append('(');
                first = false;
            }
            dfs(to);
        }
        if (some) {
            neuroNet2Result.append(")");
            neuroNet3Result.append(")");
        }
        else {
            neuroNet2Result.append(v);
            neuroNet3Result.append(operations[v]);
        }
    }

    private static double getNumber(StringBuilder str) {
        while (str.indexOf("(") != -1) {
            int openBracket = str.lastIndexOf("(");
            int closeBracket = str.indexOf(")", openBracket);
            char operation = str.charAt(openBracket - 1);

            // Проверяем на наличие неправильных операций перед скобкой
            if (operation != '*' && operation != '+' && operation != 'p') {
                return Double.MAX_VALUE;
            }

            String oneBracket = str.substring(openBracket + 1, closeBracket);
            long count = oneBracket.chars().filter(ch -> ch == ',').count();
            // Считаем количество аргументов в скобке в зависимости от операции
            if ((operation == '*' || operation == '+') && count < 1) {
                return Double.MAX_VALUE;
            }
            if (operation == 'p' && count > 0) {
                return Double.MAX_VALUE;
            }

            // Записываем числа в отдельный массив и проверяем, что нет не чисел
            oneBracket = oneBracket.replaceAll("\\s", "");
            String[] numbersStr = oneBracket.split(",");
            double[] numbers = new double[numbersStr.length];
            try {
                for (int i = 0; i < numbers.length; i++) {
                    numbers[i] = Double.parseDouble(numbersStr[i]);
                }
            }
            catch (NumberFormatException ex) {
                return Double.MAX_VALUE;
            }

            // Подсчитываем значение в зависимости от операции
            double newNumber;
            if (operation == '+') {
                double sum = 0;
                for (double el : numbers)
                    sum += el;
                newNumber = sum;
            }
            else if (operation == '*') {
                double mult = 1;
                for (double el : numbers) {
                    mult *= el;
                }
                newNumber = mult;
            }
            else {
                newNumber = Math.exp(numbers[0]);
            }

            // Меняем скобку в оригинальной строке на полученное число
            int start = operation == 'p' ? openBracket - 3 : openBracket - 1;
            String tmp = str.substring(0, start) + newNumber + str.substring(closeBracket + 1);
            str = new StringBuilder(tmp);
        }
        return Double.parseDouble(str.toString());
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.out.println("Неверное количество входных аргументов.");
            return;
        }

        String path = args[0];
        String operations = args[1];
        String outputPath = args[2];

        File input = new File(path);
        if (!input.exists() || input.isDirectory()) {
            System.out.println("Файл не найден.");
            return;
        }
        if (input.length() == 0) {
            System.out.println("Входной файл пуст.");
            return;
        }

        File correctnessInput = new File(operations);
        if (!correctnessInput.exists() || correctnessInput.isDirectory()) {
            System.out.println("Файл не найден.");
            return;
        }
        if (correctnessInput.length() == 0) {
            System.out.println("Файл соответствия пуст.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(correctnessInput))) {
            String line;

            while ((line = br.readLine())!= null) {
                try {
                    line = line.replaceAll("\\s", "");
                    int divider = line.indexOf(':');
                    int v = Integer.parseInt(line.substring(0, divider));
                    String op = line.substring(divider + 1);
                    boolean correctInput = false;

                    for (String el : allowedOperations) {
                        if (op.equals(el)) {
                            correctInput = true;
                            break;
                        }
                    }
                    if (!correctInput) {
                        try {
                            Double.parseDouble(op);
                        }
                        catch (NumberFormatException ex) {
                            throw new Exception();
                        }
                    }

                    Main.operations[v] = op;
                }
                catch (Exception ex) {
                    System.out.println("Error");
                    return;
                }
            }
        }

        ArrayList<ArrayList<Integer>> correctness = new ArrayList<>();
        int realSize = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
            String line;
            for (int i = 0; i < MAXN; i++) {
                reverseG.add(new ArrayList<>());
            }

            while ((line = br.readLine()) != null) {
                try {
                    line = line.trim().replaceAll(" +", " ");
                    int a, b, n;
                    while (line.indexOf('(') != -1) {
                        a = Integer.parseInt(line.substring(1, line.indexOf(',')));
                        line = line.substring(line.indexOf(',') + 2);
                        b = Integer.parseInt(line.substring(0, line.indexOf(',')));
                        line = line.substring(line.indexOf(',') + 2);
                        n = Integer.parseInt(line.substring(0, line.indexOf(')')));
                        if (line.indexOf('(') != -1)
                            line = line.substring(line.indexOf('('));
                        else line = "";

                        if (a > realSize) realSize = a;
                        if (b > realSize) realSize = b;
                        while (correctness.size() < b + 1) {
                            correctness.add(new ArrayList<>());
                        }
                        while (g.size() < realSize + 1) {
                            g.add(new ArrayList<>());
                        }
                        while (reverseG.size() < realSize + 1) {
                            reverseG.add(new ArrayList<>());
                        }
                        while (order.size() < realSize + 1) {
                            order.add(new ArrayList<>());
                        }
                        while (order.get(b).size() < n) {
                            order.get(b).add(new ArrayList<>());
                        }

                        if (correctness.get(b).contains(n)) {
                            throw new Exception();
                        }
                        correctness.get(b).add(n);
                        order.get(b).get(n - 1).add(a);

                        g.get(a).add(b);
                        reverseG.get(b).add(a);
                    }
                }
                catch (Exception ex) {
                    System.out.println("Error");
                    return;
                }
            }
        }

        for (var vertex : correctness) {
            for (int i = 1; i < vertex.size() + 1; i++) {
                if (!vertex.contains(i)) {
                    System.out.println("Error");
                    return;
                }
            }
        }

        for (int i = 0; i < realSize; ++i) {
            if (dfsCycles(i))
                break;
        }

        if (cycleStart != -1) {
            System.out.println("Граф содержит цикл.");
            return;
        }

        for (int i = 0; i < g.size(); i++) {
            if (g.get(i).size() == 0)
                sources.add(i);
        }

        try (FileWriter out = new FileWriter(outputPath)) {
            out.write("");
            for (Integer source : sources) {
                dfs(source);
                out.append(neuroNet2Result.toString()).append(" = ");
                out.append(neuroNet3Result.toString()).append(" = ");

                double resultNumber = getNumber(neuroNet3Result);
                if (resultNumber == Double.MAX_VALUE) {
                    System.out.println("Error");
                    return;
                }

                out.append(String.valueOf(resultNumber)).append('\n');
                neuroNet2Result.setLength(0);
                neuroNet3Result.setLength(0);
            }
        }
    }
}
