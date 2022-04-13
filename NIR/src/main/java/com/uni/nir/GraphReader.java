//package com.uni.nir;
//
//import lombok.Getter;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.*;
//
//@Getter
//public class GraphReader {
//    private final Scanner graphIn;
//    private int vertexAmount;
//
//    public GraphReader() throws IOException {
//        Scanner in = new Scanner(System.in);
//        System.out.println("Input the number of vertices");
//        vertexAmount = in.nextInt();
//        Path GRAPH_FOLDER = Paths.get("./src/main/resources/g6 graphs" + "/res" + vertexAmount);
//        in.close();
//        graphIn = new Scanner(GRAPH_FOLDER);
//    }
//
//    public List<List<Integer>> nextGraph() {
//        if (!graphIn.hasNext()) {
//            graphIn.close();
//            return null;
//        }
//        return transformGraph(graphIn.next());
//    }
//
//    public List<List<Integer>> transformGraph(String graph6) {
//        byte[] bytes = graph6.getBytes(StandardCharsets.US_ASCII);
//        for (int i = 0; i < bytes.length; i++) {
//            bytes[i] -= 63;
//        }
//        int n = bytes[0];
//        List<List<Integer>> graph = new ArrayList<>();
//        for (int i = 0; i < n; i++) {
//            List<Integer> tmp = new ArrayList<>();
//            graph.add(tmp);
//        }
//
//        Map<Integer, Boolean> bits = new HashMap<>();
//        int pos = (bytes.length - 1) * 6 - 1;
//        for (int i = bytes.length - 1; i >= 1; i--) {
//            String bitString = Integer.toBinaryString(bytes[i]);
//            int bitStringIndex = bitString.length() - 1;
//            for (int j = 5; j >= 0; j--, bitStringIndex--) {
//                if (bitStringIndex >= 0 && bitStringIndex < bitString.length()
//                        && bitString.charAt(bitStringIndex) == '1') {
//                    bits.put(pos, true);
//                } else
//                    bits.put(pos, false);
//                pos--;
//            }
//        }
//
//        int i = 0, j = i + 1;
//        for (int idx = ((n * (n - 1)) / 2) - 1; idx >= 0; idx--) {
//            if (bits.get(idx)) {
//                graph.get(i).add(j);
//                graph.get(j).add(i);
//            }
//            j++;
//            if (j >= n) {
//                i++;
//                j = i + 1;
//            }
//        }
//        return graph;
//    }
//}

package com.uni.nir;

import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Getter
public class GraphReader {
    private final Scanner graphIn;
    private int vertexAmount;

    public GraphReader(Path graphFolder, int vertexAmount) throws IOException {
        this.vertexAmount = vertexAmount;
        graphIn = new Scanner(graphFolder);
    }

    public List<List<Integer>> nextGraph() {
        if (!graphIn.hasNext()) {
            graphIn.close();
            return null;
        }
        return transformGraph(graphIn.next());
    }

    public List<List<Integer>> transformGraph(String graph6) {
        byte[] bytes = graph6.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] -= 63;
        }
        int n = bytes[0];
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Integer> tmp = new ArrayList<>();
            graph.add(tmp);
        }

        Map<Integer, Boolean> bits = new HashMap<>();
        int pos = (bytes.length - 1) * 6 - 1;
        for (int i = bytes.length - 1; i >= 1; i--) {
            String bitString = Integer.toBinaryString(bytes[i]);
            int bitStringIndex = bitString.length() - 1;
            for (int j = 5; j >= 0; j--, bitStringIndex--) {
                if (bitStringIndex >= 0 && bitStringIndex < bitString.length()
                        && bitString.charAt(bitStringIndex) == '1') {
                    bits.put(pos, true);
                } else
                    bits.put(pos, false);
                pos--;
            }
        }

        int i = 0, j = i + 1;
        for (int idx = ((n * (n - 1)) / 2) - 1; idx >= 0; idx--) {
            if (bits.get(idx)) {
                graph.get(i).add(j);
                graph.get(j).add(i);
            }
            j++;
            if (j >= n) {
                i++;
                j = i + 1;
            }
        }
        return graph;
    }
}

