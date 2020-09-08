package tdk.tamas.egyed;

import com.opencsv.CSVReader;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import tdk.tamas.egyed.dto.ErrorValues;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

public class Main {

    private static Random RANDOM = new Random();
    private static int MAX_ELEMENT = 0;
    private static int MIN_ELEMENT = 0;

    public static void main(String[] args) throws Exception {
        List<List<String>> csv = readCSV(false, "files/input.csv");

        List<String> attributes = csv.get(0);

        List<List<String>> trainCSV = new ArrayList<>();
        trainCSV.add(attributes);
        trainCSV.addAll(csv.subList(1, 31999));

        List<List<String>> testCSV = new ArrayList<>();
        testCSV.add(attributes);
        testCSV.addAll(csv.subList(31999, csv.size()));

        MAX_ELEMENT = (int) Math.round(trainCSV.stream()
                .filter(point -> !point.get(3).equals("Radiation"))
                .mapToDouble(point -> Double.parseDouble(point.get(3)))
                .max().orElse(0));
        MIN_ELEMENT = (int) Math.round(trainCSV.stream()
                .filter(point -> !point.get(3).equals("Radiation"))
                .mapToDouble(point -> Double.parseDouble(point.get(3)))
                .min().orElse(0));

        writeValidCSV(trainCSV, "files/train.csv", 7);
        writeValidCSV(testCSV, "files/test.csv", 7);

        Runtime.getRuntime().exec("python csv2libsvm.py ./files/train.csv ./files/input.txt.train 1 True");
        Runtime.getRuntime().exec("python csv2libsvm.py ./files/test.csv ./files/input.txt.test 1 True");

        DMatrix trainMat = new DMatrix("files/input.txt.train");
        DMatrix testMat = new DMatrix("files/input.txt.test");

        HashMap<String, Object> params = new HashMap<>();
        params.put("eta", 0.465);
        params.put("max_depth", 8);
        params.put("verbosity", 1);
        params.put("objective", "reg:squarederror");

        HashMap<String, DMatrix> watches = new HashMap<>();
        watches.put("train", trainMat);
        watches.put("test", testMat);

        //set round
        int round = 2;

        //train a boost model
        long tStart = currentTimeMillis();
        Booster booster = XGBoost.train(trainMat, params, round, watches, null, null);
        long tEnd = currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("XGBoost learning, elapsed seconds: " + elapsedSeconds);

        //predict
        tStart = currentTimeMillis();
        float[][] predicts = booster.predict(testMat);
        tEnd = currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        System.out.println("XGBoost classifying, elapsed seconds: " + elapsedSeconds);

        File file = new File("./model");
        if (!file.exists()) {
            file.mkdirs();
        }

        String modelPath = "./model/xgb.model";
        booster.saveModel(modelPath);

        List<Double> predictions = getPredictions(predicts);
        List<Double> targets = testCSV.subList(1, testCSV.size()).stream()
                .mapToDouble(row -> Double.parseDouble(row.get(3)))
                .boxed().collect(Collectors.toList());
        writeToFile(predictions, targets);
    }

    private static List<List<String>> readCSV(boolean isFirstRowNaming, String inPath) throws Exception {
        List<List<String>> records = new ArrayList<>();

        try (FileReader reader = new FileReader(inPath);
             CSVReader csvReader = new CSVReader(reader)) {

            String[] values;
            var isFirstRow = true;

            while ((values = csvReader.readNext()) != null) {
                if (!(isFirstRow && isFirstRowNaming)) {
                    records.add(Arrays.asList(values));
                } else {
                    isFirstRow = false;
                }
            }
        } catch (Exception e) {
            throw new Exception("CSV cannot read.");
        }

        return records;
    }

