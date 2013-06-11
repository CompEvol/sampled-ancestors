package beast.evolution.operators;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

import java.util.ArrayList;
import java.util.List;

/**
 *@author Alexandra Gavryushkina
 */

@Description("Randomly selects sampled node (not the root node) and move node height either uniformly in interval " +
        "restricted by the nodes parent and child if there is a child or uniformly (or with gaussian distribution)" +
        " within 2 window sizes.")
public class SampledNodeDatesRandomWalker extends TreeOperator {

    public Input<Double> windowSizeInput =
            new Input<Double>("windowSize", "the size of the window both up and down when using uniform interval OR standard deviation when using Gaussian", Input.Validate.REQUIRED);
    public Input<Boolean> useGaussianInput =
            new Input<Boolean>("useGaussian", "Use Gaussian to move instead of uniform interval. Default false.", false);

    double windowSize = 1;
    boolean m_bUseGaussian;

    @Override
    public void initAndValidate() throws Exception {
        windowSize = windowSizeInput.get();
        m_bUseGaussian = useGaussianInput.get();
    }

    @Override
    public double proposal() {
        final Tree tree = m_tree.get(this);

        //randomly select a sampled node
        final int nNodeCount = tree.getNodeCount(); //TODO reimplement this for the case then an array of nodes contain nulls
        Node node;
        do {
            final int iNodeNr = Randomizer.nextInt(nNodeCount);
            node = tree.getNode(iNodeNr);
        } while (node == null || node.isRoot() || node.getID() == null);

        double value = node.getHeight();
        double newValue = value;

        if (node.isLeaf()) {     // if it is a leaf then use windowSize
            if (m_bUseGaussian) {
                newValue += Randomizer.nextGaussian() * windowSize;
            } else {
                newValue += Randomizer.nextDouble() * 2 * windowSize - windowSize;
            }
            if (newValue > node.getParent().getHeight() || newValue < 0.0) {
                return Double.NEGATIVE_INFINITY;
            }

        } else {      // if it is an internal node then chose a new height uniformly from the interval bounded by parent and child heights
          final double range = node.getParent().getHeight() - node.getLeft().getHeight();
          newValue = node.getLeft().getHeight() + Randomizer.nextDouble() * (range);
        }

        if (newValue == value) {
            // this saves calculating the posterior
            return Double.NEGATIVE_INFINITY;
        }
        node.setHeight(newValue);

        return 0.0;

    }

}
