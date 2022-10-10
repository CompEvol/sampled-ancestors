package sa.math.distributions;

import beast.base.core.Input;
import beast.base.evolution.alignment.TaxonSet;
import beast.base.evolution.tree.MRCAPrior;
import beast.base.evolution.tree.Node;

/**
 * Alexandra Gavryushkina
 */

public class SpecialMRCAPrior extends MRCAPrior {

    public final Input<TaxonSet> taxonsetOutInput = new Input<TaxonSet>("taxonsetOut",
            "set of taxa which are outside of the clade");

    public void initAndValidate() {


    }

    @Override
    public double calculateLogP() {
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
