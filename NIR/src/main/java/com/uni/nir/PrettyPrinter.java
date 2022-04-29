package com.uni.nir;

public class PrettyPrinter {
    private final String[] TopLeftCorner = new String[] {
            "      \\ independence number",
            "       \\                   ",
            "bridges \\                  "};

    private void printFirstLines(int order, long[][] table, int squareLength) {
        System.out.print(TopLeftCorner[order - 1] + "|");
        for (int i = 0; i < table.length; i++) {
            StringBuilder square = order == 2 ? new StringBuilder(" " + i) : new StringBuilder();
            while (square.length() < squareLength) {
                square.append(" ");
            }
            square.append("|");
            System.out.print(square);
        }
        System.out.print("\n");
    }

    public void print(long[][] table) {
        long max = 0;
        for (long[] longs : table) {
            for (long aLong : longs) {
                if (aLong > max) {
                    max = aLong;
                }
            }
        }

        int squareLength = String.valueOf(max).length() + 2;

        for (int i = 1; i <= 3; i++) {
            printFirstLines(i, table, squareLength);
        }
        for (int i = 0; i < table.length; i++) {
            StringBuilder line = new StringBuilder(" " + i);
            while (line.length() < TopLeftCorner[0].length()) {
                line.append(" ");
            }
            line.append("|");
            for (int j = 0; j < table.length; j++) {
                StringBuilder square = new StringBuilder(" " + table[i][j]);
                while (square.length() < squareLength) {
                    square.append(" ");
                }
                line.append(square).append("|");
            }
            System.out.println(line);
        }
    }
}
