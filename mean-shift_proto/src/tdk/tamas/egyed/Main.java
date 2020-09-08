package tdk.tamas.egyed;

import jsat.ARFFLoader;
import jsat.DataSet;
import jsat.classifiers.DataPoint;
import jsat.clustering.MeanShift;
import tdk.tamas.egyed.converter.DataConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws Exception {
        int maxLines = 4000;
        DataConverter.convertCSVtoARFF("files/input.csv", "files/input.arff", maxLines);
        File file = new File("files/input.arff");
        DataSet dataSet = ARFFLoader.loadArffFile(file);
        List<DataPoint> dataPoints = dataSet.getDataPoints();

        MeanShift meanShift = new MeanShift();
        ExecutorService executor = Executors.newFixedThreadPool(16);
        meanShift.setMaxIterations(maxLines / 8);

        long tStart = System.currentTimeMillis();
        int[] clusteringResults = meanShift.cluster(dataSet, executor, null);
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Mean-Shift elapsed seconds: " + elapsedSeconds);

        writeToFile(clusteringResults, dataPoints);
        System.exit(0);
    }

    private static void writeToFile(int[] clusteringResults, List<DataPoint> dataPoints) throws Exception {
        try (FileWriter fileWriter = new FileWriter("files/output.txt");
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            int clusterNumber = IntStream.of(clusteringResults).max().getAsInt() + 1;
            for (int id = 0; id < clusterNumber; id++) {
                printWriter.println("----------------------------- " + id + ". CLUSTER ----------------------------------");

                for (int i = 0; i < clusteringResults.length; i++) {
                    if (clusteringResults[i] == id) {
                        printWriter.print(i + "., ");
                    }
                }

                printWriter.println("\n");
            }
        } catch (Exception e) {
            throw new Exception("Cannot write output.");
        }
    }
}
