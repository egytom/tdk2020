package tdk.tamas.egyed.config;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Country {

    private String name;
    private int consumerCount;

    private List<ConsumerType> consumerTypes = new ArrayList<>();

}
