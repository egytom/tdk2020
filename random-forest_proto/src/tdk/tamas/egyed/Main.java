package tdk.tamas.egyed;

import jsat.ARFFLoader;
import jsat.DataSet;
import jsat.classifiers.DataPoint;
import jsat.classifiers.evaluation.Precision;
import jsat.classifiers.trees.RandomForest;
import jsat.regression.RegressionDataSet;
import tdk.tamas.egyed.converter.DataConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.currentTimeMillis;

public class Main {

    private static int targetColumn = 1;

    public static void main(String[] args) throws Exception {
        int maxLines = -1;
        int trainDataSize = 32000;
        String sourcePath = "files/input.csv";

        DataConverter.convertCSVtoARFF(sourcePath, "files/train.arff", 0, trainDataSize, -1, maxLines);
        DataConverter.convertCSVtoARFF(sourcePath, "files/test.arff", trainDataSize, -1, -1, maxLines);
        DataConverter.convertCSVtoARFF(sourcePath, "files/toclassify.arff", trainDataSize, -1, 3, maxLines);

        File trainFile = new File("files/train.arff");
        File testFile = new File("files/test.arff");
        File toclassifyFile = new File("files/toclassify.arff");

        DataSet trainDataSet = ARFFLoader.loadArffFile(trainFile);
        DataSet testDataSet = ARFFLoader.loadArffFile(testFile);
        DataSet toclassifyDataSet = ARFFLoader.loadArffFile(toclassifyFile);

        List<DataPoint> trainDataPointList = trainDataSet.getDataPoints();
        List<DataPoint> testDataPointList = testDataSet.getDataPoints();
        List<DataPoint> dataPointToClassifyList = toclassifyDataSet.getDataPoints();

        RegressionDataSet trainDataset = new RegressionDataSet(trainDataPointList, targetColumn);

        RandomForest randomForest = new RandomForest();
        randomForest.setMaxForestSize(5);
        ExecutorService executor = Executors.newFixedThreadPool(16);

        long tStart = currentTimeMillis();
        randomForest.train(trainDataset, executor);
        long tEnd = currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Random Forest learning, elapsed seconds: " + elapsedSeconds);

        List<Double> predictionList = new ArrayList<>(dataPointToClassifyList.size());

        tStart = currentTimeMillis();
        dataPointToClassifyList.forEach(point -> predictionList.add(randomForest.regress(point)));
        tEnd = currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        System.out.println("Random Forest classifying, elapsed seconds: " + elapsedSeconds);

        writeToFile(predictionList, testDataPointList);
        System.exit(0);
    }

    private static void writeToFile(List<Double> predictionList, List<DataPoint> dataPoints) throws Exception {
        int count = dataPoints.size();
        List<Double> errorList = new ArrayList<>();
        List<Double> targetList = new ArrayList<>();

        double MSE = getTotalErrorWithSettingArrayValues(predictionList, dataPoints, errorList, targetList);
        double averageDifference = errorList.stream().mapToDouble(a -> a).average().orElse(0.0);

        try (FileWriter fileWriter = new FileWriter("files/output.txt");
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            String separator = "\t|\t";
            DecimalFormat df = new DecimalFormat("#.00");

            printWriter.println("Mean Square Error (MSE) = " + MSE);
            printWriter.println("Average Difference = " + averageDifference + "\n");

            printWriter.println("ROW\t\t|\tPREDICTION\t|\tTARGET\t|\tERROR");
            for (int i = 0; i < count; i++) {
                printWriter.println(
                        (i + 1) + (i >= 1000 ? "" : "\t") + separator
                        + df.format(predictionList.get(i)) + "\t" + separator
                        + df.format(targetList.get(i)) + separator
                        + errorList.get(i)
                );
            }
        } catch (Exception e) {
            throw new Exception("Cannot write output.");
        }
    }

    private static double getTotalErrorWithSettingArrayValues(List<Double> predictionList, List<DataPoint> dataPoints, List<Double> errorList, List<Double> targetList) {
        double totalError = 0.0;
        int count = dataPoints.size();
        for (int i = 0; i < count; i++) {
            double target = dataPoints.get(i).getNumericalValues().get(targetColumn);
            double prediction = predictionList.get(i);

            double targetMinusPredicted = target - prediction;
            double error = Math.pow(targetMinusPredicted, 2);
            totalError += error;

            targetList.add(target);
            errorList.add(Math.abs(targetMinusPredicted));
        }
        return totalError / count;
    }

}
