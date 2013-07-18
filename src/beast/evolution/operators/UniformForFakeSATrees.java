package beast.evolution.operators;

import beast.core.Description;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */
@Description("Randomly selects true internal tree node (i.e. not the root and not a fake node) and move node height uniformly in interval " +
        "restricted by the nodes parent and children.")
public class UniformForFakeSATrees extends TreeOperator {

    @Override
    public void initAndValidate() {
    }

    /**
     * change the parameter and return the hastings ratio.
     *
     * @return log of Hastings Ratio, or Double.NEGATIVE_INFINITY if proposal should not be accepted *
     */
    @Override
    public double proposal() {
        final Tree tree = m_tree.get(this);
        final int nNodeCount = tree.getNodeCount();
        int leafNodeCount = tree.getLeafNodeCount();

        //make sure that there is at least one non-fake node
        int fakeNodeCount = tree.getDirectAncestorNodeCount();
        if (fakeNodeCount == leafNodeCount-1 || (fakeNodeCount == leafNodeCount-2 && !tree.getRoot().isFake())) {
            return Double.NEGATIVE_INFINITY;
        }

        // randomly select internal node
        Node node;
        do {
            node = tree.getNode(leafNodeCount + Randomizer.nextInt(nNodeCount / 2));
        } while (node.isRoot() || node.isFake());
        final double fUpper = node.getParent().getHeight();
        final double fLower = Math.max(node.getLeft().getHeight(), node.getRight().getHeight());
        final double newValue = (Randomizer.nextDouble() * (fUpper - fLower)) + fLower;
        node.setHeight(newValue);

        return 0.0;
    }

}
