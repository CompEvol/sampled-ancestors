package beast.evolution.operators;

import beast.core.Input;
import beast.core.Operator;
import beast.core.parameter.IntegerParameter;
import beast.util.Randomizer;

/**
 * Created by agav755 on 26/06/14.
 */
public class IntRandomWalkWithExclusion extends Operator {

    public Input<Integer> windowSizeInput =
            new Input<Integer>("windowSize", "the size of the window both up and down", Input.Validate.REQUIRED);
    public Input<IntegerParameter> parameterInput =
            new Input<IntegerParameter>("parameter", "the parameter to operate a random walk on.", Input.Validate.REQUIRED);

    int windowSize = 1;

    public void initAndValidate() {
        windowSize = windowSizeInput.get();
    }

    /**
     * override this for proposals,
     * returns log of hastingRatio, or Double.NEGATIVE_INFINITY if proposal should not be accepted *
     */
    @Override
    public double proposal() {

        final IntegerParameter param = parameterInput.get(this);
        int index;
        do {
             index = Randomizer.nextInt(param.getDimension());
        } while (param.getValue(index) < 0);

        final int i = index;
        final int value = param.getValue(i);
        final int newValue = value + Randomizer.nextInt(2 * windowSize + 1) - windowSize;

        if (newValue < param.getLower() || newValue > param.getUpper()) {
            // invalid move, can be rejected immediately
            return Double.NEGATIVE_INFINITY;
        }
        if (newValue == value) {
            // this saves calculating the posterior
            return Double.NEGATIVE_INFINITY;
        }

        param.setValue(i, newValue);

        return 0.0;
    }

    @Override
    public void optimize(final double logAlpha) {
        // nothing to optimise
    }
}
