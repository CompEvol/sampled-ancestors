package sa.evolution.operators;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.spec.domain.Int;
import beast.base.spec.inference.parameter.IntVectorParam;
import beast.base.util.Randomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by agav755 on 26/06/14.
 */
@Description("A generic operator swapping a one or more pairs in a multi-dimensional integer parameter")
public class IntSwapWithExclusion extends Operator {

    public Input<IntVectorParam<? extends Int>> parameterInput = new Input<>("parameter", "an integer parameter to swap individual values for", Input.Validate.REQUIRED);


    IntVectorParam<? extends Int> parameter;
    private List<Integer> masterList = null;

    @Override
    public void initAndValidate() {
        parameter = parameterInput.get();

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < parameter.size(); i++) {
            list.add(i);
        }
        masterList = Collections.unmodifiableList(list);
    }

    @Override
    public double proposal() {

        List<Integer> allIndices = new ArrayList<>(masterList);
        int left, right;

        do {
            if (allIndices.isEmpty()) {
                return Double.NEGATIVE_INFINITY;
            }
            left = allIndices.remove(Randomizer.nextInt(allIndices.size()));
        } while (parameter.get(left) < 0);

        do {
            if (allIndices.isEmpty()) {
                return Double.NEGATIVE_INFINITY;
            }
            right = allIndices.remove(Randomizer.nextInt(allIndices.size()));
        }   while (parameter.get(right) < 0);

        parameter.swap(left, right);

        return 0.0;
    }
}
