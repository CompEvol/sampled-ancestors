package sa.evolution.tree;

import beast.base.core.Description;
import beast.base.inference.Distribution;
import beast.base.core.Input;
import beast.base.inference.State;
import beast.base.evolution.alignment.Taxon;
import beast.base.evolution.alignment.TaxonSet;
import beast.base.evolution.tree.MRCAPrior;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;

import java.io.PrintStream;
import java.util.*;

/**
 * @author: Denise Kuehnert, April 2020
 * adapted from Alexandra Gavryushkina's CladeConstraint
 */
@Description("Method to force extinct samples to be ancestral to a set of taxa in the tree. " +
        "This is meant to be used for sensitivity analyses and testing purposes only. ")
public class AncestryConstraint extends MRCAPrior {
    public final Input<TaxonSet> taxonsetInInput = new Input<TaxonSet>("taxonsetIn", "set of taxa inside the clade, in-taxa.",
            Input.Validate.REQUIRED);
    public final Input<TaxonSet> taxonsetOutInput = new Input<TaxonSet>("taxonsetOut", "set of taxa outside the clade. If it is " +
            "not specified then the in-taxa are considered to be monophyletic.");
    public final Input<Taxon> ancestorInput = new Input<Taxon>("directAncestor", "direct ancestor of the taxonsetIn clade. ");
    public final Input<Boolean> strictInput = new Input<Boolean>("strict",
        "enforce ancestry strictly (default), or allow breaking it with low probablity?", true    );

    private Tree tree;
    private int nodeCount;

    private List<String> taxaNamesInClade;
    private List<String> taxaNamesOutClade;
    private boolean outCladeExist=false;
    private String ancestorName;
    private int holds=0;
    private double mrcaHeight;
    private double storedMrcaHeight;
    private Node mrca;
    private Node storedMrca;


    @SuppressWarnings("unchecked")
    @Override
    public void initAndValidate() {
        tree = treeInput.get();
        nodeCount=tree.getNodeCount();
        taxaNamesInClade = taxonsetInInput.get().asStringList();
        if (taxonsetOutInput.get() != null) {
            taxaNamesOutClade = taxonsetOutInput.get().asStringList();
            outCladeExist = true;
        }
        ancestorName =  ancestorInput.get().getID() ;

        collectNodeTipDescendants(tree.getRoot());

    } // initAndValidate

    public double getMRCAHeight(){
        return mrcaHeight;
    }

    @Override
    public double calculateLogP() {
        holds = 0;
        collectNodeTipDescendants(tree.getRoot());
        if (holds == 0) {
            throw new RuntimeException("the most recent common ancestor of a clade is not found.");
        }
        if (!mrca.isRoot() && holds == 1 ) {
            if (taxaNamesInClade.size()==1 && mrca.getChildCount()==2) {
                if (isCorrectDirectAncestor(mrca.getChild(0)) || isCorrectDirectAncestor(mrca.getChild(1))){
                    logP = 0.0;
                    return logP;
                }
            }

            Node sibling = mrca.getParent().getChild(0);

            if (sibling==mrca)
                sibling = mrca.getParent().getChild(1);

            if (sibling!=mrca){


            }

            if (isCorrectDirectAncestor(sibling)){
                logP = 0.0;
                return logP;
            }

        }
        logP = strictInput.get() ?  Double.NEGATIVE_INFINITY : -1E5;
        return logP;
    }

    private Boolean isCorrectDirectAncestor(Node node){
        return (node.isDirectAncestor() && node.getID().equalsIgnoreCase(ancestorName));
    }

    private ArrayList<String> collectNodeTipDescendants(Node node){
        ArrayList<String> tipDescendant = new ArrayList<String>();
        // if holds isn't equal to 0 then the most recent common ancestor have already been found
        if (node.isLeaf()){
            tipDescendant.add(node.getID());
        } else {
            tipDescendant.addAll(collectNodeTipDescendants(node.getChild(0)));
            if (holds != 0) {
                return tipDescendant;
            }
            if (node.getChildCount()>1)
                tipDescendant.addAll(collectNodeTipDescendants(node.getChild(1)));
            if (holds != 0) {
                return tipDescendant;
            }

            // check if this node is a common ancestor for the in-taxa and if so check if it is not an ancestor for
            // out-taxa. Note that the node which is first found as a common ancestor is the most recent common
            // ancestor due to the way the tree is traversed - from tips towards the root.
            boolean isCommonAncestor = true;
            for (String taxaName:taxaNamesInClade){
                if (!tipDescendant.contains(taxaName)){
                    isCommonAncestor = false;
                    break;
                }
            }
            if (isCommonAncestor){
                mrcaHeight = node.getHeight();
                mrca=node;
                boolean contain = false;
                if (outCladeExist) {
                    for (String taxaName:taxaNamesOutClade){
                        if (tipDescendant.contains(taxaName)){
                            contain = true;
                            break;
                        }
                    }
                }
                if(isMonophyleticInput.get()){
//                if(monophyleticInput.get()){

                    for (String taxaName:tipDescendant){
                        if (!taxaNamesInClade.contains(taxaName)){
                            if (!taxaName.equalsIgnoreCase(ancestorName))
                                    contain = true;
                            break;
                        }
                    }
                }
                holds = contain ? 2 : 1 ;
            }
        }
        return tipDescendant;
    }


    /**
     * CalculationNode methods *
     */
    @Override
    public void store() {
        storedMrcaHeight = mrcaHeight;
        super.store();
    }

    @Override
    public void restore() {
        mrcaHeight = storedMrcaHeight;
        super.restore();
    }

    @Override
    public void log(final long nSample, final PrintStream out) {
        out.print(mrcaHeight + "\t");
    }


    @Override public List<String> getArguments() {return null;}
    @Override public List<String> getConditions() {return null;}
    @Override public void sample(State state, Random random) {}
}