    private static void writeValidCSV(List<List<String>> csv, String outPath, int columnCount) throws Exception {
        boolean isFirstRow = true;
        List<String> rows = new ArrayList<>();

        for (List<String> row : csv) {
            if (isFirstRow) {
                rows.add(String.join(",", row.subList(2,9)));
                isFirstRow = false;
            } else {
                String[] values = new String[columnCount];

                for (int i = 0; i < columnCount; i++) {
                    String value = row.get(2 + i).replaceAll("[/: AMP]", "");
                    values[i] = value.equals("") ? "0" : value;
                }

                String newRow = String.join(",", values);
                rows.add(newRow);
            }
        }

        try (FileWriter fileWriter = new FileWriter(outPath);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            rows.forEach(
                    printWriter::println
            );
        } catch (IOException e) {
            throw new Exception("Cannot write CSV.");
        }
    }

    private static List<Double> getPredictions(float[][] predicts) {
        List<Double> predictions = new ArrayList<>(predicts.length);

        for (int i = 0; i < predicts.length; i++) {
            double value = predicts[i][0];
            predictions.add(value);
        }

        return predictions;
    }

    private static void writeToFile(List<Double> predictions, List<Double> targets) throws Exception {
        int count = targets.size();
        List<Double> errorList = new ArrayList<>();
        List<Double> randomErrorList = new ArrayList<>();
        List<Double> targetList = new ArrayList<>();

        ErrorValues errorValues = getErrorValuesWithSettingListValues(predictions, targets, errorList, randomErrorList, targetList);

        double MSE = errorValues.MSE;
        double randomMSE = errorValues.randomMSE;
        double averageDifference = errorValues.averageDifference;
        double randomAverageDifference = errorValues.randomAverageDifference;

        System.out.println("Mean Square Error (MSE) Without Algorithm = " + randomMSE);
        System.out.println("Average Difference Without Algorithm = " + randomAverageDifference);

        try (FileWriter fileWriter = new FileWriter("files/output.txt");
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            String separator = "\t|\t";
            DecimalFormat df = new DecimalFormat("#.000");

            printWriter.println("Mean Square Error (MSE) = " + MSE);
            printWriter.println("Average Difference = " + averageDifference + "\n");

            printWriter.println("ROW\t\t|\tPREDICTION\t|\tTARGET\t|\tERROR");
            for (int i = 0; i < count; i++) {
                printWriter.println(
                        (i + 1) + (i >= 1000 ? "" : "\t") + separator
                                + df.format(predictions.get(i)) + "\t" + separator
                                + df.format(targetList.get(i)) + separator
                                + df.format(errorList.get(i))
                );
            }
        } catch (Exception e) {
            throw new Exception("Cannot write output.");
        }
    }

    private static ErrorValues getErrorValuesWithSettingListValues(List<Double> predictionList, List<Double> targets, List<Double> errorList,
                                                                   List<Double> randomErrorList, List<Double> targetList) {
        double MSE = 0.0;
        double randomMSE = 0.0;
        int count = targets.size();
        for (int i = 0; i < count; i++) {
            double target = targets.get(i);
            double prediction = predictionList.get(i);
            int rndPrediction = RANDOM.nextInt(MAX_ELEMENT - MIN_ELEMENT + 1) + MIN_ELEMENT;

            double targetMinusPredicted = target - prediction;
            double targetMinusRandomPredicted = target - rndPrediction;
            double error = Math.pow(targetMinusPredicted, 2);
            double rndError = Math.pow(targetMinusRandomPredicted, 2);
            MSE += error;
            randomMSE += rndError;

            targetList.add(target);
            errorList.add(Math.abs(targetMinusPredicted));
            randomErrorList.add(Math.abs(targetMinusRandomPredicted));
        }

        randomMSE = randomMSE / count;
        MSE = MSE / count;
        double averageDifference = errorList.stream().mapToDouble(a -> a).average().orElse(0.0);
        double randomAverageDifference = randomErrorList.stream().mapToDouble(a -> a).average().orElse(0.0);

        return new ErrorValues(MSE, randomMSE, averageDifference, randomAverageDifference);
    }
}
