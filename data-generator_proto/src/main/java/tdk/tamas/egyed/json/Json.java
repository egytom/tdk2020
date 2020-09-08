package tdk.tamas.egyed.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Json {

    public <T> void write(String name, T object) {
        try {
            String fileName = "files" + File.separator + name + ".json";
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8)) {
                Gson gson = createGsonBuilder();
                gson.toJson(object, writer);
            }
        } catch (Exception e) {
            String errorMessage = "Error during json writing!";
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
    }

    public <T> T read(String name, Class<T> type) {
        try {
            String fileName = "files" + File.separator + name + ".json";
            try (Reader reader = new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8)) {
                Gson gson = createGsonBuilder();
                return gson.fromJson(reader, type);
            }
        } catch (Exception e) {
            String errorMessage = "Error during json reading!";
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
    }

    private Gson createGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        return builder.setPrettyPrinting().create();
    }

}
