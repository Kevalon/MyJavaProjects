package com.main;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;


public class Main {
    private static int numberOfEpochs;
    private static final Path OUTPUT_PATH = Path.of("./output.txt");
    private static final ArrayList<ArrayList<ArrayList<Double>>> MATRIX = new ArrayList<>();
    private static final double eta = 0.9;
    private static final double mu = 0.1;
    private static final ArrayList<ArrayList<Double>> TRAINING_DATA = new ArrayList<>();
    private static final ArrayList<ArrayList<Double>> DESIRED_OUTCOME = new ArrayList<>();
    private static final ArrayList<ArrayList<ArrayList<Double>>> RAW_OUTPUT = new ArrayList<>();
    private static ArrayList<ArrayList<ArrayList<Double>>> DELTA_W_PREVIOUS = new ArrayList<>();

    private static void writeStringToFile(String message, boolean append) throws IOException {
        String writeMe = message + "\n";
        if (append) {
            Files.writeString(OUTPUT_PATH, writeMe, StandardOpenOption.APPEND);
        } else {
            Files.writeString(OUTPUT_PATH, writeMe);
        }
    }

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

    private static ArrayList<Double> oneEpochOutput(ArrayList<Double> inputVector) {
        ArrayList<Double> result = inputVector;
        ArrayList<ArrayList<Double>> oneRawEpoch = new ArrayList<>();
        for (ArrayList<ArrayList<Double>> layer : MATRIX) {
            ArrayList<Double> tmp = new ArrayList<>();
            ArrayList<Double> oneRawLayer = new ArrayList<>();
            for (ArrayList<Double> neuron : layer) {
                double rawRes = dotProduct(result, neuron);
                oneRawLayer.add(rawRes);
                tmp.add(activationFunc(rawRes));
            }
            result = (ArrayList<Double>) tmp.clone();
            oneRawEpoch.add(oneRawLayer);
        }
        RAW_OUTPUT.add(oneRawEpoch);
        return result;
    }

    private static double getError(ArrayList<ArrayList<Double>> currentOutcome) {
        double error = 0;
        for (int i = 0; i < currentOutcome.size(); i++) {
            ArrayList<Double> outputVector = currentOutcome.get(i);
            ArrayList<Double> desiredVector = DESIRED_OUTCOME.get(i);

            for (int j = 0; j < outputVector.size(); j++) {
                error += Math.pow(outputVector.get(j) - desiredVector.get(j), 2);
            }
        }

        return error / 2.0;
    }

    private static int findMin(ArrayList<ArrayList<Double>> currentOutcome) {
        ArrayList<Double> error = new ArrayList<>();
        for (int i = 0; i < currentOutcome.size(); i++) {
            ArrayList<Double> outcome = currentOutcome.get(i);
            ArrayList<Double> desired = DESIRED_OUTCOME.get(i);
            double sum = 0;
            for (int j = 0; j < outcome.size(); j++) {
                sum += Math.pow(outcome.get(j) - desired.get(j), 2);
            }
            error.add(sum);
        }

        return error.indexOf(Collections.min(error));
    }

    private static double derivative(double x) {
        double y = activationFunc(x);
        return y * (1.0 - y);
    }

    private static ArrayList<ArrayList<Double>> getDelta(int index) {
        ArrayList<ArrayList<Double>> rawOutput = RAW_OUTPUT.get(index);
        ArrayList<ArrayList<Double>> delta = new ArrayList<>();
        for (int k = rawOutput.size() - 1; k >= 0; k--) {
            ArrayList<Double> deltaK = new ArrayList<>();
            ArrayList<Double> y = rawOutput.get(k);

            if (k == rawOutput.size() - 1) {
                ArrayList<Double> d = DESIRED_OUTCOME.get(index);
                for (int l = 0; l < y.size(); l++) {
                    double ylq = y.get(l);
                    deltaK.add((activationFunc(ylq)- d.get(l)) * derivative(ylq));
                }
            } else {
                ArrayList<Double> deltaKPlusOne = delta.get(delta.size() - 1);
                double sum = 0;
                for (int j = 0; j < deltaKPlusOne.size(); j++) {
                    for (int i = 0; i < deltaKPlusOne.size(); i++) {
                        sum += deltaKPlusOne.get(i) * MATRIX.get(k + 1).get(j).get(i);
                    }
                    deltaK.add(sum * derivative(y.get(j)));
                }
            }
            delta.add(deltaK);
        }
        return delta;
    }

