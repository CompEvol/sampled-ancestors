package beast.evolution.operators;

import beast.core.Description;
import beast.evolution.tree.Node;
import beast.evolution.tree.SampledAncestorNode;
import beast.evolution.tree.SampledAncestorTree;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */

/**
 *
 */

@Description("Implements a variant of the unweighted Wilson-Balding branch swapping move for FRS trees." +
        "The restriction of this move to binary trees is similar to one proposed by WILSON and BALDING 1998  " +
        "and involves removing a subtree and re-attaching it on a new parent branch or to a leaf. " +
        "See <a href='http://www.genetics.org/cgi/content/full/161/3/1307/F1'>picture</a> for restricted version.")

public class WilsonBaldingForSampledAncestorTrees extends TreeOperator {

    @Override
    public void initAndValidate() {
    }

    /**
     * override this for proposals,
     *
     * @return log of Hastings Ratio, or Double.NEGATIVE_INFINITY if proposal should not be accepted *
     */


    @Override
    public double proposal() {

        Tree tree = m_tree.get(this);

        double oldMinAge, newMinAge, newRange, oldRange, newAge, fHastingsRatio, DimensionCoefficient;
        double x0 = 100;
        int newDimension, oldDimension;

        // choose a random node avoiding root
        int nodeCount = tree.getNodeCount();
        Node i;

        do {
            i = tree.getNode(Randomizer.nextInt(nodeCount));
        } while (i.isRoot());

        Node iP = i.getParent();
        Node CiP = null;
        if (iP.getChildCount() == 2) {
            if (iP.getLeft().getNr() == i.getNr()) {
                CiP = iP.getRight();
            } else {
                CiP = iP.getLeft();
            }
            if (iP.getParent() == null && CiP.getHeight() < i.getHeight()) {
                return Double.NEGATIVE_INFINITY;
            }
        }


        // choose another random node to insert i above or to attach i to this node if it is a leaf
        Node j;
        Node jP;

        final int leafNodeCount = tree.getLeafNodeCount();

        if (leafNodeCount != tree.getExternalNodes().size()) {
            System.out.println("node counts are incorrect. NodeCount = " + nodeCount + " leafNodeCount = " + leafNodeCount + " exteranl node count = " + tree.getExternalNodes().size());
        }

        // make sure that the target branch <jP, j> or target leaf j is above the subtree being moved

        int nodeNumber;
        double newParentHeight;
        boolean attachingToLeaf;
        boolean adjacentEdge;
        //boolean adjacentLeaf;
        do {
            adjacentEdge = false;
            //adjacentLeaf = false;
            nodeNumber = Randomizer.nextInt(nodeCount + leafNodeCount);
            if (nodeNumber < nodeCount) {
                j = tree.getNode(nodeNumber);
                jP = j.getParent();
                if (jP != null)
                    newParentHeight = jP.getHeight();
                else newParentHeight = Double.POSITIVE_INFINITY;
                if (CiP != null) {
                    adjacentEdge = (CiP.getNr() == j.getNr() || iP.getNr() == j.getNr());
                }
                attachingToLeaf = false;

            } else {
                j = tree.getExternalNodes().get(nodeNumber - nodeCount);
                newParentHeight = j.getHeight();
                attachingToLeaf = true;
                //adjacentLeaf = (iP.getNr() == j.getNr());
            }
        } while ((newParentHeight <= i.getHeight()) || (i.getNr() == j.getNr()) || adjacentEdge /*|| adjacentLeaf */);


        if (attachingToLeaf && iP.getNr() == j.getNr()) {
            System.out.println("Proposal failed because j = iP");
            return Double.NEGATIVE_INFINITY;
        }

        jP = j.getParent();

        if (jP != null && jP.getNr() == i.getNr()) {
            System.out.println("Proposal failed because jP = i. Heights of i = " + i.getHeight() + "Height of jP = " + jP.getHeight());
            return Double.NEGATIVE_INFINITY;
        }

        //Hastings dimension coefficient calculation
        /*if (iP.getChildCount() == 2 && attachingToLeaf) {
            DimensionCoefficient = (double)(nodeCount-1)/(nodeCount-2);
        } else {
            if (iP.getChildCount() == 1 && !(attachingToLeaf)) {
                DimensionCoefficient = (double)(nodeCount-1)/(nodeCount);
            }
            else DimensionCoefficient = 1;
        }  */
        oldDimension = nodeCount - 1;

        //Hastings numerator calculation + newAge of i
        if (attachingToLeaf) {
            newRange = 1;
            newAge = 0;
        } else {
            if (jP != null) {
                newMinAge = Math.max(i.getHeight(), j.getHeight());
                newRange = jP.getHeight() - newMinAge;
                newAge = newMinAge + (Randomizer.nextDouble() * newRange);
            } else {
                double randomNumberFromExponential;
                //randomNumberFromExponential = Randomizer.nextExponential(1);
                newRange = x0 - j.getHeight();
                randomNumberFromExponential = Randomizer.nextDouble() * newRange;
                //newRange = Math.exp(randomNumberFromExponential);

                newAge = j.getHeight() + randomNumberFromExponential;
            }
        }

        if (iP.getChildCount() != 1) {
            Node PiP = iP.getParent();

            //Hastings denominator calculation
            oldMinAge = Math.max(i.getHeight(), CiP.getHeight());
            if (PiP != null) {
                oldRange = PiP.getHeight() - oldMinAge;
            } else {
                //oldRange = Math.exp(iP.getHeight() - oldMinAge);
                oldRange = x0 - oldMinAge;
            }

            if (attachingToLeaf) {   // removing from a branch and attaching to a leaf (from <PiP, CiP> to j)


                //update
                iP.removeChild(CiP); //  remove <iP, CiP>, <iP, i>
                j.addChild(i); // add <j, i>
                if (PiP != null) {
                    PiP.removeChild(iP);   // remove <PiP,iP>  (deleting reference to iP from PiP)
                    PiP.addChild(CiP);   // add <PiP, CiP >     (deleting reference to iP from CiP)
                } else {
                    CiP.setParent(null); // completely remove <iP, CiP>     (deleting reference to iP from CiP)
                    tree.setRootOnly(CiP);
                }
                ((SampledAncestorTree) tree).removeNode(iP.getNr());    // remove node iP from the array and update members of the tree
            } else {     // removing from a branch and attaching to a branch (from <PiP, CiP> to <jP, j> or above the root j)
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
            }
        } else {
            oldRange = 1;
            if (attachingToLeaf) { //removing from a node and attaching to a leaf (from iP to j)
                //update
                iP.removeChild(i); // remove <iP, i>

                j.addChild(i); // add  <j, i>
            } else {          // removing from a node and attaching to a branch (from iP to <jP, j> or above the root j)

                //update
                iP.removeChild(i); // remove <iP,i>
                Node k = new SampledAncestorNode();
                if (jP != null) {
                    jP.removeChild(j); // remove <jP, j>
                    jP.addChild(k);// add <jP, k>, <k, j>, and <k, i>
                } else {
                    ((SampledAncestorTree) tree).setRootOnly(k);
                }
                k.addChild(j);
                k.addChild(i);
                k.setHeight(newAge);
                ((SampledAncestorTree) tree).addNode(k);
            }
        }

        newDimension = tree.getNodeCount() - 1;
        DimensionCoefficient = (double) oldDimension / newDimension;

        fHastingsRatio = Math.abs(DimensionCoefficient * newRange / oldRange);

        return Math.log(fHastingsRatio);

    }
}