package beast.evolution.operators;

/**
 * @Alexandra Gavryushkina
 */


import beast.core.Description;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

@Description("Implement Narrow and Wide Exchange for sampled ancestor trees." +
        "Narrow move chooses a random internal node (not a fake node) with two non-leaf children." +
        "Then it takes the older child of this node and exchange one of its children (or just a child" +
        "if there is only one) with the younger child. Wide remains the same as for regular trees.")
public class SAExchange extends Exchange {

    public double narrow(final Tree tree) {

        final int nodeCount = tree.getNodeCount();

        //make sure that there are at least two distinct non-root nodes which are not direct ancestors.
        if (nodeCount == 3 && (tree.getRoot()).isFake()) {
            return Double.NEGATIVE_INFINITY;
        }

        Node i;
        do {
            i = tree.getNode(Randomizer.nextInt(nodeCount));
        } while (i.isRoot() || i.getParent().isRoot() || (i).isDirectAncestor());

        final Node iParent = i.getParent();
        final Node iGrandParent = iParent.getParent();
        Node iUncle = iGrandParent.getLeft();
        if (iUncle.getNr() == iParent.getNr()) {
            iUncle = iGrandParent.getRight();
            //assert (iUncle.getNr() != iParent.getNr());
        }
        //assert iUncle == getOtherChild(iGrandParent, iParent);

        //assert i.getHeight() <= iGrandParent.getHeight();

        if (iUncle.getHeight() < iParent.getHeight()) {
            exchangeNodes(i, iUncle, iParent, iGrandParent);
            return 0.0;
        } else {
            // Couldn't find valid narrow move on this beast.tree!!
            return Double.NEGATIVE_INFINITY;
        }

//        final int nInternalNodes = tree.getInternalNodeCount();  //TODO look if I can implement this more efficient code for SA trees
//        final int leafNodeCount = tree.getLeafNodeCount();
//        // make sure that the tree has at least two internal nodes
//        if (nInternalNodes <= 1 ) {
//            return Double.NEGATIVE_INFINITY;
//        }
//
//        //choose one of internal nodes that has at least one non-leaf node
//        //(there is always at least one such node as long as the tree has at least 2 internal nodes)
//        Node iGrandParent;
//        do  {
//            iGrandParent = tree.getNode(leafNodeCount + Randomizer.nextInt(nInternalNodes));
//        } while (iGrandParent.getLeft().isLeaf() && iGrandParent.getRight().isLeaf());
//
//        Node iParent = iGrandParent.getLeft();
//        Node iUncle = iGrandParent.getRight();
//        if (iParent.getHeight() < iUncle.getHeight()) {
//            iParent = iGrandParent.getRight();
//            iUncle = iGrandParent.getLeft();
//        }
//
//        if( iParent.isLeaf() ) {
//            return Double.NEGATIVE_INFINITY;
//        }
//
//        final Node i;
//
//        if (iParent.isFake()) {
//            if (iParent.getLeft().isDirectAncestor()) {
//                i = iParent.getRight();
//            }  else i = iParent.getLeft();
//        }  else {
//            i = (Randomizer.nextBoolean() ? iParent.getLeft() : iParent.getRight());
//        }
//
//        exchangeNodes(i, iUncle, iParent, iGrandParent);
//
//        return 0.0;

    }

    /**
     * @param tree
     */
    public double wide(final Tree tree) {

        final int nodeCount = tree.getNodeCount();

        //make sure that there are at least two distinct non-root nodes which are not direct ancestors.
        if (nodeCount == 3 && (tree.getRoot()).isFake()) {
            return Double.NEGATIVE_INFINITY;
        }

        Node i, j, iP, jP;
        do {
            i = tree.getNode(Randomizer.nextInt(nodeCount));
        } while (i.isRoot() || (i).isDirectAncestor());

        do {
            j = tree.getNode(Randomizer.nextInt(nodeCount));
        } while (j.getNr() == i.getNr() || j.isRoot() || (j).isDirectAncestor());

        iP = i.getParent();
        jP = j.getParent();

        if ((iP != jP) && (i != jP) && (j != iP)
                && (j.getHeight() < iP.getHeight())
                && (i.getHeight() < jP.getHeight())) {
            exchangeNodes(i, j, iP, jP);

            return 0.0;
        }

        // Couldn't find valid wide move on this beast.tree!
        return Double.NEGATIVE_INFINITY;
    }

}
