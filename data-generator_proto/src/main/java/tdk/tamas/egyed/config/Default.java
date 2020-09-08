package tdk.tamas.egyed.config;

import java.util.ArrayList;
import java.util.List;

public class Default {

    private static List<ConsumerType> consumerTypes = new ArrayList<>();
    private static List<Country> countries = new ArrayList<>();
    private static List<Integer> averageConsumptionPensioner = new ArrayList<>();
    private static List<Integer> averageConsumptionFamily = new ArrayList<>();
    private static List<Integer> averageConsumptionFamilyRenewable = new ArrayList<>();
    private static List<Integer> averageConsumptionSmallCompanies = new ArrayList<>();
    private static List<Integer> averageConsumptionLargeCompanies = new ArrayList<>();

    static {

        for (int i = 0; i < 48; i++) {
            if (i < 10) { // (after 0 AM,) before 5 AM
                averageConsumptionPensioner.add(20);
                averageConsumptionFamily.add(50);
                averageConsumptionFamilyRenewable.add(20);
                averageConsumptionSmallCompanies.add(300);
                averageConsumptionLargeCompanies.add(23000);
            } else if (i < 16) { // after 5 AM, before 8 AM
                averageConsumptionPensioner.add(150);
                averageConsumptionFamily.add(200);
                averageConsumptionFamilyRenewable.add(125);
                averageConsumptionSmallCompanies.add(2100);
                averageConsumptionLargeCompanies.add(23000);
            } else if (i < 22) { // after 8 AM, before 11 AM
                averageConsumptionPensioner.add(160);
                averageConsumptionFamily.add(100);
                averageConsumptionFamilyRenewable.add(50);
                averageConsumptionSmallCompanies.add(2200);
                averageConsumptionLargeCompanies.add(23000);
            } else if (i < 26) { // after 11 AM, before 1 PM
                averageConsumptionPensioner.add(200);
                averageConsumptionFamily.add(50);
                averageConsumptionFamilyRenewable.add(-10);
                averageConsumptionSmallCompanies.add(2300);
                averageConsumptionLargeCompanies.add(23000);
            } else if (i < 34) { // after 1 PM, before 5 PM
                averageConsumptionPensioner.add(160);
                averageConsumptionFamily.add(100);
                averageConsumptionFamilyRenewable.add(-10);
                averageConsumptionSmallCompanies.add(2100);
                averageConsumptionLargeCompanies.add(23000);
            } else if (i < 44) { // after 5 PM, before 10 PM
                averageConsumptionPensioner.add(125);
                averageConsumptionFamily.add(250);
                averageConsumptionFamilyRenewable.add(200);
                averageConsumptionSmallCompanies.add(750);
                averageConsumptionLargeCompanies.add(23000);
            } else { // after 10 PM(, before 0 AM)
                averageConsumptionPensioner.add(20);
                averageConsumptionFamily.add(75);
                averageConsumptionFamilyRenewable.add(75);
                averageConsumptionSmallCompanies.add(300);
                averageConsumptionLargeCompanies.add(23000);
            }
        }

        ConsumerType pensionerHousehold = ConsumerType.builder()
                .name("pensionerHousehold")
                .percentageOfTheWholeConsumerCount(27)
                .maxDifferenceFromAverage(15)
                .averageConsumption(averageConsumptionPensioner)
                .build();

        ConsumerType familyHousehold = ConsumerType.builder()
                .name("familyHousehold")
                .percentageOfTheWholeConsumerCount(37)
                .maxDifferenceFromAverage(20)
                .averageConsumption(averageConsumptionFamily)
                .build();

        ConsumerType familyHouseholdWithRenewableEnergySource = ConsumerType.builder()
                .name("familyHouseholdWithRenewableEnergySource")
                .percentageOfTheWholeConsumerCount(11)
                .maxDifferenceFromAverage(50)
                .averageConsumption(averageConsumptionFamilyRenewable)
                .build();

        ConsumerType smallCompanies = ConsumerType.builder()
                .name("smallCompanies")
                .percentageOfTheWholeConsumerCount(18)
                .maxDifferenceFromAverage(400)
                .averageConsumption(averageConsumptionSmallCompanies)
                .build();

        ConsumerType largeCompanies = ConsumerType.builder() // With 3 shift each takes eight hours.
                .name("largeCompanies")
                .percentageOfTheWholeConsumerCount(7)
                .maxDifferenceFromAverage(4500)
                .averageConsumption(averageConsumptionLargeCompanies)
                .build();

        consumerTypes.add(pensionerHousehold);
        consumerTypes.add(familyHousehold);
        consumerTypes.add(familyHouseholdWithRenewableEnergySource);
        consumerTypes.add(smallCompanies);
        consumerTypes.add(largeCompanies);

        Country hungary = Country.builder()
                .name("hungary")
                .consumerCount(4900) // hungary / 1000
                .consumerTypes(consumerTypes)
                .build();

        countries.add(hungary);
    }

    public static final Config DEFAULT_CONFIG = Config.builder()
            .numberOfDays(1)
            .intervalsInADay(24 * 60 / 30)
            .countries(countries)
            .build();

}
