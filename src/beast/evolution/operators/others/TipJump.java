package beast.evolution.operators.others;

import beast.evolution.operators.TreeOperator;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */
public class TipJump extends TreeOperator {

    @Override
    public void initAndValidate() {
    }

    @Override
    public double proposal() {

        Tree tree = m_tree.get();

        int leafNodeCount = tree.getLeafNodeCount();

        Node i, iP, CiP;

        i = tree.getNode(Randomizer.nextInt(leafNodeCount));
        iP = i.getParent();
        CiP = getOtherChild(iP, i);

        if (CiP.isDirectAncestor()) {
            return Double.NEGATIVE_INFINITY;
        }

        CiP = getOtherChild(iP, i);

        // make sure that there is at least one edge to attach a leaf to
        if (iP.getParent() == null && CiP.getHeight() <= i.getHeight()) {
            return Double.NEGATIVE_INFINITY;
        }

        Node j, jP;

        int nodeCount = tree.getNodeCount();
        double newParentHeight;
        do {
            j = tree.getNode(Randomizer.nextInt(nodeCount));
            if (j.isRoot()) {
                newParentHeight = Double.POSITIVE_INFINITY;
            } else {
                newParentHeight = j.getParent().getHeight();
            }
        } while (j.getNr() == i.getNr() || j.getNr() == iP.getNr() || j.getNr() == CiP.getNr() || i.getHeight() >= newParentHeight);

        boolean makeDirectAncestor = Randomizer.nextBoolean();

        // make sure that a new direct ancestor height is higher than its new child
        if (makeDirectAncestor && i.getHeight() <= j.getHeight()) {
            return Double.NEGATIVE_INFINITY;
        }

        jP = j.getParent();

        double newRange, newAge, newMinAge, oldRange, oldMinAge, fHastingsRatio;

        //Hastings numerator calculation + newAge of iP
        if (makeDirectAncestor) {
            newRange = 1;
            newAge = i.getHeight();
        } else {
            if (jP != null) {
                newMinAge = Math.max(i.getHeight(), j.getHeight());
                newRange = jP.getHeight() - newMinAge;
                newAge = newMinAge + (Randomizer.nextDouble() * newRange);
            } else {
                double randomNumberFromExponential;
                randomNumberFromExponential = Randomizer.nextExponential(1);
                newRange = Math.exp(randomNumberFromExponential);
                newAge = j.getHeight() + randomNumberFromExponential;
            }
        }

        Node PiP = iP.getParent();

        //Hastings denominator calculation
        if (i.isDirectAncestor()) {
            oldRange = 1;
        }
        else {
            oldMinAge = Math.max(i.getHeight(), CiP.getHeight());
            if (PiP != null) {
                oldRange = PiP.getHeight() - oldMinAge;
            } else {
                oldRange = Math.exp(iP.getHeight() - oldMinAge);
            }
        }

        //update
        iP.removeChild(CiP); //remove <iP, CiP>

        if (PiP != null) {
            PiP.removeChild(iP);   // remove <PiP,iP>
            PiP.addChild(CiP);   // add <PiP, CiP>
            PiP.makeDirty(Tree.IS_FILTHY);
            CiP.makeDirty(Tree.IS_FILTHY);
        } else {
            CiP.setParent(null); // completely remove <iP, CiP>
            tree.setRootOnly(CiP);
        }

        if (jP != null) {
            jP.removeChild(j);  // remove <jP, j>
            jP.addChild(iP);   // add <jP, iP>
            jP.makeDirty(Tree.IS_FILTHY);
        } else {
            iP.setParent(null); // completely remove <PiP, iP>
            tree.setRootOnly(iP);
        }
        iP.addChild(j);
        iP.makeDirty(Tree.IS_FILTHY);
        j.makeDirty(Tree.IS_FILTHY);

        iP.setHeight(newAge);

        fHastingsRatio = Math.abs(newRange / oldRange);

        return Math.log(fHastingsRatio);

    }

}
