package beast.evolution.tree;

import beast.core.Distribution;
import beast.core.Input;
import beast.core.State;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.evolution.alignment.Taxon;
import beast.evolution.alignment.TaxonSet;

import java.util.*;

/**
 * Gavryushkina Alexandra
 */
public class CladeConstraint  extends Distribution {
    public Input<Tree> treeInput = new Input<Tree>("tree","tree to apply the constraint on", Input.Validate.REQUIRED);
    public final Input<TaxonSet> taxonsetInInput = new Input<TaxonSet>("taxonsetIn", "set of taxa inside the clade", Input.Validate.REQUIRED);
    public final Input<TaxonSet> taxonsetOutInput = new Input<TaxonSet>("taxonsetOut", "set of taxa outside the clade");
    public final Input<Boolean> isStronglyMonophyleticInput = new Input<Boolean>("stronglyMonophyletic",
            "whether the taxon set is monophyletic (forms a clade without other taxa) or nor. Default is false.", false);

    public Input<Alignment> taxaInput = new Input<Alignment>("taxa","set taxa to choose from", Input.Validate.REQUIRED);

    private List<String>[] nodeTipDescendats;
    private Tree tree;

    private List<String> taxaNamesByNodeNr;
    private List<String> taxaNamesInClade;
    private List<String> taxaNamesOutClade;
    private int holds=0;


    @SuppressWarnings("unchecked")
    @Override
    public void initAndValidate() throws Exception {
        nodeTipDescendats = new ArrayList[treeInput.get().getNodeCount()];
        collectNodeTipDescendants(treeInput.get().getRoot());
        tree = treeInput.get();
        tree.collectTaxaNames(tree.getRoot());
        taxaNamesByNodeNr = new ArrayList<String>(Arrays.asList(tree.m_sTaxaNames));
        taxaNamesInClade = taxonsetInInput.get().asStringList();
        taxaNamesOutClade = taxonsetOutInput.get().asStringList();



    } // initAndValidate

    @Override
    public double calculateLogP() throws Exception {
        holds = 0;
        nodeTipDescendats = new ArrayList[treeInput.get().getNodeCount()];
        collectNodeTipDescendants(treeInput.get().getRoot());
        if (holds == 1) {
            logP = 0.0;
            return 0.0;
        }
        logP = Double.NEGATIVE_INFINITY;
        return Double.NEGATIVE_INFINITY;
    }

    private void collectNodeTipDescendants(Node node){
        if (holds != 0) {
            return;
        }
        int iNode=node.getNr();
        if (node.isLeaf()){
            nodeTipDescendats[iNode].add(node.getID());
        } else {
            collectNodeTipDescendants(node.getLeft());
            collectNodeTipDescendants(node.getRight());
            nodeTipDescendats[iNode].addAll(nodeTipDescendats[node.getRight().getNr()]);
            nodeTipDescendats[iNode].addAll(nodeTipDescendats[node.getRight().getNr()]);
            // check if this node is a common ancestor for the in taxa and if so check if it is not an ancestor for
            // out taxa.
            boolean isCA = true;
            for (String taxaName:taxaNamesInClade){
                if (!nodeTipDescendats[iNode].contains(taxaName)){
                    isCA = false;
                    break;
                }
            }
            if (isCA){
                boolean contain = false;
                for (String taxaName:taxaNamesOutClade){
                    if (nodeTipDescendats[iNode].contains(taxaName)){
                        contain = true;
                        break;
                    }
                }
                if (!contain) {
                    if (isStronglyMonophyleticInput.get()) {
                        if (hasExtantDescendant(node.getLeft()) && hasExtantDescendant(node.getRight())){
                            holds = 1;
                        } else {
                            holds = 2;
                        }
                    } else {
                        holds = 1;
                    }
                } else {
                    holds = 2;
                }
            }
        }
    }

    private boolean hasExtantDescendant(Node node){
        if (node.isLeaf()) {
            return node.getHeight() == 0;
        }
        return hasExtantDescendant(node.getLeft()) || hasExtantDescendant(node.getRight());
    }

    @Override public List<String> getArguments() {return null;}
    @Override public List<String> getConditions() {return null;}
    @Override public void sample(State state, Random random) {}
}
