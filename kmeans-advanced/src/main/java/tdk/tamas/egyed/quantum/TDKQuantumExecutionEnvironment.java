package tdk.tamas.egyed.quantum;

import com.gluonhq.strange.*;
import com.gluonhq.strange.gate.Identity;
import com.gluonhq.strange.gate.PermutationGate;
import com.gluonhq.strange.gate.ProbabilitiesGate;
import com.gluonhq.strange.gate.Swap;
import com.gluonhq.strange.local.Computations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TDKQuantumExecutionEnvironment implements QuantumExecutionEnvironment {
    static void dbg(String s) {
        Complex.dbg(s);
    }

    @Override
    public TDKResult runProgram(Program p) {
        dbg("runProgram ");
        int nQubits = p.getNumberQubits();
        Qubit[] qubit = new Qubit[nQubits];
        for (int i = 0; i < nQubits; i++) {
            qubit[i] = new Qubit();
        }
        int dim = 1 << nQubits;
        double[] initalpha = p.getInitialAlphas();
        Complex[] probs = new Complex[dim];
        for (int i = 0; i < dim; i++) {
            probs[i] = Complex.ONE;
            for (int j = 0; j < nQubits; j++) {
                int pw = nQubits - j - 1;
                int pt = 1 << pw;
                int div = i / pt;
                int md = div % 2;
                if (md == 0) {
                    probs[i] = probs[i].mul(initalpha[j]);
                } else {
                    probs[i] = probs[i].mul(Math.sqrt(1 - initalpha[j] * initalpha[j]));
                }
            }
        }
        List<Step> steps = p.getSteps();
        List<Step> simpleSteps = p.getDecomposedSteps();
        if (simpleSteps == null) {
            simpleSteps = new ArrayList<>();
            for (Step step : steps) {
                simpleSteps.addAll(Computations.decomposeStep(step, nQubits));
            }
            p.setDecomposedSteps(simpleSteps);
        }
        TDKResult result = new TDKResult(nQubits, steps.size());
        int cnt = 0;
        if (simpleSteps.isEmpty()) {
            result.setIntermediateProbability(0, probs);
        }
        dbg("START RUN, number of steps = " + simpleSteps.size());
        for (Step step : simpleSteps) {
            if (!step.getGates().isEmpty()) {
                dbg("RUN STEP " + step + ", cnt = " + cnt);
                cnt++;
                dbg("before this step, probs = ");
                //      printProbs(probs);
                probs = applyStep(step, probs, qubit);
                dbg("after this step, probs = " + probs);
                //    printProbs(probs);
                int idx = step.getComplexStep();
                //       System.err.println("complex? "+idx);
                if (idx > -1) {
                    result.setIntermediateProbability(idx, probs);
                }
            }
        }
        dbg("DONE RUN, probability vector = " + probs);
        printProbs(probs);
        double[] qp = calculateQubitStatesFromVector(probs);
        for (int i = 0; i < nQubits; i++) {
            qubit[i].setProbability(qp[i]);
        }
        result.measureSystem();
        p.setResult(result);
        return result;
    }

    @Override
    public void runProgram(Program p, Consumer<Result> result) {
        Thread t = new Thread(() -> result.accept(runProgram(p)));
        t.start();
    }

    private void printProbs(Complex[] p) {
        Complex.printArray(p);
    }


    private List<Step> decomposeSteps(List<Step> steps) {
        return steps;
    }

    private Complex[] applyStep(Step step, Complex[] vector, Qubit[] qubits) {
        dbg("start applystep");
        long s0 = System.currentTimeMillis();
        List<Gate> gates = step.getGates();
        if (!gates.isEmpty() && gates.get(0) instanceof ProbabilitiesGate) {
            return vector;
        }
        if (gates.size() == 1 && gates.get(0) instanceof PermutationGate) {
            PermutationGate pg = (PermutationGate) gates.get(0);
            return TDKComputations.permutateVector(vector, pg.getIndex1(), pg.getIndex2());
        }

        Complex[] result = new Complex[vector.length];
        boolean vdd = true;
        if (vdd) {
            result = TDKComputations.calculateNewState(gates, vector, qubits.length);
        } else {
            dbg("start calcstepmatrix with gates " + gates);
            Complex[][] a = calculateStepMatrix(gates, qubits.length);
            dbg("done calcstepmatrix");
            dbg("vector");
            // printProbs(vector);
            if (a.length != result.length) {
                System.err.println("fatal issue calculating step for gates " + gates);
                throw new RuntimeException("Wrong length of matrix or probability vector: expected " + result.length + " but got " + a.length);
            }
            dbg("start matrix-vector multiplication for vector size = " + vector.length);
            for (int i = 0; i < vector.length; i++) {
                result[i] = Complex.ZERO;
                for (int j = 0; j < vector.length; j++) {
                    result[i] = result[i].add(a[i][j].mul(vector[j]));
                }
            }
        }
        long s1 = System.currentTimeMillis();
        dbg("done applystep took " + (s1 - s0));

        return result;
    }

    private Complex[][] calculateStepMatrix(List<Gate> gates, int nQubits) {
        return Computations.calculateStepMatrix(gates, nQubits);

    }

    // replaced by the similar function on Complex
    @Deprecated
    public Complex[][] tensor(Complex[][] a, Complex[][] b) {
        int d1 = a.length;
        int d2 = b.length;
        Complex[][] result = new Complex[d1 * d2][d1 * d2];
        for (int rowa = 0; rowa < d1; rowa++) {
            for (int cola = 0; cola < d1; cola++) {
                for (int rowb = 0; rowb < d2; rowb++) {
                    for (int colb = 0; colb < d2; colb++) {
                        result[d2 * rowa + rowb][d2 * cola + colb] = a[rowa][cola].mul(b[rowb][colb]);
                    }
                }
            }
        }
        return result;
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

    public Complex[][] createPermutationMatrix(int first, int second, int n) {
        Complex[][] swapMatrix = new Swap().getMatrix();
        Complex[][] iMatrix = new Identity().getMatrix();
        Complex[][] answer = iMatrix;
        int i = 1;
        if (first == 0) {
            answer = swapMatrix;
            i++;
        }
        while (i < n) {
            if (i == first) {
                i++;
                answer = tensor(answer, swapMatrix);
            } else {
                answer = tensor(answer, iMatrix);
            }
            i++;
        }
        return answer;
    }


}
