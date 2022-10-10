package beast.evolution.operators;

import beast.base.core.Description;
import beast.base.evolution.operator.ScaleOperator;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
/**
 * @author Alexandra Gavryushkina
 */
@Description("")
public class SAScaleOperator extends ScaleOperator {

    //public Input<Boolean> m_pScaleSNodes = new Input<Boolean>("scaleSampledNodes", "If it is true then sampled node dates are scaled (default false).", false);

    @Override   //WARNING works with bifurcating (exactly 2 children) trees only
    // sampled ancestors are assumed to be on zero branches

    public double proposal() {

        final double scale = getScaler();
        final boolean scaleSNodes = false; // m_pScaleSNodes.get();

        try {

            if (isTreeScaler()) {
                Tree tree = treeInput.get();
                tree.startEditing(this);
                if (rootOnlyInput.get()) {
                    Node root = tree.getRoot();
                    if ((root).isFake() && !scaleSNodes) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    double fNewHeight = root.getHeight() * scale;

                    //make sure the new height doesn't make a parent younger than a child
                    double oldestChildHeight;
                    if ((root).isFake()) {
                        oldestChildHeight = root.getNonDirectAncestorChild().getHeight();
                    } else oldestChildHeight = Math.max(root.getLeft().getHeight(), root.getRight().getHeight());
                    if (fNewHeight < oldestChildHeight) {
                        return Double.NEGATIVE_INFINITY;
                    }

                    root.setHeight(fNewHeight);

                    return -Math.log(scale);
                } else {
                    // scale the beast.tree
                    final int nScaledDimensions = tree.scale(scale);
                    //final int nScaledDimensions = tree.scale(scale, scaleSNodes);
                    return Math.log(scale) * (nScaledDimensions - 2);
                }
            }
            return Double.NEGATIVE_INFINITY;

        }  catch (Exception e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

}
