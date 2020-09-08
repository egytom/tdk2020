package tdk.tamas.egyed.kmeans;

import com.gluonhq.strange.*;
import com.gluonhq.strange.gate.Hadamard;
import tdk.tamas.egyed.quantum.CSwap;
import tdk.tamas.egyed.quantum.TDKQuantumExecutionEnvironment;
import tdk.tamas.egyed.quantum.U3;
import tdk.tamas.egyed.util.Util;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * Calculates the distance between two items using Quantum computing.
 */
public class QuantumDistance implements Distance {

    private final double maxValue;
    private final double maxSum;

    public QuantumDistance(double maxValue, double maxSum) {
        super();
        this.maxValue = maxValue;
        this.maxSum = maxSum;
    }

    @Override
    public double calculate(Map<String, Double> f1, Map<String, Double> f2) {
        if (f1 == null || f2 == null) {
            throw new IllegalArgumentException("Feature vectors can't be null");
        }

        // Make 2 features vectors from the n dimensional vectors and normalize them
        double vector1MaxValue = normalize(getMaxFeatureValueFromFeatures(f1), maxValue);
        double vector2MaxValue = normalize(getMaxFeatureValueFromFeatures(f2), maxValue);
        double vector1MaxSum = normalize(getMaxSumValueFromFeatures(f1), maxSum);
        double vector2MaxSum = normalize(getMaxSumValueFromFeatures(f2), maxSum);

        // Get degrees from vectors
        double f1Alpha = (vector1MaxValue + 1) * (Math.PI / 2);
        double f2Alpha = (vector2MaxValue + 1) * (Math.PI / 2);
        double f1Beta = (vector1MaxSum + 1) * (Math.PI / 2);
        double f2Beta = (vector2MaxSum + 1) * (Math.PI / 2);

        // Run the quantum estimation with repetitions
        double result = 0.0;

        for (int i = 0; i < 5; i++) {
            // Create a program with 3 qubits
            Program p = new Program(3);

            // Init Qubits
            p.initializeQubit(2, 0.0);

            // Create required gates for the relevant qubits and apply them in steps
            Gate hadamardGateToAncillary1 = new Hadamard(2);
            Step step1 = new Step();
            step1.addGate(hadamardGateToAncillary1);
            p.addStep(step1);

            Gate u3ToFirstQubit = new U3(f1Alpha, f1Beta, 0, 0);
            Gate u3ToSecondQubit = new U3(f2Alpha, f2Beta, 0, 1);
            Step step2 = new Step();
            step2.addGates(u3ToFirstQubit, u3ToSecondQubit);
            p.addStep(step2);

            Gate hadamardGateToAncillary2 = new Hadamard(2);
            Step step3 = new Step();
            step3.addGate(hadamardGateToAncillary2);
            p.addStep(step3);

            Gate cSwap = new CSwap(2, 0, 1);
            Step step4 = new Step();
            step4.addGate(cSwap);
            p.addStep(step4);

            // Create an environment for quantum simulation
            TDKQuantumExecutionEnvironment tdkQuantumExecutionEnvironment = new TDKQuantumExecutionEnvironment();

            // Execute quantum computing and calculate distance estimation
            Result res = tdkQuantumExecutionEnvironment.runProgram(p);
            Qubit[] qubits = res.getQubits();

            // Add the actual turn's measure to the estimated result (distance)
            result += qubits[2].measure();
        }

        return result;
    }

    private double normalize(double numberToNormalize, double maxNumber) {
        return numberToNormalize / maxNumber;
    }

    private double getMaxFeatureValueFromFeatures(Map<String, Double> vector) {
        Optional<Map.Entry<String, Double>> maxEntry = vector.entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue));

        return Util.getDoubleFromOptionalEntry(maxEntry);
    }

    private double getMaxSumValueFromFeatures(Map<String, Double> vector) {
        return vector.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

}
