package test.beast.evolution.operators;

import beast.evolution.operators.ScaleOperatorForZeroBranchSATrees;
import beast.util.ZeroBranchSATreeParser;
import junit.framework.TestCase;
import org.junit.Test;


import java.util.ArrayList;

/**
 *@author Alexandra Gavryushkina
 */
public class ZeroBranchSATreeScalerTest  extends TestCase {

    @Test
    public void testOperator() throws Exception {

        String newick = "((((0:0.5)2:0.5,1:0.7):0.5)4:0.8,3:1.5):0.0";

        ArrayList<String> taxa = new ArrayList<String>();
        for (int i=0; i<5; i++) {
            taxa.add(Integer.toString(i));
        }
        ZeroBranchSATreeParser tree = new ZeroBranchSATreeParser(taxa, newick, 0);

        double oldTreeHeight = tree.getRoot().getHeight();
        double oldLeftLeftHeight = tree.getRoot().getLeft().getLeft().getHeight();

        ScaleOperatorForZeroBranchSATrees operator = new ScaleOperatorForZeroBranchSATrees();
        operator.initByName("tree", tree, "weight", 1.0);
        operator.initByName("scaleFactor", 0.95);
        operator.initAndValidate();


        double HR;
        do {
            HR = operator.proposal();
        }  while (HR == Double.NEGATIVE_INFINITY);

        System.out.println(tree.getRoot().toShortNewick(false));

        double scaler = tree.getRoot().getHeight()/oldTreeHeight;
        double newLefLeftHeight = tree.getRoot().getLeft().getLeft().getHeight();

        assertEquals(oldLeftLeftHeight, newLefLeftHeight/scaler, 1e-5);
        boolean sampledNodeScaled = false;
        for (int i=0; i<tree.getNodeCount(); i++) {
            if (tree.getNode(i).getParent() != null && tree.getNode(i).getParent().getHeight() < tree.getNode(i).getHeight() ) {
                sampledNodeScaled = true;
            }
        }

        assertTrue(!sampledNodeScaled);
    }
}
