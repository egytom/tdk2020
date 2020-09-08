package tdk.tamas.egyed.generator;

import tdk.tamas.egyed.config.Config;
import tdk.tamas.egyed.config.ConsumerType;
import tdk.tamas.egyed.config.Country;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {

    private int dayInMinute = 24 * 60;

    private Config config;
    private Random random = new Random();

    public Generator(Config config) {
        this.config = config;
    }

    public List<List<String>> generate(boolean isRealData) {
        List<List<String>> data = new ArrayList<>();
        data.add(getFirstRow(isRealData));

        int maxDays = config.getNumberOfDays();
        int maxIntervals = config.getIntervalsInADay();
        int intervalCount = 60 / (dayInMinute / maxIntervals);

        for (int day = 0; day < maxDays; day++) {
            for (Country country : config.getCountries()) {
                int consumerTypePercentage = 0;
                for (ConsumerType consumerType : country.getConsumerTypes()) {
                    if (consumerTypePercentage > 100) {
                        break;
                    }

                    consumerTypePercentage += consumerType.getPercentageOfTheWholeConsumerCount();
                    if (isRealData) {
                        generateRealData(data, maxIntervals, intervalCount, day, country, consumerType);
                    } else {
                        generateDefaultRow(data, maxIntervals, intervalCount, day, country, consumerType);
                    }
                }
            }
        }

        return data;
    }

    private void generateRealData(List<List<String>> data, int maxIntervals, int intervalCount, int day, Country country, ConsumerType consumerType) {
        long numberOfConsumers = Math.round(country.getConsumerCount() * (consumerType.getPercentageOfTheWholeConsumerCount() / 100.0));
        for (int i = 0; i < numberOfConsumers; i++) {
            String id = country.getName() + "_" + consumerType.getName() + "_" + i;
            createDailyConsumptionToSpecifiedConsumerType(data, maxIntervals, intervalCount, day, consumerType, id, false);
        }
    }

    private void generateDefaultRow(List<List<String>> data, int maxIntervals, int intervalCount, int day, Country country, ConsumerType consumerType) {
        String id = country.getName() + "_" + consumerType.getName();
        createDailyConsumptionToSpecifiedConsumerType(data, maxIntervals, intervalCount, day, consumerType, id, true);
    }

    private List<String> getFirstRow(boolean isRealData) {
        List<String> firstRow = new ArrayList<>();

        if (!isRealData) {
            firstRow.add("percentage");
        }

        firstRow.add("id");
        firstRow.add("day");
        firstRow.add("hour");
        firstRow.add("minute");
        firstRow.add("consumption");

        return firstRow;
    }

    private void createDailyConsumptionToSpecifiedConsumerType(List<List<String>> data, int maxIntervals, int intervalCount,
                                                               int day, ConsumerType consumerType, String id, boolean isDefaultConsumption) {
        int hour = 0;
        for (int section = 0; section < maxIntervals; section++) {
            if (section >= consumerType.getAverageConsumption().size()) {
                break;
            }

            if (section >= (intervalCount * (hour + 1))) {
                hour++;
            }

            List<String> row = new ArrayList<>();
            int consumption = 0;
            if (isDefaultConsumption) {
                row.add(consumerType.getPercentageOfTheWholeConsumerCount() + ""); //PERCENTAGE
                consumption = calculateDefaultConsumption(consumerType, section);
            } else {
                consumption = calculateConsumption(consumerType, section);
            }

            row.add(id); // ID
            row.add(Integer.toString(day)); // DAY
            row.add(Integer.toString(hour)); // HOUR
            row.add(Integer.toString((section * (60 / intervalCount)) - (hour * 60))); // MINUTE
            row.add(Integer.toString(consumption)); // CONSUMPTION

            data.add(row);
        }
    }

    private int calculateDefaultConsumption(ConsumerType consumerType, int section) {
        return consumerType.getAverageConsumption().get(section);
    }

    private int calculateConsumption(ConsumerType consumerType, int section) {
        int randomDifferenceFromAverage = random.nextInt(consumerType.getMaxDifferenceFromAverage());
        int differenceWithPresage = random.nextBoolean() ? randomDifferenceFromAverage : (-1) * randomDifferenceFromAverage;
        return consumerType.getAverageConsumption().get(section) + differenceWithPresage;
    }

}
