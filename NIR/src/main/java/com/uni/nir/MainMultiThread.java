package com.uni.nir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainMultiThread {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("Input the number of vertices");
        int vertexAmount = in.nextInt();
        Path graphFolder = Paths.get("./src/main/resources/g6 graphs" + "/res" + vertexAmount);
        in.close();
        Instant start;
        Instant end;
        long[][] table;

        if (vertexAmount > 12) {
            Worker worker = new Worker(graphFolder, vertexAmount);
            start = Instant.now();
            table = worker.work();
        } else {
            GraphReader graphReader = new GraphReader(graphFolder, vertexAmount);
            InvariantCalculator calculator = new InvariantCalculator();
            //table = new long[graphReader.getVertexAmount() + 1][graphReader.getVertexAmount() + 1];
            List<List<Integer>> graph;

            start = Instant.now();
            int cnt = 0;
            int total = 0;
            List<Integer> stringNumbers = new ArrayList<>();
            while((graph = graphReader.nextGraph()) != null) {
                if (total == 17) break;
                cnt++;
                calculator.setGraph(graph);
                calculator.findBridgesAmount();
                if (calculator.getLastBridgesAmount() == 0) {
                    calculator.findIndependentSet();
                    if (calculator.getLastIndependentSetSize() == 9) {
                        stringNumbers.add(cnt);
                        total++;
                        //break;
                    }
                }
                //table[calculator.getLastBridgesAmount()][calculator.getLastIndependentSetSize()]++;
            }

            cnt = 0;
            Path writeMe = Paths.get("./src/main/resources/5.11.txt");
            Scanner scan = new Scanner(graphFolder);
            for (int i = 0; i < stringNumbers.size(); i++) {
                String string = null;
                while (cnt < stringNumbers.get(i)) {
                    cnt++;
                    string = scan.next();
                }
                Files.writeString(writeMe, string + "\n", StandardOpenOption.APPEND);
            }
        }
        end = Instant.now();

        Duration timeElapsed = Duration.between(start, end);
        System.out.println("Time taken: "+ (double) timeElapsed.toMillis() / 1000.0 +" seconds");

        //PrettyPrinter printer = new PrettyPrinter();
        //printer.print(table);
    }
}
