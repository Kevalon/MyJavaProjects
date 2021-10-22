import java.io.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.err.println("Отсутствуют аргументы программы");
            System.exit(1);
        }

        String path = args[0];
        File input = new File(path);
        if (!input.exists() || input.isDirectory()) {
            System.out.println("Файл не найден.");
            return;
        }

        String outputPath = args[1];

        FileWriter out = new FileWriter(outputPath);
        out.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n\n");
        out.append("<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xsi:schemaLocation=\"urn:prng-config prng-config.xsd\"\n" +
                "        xmlns=\"urn:prng-config\">\n");
        out.append("    <graph>\n");

        boolean empty = true;
        boolean error = false;
        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
            String line;
            int lineNumber = 0;
            ArrayList<Tuple> arcs = new ArrayList<>();
            ArrayList<ArrayList<Integer>> order = new ArrayList<>();
            int vertexAmount = 0;

            while ((line = br.readLine()) != null) {
                empty = false;
                lineNumber++;
                try {
                    line = line.trim().replaceAll("\\s+","");
                    int a, b, n;
                    while (line.indexOf('(') != -1) {
                        line = line.substring(line.indexOf('('));
                        a = Integer.parseInt(line.substring(line.indexOf('(') + 1, line.indexOf(',')));
                        line = line.substring(line.indexOf(',') + 1);
                        b = Integer.parseInt(line.substring(0, line.indexOf(',')));
                        line = line.substring(line.indexOf(',') + 1);
                        n = Integer.parseInt(line.substring(0, line.indexOf(')')));
                        if (line.indexOf('(') != -1)
                            line = line.substring(line.indexOf('('));
                        else line = "";

                        if (a > vertexAmount) vertexAmount = a;
                        if (b > vertexAmount) vertexAmount = b;
                        while (order.size() < b + 1) {
                            order.add(new ArrayList<>());
                        }

                        if (order.get(b).contains(n)) {
                            throw new Exception();
                        }
                        order.get(b).add(n);
                        Tuple newOne = new Tuple(a, b, n);
                        arcs.add(newOne);
                    }
                }
                catch (Exception ex) {
                    System.out.println("Error");
                    out.append("        <Error>File contains an error in line ");
                    out.append(String.valueOf(lineNumber)).append("</Error>\n");
                    error = true;
                    break;
                }
            }
            
            for (var vertex : order) {
                for (int i = 1; i < vertex.size() + 1; i++) {
                    if (!vertex.contains(i)) {
                        System.out.println("Error");
                        out.append("        <Error>File contains an error in line ");
                        out.append(String.valueOf(lineNumber)).append("</Error>\n");
                        error = true;
                        break;
                    }
                }
                if (error) break;
            }

            if (empty) {
                out.append("        <Error>Input file is empty</Error>\n");
                error = true;
            }

            if (!error) {
                for (int i = 0; i < vertexAmount; i++) {
                    out.append("        <vertex>v");
                    out.append(String.valueOf(i + 1)).append("</vertex>\n");
                }

                for (Tuple tup : arcs) {
                    tup.writeToFile(out);
                }
            }
        }

        out.append("    </graph>\n");
        out.append("</config>");
        out.close();
    }
}
