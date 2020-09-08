package tdk.tamas.egyed.quantum;

import com.gluonhq.strange.Complex;
import com.gluonhq.strange.gate.SingleQubitGate;

public class U3 extends SingleQubitGate {

    private final Complex[][] matrix;
    private int pow = -1;

    public U3(double alpha, double beta, double gamma, int idx) {
        super(idx);
        matrix =  new Complex[][]{
                {
                    new Complex(Math.cos(alpha/2), 0),
                    new Complex(Math.sin(alpha/2)*((-1)*(Math.cos(gamma))), (-1)*Math.sin(alpha/2)*Math.sin(gamma))
                },
                {
                    new Complex(Math.sin(alpha/2)*Math.cos(beta), Math.sin(alpha/2)*Math.sin(beta)),
                    new Complex(Math.cos(alpha/2)*Math.cos(alpha+gamma), Math.cos(alpha/2)*Math.sin(alpha+gamma))
                }
        };
    }

    @Override
    public Complex[][] getMatrix() {
        return matrix;
    }

    @Override public String getCaption() {
        return "U3" + ((pow> -1)? Integer.toString(pow): "th");
    }

}
