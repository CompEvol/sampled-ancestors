package beast.evolution.operators;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */
public class WilsonBaldingForFakeSampledAncestorTrees1 extends TreeOperator {

    @Override
    public void initAndValidate() {
    }

    /**
     * @return log of Hastings Ratio, or Double.NEGATIVE_INFINITY if proposal should not be accepted *
     */
    @Override
    public double proposal() {

        Tree tree = m_tree.get(this);

        //System.out.println("Tree before = " + tree.getRoot().toShortNewick(false));

        double oldMinAge, newMinAge, newRange, oldRange, newAge, fHastingsRatio, correction;
        double newCorrection = 1;
        double oldCorrection = 1;
        boolean makeIDirectAncestor, makeJDirectAncestor;

        // choose a random node avoiding root and leaves that are direct ancestors
        int nodeCount = tree.getNodeCount();
        Node i;

        do {
            i = tree.getNode(Randomizer.nextInt(nodeCount));
        } while (i.isRoot());

        Node iP = i.getParent();
        Node CiP;
        if (iP.getLeft().getNr() == i.getNr()) {
            CiP = iP.getRight();
        } else {
            CiP = iP.getLeft();
        }

        // make sure that there is at least one candidate edge to attach node iP to
        if (iP.getParent() == null && (CiP.getHeight() < i.getHeight() || CiP.isLeaf())) {
            return Double.NEGATIVE_INFINITY;
        }

        // choose another random node to insert i above or to attach i to this node if it is a leaf
        Node j;
        Node jP;

        // make sure that the target branch <jP, j> or target leaf j is above the subtree being moved

        int nodeNumber;
        double newParentHeight;
        boolean adjacentEdge;
        do {
            nodeNumber = Randomizer.nextInt(nodeCount);
            j = tree.getNode(nodeNumber);
            jP = j.getParent();
            if (jP != null)
                newParentHeight = jP.getHeight();
            else newParentHeight = Double.POSITIVE_INFINITY;
            adjacentEdge = (CiP.getNr() == j.getNr() || iP.getNr() == j.getNr());
        } while (j.isDirectAncestor() || (newParentHeight <= i.getHeight()) || (i.getNr() == j.getNr()) || adjacentEdge);

        //decide if attach the leaf as a direct ancestor
        if (i.isLeaf() && j.getHeight() < i.getHeight()) {
            int toss = Randomizer.nextInt(2);
            makeIDirectAncestor = (toss == 1) ? true : false;
            newCorrection = 2;
        }  else makeIDirectAncestor = false;
        
        if (j.isLeaf() && i.getHeight() < j.getHeight()) {
            int toss = Randomizer.nextInt(2);
            makeJDirectAncestor = (toss == 1) ? true : false;
            newCorrection = 2;
        }  else makeJDirectAncestor = false;

        if (i.isLeaf() && CiP.getHeight() < i.getHeight()) {
            oldCorrection = 2;
        }

        if (CiP.isLeaf() && i.getHeight() < CiP.getHeight()) {
            oldCorrection = 2;
        }

        if (jP != null && jP.getNr() == i.getNr()) {
            System.out.println("Proposal failed because jP = i. Heights of i = " + i.getHeight() + " Height of jP = " + jP.getHeight());
            return Double.NEGATIVE_INFINITY;
        }


        //Hastings numerator calculation + newAge of iP
        if (makeIDirectAncestor || makeJDirectAncestor) {
            newRange = 1;
            newAge = makeIDirectAncestor ? i.getHeight(): j.getHeight();
        } else {
            if (jP != null) {
                newMinAge = Math.max(i.getHeight(), j.getHeight());
                newRange = jP.getHeight() - newMinAge;
                newAge = newMinAge + (Randomizer.nextDouble() * newRange);
            } else {
                double randomNumberFromExponential;
                randomNumberFromExponential = Randomizer.nextExponential(1);
                //newRange = x0 - j.getHeight();
                //randomNumberFromExponential = Randomizer.nextDouble() * newRange;
                newRange = Math.exp(randomNumberFromExponential);
                newAge = j.getHeight() + randomNumberFromExponential;
            }
        }

        Node PiP = iP.getParent();

        //Hastings denominator calculation
        if (i.isDirectAncestor() || CiP.isDirectAncestor()) {
            oldRange = 1;
        }
        else {
            oldMinAge = Math.max(i.getHeight(), CiP.getHeight());
            if (PiP != null) {
                oldRange = PiP.getHeight() - oldMinAge;
            } else {
                oldRange = Math.exp(iP.getHeight() - oldMinAge);
                //oldRange = x0 - oldMinAge;
            }
        }

        //update
        iP.removeChild(CiP); //remove <iP, CiP>

        if (PiP != null) {
            PiP.removeChild(iP);   // remove <PiP,iP>
            PiP.addChild(CiP);   // add <PiP, CiP>
        } else {
            CiP.setParent(null); // completely remove <iP, CiP>
            tree.setRootOnly(CiP);
        }

        if (jP != null) {
            jP.removeChild(j);  // remove <jP, j>
            jP.addChild(iP);   // add <jP, iP>
        } else {
            iP.setParent(null); // completely remove <PiP, iP>
            tree.setRootOnly(iP);
        }
        iP.addChild(j);
        iP.setHeight(newAge);

        if(CiP.isDirectAncestor()) {
            CiP.setDirectAncestor(false);
        }
        if(i.isDirectAncestor()) {
            if (!makeIDirectAncestor)
                i.setDirectAncestor(false);
        } else {
            if (makeIDirectAncestor)
                i.setDirectAncestor(true);
        }


        correction = newCorrection/oldCorrection;
        //System.out.println("Correction is " + correction);

        fHastingsRatio = Math.abs(correction * newRange / oldRange);

//        System.out.println("Tree after = " + tree.getRoot().toShortNewick(false));
//
//        for (int inx = 0; inx < nodeCount; inx++) {
//                System.out.println("Node " + inx + " is " + tree.getNode(inx).isDirectAncestor());
//        }

        return Math.log(fHastingsRatio);

    }
}
