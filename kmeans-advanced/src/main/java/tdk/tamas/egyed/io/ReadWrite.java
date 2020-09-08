package tdk.tamas.egyed.io;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import tdk.tamas.egyed.kmeans.Centroid;
import tdk.tamas.egyed.kmeans.Record;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ReadWrite {

    public List<Record> readRecordsFromCsv(String name, boolean isFirstRowNaming, int rowCount) throws Exception {
        List<List<String>> inputCSV = readCSV(name, isFirstRowNaming, rowCount);
        List<List<String>> inputCSVAfterDataSorting = sortCSVDataByID(inputCSV, false);
        return createRecordsFromInput(inputCSVAfterDataSorting);

    }

    public List<Centroid> readCentroidsFromCsv(String name, boolean isFirstRowNaming, int rowCount, Map<String, Integer> clusterNamesWithPercentage) throws Exception {
        List<List<String>> configCSV = readCSV(name, isFirstRowNaming, rowCount);
        List<List<String>> configCSVAfterDataSorting = sortCSVDataByID(configCSV, true);
        return createCentroidsFromConfig(configCSVAfterDataSorting, clusterNamesWithPercentage);
    }

    public void writeToFile(String fileName, Map<Centroid, List<Record>> clusters, Map<String, Integer> clusterNamesWithPercentage) throws Exception {
        try (FileWriter fileWriter = new FileWriter("files/" + fileName + ".txt");
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            AtomicLong correctElementsCount = new AtomicLong();
            AtomicLong allElementsCount = new AtomicLong();

            clusters.forEach((key, value) -> {
                List<String> elements = value.stream().map(Record::getDescription).collect(Collectors.toList());
                String members = String.join(", ", elements);
                String clusterName = getNameOfTheCluster(members, clusterNamesWithPercentage);
                correctElementsCount.addAndGet(getCorrectElementsCount(clusterName, elements));
                allElementsCount.addAndGet(elements.size());
                double percentageOfCorrectElements = getPercentageOfCorrectElements(clusterName, elements);

                printWriter.println("------------------------------ CLUSTER -----------------------------------");
                printWriter.println(clusterName + ": " + sortedCentroid(key));
                printWriter.println("Correct element percentage: " + (percentageOfCorrectElements*100) + "%, elements count: " + elements.size());
                printWriter.print(members);

                printWriter.println("\n");
            });

            printWriter.println("Correct elements: " + correctElementsCount);
            printWriter.println("All elements: " + allElementsCount);
            printWriter.println("Correct percentage: " + ((correctElementsCount.doubleValue() / allElementsCount.doubleValue()) * 100.0) + "%");
        } catch (Exception e) {
            throw new Exception("Cannot write output.");
        }
    }

    private long getCorrectElementsCount(String clusterName, List<String> elements) {
        return elements.stream().filter(e -> e.substring(0, e.lastIndexOf("_")).equals(clusterName)).count();
    }

    private double getPercentageOfCorrectElements(String clusterName, List<String> elements) {
        double size = elements.size() * 1.0;
        double correctElements = getCorrectElementsCount(clusterName, elements) * 1.0;
        return correctElements / size;
    }

    private String getNameOfTheCluster(String clusterMembers, Map<String, Integer> clusterNamesWithPercentage) {
        String result = "";
        Double previousCount = -10.0;

        for (Map.Entry<String, Integer> clusterNameWithPercentage : clusterNamesWithPercentage.entrySet()) {
            String clusterName = clusterNameWithPercentage.getKey();
            Integer percentage = clusterNameWithPercentage.getValue();

            if (result.equals("")) {
                result = clusterName;
            }

            Double count = StringUtils.countMatches(clusterMembers, clusterName) / (double) percentage;

            if (previousCount.compareTo(count) < 0) {
                previousCount = count;
                result = clusterName;
            }
        }

        return result;
    }

    private List<Record> createRecordsFromInput(List<List<String>> csv) {
        List<Record> records = new ArrayList<>();

        for (List<String> row : csv) {
            String id = row.remove(0);

            Map<String, Double> consumerConsumption = new HashMap<>();
            getMapFromRow(row, consumerConsumption);

            records.add(new Record(id, consumerConsumption));
        }

        return records;
    }

    private List<Centroid> createCentroidsFromConfig(List<List<String>> csv, Map<String, Integer> clusterNamesWithPercentage) {
        List<Centroid> centroids = new ArrayList<>();

        for (List<String> row : csv) {
            String clusterName = row.remove(1);
            Integer percentage = Integer.parseInt(row.remove(0));

            Map<String, Double> coordinates = new HashMap<>();
            getMapFromRow(row, coordinates);
            centroids.add(new Centroid(coordinates));

            clusterNamesWithPercentage.put(clusterName, percentage);
        }

        return centroids;
    }

    private void getMapFromRow(List<String> row, Map<String, Double> coordinates) {
        // Only consumption data, time not count => "/4" and "3 + i * 4"
        for (int i = 0; i < row.size() / 4; i++) {
            String valueText = row.get(3 + i * 4);
            double value = valueText.equals("") ? 0.0 : Double.parseDouble(valueText);
            coordinates.put(i + "", value);
        }
    }

    private Centroid sortedCentroid(Centroid key) {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(key
                .getCoordinates()
                .entrySet());
        entries.sort((e1, e2) -> e2
                .getValue()
                .compareTo(e1.getValue()));

        Map<String, Double> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : entries) {
            sorted.put(entry.getKey(), entry.getValue());
        }

        return new Centroid(sorted);
    }

    private List<List<String>> readCSV(String name, boolean isFirstRowNaming, int rowCount) throws Exception {
        List<List<String>> records = new ArrayList<>();

        try (FileReader reader = new FileReader("files/" + name + ".csv");
             CSVReader csvReader = new CSVReader(reader)) {

            String[] values;
            var isFirstRow = true;
            var rowNumber = 0;

            while ((values = csvReader.readNext()) != null && rowNumber <= rowCount) {
                if (!(isFirstRow && isFirstRowNaming)) {
                    records.add(Arrays.asList(values));
                    rowNumber++;
                } else {
                    isFirstRow = false;
                }
            }
        } catch (Exception e) {
            throw new Exception("CSV cannot read.");
        }

        return records;
    }

    private List<List<String>> sortCSVDataByID(List<List<String>> inputCSV, boolean isConfig) {
        List<List<String>> result = new ArrayList<>();

        inputCSV.forEach(row -> {
            String id = row.get(isConfig ? 1 : 0);

            if (result.stream().noneMatch(resultRow -> resultRow.get(isConfig ? 1 : 0).equals(id))) {
                List<String> sortedRowsById = new ArrayList<>();

                if (isConfig) {
                    sortedRowsById.add(row.get(0));
                }

                sortedRowsById.add(id);

                List<List<String>> relevantRowsById = inputCSV.stream()
                        .filter(columns -> columns.get(isConfig ? 1 : 0).equals(id))
                        .collect(Collectors.toList());

                relevantRowsById.forEach(columns -> {
                    for (int i = (isConfig ? 2 : 1); i <= (isConfig ? 5 : 4); i++) {
                        sortedRowsById.add(columns.get(i));
                    }
                });

                result.add(sortedRowsById);
            }
        });

        return result;
    }

}
