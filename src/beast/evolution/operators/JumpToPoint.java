package beast.evolution.operators;

import beast.core.Input;
import beast.core.Operator;
import beast.core.parameter.RealParameter;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */
public class JumpToPoint extends Operator {

    public final Input<RealParameter> rInput = new Input<RealParameter>("becomeNoninfectiousAfterSamplingProbability", "parameter r", Input.Validate.REQUIRED);

    public final Input<Tree> treeInput = new Input<Tree>("tree", "tree", Input.Validate.REQUIRED);

    public Input<Double> pointInput = new Input<Double>("point", "the point on which the probability mass is put", 1.0);

    double point;

    @Override
    public void initAndValidate() throws Exception {
        point = pointInput.get();
        if (point < 0 || point > 1) {
            throw new Exception("Point in JumpToPoint operator have to be between 0 and 1");
        }
    }

    @Override
    public double proposal() {

        Tree tree = treeInput.get();
        final RealParameter rParameter = rInput.get(this);

        double r = rParameter.getValue();

        if (r != 1 && tree.getDirectAncestorNodeCount() == 0) {
            rParameter.setValue(1.);
            return Math.log((double)1/r);
        } else if (r == 1) {
            double u = Randomizer.nextDouble();
            rParameter.setValue(u);
            return Math.log(u);
        } else return Double.NEGATIVE_INFINITY;


    }

}
