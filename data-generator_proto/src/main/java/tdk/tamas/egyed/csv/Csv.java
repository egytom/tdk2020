package tdk.tamas.egyed.csv;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public class Csv {

    public void write(String name, List<List<String>> data) throws IOException {
        String fileName = "files" + File.separator + name + ".csv";
        File csvOutputFile = new File(fileName);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            data.stream()
                    .map(this::convertToCsvRow)
                    .forEach(pw::println);
        }
    }

    private String convertToCsvRow(List<String> row) {
        return row.stream()
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    private String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

}
