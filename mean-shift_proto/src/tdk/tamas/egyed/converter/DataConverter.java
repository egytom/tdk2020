package tdk.tamas.egyed.converter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class DataConverter {

    public static void convertCSVtoARFF(String sourcePath, String destinationPath, int maxLines) throws Exception {
        // create valid CSV
        var csv = readCSV(sourcePath, maxLines);
        writeValidCSVtoFile(csv);

        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("files/arff.csv"));
        Instances data = loader.getDataSet();

        // save ARFF
        File destinationFile = new File(destinationPath);
        if (destinationFile.exists())
        {
            destinationFile.delete();
        }
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(destinationFile);
        saver.setDestination(new File(destinationPath));
        saver.writeBatch();
    }

    private static List<List<String>> readCSV(String sourcePath, int maxLines) throws Exception {
        List<List<String>> records = new ArrayList<>();

        try (FileReader reader = new FileReader(sourcePath);
             CSVReader csvReader = new CSVReader(reader)) {

            String[] values;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (Exception e) {
            throw new Exception("CSV cannot read.");
        }

        int size = records.size();
        if (size < maxLines) {
            maxLines = size;
        }

        return records.subList(0, maxLines);
    }

    private static void writeValidCSVtoFile(List<List<String>> csv) throws Exception {
        int id = 0;
        List<String> rows = new ArrayList<>();

        for (List<String> row : csv) {
            if (id == 0) {
                String[] columns = new String[12];
                for (int i = 0; i < 12; i++) {
                    String columnName = row.get(4 + i);
                    columns[i] = columnName;
                }
                String columnRow = String.join(", ", columns);
                rows.add(columnRow);
            } else {
                String[] monthlyConsumptionWithId = new String[12];
                for (int i = 0; i < 12; i++) {
                    String value = row.get(4 + i);
                    monthlyConsumptionWithId[i] = value.equals("") ? "0" : value;
                }

                String newRow = String.join(", ", monthlyConsumptionWithId);
                rows.add(newRow);
            }
            id++;
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