    private static ArrayList<ArrayList<ArrayList<Double>>> getDeltaW(int index, ArrayList<ArrayList<Double>> delta) {
        ArrayList<ArrayList<Double>> rawOutput = RAW_OUTPUT.get(index);
        ArrayList<ArrayList<ArrayList<Double>>> deltaW = new ArrayList<>();
        if (MATRIX.size() > 1) {
            for (int k = MATRIX.size() - 1; k >= 0; k--) {
                ArrayList<ArrayList<Double>> deltaWk = new ArrayList<>();
                ArrayList<Double> deltaK = delta.get(k);
                ArrayList<Double> y;
                if (k == 0) {
                    y = TRAINING_DATA.get(index);
                } else {
                    y = rawOutput.get(k - 1);
                }
                for (int j = 0; j < MATRIX.get(k).size(); j++) {
                    ArrayList<Double> deltaWKj = new ArrayList<>();
                    double deltaKj = deltaK.get(j);
                    for (int i = 0; i < MATRIX.get(k).get(j).size(); i++) {
                        if (DELTA_W_PREVIOUS.size() > 0) {
                            deltaWKj.add(
                                    -eta *
                                            (mu *
                                                    DELTA_W_PREVIOUS.get(k).get(j).get(i)
                                                    + (1 - mu) * deltaKj * activationFunc(y.get(i)))
                            );
                        } else {
                            deltaWKj.add(-eta * (mu * 0 + (1 - mu) * deltaKj * activationFunc(y.get(i))));
                        }
                    }
                    deltaWk.add(deltaWKj);
                }
                deltaW.add(deltaWk);
            }
        } else {
            for (int k = MATRIX.size() - 1; k >= 0; k--) {
                ArrayList<ArrayList<Double>> wk = MATRIX.get(k);
                ArrayList<ArrayList<Double>> deltaWk = new ArrayList<>();
                ArrayList<Double> deltaK = delta.get(k);
                ArrayList<Double> y;
                y = rawOutput.get(k);
                for (int j = 0; j < wk.size(); j++) {
                    ArrayList<Double> wkj = wk.get(j);
                    ArrayList<Double> deltaWKj = new ArrayList<>();
                    double deltaKj = deltaK.get(j);
                    for (int i = 0; i < wkj.size(); i++) {
                        deltaWKj.add(-eta *(mu * MATRIX.get(k).get(j).get(i) + (1 - mu)
                                * deltaKj * activationFunc(y.get(i))));
                    }
                    deltaWk.add(deltaWKj);
                }
                deltaW.add(deltaWk);
            }
        }
        return deltaW;
    }

