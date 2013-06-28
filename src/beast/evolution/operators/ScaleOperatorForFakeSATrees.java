package beast.evolution.operators;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

/**
 * Alexandra Gavryushkina
 */
public class ScaleOperatorForFakeSATrees extends ScaleOperator {

    @Override
    public double proposal() {

        double hastingsRatio;
        final double scale = getScaler();

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
                //final int nInternalNodes = tree.scale(scale);
                //return Math.log(scale) * (nInternalNodes - 2);
            }
        }

        return Double.NEGATIVE_INFINITY;
    }

}
