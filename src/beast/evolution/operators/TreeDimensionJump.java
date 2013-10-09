package beast.evolution.operators;

import beast.core.Description;
import beast.core.Input;
import beast.core.parameter.RealParameter;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.evolution.tree.ZeroBranchSANode;
import beast.evolution.tree.ZeroBranchSATree;
import beast.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */

@Description("Implements a narrow move between trees of different dimensions (number of nodes in trees)." +
        "It takes a random sampled node which is either a leaf with the younger sibling" +
        "or a sampled internal node. In the first case, the leaf becomes a sampled internal node by replacing its " +
        "parent (the height of the leaf remains unchanged). In the second case, the sampled internal node becomes " +
        "a leaf by inserting a new parent node at a height which is uniformly chosen on the interval " +
        " between the sampled node height and its old parent height.")
public class TreeDimensionJump extends TreeOperator {

    public Input<RealParameter> rInput =
            new Input<RealParameter>("becomeNoninfectiousAfterSamplingProbability", "The probability of an individual to become noninfectious immediately after the sampling");

    @Override
    public void initAndValidate() {
    }

    @Override
    public double proposal() {

        double newHeight, newRange, oldRange;

        Tree tree = treeInput.get();

        int leafNodeCount = tree.getLeafNodeCount();

        Node leaf = tree.getNode(Randomizer.nextInt(leafNodeCount));
        Node parent = leaf.getParent();

        if (((ZeroBranchSANode)leaf).isDirectAncestor()) {
            oldRange = 1;
            if (parent.isRoot()) {
                final double randomNumber = Randomizer.nextExponential(1);
                newHeight = parent.getHeight() + randomNumber;
                newRange = Math.exp(randomNumber);
            } else {
                newRange = parent.getParent().getHeight() - parent.getHeight();
                newHeight = parent.getHeight() + Randomizer.nextDouble() * newRange;
            }
        } else {
            newRange = 1;
            //make sure that the branch where a new sampled node to appear is not above that sampled node
            if (getOtherChild(parent, leaf).getHeight() >= leaf.getHeight())  {
                return Double.NEGATIVE_INFINITY;
            }
            if (parent.isRoot()) {
                oldRange = Math.exp(parent.getHeight() - leaf.getHeight());
            } else {
                oldRange = parent.getParent().getHeight() - leaf.getHeight();
            }
            newHeight = leaf.getHeight();
        }
        parent.setHeight(newHeight);

        //make sure that either there are no direct ancestors or r<1
        if ((rInput.get() != null) && (((ZeroBranchSATree)tree).getDirectAncestorNodeCount() > 0 && rInput.get().getValue() == 1))  {
            return Double.NEGATIVE_INFINITY;
        }

        return Math.log(newRange/oldRange);
    }
}
