package test.beast.evolution.operators;

import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import junit.framework.TestCase;
import sa.evolution.operators.TreeDimensionJump;

import org.junit.Test;

/**
 * @author Alexandra Gavryushkina
 */

public class TreeDimensionJumpTest extends TestCase {

    @Test
    public void testOperator1() throws Exception {

        Tree tree;
        int taxaSize = 3;

        // make a caterpillar
        Node left = new Node();
        left.setNr(0);
        left.setHeight(0.0);
        for (int i = 1; i < taxaSize; i++) {
            Node right = new Node(); 
            right.setNr(i);
            right.setHeight(i);
            Node parent = new Node(); 
            parent.setNr(taxaSize + i - 1);
            parent.setHeight(i);
            left.setParent(parent);
            parent.addChild(left);
            right.setParent(parent);
            parent.addChild(right);
            left = parent;
        }
        tree = new Tree(left);

        System.out.println("Tree was = " + tree.getRoot().toShortNewick(false));

        TreeDimensionJump operator = new TreeDimensionJump();
        operator.initByName("tree", tree, "weight", 1.0);
        double logHastingsRatio = operator.proposal();

        System.out.println("Proposed tree = " + tree.getRoot().toShortNewick(false));
        System.out.println("Log Hastings ratio = " + logHastingsRatio);

    }

    @Test
    public void testOperator2() throws Exception {

        Tree tree;
        int taxaSize = 3;

        // make a caterpillar
        Node left = new Node(); 
        left.setNr(0);
        left.setHeight(0.0);
        for (int i = 1; i < taxaSize; i++) {
            Node right = new Node(); 
            right.setNr(i);
            right.setHeight(i);
            Node parent = new Node(); 
            parent.setNr(taxaSize + i - 1);
            parent.setHeight(i);
            left.setParent(parent);
            parent.addChild(left);
            right.setParent(parent);
            parent.addChild(right);
            left = parent;
        }
        left.setHeight(left.getRight().getHeight()+2);
        tree = new Tree(left);

        System.out.println("Tree was = " + tree.getRoot().toShortNewick(false));

        TreeDimensionJump operator = new TreeDimensionJump();
        operator.initByName("tree", tree, "weight", 1.0);
        double logHastingsRatio = operator.proposal();

        System.out.println("Proposed tree = " + tree.getRoot().toShortNewick(false));
        System.out.println("Log Hastings ratio = " + logHastingsRatio);

    }

}
