package tdk.tamas.egyed;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import tdk.tamas.egyed.kmeans.Centroid;
import tdk.tamas.egyed.kmeans.EuclideanDistance;
import tdk.tamas.egyed.kmeans.KMeans;
import tdk.tamas.egyed.kmeans.Record;

public class Main {

    public static void main(String[] args) throws Exception {
        var clusterCount = 5;
        var rowCount = 64000;

        if (args != null && args.length > 0) {
            try {
                clusterCount = Integer.parseInt(args[0]);
                rowCount = Integer.parseInt(args[1]);
            } catch (Exception e) {
                throw new InvalidParameterException("The given argument is invalid.");
            }
        }

        List<List<String>> inputCSV = readCSV(true, rowCount);
        List<Record> records = createRecordsFromInput(inputCSV);

        long tStart = System.currentTimeMillis();
        Map<Centroid, List<Record>> clusters = KMeans.fit(records, clusterCount, new EuclideanDistance(), 1000, Optional.empty());
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("KMeans elapsed seconds: " + elapsedSeconds);

        writeToFile(clusters);
    }

    private static List<Record> createRecordsFromInput(List<List<String>> inputCSV) {
        int id = 0;
        List<Record> records = new ArrayList<>();
        String[] months = new String[]{
                "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
        };

        for (List<String> row : inputCSV) {
            id++;

            Map<String, Double> monthlyConsumption = new HashMap<>();
            for (int i = 0; i < 12; i++) {
                String valueText = row.get(4 + i);
                double value = valueText.equals("") ? 0.0 : Double.parseDouble(valueText);
                monthlyConsumption.put(months[i], value);
            }

            records.add(new Record(id + ". " + row.get(0), monthlyConsumption));
        }

        return records;
    }

    private static Centroid sortedCentroid(Centroid key) {
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

    private static List<List<String>> readCSV(boolean isFirstRowNaming, int rowCount) throws Exception {
        List<List<String>> records = new ArrayList<>();

        try (FileReader reader = new FileReader("csv/input.csv");
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

    private static void writeToFile(Map<Centroid, List<Record>> clusters) throws Exception {
        try (FileWriter fileWriter = new FileWriter("csv/output.txt");
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            clusters.forEach((key, value) -> {
                printWriter.println("------------------------------ CLUSTER -----------------------------------");
                printWriter.println(sortedCentroid(key));

                String members = String.join(", ", value.stream().map(Record::getDescription).collect(Collectors.toSet()));
                printWriter.print(members);

                printWriter.println("\n");
            });
        } catch (Exception e) {
            throw new Exception("Cannot write output.");
        }


    }
}
