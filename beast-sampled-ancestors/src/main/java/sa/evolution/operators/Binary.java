package sa.evolution.operators;

/**
 * @author Alexandra Gavryushkina
 */

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.inference.parameter.IntScalarParam;

@Description("Implement an operator that jumps between zero and one")
public class Binary extends Operator {

    public final Input<IntScalarParam<? extends NonNegativeInt>> parameterInput = new Input<>("parameter", "binary parameter", Input.Validate.REQUIRED);

    double point;

    @Override
    public void initAndValidate() {
    }

    @Override
    public double proposal() {

        if (parameterInput.get().get() == 0) {
            parameterInput.get().set(1);
        } else {
            parameterInput.get().set(0);
        }

        return 0.0;


    }

}
