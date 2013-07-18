package beast.evolution.operators;

import beast.core.Input;
import beast.core.parameter.BooleanParameter;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

/**
 * @author Alexandra Gavryushkina
 */
public class ScaleOperatorForFakeSATrees extends ScaleOperator {

    public Input<Boolean> m_pScaleTips = new Input<Boolean>("scaleTips", "If it is true then tip dates are not scaled (default false)", false);

    @Override
    public double proposal() {

        final double scale = getScaler();

        try {

            if (m_bIsTreeScaler) {
                Tree tree = m_pTree.get(this);
                if (m_pRootOnly.get()) {
                    Node root = tree.getRoot();
                    if (root.isFake()) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    double fNewHeight = root.getHeight() * scale;
                    if (fNewHeight < Math.max(root.getLeft().getHeight(), root.getRight().getHeight())) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    root.setHeight(fNewHeight);
                    return -Math.log(scale);
                } else {
                    // scale the beast.tree
                    final int nScaledDimensions = tree.scaleSATrees(scale, m_pScaleTips.get());
                    return Math.log(scale) * (nScaledDimensions - 2);
                }
            }
            return Double.NEGATIVE_INFINITY;

        }  catch (Exception e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

}
