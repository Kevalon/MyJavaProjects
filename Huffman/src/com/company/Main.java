package com.company;

import java.util.*;
import java.io.*;
import java.nio.*;

public class Main {

    public static String code = "";
    public static Map<Character, String> table = new HashMap<>();

    public static void buildTable(Node root){
        if (root.left != null) {
            code += "0";
            buildTable(root.left);
        }
        if (root.right != null) {
            code += "1";
            buildTable(root.right);
        }
        if (root.symbol != null) {
            table.put(root.symbol, code);
        }
        if (code.length() != 0) {
            code = code.substring(0, code.length() - 1);
        }
    }

    public static void main(String[] args) {
        Map <Character, Integer> mp = new HashMap<>();

        System.out.println("Выберите операцию:");
        System.out.println("1. Архивирование");
        System.out.println("2. Разархивирование");
        Scanner input = new Scanner(System.in);
        int op = input.nextInt();
        input.close();

        if (op == 1) {
            double time = (double)System.currentTimeMillis();
            try (FileReader in = new FileReader("input.txt")) {
                int c;
                while ((c = in.read()) != -1) {
                    mp.putIfAbsent((char) c, 0);
                    Integer val = mp.get((char) c);
                    mp.replace((char)c, val + 1);
                }
            }
            catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

            // Построение дерева
            ArrayList<Node> t = new ArrayList<>();
            for (Map.Entry<Character, Integer> entry : mp.entrySet()) {
                Character key = entry.getKey();
                Integer value = entry.getValue();
                Node p = new Node();
                p.amount = value;
                p.symbol = key;
                t.add(p);
            }

            while (t.size() != 1 && t.size() != 0) {
                t.sort(Comparator.comparing(Node::getAmount));
                Node l = t.get(0);
                t.remove(0);
                Node r = t.get(0);
                t.remove(0);
                Node parent = new Node(l, r);
                t.add(parent);
            }
            Node root;
            if (t.size() > 0) {
                root = t.get(0);
                buildTable(root);
            }

            // Архивация
            try (FileReader in = new FileReader("input.txt")) {
                try (FileWriter out = new FileWriter("archived.txt")) {

                    // Запись дерева в файл
                    int n = mp.size();
                    ByteBuffer buf2 = ByteBuffer.allocate(4);
                    buf2.putInt(n);
                    for (byte b : buf2.array()) {
                        out.write(b);
                    }

                    for (Map.Entry<Character, Integer> entry : mp.entrySet()) {
                        char key = entry.getKey();
                        Integer value = entry.getValue();
                        out.write(key);
                        ByteBuffer valueByte = ByteBuffer.allocate(4);
                        valueByte.putInt(value);
                        for (byte b : valueByte.array()) {
                            out.write(b);
                        }
                    }

                    // Архивирование
                    int tmp;
                    String buf = "";
                    while ((tmp = in.read()) != -1) {
                        char c = (char) tmp;
                        String x = table.get(c);
                        while (!x.equals("")) {
                            if (buf.length() == 8) {
                                Integer tmp2 = Integer.parseInt(buf, 2);
                                byte wr = tmp2.byteValue();
                                buf = "";
                                out.write(wr);
                            } else {
                                buf += x.substring(0, 1);
                                if (x.length() > 1)
                                    x = x.substring(1);
                                else x = "";
                            }
                        }
                    }
                    if (buf.length() > 0) {
                        while (buf.length() < 8) {
                            buf += "0";
                        }
                        Integer tmp3 = Integer.parseInt(buf, 2);
                        byte wr = tmp3.byteValue();
                        out.write(wr);
                    }

                }
                catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println(((double)System.currentTimeMillis() - time) / 1000.0);
        }

        if (op == 2) {
            try (FileReader in = new FileReader("archived.txt")) {
                try (FileWriter out = new FileWriter("unarchived.txt")) {

                    // Построение дерева
                    byte[] amount = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        amount[i] = (byte)in.read();
                    }
                    ByteBuffer amountBuf = ByteBuffer.wrap(amount);
                    int N = amountBuf.getInt();
                    int s;

                    for (int i = 0; i < N; i++) {
                       s = in.read();
                       char key = (char) s;
                       byte[] valAr = new byte[4];
                        for (int j = 0; j < 4; j++) {
                            valAr[j] = (byte)in.read();
                        }
                       ByteBuffer buf = ByteBuffer.wrap(valAr);
                       int value = buf.getInt();
                       mp.put(key, value);
                    }

                    ArrayList<Node> t = new ArrayList<>();
                    for (Map.Entry<Character, Integer> entry : mp.entrySet()) {
                        Character key = entry.getKey();
                        Integer value = entry.getValue();
                        Node p = new Node();
                        p.amount = value;
                        p.symbol = key;
                        t.add(p);
                    }

                    while (t.size() != 1 && t.size() != 0) {
                        t.sort(Comparator.comparing(Node::getAmount));
                        Node l = t.get(0);
                        t.remove(0);
                        Node r = t.get(0);
                        t.remove(0);
                        Node parent = new Node(l, r);
                        t.add(parent);
                    }
                    Node root = new Node();
                    if (t.size() > 0) {
                        root = t.get(0);
                        buildTable(root);
                    }

                    // Разархивирование
                    int letterAmount = 0;
                    for (Map.Entry<Character, Integer> entry : mp.entrySet()) {
                        Integer value = entry.getValue();
                        letterAmount += value;
                    }
                    Node p = root;
                    int count = 0;
                    s = in.read();
                    while (count < letterAmount) {
                        byte b = (byte) s;
                        String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                        while (!s1.equals("")) {
                            if (s1.charAt(0) == '1') {
                                p = p.right;
                            }
                            else p = p.left;
                            if (p.left == null && p.right == null) {
                                if (count < letterAmount)
                                    out.write(p.symbol);
                                count++;
                                p = root;
                            }
                            if (s1.length() == 1) {
                                s1 = "";
                            }
                            else {
                                s1 = s1.substring(1);
                            }
                        }
                        s = in.read();
                    }
                }
                catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
