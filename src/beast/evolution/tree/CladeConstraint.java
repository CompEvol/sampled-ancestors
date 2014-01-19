package beast.evolution.tree;

import beast.core.Distribution;
import beast.core.Input;
import beast.core.State;
import beast.evolution.alignment.TaxonSet;

import java.util.*;

/**
 * Gavryushkina Alexandra
 */
public class CladeConstraint  extends Distribution {
    public Input<Tree> treeInput = new Input<Tree>("tree","tree to apply the constraint on", Input.Validate.REQUIRED);
    public final Input<TaxonSet> taxonsetInInput = new Input<TaxonSet>("taxonsetIn", "set of taxa inside the clade, in-ta", Input.Validate.REQUIRED);
    public final Input<TaxonSet> taxonsetOutInput = new Input<TaxonSet>("taxonsetOut", "set of taxa outside the clade");
    public final Input<Boolean> isStronglyMonophyleticInput = new Input<Boolean>("stronglyMonophyletic",
            "whether the taxon set is monophyletic (forms a clade without other taxa) or nor. Default is false.", false);

    private List<String>[] nodeTipDescendats;
    private Tree tree;
    private int nodeCount;

    private List<String> taxaNamesInClade;
    private List<String> taxaNamesOutClade;
    private boolean outCladeExist=false;
    private int holds=0;


    @SuppressWarnings("unchecked")
    @Override
    public void initAndValidate() throws Exception {
        tree = treeInput.get();
        nodeCount=tree.getNodeCount();
        taxaNamesInClade = taxonsetInInput.get().asStringList();
        if (taxonsetOutInput.get() != null) {
            taxaNamesOutClade = taxonsetOutInput.get().asStringList();
            outCladeExist = true;
        }
        nodeTipDescendats = new ArrayList[nodeCount];
        collectNodeTipDescendants(tree.getRoot());

    } // initAndValidate

    @Override
    public double calculateLogP() throws Exception {
        holds = 0;
        nodeTipDescendats = new ArrayList[nodeCount];
        collectNodeTipDescendants(tree.getRoot());
        if (holds == 0) {
            throw new Exception("the most recent common ancestor of a clade is not found.");
        }
        if (holds == 1) {
            logP = 0.0;
            return 0.0;
        }
        logP = Double.NEGATIVE_INFINITY;
        return Double.NEGATIVE_INFINITY;
    }

    private void collectNodeTipDescendants(Node node){
        int iNode=node.getNr();
        nodeTipDescendats[iNode] = new ArrayList<String>();
        // if holds isn't equal to 0 then the most recent common ancestor have already been found


        if (node.isLeaf()){
            nodeTipDescendats[iNode].add(node.getID());
        } else {
            collectNodeTipDescendants(node.getLeft());
            collectNodeTipDescendants(node.getRight());
            nodeTipDescendats[iNode].addAll(nodeTipDescendats[node.getLeft().getNr()]);
            if (holds != 0) {
                return;
            }
            nodeTipDescendats[iNode].addAll(nodeTipDescendats[node.getRight().getNr()]);
            if (holds != 0) {
                return;
            }
            // check if this node is a common ancestor for the in-taxa and if so check if it is not an ancestor for
            // out-taxa. Note that the node which is first found as a common ancestor is the most recent common
            // ancestor due to the way the tree is traversed - from tip nodes towards the root. 
            boolean isCommonAncestor = true;   
            for (String taxaName:taxaNamesInClade){
                if (!nodeTipDescendats[iNode].contains(taxaName)){
                    isCommonAncestor = false;
                    break;
                }
            }
            if (isCommonAncestor){
                boolean contain = false;
                if (outCladeExist) {
                    for (String taxaName:taxaNamesOutClade){
                        if (nodeTipDescendats[iNode].contains(taxaName)){
                            contain = true;
                            break;
                        }
                    }
                }
                if (!contain) {
                    if (isStronglyMonophyleticInput.get()) {
                        holds = (hasExtantDescendant(node.getLeft()) && hasExtantDescendant(node.getRight()))?1:2; 
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
