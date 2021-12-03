package com.main;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final ArrayList<Double> outputVector = new ArrayList<>();
    private static ArrayList<ArrayList<ArrayList<Double>>> network = new ArrayList<>();

    private static double dotProduct(ArrayList<Double> v1, ArrayList<Double> v2) {
        double sum = 0;
        for (int i = 0; i < v1.size(); i++) {
            sum += v1.get(i) * v2.get(i);
        }
        return sum;
    }

    private static double activationFunc(double z) {
        return 1.0 / (1.0 + Math.exp(-1.0 * z));
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Неверное количество аргументов");
            return;
        }

        String networkPath = args[0];
        String vectorPath = args[1];
        String outputPath = "network.xml";

        FileWriter xmlOut = new FileWriter(outputPath);
        xmlOut.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n\n");
        xmlOut.append("<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xsi:schemaLocation=\"urn:prng-config prng-config.xsd\"\n" +
                "        xmlns=\"urn:prng-config\">\n");
        xmlOut.append("    <layers>\n");

        // читаем входной вектор
        String inputVectorString = Files.readString(Paths.get(vectorPath));
        if (inputVectorString.length() == 0) {
            System.out.println("Ошибка чтения входного вектора");
            return;
        }

        // Обрабатываем входной вектор
        ArrayList<Double> inputVector = new ArrayList<>();
        try (Scanner in = new Scanner(inputVectorString)) {
            while (in.hasNextDouble()) {
                inputVector.add(in.nextDouble());
            }
            if (in.hasNext()) {
                System.out.println("Ошибка формата данных во входном векторе");
                return;
            }
        }

        // Считываем входные матрицы слоев
        List<String> layers;
        try {
            layers = Files.readAllLines(Paths.get(networkPath));
        } catch (NoSuchFileException exception) {
            System.out.println("Файл с матрицами не найден");
            return;
        }

        if (layers.size() == 0) {
            System.out.println("Ошибка чтения файла матриц.");
            return;
        }

        //Обрабатываем матрицы
        long n = inputVector.size(); // количество нейронов на пред слое
        boolean first = true;
        for (String layer : layers) {
            xmlOut.append("        <layer>");
            xmlOut.append(layer);
            xmlOut.append("</layer>\n");

            String preparedLayer = layer.replaceAll("\\s+","") + ",";
            if (!first && preparedLayer.chars().filter(ch -> ch == ']').count() != n) {
                System.out.println("Ошибка. Несоответствие количества нейронов и связей на разных слоях.");
                return;
            }
            if (first) {
                if (preparedLayer
                        .substring(preparedLayer.indexOf('['), preparedLayer.indexOf(']'))
                        .chars()
                        .filter(ch -> ch == ',')
                        .count() != n - 1) {
                    System.out.println("Ошибка. Несоответствие количества нейронов и связей на разных слоях.");
                    return;
                }
                first = false;
            }

            if (preparedLayer.indexOf('[') == -1) {
                System.out.println("Ошибка формата данных в файле слоев.");
                return;
            }

            // Обработка каждого нейрона
            long m = 0; // количество нейронов на данном слое
            boolean firstInner = true;
            int start, end;
            while (true) {
                start = preparedLayer.indexOf('[');
                end = preparedLayer.indexOf(']');
                int pos = start + 1;
                ArrayList<Double> coordinates = new ArrayList<>();
                while (pos < end) {
                    int comma = preparedLayer.indexOf(',', pos);
                    if (comma == -1) {
                        comma = end - 1;
                    } else if (comma > end) {
                        comma = end;
                    }
                    try {
                        coordinates.add(Double.parseDouble(preparedLayer.substring(pos, comma)));
                    }
                    catch (NumberFormatException exception) {
                        System.out.println("Ошибка данных в матрице слоя.");
                        return;
                    }
                    pos = comma + 1;
                }

                if (firstInner) {
                    firstInner = false;
                    m = coordinates.size();
                    n = m;
                } else if (coordinates.size() != m) {
                    System.out.println("Ошибка. Неравное количество связей нейрона.");
                    return;
                }

                // считаем координату выходного вектора
                outputVector.add(activationFunc(dotProduct(inputVector, coordinates)));

                start = preparedLayer.indexOf('[', start + 1);
                if (start != - 1) {
                    preparedLayer = preparedLayer.substring(start);
                }
                else break;
            }
            inputVector = (ArrayList<Double>) outputVector.clone();
            outputVector.clear();
        }

        // Запись выходного вектора в файл
        try (FileWriter out = new FileWriter("output.txt")) {
            out.write("");
            for (int i = 0; i < inputVector.size(); i++) {
                out.append(inputVector.get(i).toString());
                if (i != inputVector.size() - 1) {
                    out.append(", ");
                }
            }
        }

        xmlOut.append("    </layers>\n");
        xmlOut.append("</config>");
        xmlOut.close();
    }
}
