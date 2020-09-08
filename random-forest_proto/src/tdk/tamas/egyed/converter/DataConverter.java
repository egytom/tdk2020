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

    public static void convertCSVtoARFF(String sourcePath, String destinationPath, int fromRow, int toRow, int excludedColumnNumber, int maxLines) throws Exception {
        // create valid CSV
        var csv = readCSV(sourcePath, fromRow, toRow, excludedColumnNumber, maxLines);
        writeValidCSVtoFile(csv, excludedColumnNumber != -1);

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

    private static List<List<String>> readCSV(String sourcePath, int fromRow, int toRow, int excludedColumnNumber, int maxLines) throws Exception {
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

        return records.subList(0, maxLines);
    }

    private static void writeValidCSVtoFile(List<List<String>> csv, boolean isTargetColumnMissing) throws Exception {
        boolean isFirstColumn = true;
        List<String> rows = new ArrayList<>();
        int columnCount = isTargetColumnMissing? 6 : 7;

        for (List<String> row : csv) {
            if (isFirstColumn) {
                row.remove(row.size() - 1);
                row.remove(row.size() - 1);
                row.remove(0);
                row.remove(0);
                String columnRow = String.join(", ", row);
                rows.add(columnRow);
                isFirstColumn = false;
            } else {
                String[] values = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    String value = row.get(2 + i).replaceAll("[/: AMP]", "");
                    values[i] = value.equals("") ? "0" : value;
                }

                String newRow = String.join(", ", values);
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
