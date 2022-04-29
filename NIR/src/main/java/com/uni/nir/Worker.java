package com.uni.nir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Worker {
    private final BlockingQueue<List<List<Integer>>> blockingQueue = new LinkedBlockingDeque<>(30);
    private final Path graphFolder;
    private final int vertexAmount;
    private final long[][] table;
    private final int threadCount = 8;

    public Worker(Path graphFolder, int vertexAmount) {
        this.graphFolder = graphFolder;
        this.vertexAmount = vertexAmount;
        table = new long[vertexAmount + 1][vertexAmount + 1];
    }

    public long[][] work() {
        Thread producerThread = new Thread(() -> {
            try {
                produce();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        producerThread.start();
        Thread[] consumerThreads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            consumerThreads[i] = new Thread(this::consume);
            consumerThreads[i].start();
        }
        while (true) {
            boolean allDead = true;
            for (Thread consumerThread : consumerThreads) {
                if (consumerThread.isAlive()) {
                    allDead = false;
                    break;
                }
            }
            if (allDead) break;
        }

        return table;
    }


    private void produce() throws IOException {
        GraphReader graphReader = new GraphReader(graphFolder, vertexAmount);
        List<List<Integer>> value;
        while ((value = graphReader.nextGraph())!=null) {
            try {
                blockingQueue.put(value);
            } catch (InterruptedException e) {
                break;
            }
        }
        System.out.println("I'm dead");
        for (int i = 0; i < threadCount; i++) {
            try {
                blockingQueue.put(new ArrayList<>());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void consume() {
        InvariantCalculator invariantCalculator = new InvariantCalculator();
        List<List<Integer>> value;
        while (true) {
            try {
                value = blockingQueue.take();
            } catch (InterruptedException e) {
                break;
            }
            if (value.size() < 1) break;
            // Consume value
            invariantCalculator.setGraph(value);
            invariantCalculator.findBridgesAmount();
            invariantCalculator.findIndependentSet();
            updateTable(invariantCalculator.getLastBridgesAmount(), invariantCalculator.getLastIndependentSetSize());
        }
        System.out.println(Thread.currentThread().getName() + " is done!");
    }

    synchronized void updateTable(int bridges, int setSize) {
        table[bridges][setSize]++;
    }
}
