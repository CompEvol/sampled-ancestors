package sa.evolution.tree;

import beast.base.inference.Distribution;
import beast.base.core.Input;
import beast.base.inference.State;
import beast.base.evolution.alignment.TaxonSet;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;

import java.io.PrintStream;
import java.util.*;

/**
 * Gavryushkina Alexandra
 */
public class CladeConstraint  extends Distribution {
    public Input<Tree> treeInput = new Input<Tree>("tree","tree to apply the constraint on", Input.Validate.REQUIRED);
    public final Input<TaxonSet> taxonsetInInput = new Input<TaxonSet>("taxonsetIn", "set of taxa inside the clade, in-taxa.",
            Input.Validate.REQUIRED);
    public final Input<TaxonSet> taxonsetOutInput = new Input<TaxonSet>("taxonsetOut", "set of taxa outside the clade. If it is " +
            "not specified then the in-taxa are considered to be monophyletic.");
    public final Input<Boolean> isStronglyMonophyleticInput = new Input<Boolean>("stronglyMonophyletic",
            "is true if the most recent common ancestor of the clade is not a sampled node and" +
            "therefore has two children which both have extant descendants. Default is false.", false);

    private Tree tree;
    private int nodeCount;

    private List<String> taxaNamesInClade;
    private List<String> taxaNamesOutClade;
    private boolean outCladeExist=false;
    private int holds=0;
    private double mrcaHeight;
    private double storedMrcaHeight;


    @Override
    public void initAndValidate() {
        tree = treeInput.get();
        nodeCount=tree.getNodeCount();
        taxaNamesInClade = taxonsetInInput.get().asStringList();
        if (taxonsetOutInput.get() != null) {
            taxaNamesOutClade = taxonsetOutInput.get().asStringList();
            outCladeExist = true;
        }
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
        if (holds == 1) {
            logP = 0.0;
            return 0.0;
        }
        logP = Double.NEGATIVE_INFINITY;
        return Double.NEGATIVE_INFINITY;
    }

    private ArrayList<String> collectNodeTipDescendants(Node node){
        ArrayList<String> tipDescendant = new ArrayList<String>();
        // if holds isn't equal to 0 then the most recent common ancestor have already been found
        if (node.isLeaf()){
            tipDescendant.add(node.getID());
        } else {
            tipDescendant.addAll(collectNodeTipDescendants(node.getLeft()));
            if (holds != 0) {
                return tipDescendant;
            }
            tipDescendant.addAll(collectNodeTipDescendants(node.getRight()));
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
                boolean contain = false;
                if (outCladeExist) {
                    for (String taxaName:taxaNamesOutClade){
                        if (tipDescendant.contains(taxaName)){
                            contain = true;
                            break;
                        }
                    }
                } else {
                    for (String taxaName:tipDescendant){
                        if (!taxaNamesInClade.contains(taxaName)){
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
        return tipDescendant;
    }

    public static boolean hasExtantDescendant(Node node){
        if (node.isLeaf()) {
            return node.getHeight() == 0;
        }
        return hasExtantDescendant(node.getLeft()) || hasExtantDescendant(node.getRight());
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
