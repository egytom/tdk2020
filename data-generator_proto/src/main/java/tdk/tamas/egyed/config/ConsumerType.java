package tdk.tamas.egyed.config;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerType {

    private String name;
    private int maxDifferenceFromAverage; // Wh - the unit
    private int percentageOfTheWholeConsumerCount; // % - the unit

    private List<Integer> averageConsumption = new ArrayList<>();

}
