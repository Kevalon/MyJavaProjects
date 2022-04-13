package com.uni.nir;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Getter
@Setter
public class InvariantCalculator {
    private final int MAXN = 11;

    private int lastBridgesAmount;
    private int lastIndependentSetSize;
    List<List<Integer>> graph;
    Scanner in;

    private boolean[] used;
    private int timer;
    private int[] tin;
    private int[] fup;

    private void dfs(int v, int p) {
        used[v] = true;
        timer++;
        tin[v] = fup[v] = timer;
        for (int i = 0; i < graph.get(v).size(); ++i) {
            int to = graph.get(v).get(i);
            if (to == p) continue;
            if (used[to])
                fup[v] = Math.min(fup[v], tin[to]);
            else {
                dfs(to, v);
                fup[v] = Math.min(fup[v], fup[to]);
                if (fup[to] > tin[v])
                    lastBridgesAmount++;
            }
        }
    }

    public void findBridgesAmount() {
        lastBridgesAmount = 0;
        used = new boolean[graph.size()];
        Arrays.fill(used, false);
        timer = 0;
        tin = new int[MAXN];
        fup = new int[MAXN];

        for (int i = 0; i < used.length; ++i)
            if (!used[i])
                dfs(i, -1);
    }

    private <E> List<List<E>> generatePerm(List<E> original) {
        if (original.isEmpty()) {
            List<List<E>> result = new ArrayList<>();
            result.add(new ArrayList<>());
            return result;
        }
        E firstElement = original.remove(0);
        List<List<E>> returnValue = new ArrayList<>();
        List<List<E>> permutations = generatePerm(original);
        for (List<E> smallerPermuted : permutations) {
            for (int index = 0; index <= smallerPermuted.size(); index++) {
                List<E> temp = new ArrayList<>(smallerPermuted);
                temp.add(index, firstElement);
                returnValue.add(temp);
            }
        }
        return returnValue;
    }

    private void subsetsOf(
            List<Integer> vertexList,
            int k,
            int index,
            List<Integer> tempList,
            List<List<Integer>> finalList) {
        if (tempList.size() == k) {
            finalList.add(new ArrayList<>(tempList));
            return;
        }
        if (index == vertexList.size())
            return;
        Integer integer = vertexList.get(index);

        tempList.add(integer);
        subsetsOf(vertexList, k, index + 1, tempList, finalList);

        tempList.remove(integer);
        subsetsOf(vertexList, k, index + 1, tempList, finalList);
    }

    private List<List<Integer>> getAllSublists(List<Integer> vertexList, int k) {
        List<List<Integer>> result = new ArrayList<>();
        subsetsOf(vertexList, k, 0, new ArrayList<>(), result);
        return result;
    }

    public void findIndependentSet() {
        lastIndependentSetSize = 1;
        List<Integer> vertexList = new ArrayList<>();
        for (int i = 0; i < graph.size(); i++) {
            vertexList.add(i);
        }

        boolean containsEdge;
        for (int size = graph.size(); size >= 1; size--) {
            List<List<Integer>> allSubLists = getAllSublists(vertexList, size);
            for (List<Integer> subList : allSubLists) {
                containsEdge = false;
                for (Integer from : subList) {
                    for (Integer to : subList) {
                        if (graph.get(from).contains(to)) {
                            containsEdge = true;
                            break;
                        }
                    }
                    if (containsEdge) break;
                }
                if (!containsEdge) {
                    lastIndependentSetSize = size;
                    return;
                }
            }
        }
    }
}
