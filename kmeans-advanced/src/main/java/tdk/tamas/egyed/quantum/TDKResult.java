package tdk.tamas.egyed.quantum;

import com.gluonhq.strange.Complex;
import com.gluonhq.strange.Qubit;
import com.gluonhq.strange.Result;

public class TDKResult extends Result {

    private int nqubits;
    private int nsteps;

    private Qubit[] qubits;
    private Complex[] probability;
    private Complex[][] intermediates;
    private int measuredProbability = -1;

    public TDKResult(int nqubits, int steps) {
        super(nqubits, steps);
        assert(steps >= 0);
        this.nqubits = nqubits;
        this.nsteps = steps;
        intermediates = new Complex[steps > 0 ? steps : 1][];
    }

    public TDKResult(Qubit[] q, Complex[] p) {
        super(q, p);
        this.qubits = q;
        this.probability = p;
    }

    @Override
    public Qubit[] getQubits() {
        if (this.qubits == null) {

            this.qubits = calculateQubits();
        }
        return this.qubits;
    }

    private Qubit[] calculateQubits() {
        Qubit[] answer = new Qubit[nqubits];
        if (nqubits == 0) {
            return answer;
        }
        double[] d = calculateQubitStatesFromVector(intermediates[nsteps-1]);
        for (int i = 0; i < answer.length; i++) {
            answer[i] = new Qubit();
            answer[i].setProbability(d[i]);
        }
        return answer;
    }

    @Override
    public Complex[] getProbability() {
        return this.probability;
    }

    @Override
    public void setIntermediateProbability(int step, Complex[] p) {
        this.intermediates[step] = p;
        this.probability = p;
    }

    @Override
    public Complex[] getIntermediateProbability(int step) {
        return intermediates[step];
    }

    private double[] calculateQubitStatesFromVector(Complex[] vectorresult) {
        int nq = (int) Math.round(Math.log(vectorresult.length) / Math.log(2));
        double[] answer = new double[nq];
        int ressize = 1 << nq;
        for (int i = 0; i < nq; i++) {
            int pw = i;//nq - i - 1;
            int div = 1 << pw;
            for (int j = 0; j < ressize; j++) {
                int p1 = j / div;
                if (p1 % 2 == 1) {
                    answer[i] = answer[i] + vectorresult[j].abssqr();
                }
            }
        }
        return answer;
    }

    /**
     * Based on the probabilities of the system, this method will measure all qubits.
     * When this method is called, the <code>measuredValue</code> value of every qubit
     * contains a possible measurement value. The values are consistent for the entire system.
     * (e.g. when an entangled pair is measured, its values are equal)
     * However, different invocations of this method may result in different values.
     */
    @Override
    public void measureSystem() {
        if (this.qubits == null) {
            this.qubits = getQubits();
        }
        double random = Math.random();
        int ressize = 1 << nqubits;
        double[] probamp = new double[ressize];
        double probtot = 0;
        // we don't need all probabilities, but we might use this later
        for (int i = 0; i < ressize; i++) {
            probamp[i] = this.probability[i].abssqr();
        }
        int sel = 0;
        probtot = probamp[0];
        while (probtot < random && sel < (probamp.length - 1)) {
            sel++;
            probtot = probtot + probamp[sel];
        }
        this.measuredProbability = sel;
        double outcome = probamp[sel];
        for (int i = 0; i < nqubits; i++) {
            qubits[i].setMeasuredValue(sel %2 == 1);
            sel = sel/2;
        }
    }

    /**
     * Returns a measurement based on the probability vector
     * @return an integer representation of the measurement
     */
    @Override
    public int getMeasuredProbability() {
        return measuredProbability;
    }

    /**
     * Print info about this result to stdout
     */
    @Override
    public void printInfo() {
        System.out.println("Info about Quantum Result");
        System.out.println("==========================");
        System.out.println("Number of qubits = "+nqubits+", number of steps = "+nsteps);
        for (int i = 0; i < probability.length;i++) {
            System.out.println("Probability on "+i+":"+ probability[i].abssqr());
        }
        System.out.println("==========================");
    }
}
