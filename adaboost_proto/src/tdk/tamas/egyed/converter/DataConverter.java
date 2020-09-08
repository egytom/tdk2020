package tdk.tamas.egyed.converter;

import com.opencsv.CSVReader;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataConverter {

    public static int MAX_ELEMENT = 0;
    public static int MIN_ELEMENT = 0;

    public static void convertCSVtoARFF(String sourcePath, String destinationPath, int fromRow, int toRow,
                                        int excludedColumnNumber, int maxLines, boolean isTargetCategorical, boolean isMaxAndMinRequired) throws Exception {
        // create valid CSV
        var csv = readCSV(sourcePath, fromRow, toRow, excludedColumnNumber, maxLines, isMaxAndMinRequired);
        writeValidCSVtoFile(csv, excludedColumnNumber != -1, isTargetCategorical);

        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("files/arff.csv"));
        Instances data = loader.getDataSet();

        // save ARFF
        File destinationFile = new File(destinationPath);
        if (destinationFile.exists()) {
            destinationFile.delete();
        }
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(destinationFile);
        saver.setDestination(new File(destinationPath));
        saver.writeBatch();
    }

    private static List<List<String>> readCSV(String sourcePath, int fromRow, int toRow, int excludedColumnNumber, int maxLines, boolean isMaxAndMinRequired) throws Exception {
        List<List<String>> records = new ArrayList<>();

        try (FileReader reader = new FileReader(sourcePath);
             CSVReader csvReader = new CSVReader(reader)) {

            int counter = 0;
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                counter++;

                boolean isRowWanted = false;
                if (counter == 1 || (fromRow <= counter && (toRow == -1 || counter < toRow))) {
                    isRowWanted = true;
                    if (fromRow > 0 && toRow != -1) {
                        toRow++;
                    }
                }

                if (isRowWanted) {
                    List<String> valueAbstractList = Arrays.asList(values);
                    ArrayList<String> valueList = new ArrayList<>(valueAbstractList);
                    if (excludedColumnNumber != -1 && valueList.size() >= excludedColumnNumber) {
                        valueList.remove(excludedColumnNumber);
                    }
                    records.add(valueList);
                }
            }
        } catch (Exception e) {
            throw new Exception("CSV cannot read.");
        }

        int size = records.size();
        if (size < maxLines || maxLines == -1) {
            maxLines = size;
        }

        if (isMaxAndMinRequired) {
            MAX_ELEMENT = (int) Math.round(records.stream()
                    .filter(point -> !point.get(3).equals("Radiation"))
                    .mapToDouble(point -> Double.parseDouble(point.get(3)))
                    .max().orElse(0));
            MIN_ELEMENT = (int) Math.round(records.stream()
                    .filter(point -> !point.get(3).equals("Radiation"))
                    .mapToDouble(point -> Double.parseDouble(point.get(3)))
                    .min().orElse(0));
        }

        return records.subList(0, maxLines);
    }

    private static void writeValidCSVtoFile(List<List<String>> csv, boolean isTargetColumnMissing, boolean isTargetCategorical) throws Exception {
        boolean isFirstColumn = true;
        List<String> rows = new ArrayList<>();
        int columnCount = isTargetColumnMissing ? 6 : 7;

        for (List<String> row : csv) {
            if (isFirstColumn) {
                row.remove(row.size() - 1); // TimeSunSet
                row.remove(row.size() - 1); // TimeSunRise
                row.remove(0); // UNIX time
                row.remove(0); // Data
                String columnRow = String.join(", ", row);
                rows.add(columnRow);
                isFirstColumn = false;
            } else {
                String[] values = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    String value = row.get(2 + i).replaceAll("[/: AMP]", "");

                    if (isTargetCategorical && 2 + i == 3) {
                        values[i] = 5 * (Math.round(Double.parseDouble(value) / 5)) + " category";
                    } else {
                        values[i] = value.equals("") ? "0" : value;
                    }
                }

                String newRow = String.join(",", values);
                rows.add(newRow);
            }
        }

        try (FileWriter fileWriter = new FileWriter("files/arff.csv");
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            rows.forEach(
                    printWriter::println
            );
        } catch (IOException e) {
            throw new Exception("Cannot write CSV.");
        }
    }

}
