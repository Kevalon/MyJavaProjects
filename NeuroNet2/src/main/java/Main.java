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
    public static StringBuilder result = new StringBuilder();

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
                result.append(", ");
            }
            int to = order.get(v).get(i).get(0);
            some = true;
            if (first) {
                result.append(v).append('(');
                first = false;
            }
            dfs(to);
        }
        if (some) {
            result.append(")");
        }
        if (!some && first) {
           result.append(v);
        }
    }

    public static void main(String[] args) throws IOException {

        /*if (args.length != 2) {
            System.out.println("Неверное количество входных аргументов.");
            return;
        }*/

        String path = "test3.txt";
        String outputpath = "result.txt";

        File input = new File(path);
        if (!input.exists() || input.isDirectory()) {
            System.out.println("Файл не найден.");
            return;
        }
        if (input.length() == 0) {
            System.out.println("Входной файл пуст.");
            return;
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

        for (int i = 0; i < sources.size(); i++) {
            dfs(sources.get(i));
            if (i != sources.size() - 1)
                result.append(", ");
        }

        try (FileWriter out = new FileWriter(outputpath)) {
            out.write(result.toString());
        }
    }
}
