package beast.evolution.operators;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.RealParameter;
import beast.base.evolution.operator.TreeOperator;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */

@Description("Implements a narrow move between trees of different dimensions (number of nodes in trees)." +
        "It takes a random sampled node which is either a leaf with the younger sibling" +
        "or a sampled internal node. In the first case, the leaf becomes a sampled internal node by replacing its " +
        "parent (the height of the leaf remains unchanged). In the second case, the sampled internal node becomes " +
        "a leaf by inserting a new parent node at a height which is uniformly chosen on the interval " +
        " between the sampled node height and its old parent height.")
public class LeafToSampledAncestorJump extends TreeOperator {

    public Input<IntegerParameter> categoriesInput = new Input<IntegerParameter>("rateCategories", "rate category per branch");

    public Input<RealParameter> rInput =
            new Input<RealParameter>("removalProbability", "The probability of an individual to be removed from the process immediately after the sampling");

    @Override
    public void initAndValidate() {
    }

    @Override
    public double proposal() {

        double newHeight, newRange, oldRange;
        int categoryCount = 1;
        if (categoriesInput.get() != null) {

            categoryCount = categoriesInput.get().getUpper() - categoriesInput.get().getLower() +1;
        }

        Tree tree = treeInput.get();

        int leafNodeCount = tree.getLeafNodeCount();

        Node leaf = tree.getNode(Randomizer.nextInt(leafNodeCount));
        Node parent = leaf.getParent();

        if (leaf.isDirectAncestor()) {
            oldRange = (double) 1;
            if (parent.isRoot()) {
                final double randomNumber = Randomizer.nextExponential(1);
                newHeight = parent.getHeight() + randomNumber;
                newRange = Math.exp(randomNumber);
            } else {
                newRange = parent.getParent().getHeight() - parent.getHeight();
                newHeight = parent.getHeight() + Randomizer.nextDouble() * newRange;
            }

            if (categoriesInput.get() != null) {
                int index = leaf.getNr();
                int newValue = Randomizer.nextInt(categoryCount) + categoriesInput.get().getLower(); // from 0 to n-1, n must > 0,
                categoriesInput.get().setValue(index, newValue);
            }
        } else {
            newRange = (double) 1;
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
            if  (categoriesInput.get() != null) {
                int index = leaf.getNr();
                categoriesInput.get().setValue(index, -1);
            }
        }
        parent.setHeight(newHeight);

        //make sure that either there are no direct ancestors or r<1
        if ((rInput.get() != null) && (tree.getDirectAncestorNodeCount() > 0 && rInput.get().getValue() == 1))  {
            return Double.NEGATIVE_INFINITY;
        }

        return Math.log(newRange/oldRange);
    }
}
