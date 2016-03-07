package beast.evolution.operators;

import beast.core.Description;
import beast.core.Input;
import beast.core.Operator;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.Parameter;
import beast.util.Randomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by agav755 on 26/06/14.
 */
@Description("A generic operator swapping a one or more pairs in a multi-dimensional integer parameter")
public class IntSwapWithExclusion extends Operator {

    public Input<IntegerParameter> parameterInput = new Input<IntegerParameter>("parameter", "an integer parameter to swap individual values for", Input.Validate.REQUIRED);


    Parameter<?> parameter;
    private List<Integer> masterList = null;

    @Override
    public void initAndValidate() {
        parameter = parameterInput.get();

        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < parameter.getDimension(); i++) {
            list.add(i);
        }
        masterList = Collections.unmodifiableList(list);
    }

    @Override
    public double proposal() {

        List<Integer> allIndices = new ArrayList<Integer>(masterList);
        int left, right;

        do {
            if (allIndices.isEmpty()) {
                return Double.NEGATIVE_INFINITY;
            }
            left = allIndices.remove(Randomizer.nextInt(allIndices.size()));
        } while (((IntegerParameter)parameter).getValue(left) < 0);

        do {
            if (allIndices.isEmpty()) {
                return Double.NEGATIVE_INFINITY;
            }
            right = allIndices.remove(Randomizer.nextInt(allIndices.size()));
        }   while (((IntegerParameter)parameter).getValue(right) < 0);

        parameter.swap(left, right);

        return 0.0;
    }
}
