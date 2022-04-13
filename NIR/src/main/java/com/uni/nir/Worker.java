package com.uni.nir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Worker {
    private final BlockingQueue<List<List<Integer>>> blockingQueue = new LinkedBlockingDeque<>(30);
    private final Path graphFolder;
    private final int vertexAmount;
    private final long[][] table;
    private boolean done = false;

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
        Thread[] consumerThreads = new Thread[8];
        for (int i = 0; i < 8; i++) {
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
        done = true;
    }

    private void consume() {
        InvariantCalculator invariantCalculator = new InvariantCalculator();
        List<List<Integer>> value;
        while (true) {
            if (blockingQueue.isEmpty() && done) break;
            try {
                value = blockingQueue.take();
            } catch (InterruptedException e) {
                break;
            }
            // Consume value
            invariantCalculator.setGraph(value);
            invariantCalculator.findBridgesAmount();
            invariantCalculator.findIndependentSet();
            updateTable(invariantCalculator.getLastBridgesAmount(), invariantCalculator.getLastIndependentSetSize());
        }
    }

    synchronized void updateTable(int bridges, int setSize) {
        table[bridges][setSize]++;
    }
}
