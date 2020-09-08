package tdk.tamas.egyed.util;

import java.util.Map;
import java.util.Optional;

public class Util {

    public static Double getDoubleFromOptionalEntry(Optional<Map.Entry<String, Double>> maxEntry) {
        Double maxValue = 0.0;
        if (maxEntry.isPresent()) {
            maxValue = maxEntry.get().getValue();
        }
        return maxValue;
    }

}
