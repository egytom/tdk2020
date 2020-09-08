package tdk.tamas.egyed;

import lombok.extern.slf4j.Slf4j;
import tdk.tamas.egyed.io.ReadWrite;
import tdk.tamas.egyed.kmeans.*;
import tdk.tamas.egyed.util.Util;

import java.security.InvalidParameterException;
import java.util.*;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {
        var rowCount = 23472;
        var readWrite = new ReadWrite();

        if (args != null && args.length > 0) {
            try {
                rowCount = Integer.parseInt(args[0]);
            } catch (Exception e) {
                throw new InvalidParameterException("The given argument is invalid.");
            }
        }

        Map<String, Integer> clusterNamesWithPercentage = new HashMap<>();
        List<Record> records = readWrite.readRecordsFromCsv("input", true, rowCount);
        List<Centroid> centroids = readWrite.readCentroidsFromCsv("config", true, rowCount, clusterNamesWithPercentage);

        var clusterCount = centroids.size();
        runDefaultKMeans(clusterCount, records, readWrite, clusterNamesWithPercentage);
        runKMeansWithPreprocessing(clusterCount, records, centroids, readWrite, clusterNamesWithPercentage);
        runKMeansWithQuantumComputing(clusterCount, records, readWrite, clusterNamesWithPercentage);
        runKMeansWithPreprocessingAndQuantumComputing(clusterCount, records, centroids, readWrite, clusterNamesWithPercentage);
    }

    private static void runDefaultKMeans(int clusterCount, List<Record> records, ReadWrite readWrite, Map<String, Integer> clusterNamesWithPercentage) throws Exception {
        long tStart = System.currentTimeMillis();

        Map<Centroid, List<Record>> clusters = KMeans.fit(records, clusterCount, new EuclideanDistance(), 4000, Optional.empty());

        writeElapsedSecondsToLogs(tStart, "Default KMeans elapsed seconds: ");
        readWrite.writeToFile("kmeans_default", clusters, clusterNamesWithPercentage);
    }

    private static void runKMeansWithPreprocessing(int clusterCount, List<Record> records, List<Centroid> centroids, ReadWrite readWrite, Map<String, Integer> clusterNamesWithPercentage) throws Exception {
        long tStart = System.currentTimeMillis();

        Map<Centroid, List<Record>> clusters = KMeans.fit(records, clusterCount, new EuclideanDistance(), 4000, Optional.of(centroids));

        writeElapsedSecondsToLogs(tStart, "KMeans with preprocessing elapsed seconds: ");
        readWrite.writeToFile("kmeans_with_preprocessing", clusters, clusterNamesWithPercentage);
    }

    private static void runKMeansWithQuantumComputing(int clusterCount, List<Record> records, ReadWrite readWrite, Map<String, Integer> clusterNamesWithPercentage) throws Exception {
        double maxValue = getMaxFeatureValueFromRecords(records);
        double maxSum = getMaxSumValueFromRecords(records);

        long tStart = System.currentTimeMillis();

        Map<Centroid, List<Record>> clusters = KMeans.fit(records, clusterCount, new QuantumDistance(maxValue, maxSum), 4000, Optional.empty());

        writeElapsedSecondsToLogs(tStart, "KMeans with quantum computing elapsed seconds: ");
        readWrite.writeToFile("kmeans_with_quantum_computing", clusters, clusterNamesWithPercentage);
    }

    private static void runKMeansWithPreprocessingAndQuantumComputing(int clusterCount, List<Record> records, List<Centroid> centroids, ReadWrite readWrite, Map<String, Integer> clusterNamesWithPercentage) throws Exception {
        double maxValue = getMaxFeatureValueFromRecords(records);
        double maxSum = getMaxSumValueFromRecords(records);

        long tStart = System.currentTimeMillis();

        Map<Centroid, List<Record>> clusters = KMeans.fit(records, clusterCount, new QuantumDistance(maxValue, maxSum), 4000, Optional.of(centroids));

        writeElapsedSecondsToLogs(tStart, "KMeans with preprocessing and quantum computing elapsed seconds: ");
        readWrite.writeToFile("kmeans_with_preprocessing_and_quantum_computing", clusters, clusterNamesWithPercentage);
    }

    private static double getMaxFeatureValueFromRecords(List<Record> records) {
        Double result = 0.0;

        for (Record record : records) {
            Optional<Map.Entry<String, Double>> maxEntry = record.getFeatures().entrySet()
                    .stream()
                    .max(Comparator.comparing(Map.Entry::getValue));

            Double maxValue = Util.getDoubleFromOptionalEntry(maxEntry);
            result = getMax(result, maxValue);
        }

        return result;
    }

    private static double getMaxSumValueFromRecords(List<Record> records) {
        Double result = 0.0;

        for (Record record : records) {
            Double sumValue = record.getFeatures().values()
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            result = getMax(result, sumValue);
        }

        return result;
    }

    private static Double getMax(Double first, Double second) {
        return  (second.compareTo(first) > 0) ? second : first;
    }

    private static void writeElapsedSecondsToLogs(long tStart, String text) {
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;

        log.info(text + elapsedSeconds);
    }

}
