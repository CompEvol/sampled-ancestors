package sa.evolution.operators;

import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.spec.domain.UnitInterval;
import beast.base.spec.inference.parameter.RealScalarParam;
import beast.base.evolution.tree.Tree;
import beast.base.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */
public class JumpToPoint extends Operator {

    public final Input<RealScalarParam<? extends UnitInterval>> rInput = new Input<>("removalProbability", "parameter r", Input.Validate.REQUIRED);

    public final Input<Tree> treeInput = new Input<>("tree", "tree", Input.Validate.REQUIRED);

    public Input<Double> pointInput = new Input<>("point", "the point on which the probability mass is put", 1.0);

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
        final RealScalarParam<? extends UnitInterval> rParameter = rInput.get();

        double r = rParameter.get();

        if (r != 1 && tree.getDirectAncestorNodeCount() == 0) {
            rParameter.set(1.);
            return 0.0;
        } else if (r == 1) {
            rParameter.set(Randomizer.nextDouble());
            return 0.0;
        } else return Double.NEGATIVE_INFINITY;


    }

}
