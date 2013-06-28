package beast.evolution.operators;

/**
 * @Alexandra Gavryushkina
 */


import beast.core.Description;
import beast.core.Input;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

@Description("Implement Narrow and Wide Exchange for sampled ancestor trees." +
        "Narrow move chooses a random internal node (not a fake node) with two non-leaf children." +
        "Then it takes the older child of this node and exchange one of its children (or just a child" +
        "if there is only one) with the younger child. Wide remains the same as for regular trees.")
public class ExchangeForFakeSampledAncestorTrees extends Exchange {

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
     * WARNING: Assumes strictly bifurcating beast.tree.
     * @param tree
     */
    public double wide(final Tree tree) {

        final int nodeCount = tree.getNodeCount();

        Node i, j, iP, jP;

        //make sure that there are at least two distinct non-root nodes which are not direct ancestors.
        if (tree.getNodeCount() == 3 && tree.getRoot().isFake()) {
            return Double.NEGATIVE_INFINITY;
        }

        do {
            i = tree.getNode(Randomizer.nextInt(nodeCount));
        } while (i.isRoot() || i.isDirectAncestor());


        do {
            j = tree.getNode(Randomizer.nextInt(nodeCount));
        } while (j.getNr() == i.getNr() || j.isRoot() || j.isDirectAncestor());

        iP = i.getParent();
        jP = j.getParent();

        if ((iP != jP) && (i != jP) && (j != iP)
                && (j.getHeight() < iP.getHeight())
                && (i.getHeight() < jP.getHeight())) {
            exchangeNodes(i, j, iP, jP);

            return 0;
        }
        // Couldn't find valid wide move on this beast.tree!
        return Double.NEGATIVE_INFINITY;
    }

}
