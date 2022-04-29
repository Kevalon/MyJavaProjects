package com.uni.nir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
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

        if (vertexAmount > 8) {
            Worker worker = new Worker(graphFolder, vertexAmount);
            start = Instant.now();
            table = worker.work();
        } else {
            GraphReader graphReader = new GraphReader(graphFolder, vertexAmount);
            InvariantCalculator calculator = new InvariantCalculator();
            table = new long[graphReader.getVertexAmount() + 1][graphReader.getVertexAmount() + 1];
            List<List<Integer>> graph;

            start = Instant.now();
            while((graph = graphReader.nextGraph()) != null) {
                calculator.setGraph(graph);
                calculator.findBridgesAmount();
                calculator.findIndependentSet();
                table[calculator.getLastBridgesAmount()][calculator.getLastIndependentSetSize()]++;
            }
        }
        end = Instant.now();

        Duration timeElapsed = Duration.between(start, end);
        System.out.println("Time taken: "+ (double) timeElapsed.toMillis() / 1000.0 +" seconds");

        PrettyPrinter printer = new PrettyPrinter();
        printer.print(table);
    }
}
