package tdk.tamas.egyed;

import jsat.ARFFLoader;
import jsat.DataSet;
import jsat.classifiers.ClassificationDataSet;
import jsat.classifiers.DataPoint;
import jsat.classifiers.OneVSAll;
import jsat.classifiers.boosting.AdaBoostM1;
import jsat.classifiers.trees.DecisionStump;
import jsat.utils.SystemInfo;
import tdk.tamas.egyed.converter.DataConverter;
import tdk.tamas.egyed.dto.ErrorValues;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

public class Main {

    private static int targetColumn = 1;
    private static int minElement = 0;
    private static int maxElement = 0;

    private static Random rnd = new Random();

    public static void main(String[] args) throws Exception {
        int maxLines = -1;
        int trainDataSize = 32000;
        String sourcePath = "files/input.csv";

        DataConverter.convertCSVtoARFF(sourcePath, "files/train.arff", 0, trainDataSize, -1, maxLines, true, true);
        DataConverter.convertCSVtoARFF(sourcePath, "files/test.arff", trainDataSize, -1, -1, maxLines, false, false);
        DataConverter.convertCSVtoARFF(sourcePath, "files/toclassify.arff", trainDataSize, -1, 3, maxLines, false, false);

        File trainFile = new File("files/train.arff");
        File testFile = new File("files/test.arff");
        File toclassifyFile = new File("files/toclassify.arff");

        DataSet trainDataSet = ARFFLoader.loadArffFile(trainFile);
        DataSet testDataSet = ARFFLoader.loadArffFile(testFile);
        DataSet toclassifyDataSet = ARFFLoader.loadArffFile(toclassifyFile);

        List<DataPoint> trainDataPointList = trainDataSet.getDataPoints();
        List<DataPoint> testDataPointList = testDataSet.getDataPoints();
        List<DataPoint> dataPointToClassifyList = toclassifyDataSet.getDataPoints();

        maxElement = DataConverter.MAX_ELEMENT;
        minElement = DataConverter.MIN_ELEMENT;

        ClassificationDataSet trainDataset = new ClassificationDataSet(trainDataPointList, 0);

        DecisionStump decisionStump = new DecisionStump();
        AdaBoostM1 adaBoost = new AdaBoostM1(decisionStump, 5);
        OneVSAll classifier = new OneVSAll(adaBoost);
        ExecutorService executor = Executors.newFixedThreadPool(SystemInfo.LogicalCores);

        long tStart = currentTimeMillis();
        classifier.trainC(trainDataset, executor);
        long tEnd = currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("AdaBoost learning, elapsed seconds: " + elapsedSeconds);

        List<Integer> predictionList = new ArrayList<>(dataPointToClassifyList.size());

        tStart = currentTimeMillis();
        dataPointToClassifyList.forEach(point -> predictionList.add(classifier.classify(point).mostLikely()));
        tEnd = currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        System.out.println("AdaBoost classifying, elapsed seconds: " + elapsedSeconds);

        writeToFile(predictionList, testDataPointList);
        System.exit(0);
    }

    private static void writeToFile(List<Integer> predictions, List<DataPoint> dataPoints) throws Exception {
        List<Double> predictionList = predictions.parallelStream()
                .map(prediction -> prediction * 5.0)
                .collect(Collectors.toList());

        int count = dataPoints.size();
        List<Double> errorList = new ArrayList<>();
        List<Double> randomErrorList = new ArrayList<>();
        List<Double> targetList = new ArrayList<>();

        ErrorValues errorValues = getErrorValuesWithSettingListValues(predictionList, dataPoints, errorList, randomErrorList, targetList);

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
                                + df.format(predictionList.get(i)) + "\t" + separator
                                + df.format(targetList.get(i)) + separator
                                + df.format(errorList.get(i))
                );
            }
        } catch (Exception e) {
            throw new Exception("Cannot write output.");
        }
    }

    private static ErrorValues getErrorValuesWithSettingListValues(List<Double> predictionList, List<DataPoint> dataPoints, List<Double> errorList,
                                                                   List<Double> randomErrorList, List<Double> targetList) {
        double MSE = 0.0;
        double randomMSE = 0.0;
        int count = dataPoints.size();
        for (int i = 0; i < count; i++) {
            double target = dataPoints.get(i).getNumericalValues().get(targetColumn);
            double prediction = predictionList.get(i);
            int rndPrediction = rnd.nextInt(maxElement - minElement + 1) + minElement;

            double targetMinusPredicted = target - prediction;
            double targetMinusRndPredicted = target - rndPrediction;
            double error = Math.pow(targetMinusPredicted, 2);
            double rndError = Math.pow(targetMinusRndPredicted, 2);
            MSE += error;
            randomMSE += rndError;

            targetList.add(target);
            errorList.add(Math.abs(targetMinusPredicted));
            randomErrorList.add(Math.abs(targetMinusRndPredicted));
        }

        randomMSE = randomMSE / count;
        MSE = MSE / count;
        double averageDifference = errorList.stream().mapToDouble(a -> a).average().orElse(0.0);
        double randomAverageDifference = randomErrorList.stream().mapToDouble(a -> a).average().orElse(0.0);

        return new ErrorValues(MSE, randomMSE, averageDifference, randomAverageDifference);
    }

}
