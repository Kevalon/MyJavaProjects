package com.uni.nir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class MainMultiThread {


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Input the number of vertices");
        int vertexAmount = in.nextInt();
        Path graphFolder = Paths.get("./src/main/resources/g6 graphs" + "/res" + vertexAmount);
        in.close();
        Worker worker = new Worker(graphFolder, vertexAmount);
        Instant start, end;
        start = Instant.now();
        long[][] table = worker.work();
        end = Instant.now();

        Duration timeElapsed = Duration.between(start, end);
        System.out.println("Time taken: "+ (double) timeElapsed.toMillis() / 1000.0 +" seconds");

        PrettyPrinter printer = new PrettyPrinter();
        printer.print(table);
    }
}
