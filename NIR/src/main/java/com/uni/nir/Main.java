//package com.uni.nir;
//
//import java.io.IOException;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.*;
//
//public class Main {
//    public static void main(String[] args) throws IOException {
//        GraphReader graphReader = new GraphReader();
//        InvariantCalculator calculator = new InvariantCalculator();
//        Instant start, end;
//        long[][] table = new long[graphReader.getVertexAmount() + 1][graphReader.getVertexAmount() + 1];
//        List<List<Integer>> graph;
//
//        start = Instant.now();
//        while((graph = graphReader.nextGraph()) != null) {
//            calculator.setGraph(graph);
//            calculator.findBridgesAmount();
//            calculator.findIndependentSet();
//            table[calculator.getLastBridgesAmount()][calculator.getLastIndependentSetSize()]++;
//        }
//        end = Instant.now();
//
//        Duration timeElapsed = Duration.between(start, end);
//        System.out.println("Time taken: "+ (double) timeElapsed.toMillis() / 1000.0 +" seconds");
//
//        PrettyPrinter printer = new PrettyPrinter();
//        printer.print(table);
//    }
//}
