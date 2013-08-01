package beast.evolution.operators;

import beast.core.Input;
import beast.core.parameter.BooleanParameter;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

/**
 * @author Alexandra Gavryushkina
 */
public class ScaleOperatorForFakeSATrees extends ScaleOperator {

    public Input<Boolean> m_pScaleSNodes = new Input<Boolean>("scaleSampledNodes", "If it is true then sampled node dates are scaled (default false).", false);

    @Override   //WARNING works with bifurcating (exactly 2 children) trees only
    public double proposal() {

        final double scale = getScaler();
        final boolean scaleSNodes = m_pScaleSNodes.get();

        try {

            if (m_bIsTreeScaler) {
                Tree tree = m_pTree.get(this);
                if (m_pRootOnly.get()) {
                    Node root = tree.getRoot();
                    if (root.isFake() && !scaleSNodes) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    double fNewHeight = root.getHeight() * scale;

                    //make sure the new height doesn't make a parent younger than a child
                    double oldestChildHeight;
                    if (root.isFake()) {
                        oldestChildHeight = root.getRight().getHeight();
                        if (root.getLeft().isDirectAncestor()) {
                            oldestChildHeight = root.getLeft().getHeight();
                        }
                    } else oldestChildHeight = Math.max(root.getLeft().getHeight(), root.getRight().getHeight());
                    if (fNewHeight < oldestChildHeight) {
                        return Double.NEGATIVE_INFINITY;
                    }

                    root.setHeight(fNewHeight);
                    if (root.isFake() && scaleSNodes) {
                        Node directAncestor = root.getLeft();
                        if (!directAncestor.isDirectAncestor())
                            directAncestor = root.getRight();
                        directAncestor.setHeight(fNewHeight);
                    }
                    return -Math.log(scale);
                } else {
                    // scale the beast.tree
                    final int nScaledDimensions = tree.scaleSATrees(scale, scaleSNodes);
                    return Math.log(scale) * (nScaledDimensions - 2);
                }
            }
            return Double.NEGATIVE_INFINITY;

        }  catch (Exception e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

}
