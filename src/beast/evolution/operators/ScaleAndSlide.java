package beast.evolution.operators;

import beast.evolution.tree.Tree;
import beast.evolution.tree.ZeroBranchSATree;

/**
 * Alexandra Gavryushkina
 * AT THE DEVELOPING STAGE
 */

public class ScaleAndSlide extends ScaleOperator {

    @Override   //WARNING works with bifurcating (exactly 2 children) trees only
                // sampled ancestors are assumed to be on zero branches
    public double proposal() {

        final double scale = getScaler();

        System.out.println("tree was " + treeInput.get().getRoot().toShortNewick(false));

        try {

            if (m_bIsTreeScaler) {
                Tree tree = treeInput.get(this);
                if (rootOnlyInput.get()) {
                    throw new Exception("ScaleAndSlide operator can not have input rootOnly");
                } else {
                    // scale the beast.tree
                    return ((ZeroBranchSATree)tree).scaleAndSlide(scale);
                }
            }
            return Double.NEGATIVE_INFINITY;

        }  catch (Exception e) {
            return Double.NEGATIVE_INFINITY;
        }
    }

}
