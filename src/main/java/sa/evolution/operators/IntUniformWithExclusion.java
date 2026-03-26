package sa.evolution.operators;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.spec.domain.Int;
import beast.base.spec.inference.parameter.IntVectorParam;
import beast.base.util.Randomizer;

/**
 * Created by agav755 on 26/06/14.
 */
@Description("Assign one or more parameter values (excluding negative values) to a uniformly selected value in its range.")
public class IntUniformWithExclusion extends Operator {

    public Input<IntVectorParam<? extends Int>> parameterInput = new Input<>("parameter", "an integer parameter to sample individual values for", Input.Validate.REQUIRED);

    IntVectorParam<? extends Int> parameter;
    int iLower, iUpper;

    @Override
    public void initAndValidate() {
        parameter = parameterInput.get();
        iLower = parameter.getLower();
        iUpper = parameter.getUpper();
    }

    @Override
    public double proposal() {

        int index;
        do {
            index = Randomizer.nextInt(parameter.size());
        } while (parameter.get(index) < 0);


        int newValue = Randomizer.nextInt(iUpper - iLower + 1) + iLower; // from 0 to n-1, n must > 0,
        parameter.set(index, newValue);

        return 0.0;
    }
}
