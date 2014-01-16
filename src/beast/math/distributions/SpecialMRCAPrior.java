package beast.math.distributions;

import beast.core.Input;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.tree.Node;

/**
 * Alexandra Gavryushkina
 */

public class SpecialMRCAPrior extends MRCAPrior {

    public final Input<TaxonSet> taxonsetOutInput = new Input<TaxonSet>("taxonsetOut",
            "set of taxa which are outside of the clade");

    public void initAndValidate() throws Exception {


    }

    @Override
    public double calculateLogP() throws Exception {
        logP = 0;
        return logP;
    }

    private boolean hasExtantDescendant(Node node){
        if (node.isLeaf()) {
            return node.getHeight() == 0;
        }
        return hasExtantDescendant(node.getLeft()) || hasExtantDescendant(node.getRight());
    }

}
