package beast.evolution.operators;

import beast.core.Description;
import beast.core.Input;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.evolution.tree.ZeroBranchSANode;
import beast.evolution.tree.ZeroBranchSATree;
import beast.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */
@Description("Implement Wilson Balding operator for (zero branch) sampled ancestor trees and per branch rate categories" +
        "This version of Wilson Balding operator does not change the root.  ")
public class WilsonBaldingWithRateCategories extends TreeOperator {

    public Input<IntegerParameter> categoriesInput = new Input<IntegerParameter>("rateCategories", "rate category per branch");

    @Override
    public void initAndValidate() {
    }

    /**
     * @return log of Hastings Ratio, or Double.NEGATIVE_INFINITY if proposal should not be accepted *
     */
    @Override
    public double proposal() {

        Tree tree = treeInput.get(this);

        //double x0 = 10;

        double oldMinAge, newMinAge, newRange, oldRange, newAge, fHastingsRatio, DimensionCoefficient;
        int newDimension, oldDimension;

        // choose a random node avoiding root and leaves that are direct ancestors
        int nodeCount = tree.getNodeCount();

        if (nodeCount <=3 ) {
            return Double.NEGATIVE_INFINITY;
        }

        int categoryCount = 1;
        if (categoriesInput.get() != null) {
            categoryCount = categoriesInput.get().getUpper() - categoriesInput.get().getLower() +1;
        }

        Node i, iP;

        do {
            i = tree.getNode(Randomizer.nextInt(nodeCount));
            iP = i.getParent();
        } while (i.isRoot() || ((ZeroBranchSANode)i).isDirectAncestor() || iP.isRoot());


        Node CiP;
        if (iP.getLeft().getNr() == i.getNr()) {
            CiP = iP.getRight();
        } else {
            CiP = iP.getLeft();
        }

        // choose another random node to insert i above or to attach i to this node if it is a leaf
        Node j;
        Node jP;

        final int leafNodeCount = tree.getLeafNodeCount();

        // make sure that the target branch <jP, j> or target leaf j is above the subtree being moved
        int nodeNumber;
        double newParentHeight;
        boolean attachingToLeaf;
        boolean adjacentEdge;
        do {
            adjacentEdge = false;
            nodeNumber = Randomizer.nextInt(nodeCount + leafNodeCount);
            if (nodeNumber < nodeCount) {
                j = tree.getNode(nodeNumber);
                jP = j.getParent();
                if (jP != null)
                    newParentHeight = jP.getHeight();
                else newParentHeight = Double.POSITIVE_INFINITY;
                if (!((ZeroBranchSANode)CiP).isDirectAncestor())
                    adjacentEdge = (CiP.getNr() == j.getNr() || iP.getNr() == j.getNr());
                attachingToLeaf = false;
            } else {
                j = tree.getExternalNodes().get(nodeNumber - nodeCount);
                jP = j.getParent();
                newParentHeight = j.getHeight();
                attachingToLeaf = true;
                //adjacentLeaf = (iP.getNr() == j.getNr());
            }
        } while (jP == null || ((ZeroBranchSANode)j).isDirectAncestor() || (newParentHeight <= i.getHeight()) || (i.getNr() == j.getNr()) || adjacentEdge /*|| adjacentLeaf */);


        if (attachingToLeaf && iP.getNr() == j.getNr()) {
            System.out.println("Proposal failed because j = iP");
            return Double.NEGATIVE_INFINITY;
        }

        if (jP != null && jP.getNr() == i.getNr()) {
            System.out.println("Proposal failed because jP = i. Heights of i = " + i.getHeight() + " Height of jP = " + jP.getHeight());
            return Double.NEGATIVE_INFINITY;
        }

        oldDimension = nodeCount - ((ZeroBranchSATree)tree).getDirectAncestorNodeCount() - 2;

        int iPCategory = categoriesInput.get().getValue(iP.getNr()),
            jCategory = categoriesInput.get().getValue(j.getNr());

        //Hastings numerator calculation + newAge of iP
        if (attachingToLeaf) {
            newRange = (double) 1/categoryCount;
            newAge = j.getHeight();
            categoriesInput.get().setValue(iP.getNr(), jCategory);
            categoriesInput.get().setValue(j.getNr(), -1);
        } else {
            newMinAge = Math.max(i.getHeight(), j.getHeight());
            newRange = jP.getHeight() - newMinAge;
            newAge = newMinAge + (Randomizer.nextDouble() * newRange);
            if (CiP.isDirectAncestor()) {
                int newCategory = Randomizer.nextInt(categoryCount) + categoriesInput.get().getLower();
                categoriesInput.get().setValue(iP.getNr(), newCategory);
            }
        }

        Node PiP = iP.getParent();


        //Hastings denominator calculation
        if (((ZeroBranchSANode)CiP).isDirectAncestor()) {
            oldRange = (double) 1/categoryCount;
            categoriesInput.get().setValue(CiP.getNr(), iPCategory);
        }
        else {
            oldMinAge = Math.max(i.getHeight(), CiP.getHeight());
            oldRange = PiP.getHeight() - oldMinAge;
        }

        //update
        if (iP.getNr() != j.getNr() && CiP.getNr() != j.getNr()) {
            iP.removeChild(CiP); //remove <iP, CiP>
            PiP.removeChild(iP);   // remove <PiP,iP>
            PiP.addChild(CiP);   // add <PiP, CiP>
            PiP.makeDirty(Tree.IS_FILTHY);
            CiP.makeDirty(Tree.IS_FILTHY);
            jP.removeChild(j);  // remove <jP, j>
            jP.addChild(iP);   // add <jP, iP>
            jP.makeDirty(Tree.IS_FILTHY);
            iP.addChild(j);
            iP.makeDirty(Tree.IS_FILTHY);
            j.makeDirty(Tree.IS_FILTHY);
        }
        iP.setHeight(newAge);

        newDimension = nodeCount - ((ZeroBranchSATree)tree).getDirectAncestorNodeCount() - 2;
        DimensionCoefficient = (double) oldDimension / newDimension;

        fHastingsRatio = Math.abs(DimensionCoefficient * newRange / oldRange);

        return Math.log(fHastingsRatio);

    }

}
