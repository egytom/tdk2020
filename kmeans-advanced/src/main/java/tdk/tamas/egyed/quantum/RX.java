package tdk.tamas.egyed.quantum;

import com.gluonhq.strange.Complex;
import com.gluonhq.strange.gate.SingleQubitGate;

public class RX extends SingleQubitGate {

    private final Complex[][] matrix;
    private final double expv;
    private int pow = -1;

    public RX(double exp, int idx) {
        super(idx);
        this.expv = exp;
        matrix = new Complex[][]{
                {new Complex(Math.cos(exp / 2), 0), new Complex(0, (-1)*Math.sin(exp / 2))},
                {new Complex(0, (-1)*Math.sin(exp / 2)), new Complex(Math.cos(exp / 2), 0)}
        };
    }

    @Override
    public Complex[][] getMatrix() {
        return matrix;
    }

    @Override
    public String getCaption() {
        return "RX" + ((pow > -1) ? Integer.toString(pow) : "th");
    }

}

