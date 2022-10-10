package beast.evolution.operators;

import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.inference.parameter.RealParameter;
import beast.base.evolution.tree.Tree;
import beast.base.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */
public class JumpToPoint extends Operator {

    public final Input<RealParameter> rInput = new Input<RealParameter>("removalProbability", "parameter r", Input.Validate.REQUIRED);

    public final Input<Tree> treeInput = new Input<Tree>("tree", "tree", Input.Validate.REQUIRED);

    public Input<Double> pointInput = new Input<Double>("point", "the point on which the probability mass is put", 1.0);

    double point;

    @Override
    public void initAndValidate() {
        point = pointInput.get();
        if (point < 0 || point > 1) {
            throw new IllegalArgumentException("Point in JumpToPoint operator has to be between 0 and 1");
        }
    }

    @Override
    public double proposal() {

        Tree tree = treeInput.get();
        final RealParameter rParameter = rInput.get();

        double r = rParameter.getValue();

        if (r != 1 && tree.getDirectAncestorNodeCount() == 0) {
            rParameter.setValue(1.);
            return 0.0;
        } else if (r == 1) {
            rParameter.setValue(Randomizer.nextDouble());
            return 0.0;
        } else return Double.NEGATIVE_INFINITY;


    }

}
