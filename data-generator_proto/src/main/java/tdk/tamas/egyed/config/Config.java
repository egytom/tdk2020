package tdk.tamas.egyed.config;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    private int numberOfDays;
    private int intervalsInADay = 24 * 60 / 30; // Must be valid divider of the 24 * 60

    private List<Country> countries = new ArrayList<>();

}
