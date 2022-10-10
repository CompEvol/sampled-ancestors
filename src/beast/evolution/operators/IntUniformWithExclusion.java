package beast.evolution.operators;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.Parameter;
import beast.base.util.Randomizer;

/**
 * Created by agav755 on 26/06/14.
 */
@Description("Assign one or more parameter values (excluding negative values) to a uniformly selected value in its range.")
public class IntUniformWithExclusion extends Operator {

    public Input<IntegerParameter> parameterInput = new Input<IntegerParameter>("parameter", "an integer parameter to sample individual values for", Input.Validate.REQUIRED, Parameter.class);

    Parameter<?> parameter;
    int iLower, iUpper;

    @Override
    public void initAndValidate() {
        parameter = parameterInput.get();
        if (parameter instanceof IntegerParameter) {
            iLower = (Integer) parameter.getLower();
            iUpper = (Integer) parameter.getUpper();
        } else {
            throw new RuntimeException("parameter should be an IntergerParameter, not " + parameter.getClass().getName());
        }
    }

    @Override
    public double proposal() {

        int index;
        do {
            index = Randomizer.nextInt(parameter.getDimension());
        } while (((IntegerParameter)parameter).getValue(index) < 0);


        int newValue = Randomizer.nextInt(iUpper - iLower + 1) + iLower; // from 0 to n-1, n must > 0,
        ((IntegerParameter) parameter).setValue(index, newValue);

        return 0.0;
    }
}
