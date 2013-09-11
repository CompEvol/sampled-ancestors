package beast.evolution.tree;

import beast.core.Description;

/**
 * @author  Alexandra Gavryushkina
 */

@Description("Test class")
public abstract class SampledAncestorHelpers {

    public int getDirectAncestorNodeCount(Tree tree) {
        int directAncestorNodeCount = 0;
        for (int i = 0; i < tree.getLeafNodeCount(); i++) {
            if (isDirectAncestor(tree.getNode(i))) {
                directAncestorNodeCount += 1;
            }
        }
        return directAncestorNodeCount;
    }

    //is true if this leaf actually represents a direct ancestor (i.e. is on the end of a zero-length branch)
    public boolean isDirectAncestor(Node node) {
        return (!node.isRoot() && node.getParent().getHeight() == node.getHeight());
    }

    //is true if this internal node is "fake" (i.e. one of its children is a direct ancestor)
    //works only for trees where each node has at least 2 children
    public boolean isFake(Node node) {
        if (node.isLeaf())
            return false;
        return (node.getLeft().isDirectAncestor() || (node.getRight() != null && node.getRight().isDirectAncestor()));
    }

    /**
     * Scales nodes in tree (either all nodes, or only non-sampled nodes)
     *
     * @param tree         the tree to scale
     * @param fScale      the scalar to multiply each scaled node age by
     * @param scaleSNodes true if sampled nodes should be scaled as well as internal nodes, false if only non-sampled
     *                    internal nodes should be scaled.
     * @return the number of nodes that were scaled.
     * @throws Exception
     */
    public int scale(Tree tree, double fScale, boolean scaleSNodes) throws Exception {
        tree.getRoot().scale(fScale, scaleSNodes);
        if (scaleSNodes) {
            return tree.getNodeCount() - getDirectAncestorNodeCount(tree);
        } else {
            return tree.getInternalNodeCount() - getDirectAncestorNodeCount(tree);
        }
    }




}
