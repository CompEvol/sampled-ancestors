package beast.evolution.operators;

/**
 * @author Alexandra Gavryushkina
 */

import beast.core.Description;
import beast.core.Input;
import beast.core.Operator;
import beast.core.parameter.IntegerParameter;

@Description("Implement an operator that jumps between zero and one")
public class Binary extends Operator {

    public final Input<IntegerParameter> parameterInput = new Input<IntegerParameter>("parameter", "binary parameter", Input.Validate.REQUIRED);

    double point;

    @Override
    public void initAndValidate() throws Exception {
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