    private static void changeMatrix(ArrayList<ArrayList<ArrayList<Double>>> deltaW) {
        for (int k = MATRIX.size() - 1; k >= 0; k--) {
            for (int j = 0; j < MATRIX.get(k).size(); j++) {
                for (int i = 0; i < MATRIX.get(k).get(j).size(); i++) {
                    MATRIX.get(k).get(j).set(i, MATRIX.get(k).get(j).get(i) + deltaW.get(k).get(j).get(i));
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        if (args.length < 3) {
            System.out.println("Ошибка. Неверное количество аргументов.");
            return;
        }

        String networkPath = args[0];
        String trainingDataPath = args[1];
        numberOfEpochs = Integer.parseInt(args[2]);

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

        // Читаем обучающую выборку
        List<String> trainingDataLines = Files.readAllLines(Path.of(trainingDataPath));
        for (String line : trainingDataLines) {
            ArrayList<Double> currentInputData = new ArrayList<>();
            ArrayList<Double> currentOutcomeData = new ArrayList<>();
            try {
                int breakPos = line.indexOf("->");
                String input = line.substring(0, breakPos);
                String outcome = line.substring(breakPos + 2);
                try (
                        Scanner inputScan = new Scanner(input);
                        Scanner outcomeScan = new Scanner(outcome)
                        ) {
                    while (inputScan.hasNextDouble()) {
                        currentInputData.add(inputScan.nextDouble());
                    }
                    if (TRAINING_DATA.size() > 0 && TRAINING_DATA.get(0).size() != currentInputData.size()) {
                        System.out.println("Несоотвествие длин входных векторов в выборке");
                        return;
                    }
                    TRAINING_DATA.add(currentInputData);

                    while (outcomeScan.hasNextDouble()) {
                        currentOutcomeData.add(outcomeScan.nextDouble());
                    }
                    if (DESIRED_OUTCOME.size() > 0 && DESIRED_OUTCOME.get(0).size() != currentOutcomeData.size()) {
                        System.out.println("Несоотвествие длин выходных векторов в выборке");
                        return;
                    }
                    DESIRED_OUTCOME.add(currentOutcomeData);
                }
            } catch (Exception exception) {
                System.out.println("Ошибка чтения файла с обучающей выборкой");
                return;
            }
        }

        // Проверка матрицы на валидность и подготовка нейронов к работе
        long n = TRAINING_DATA.get(0).size(); // количество нейронов на пред слое
        boolean first = true;
        for (String layer : layers) {
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

            long m = 0; // количество нейронов на данном слое
            boolean firstInner = true;
            int start, end;
            ArrayList<ArrayList<Double>> oneLayer = new ArrayList<>();
            while (true) {
                start = preparedLayer.indexOf('[');
                end = preparedLayer.indexOf(']');
                int pos = start + 1;
                ArrayList<Double> oneNeuron = new ArrayList<>();
                while (pos < end) {
                    int comma = preparedLayer.indexOf(',', pos);
                    if (comma == -1) {
                        comma = end - 1;
                    } else if (comma > end) {
                        comma = end;
                    }
                    try {
                        oneNeuron.add(Double.parseDouble(preparedLayer.substring(pos, comma)));
                    } catch (NumberFormatException exception) {
                        System.out.println("Ошибка данных в матрице слоя.");
                        return;
                    }
                    pos = comma + 1;
                }

                if (firstInner) {
                    firstInner = false;
                    m = oneNeuron.size();
                    n = m;
                } else if (oneNeuron.size() != m) {
                    System.out.println("Ошибка. Неравное количество связей нейрона.");
                    return;
                }
                oneLayer.add(oneNeuron);

                start = preparedLayer.indexOf('[', start + 1);
                if (start != - 1) {
                    preparedLayer = preparedLayer.substring(start);
                }
                else break;
            }
            MATRIX.add(oneLayer);
        }

        // Обучение пошло
        double D; // Ошибка
        for (int i = 0; i < numberOfEpochs; i++) {
            // Работа всех слоев
            ArrayList<ArrayList<Double>> currentOutcome = new ArrayList<>();
            for (ArrayList<Double> inputVector : TRAINING_DATA) {
                currentOutcome.add(oneEpochOutput(inputVector));
            }

            // Подсчет ошибки
            D = getError(currentOutcome);

            // Запись ошибки в файл
            writeStringToFile(Double.toString(D), i != 0);

            // Обратное распространение ошибки (изменение нейронов)

            int minInd = findMin(currentOutcome); // индекс выборки с минимальной ошибкой
            ArrayList<ArrayList<Double>> delta = getDelta(minInd);
            ArrayList<ArrayList<ArrayList<Double>>> deltaW = getDeltaW(minInd, delta);
            DELTA_W_PREVIOUS = (ArrayList<ArrayList<ArrayList<Double>>>) deltaW.clone();
            Collections.reverse(DELTA_W_PREVIOUS);
            changeMatrix(deltaW);
        }
    }
}
