package tdk.tamas.egyed.quantum;

import com.gluonhq.strange.Complex;
import com.gluonhq.strange.gate.SingleQubitGate;

public class RZ extends SingleQubitGate {

    private final Complex[][] matrix;
    private final double expv;
    private int pow = -1;

    public RZ(double exp, int idx) {
        super(idx);
        this.expv = exp;
        matrix =  new Complex[][]{
                {new Complex(Math.cos(exp/2), (-1)*Math.sin(exp/2)), Complex.ZERO},
                {Complex.ZERO, new Complex(Math.cos(exp/2), Math.sin(exp/2))}
        };
    }

    @Override
    public Complex[][] getMatrix() {
        return matrix;
    }

    @Override public String getCaption() {
        return "RZ" + ((pow> -1)? Integer.toString(pow): "th");
    }

}
