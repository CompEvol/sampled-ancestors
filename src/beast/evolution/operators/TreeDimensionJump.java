package beast.evolution.operators;

import beast.core.Description;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */

@Description("Implements a narrow move between trees of different dimensions (number of nodes in trees)." +
        "It take a random sampled node which is either a leaf with the younger sibling (or no sibling)" +
        "or a sampled internal node. In the first case, the leaf becomes a sampled internal node by replacing its " +
        "parent and in the second case the sampled internal node becomes a leaf by inserting a new parent node between " +
        "this node and its old parent at a uniformly random height.")
public class TreeDimensionJump extends TreeOperator {

    @Override
    public void initAndValidate() {
    }

    @Override
    public double proposal() {

        double newHeight, newRange, oldRange, fHastingsRatio;

        Tree tree = m_tree.get();


        return 0.0;
    }
}
