package beast.evolution.operators;

import beast.core.Description;
import beast.core.Input;
import beast.core.parameter.BooleanParameter;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.evolution.tree.ZeroBranchSANode;
import beast.evolution.tree.ZeroBranchSATree;

/**
 * @author Alexandra Gavryushkina
 */
@Description("")
public class ScaleOperatorForFakeSATrees extends ScaleOperator {

    public Input<Boolean> m_pScaleSNodes = new Input<Boolean>("scaleSampledNodes", "If it is true then sampled node dates are scaled (default false).", false);

    @Override   //WARNING works with bifurcating (exactly 2 children) trees only
                // sampled ancestors are assumed to be on zero branches

    public double proposal() {

        final double scale = getScaler();
        final boolean scaleSNodes = m_pScaleSNodes.get();

        try {

            if (m_bIsTreeScaler) {
                Tree tree = treeInput.get(this);
                if (rootOnlyInput.get()) {
                    Node root = tree.getRoot();
                    if (((ZeroBranchSANode)root).isFake() && !scaleSNodes) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    double fNewHeight = root.getHeight() * scale;

                    //make sure the new height doesn't make a parent younger than a child
                    double oldestChildHeight;
                    if (((ZeroBranchSANode)root).isFake()) {
                        oldestChildHeight = root.getRight().getHeight();
                        if (((ZeroBranchSANode)root.getLeft()).isDirectAncestor()) {
                            oldestChildHeight = root.getLeft().getHeight();
                        }
                    } else oldestChildHeight = Math.max(root.getLeft().getHeight(), root.getRight().getHeight());
                    if (fNewHeight < oldestChildHeight) {
                        return Double.NEGATIVE_INFINITY;
                    }

                    root.setHeight(fNewHeight);
                    if (((ZeroBranchSANode)root).isFake() && scaleSNodes) {
                        Node directAncestor = root.getLeft();
                        if (!((ZeroBranchSANode)directAncestor).isDirectAncestor())
                            directAncestor = root.getRight();
                        directAncestor.setHeight(fNewHeight);
                    }
                    return -Math.log(scale);
                } else {
                    // scale the beast.tree
                    final int nScaledDimensions = ((ZeroBranchSATree)tree).scale(scale, scaleSNodes);
                    return Math.log(scale) * (nScaledDimensions - 2);
                }
            }
            return Double.NEGATIVE_INFINITY;

        }  catch (Exception e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

}
