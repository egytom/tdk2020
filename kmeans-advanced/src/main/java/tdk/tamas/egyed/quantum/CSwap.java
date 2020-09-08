package tdk.tamas.egyed.quantum;

import com.gluonhq.strange.Complex;
import com.gluonhq.strange.gate.ThreeQubitGate;

public class CSwap extends ThreeQubitGate {

    Complex[][] matrix = new Complex[][]{
            {Complex.ONE, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO},
            {Complex.ZERO, Complex.ONE, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO},
            {Complex.ZERO, Complex.ZERO, Complex.ONE, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO},
            {Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ONE, Complex.ZERO, Complex.ZERO},
            {Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ONE, Complex.ZERO, Complex.ZERO, Complex.ZERO},
            {Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ONE, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO},
            {Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ONE, Complex.ZERO},
            {Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ZERO, Complex.ONE}
    };

    public CSwap() {
    }

    public CSwap(int a, int b, int c) {
        super(a, b, c);
    }

    @Override
    public Complex[][] getMatrix() {
        return matrix;
    }

    @Override
    public String getCaption() {
        return "CS";
    }

}
