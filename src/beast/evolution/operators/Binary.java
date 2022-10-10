package beast.evolution.operators;

/**
 * @author Alexandra Gavryushkina
 */

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.inference.parameter.IntegerParameter;

@Description("Implement an operator that jumps between zero and one")
public class Binary extends Operator {

    public final Input<IntegerParameter> parameterInput = new Input<IntegerParameter>("parameter", "binary parameter", Input.Validate.REQUIRED);

    double point;

    @Override
    public void initAndValidate() {
    }

    @Override
    public double proposal() {

        if (parameterInput.get().getValue() == 0) {
            parameterInput.get().setValue(1);
        } else {
            parameterInput.get().setValue(0);
        }

        return 0.0;


    }

}
