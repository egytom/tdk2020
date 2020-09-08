package tdk.tamas.egyed;

import tdk.tamas.egyed.config.Config;
import tdk.tamas.egyed.config.Default;
import tdk.tamas.egyed.csv.Csv;
import tdk.tamas.egyed.generator.Generator;
import tdk.tamas.egyed.json.Json;

import java.io.IOException;
import java.util.List;

public class App {

    public static void main(String[] args) throws IOException {
        Json json = new Json();

        // If default configuration required.
        Config defaultConfig = Default.DEFAULT_CONFIG;
        json.write("config", defaultConfig);

        // If configuration from resources file required.
        Config config = json.read("config", Config.class);

        Generator generator = new Generator(config);
        List<List<String>> data = generator.generate(true);
        List<List<String>> configForOtherApps = generator.generate(false);

        Csv csv = new Csv();
        csv.write("data", data);
        csv.write("configForOtherApps", configForOtherApps);
    }

}
