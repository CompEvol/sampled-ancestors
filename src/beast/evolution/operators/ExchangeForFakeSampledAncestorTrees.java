package beast.evolution.operators;

/**
 * @Alexandra Gavryushkina
 */


import beast.core.Description;
import beast.core.Input;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

/**
 *@author Alexandra Gavryushkina
 */

@Description("Implements branch exchange operations. There is a NARROW and WIDE variety. " +
        "The narrow exchange is very similar to a rooted-beast.tree nearest-neighbour " +
        "interchange but with the restriction that node height must remain consistent.")
public class ExchangeForFakeSampledAncestorTrees extends TreeOperator {

    public Input<Boolean> m_bIsNarrow = new Input<Boolean>("isNarrow", "if true (default) a narrow exchange is performed, otherwise a wide exchange", true);

    @Override
    public void initAndValidate() {
    }

    /**
     * override this for proposals,
     *
     * @return log of Hastings Ratio, or Double.NEGATIVE_INFINITY if proposal should not be accepted *
     */
    @Override
    public double proposal() {
        final Tree tree = m_tree.get(this);

        double fLogHastingsRatio = 0;

        if (m_bIsNarrow.get()) {
            fLogHastingsRatio = narrow(tree);
        } else {
            fLogHastingsRatio = wide(tree);
        }

        return fLogHastingsRatio;
    }

    public double narrow(final Tree tree) {

        // make sure that the tree has at least two internal nodes and at least one non-fake node
        final int nInternalNodes = tree.getInternalNodeCount();
        if (nInternalNodes <= 1 || tree.getDirectAncestorNodeCount() == nInternalNodes ) {
            return Double.NEGATIVE_INFINITY;
        }

        // if there is only one non-fake internal node, make sure that it has at least one non-leaf child
        if (tree.getDirectAncestorNodeCount() == nInternalNodes-1) {
            for (int inx = 0; inx < nInternalNodes ; inx++) {
                Node parent = tree.getNode(nInternalNodes+ 1 + inx);
                if (!parent.isFake() && parent.getLeft().isLeaf() && parent.getRight().isLeaf()) {
                    return Double.NEGATIVE_INFINITY;
                }
            }
        }

        Node iGrandParent = tree.getNode(nInternalNodes + 1 + Randomizer.nextInt(nInternalNodes));
        while (iGrandParent.isFake() && iGrandParent.getLeft().isLeaf() && iGrandParent.getRight().isLeaf()) {
            iGrandParent = tree.getNode(nInternalNodes + 1 + Randomizer.nextInt(nInternalNodes));
        }

        Node iParent = iGrandParent.getLeft();
        Node iUncle = iGrandParent.getRight();
        if (iParent.getHeight() < iUncle.getHeight()) {
            iParent = iGrandParent.getRight();
            iUncle = iGrandParent.getLeft();
        }

        if( iParent.isLeaf() ) {
            // tree with dated tips
            return Double.NEGATIVE_INFINITY;
        }

        final Node i;

        if (iParent.isFake()) {
            if (iParent.getLeft().isDirectAncestor()) {
                i = iParent.getRight();
            }  else i = iParent.getLeft();
        }  else {
            i = (Randomizer.nextBoolean() ? iParent.getLeft() : iParent.getRight());
        }

        exchangeNodes(i, iUncle, iParent, iGrandParent);
        return 0;


    }

    /**
     * WARNING: Assumes strictly bifurcating beast.tree. with more than 2 tips
     * @param tree
     */
    public double wide(final Tree tree) {

        final int nodeCount = tree.getNodeCount();

        Node i = tree.getRoot();

        while (i.isRoot() || i.isDirectAncestor()) {
            i = tree.getNode(Randomizer.nextInt(nodeCount));
        }

        Node j = i;
        while (j.getNr() == i.getNr() || j.isRoot() || j.isDirectAncestor()) {
            j = tree.getNode(Randomizer.nextInt(nodeCount));
        }

        final Node iP = i.getParent();
        final Node jP = j.getParent();

        if ((iP != jP) && (i != jP) && (j != iP)
                && (j.getHeight() < iP.getHeight())
                && (i.getHeight() < jP.getHeight())) {
            exchangeNodes(i, j, iP, jP);
            // System.out.println("tries = " + tries+1);
            return 0;
        }
        // Couldn't find valid wide move on this beast.tree!
        return Double.NEGATIVE_INFINITY;
    }


    /* exchange sub-trees whose root are i and j */

    protected void exchangeNodes(Node i, Node j,
                                 Node iP, Node jP) {
        // precondition iP -> i & jP -> j
        replace(iP, i, j);
        replace(jP, j, i);
        // postcondition iP -> j & iP -> i
    }

}
