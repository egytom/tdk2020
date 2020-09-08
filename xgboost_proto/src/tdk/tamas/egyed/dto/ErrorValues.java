package tdk.tamas.egyed.dto;

public class ErrorValues {
    public double MSE;
    public double randomMSE;
    public double averageDifference;
    public double randomAverageDifference;

    public ErrorValues(double MSE, double randomMSE, double averageDifference, double randomAverageDifference) {
        this.MSE = MSE;
        this.randomMSE = randomMSE;
        this.averageDifference = averageDifference;
        this.randomAverageDifference = randomAverageDifference;
    }
}
