package beast.app.tools;

import beast.core.Description;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.TreeParser;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @AlexandraGavryushkina
 */

@Description("Compare the distribution of trees in two tree files. " +
        "If trees are in different format then it first unifies them.")

public class TreeFilesComparator {

    public static void main(String[] args) throws Exception{


//        Node left = new Node();
//        left.setNr(0);
//        left.setHeight(0);
//        for (int i = 1; i < 10; i++) {
//            Node right = new Node();
//            right.setNr(i);
//            right.setHeight((double)i/10);
//            Node parent = new Node();
//            parent.setNr(10 + i - 1);
//            parent.setHeight(i);
//            left.setParent(parent);
//            parent.setLeft(left);
//            right.setParent(parent);
//            parent.setRight(right);
//            left = parent;
//        }

        TreeParser tree = new TreeParser(null, "(((0:1.0,1:1.0):1.0)3:1.0,2:3.0):0.0", 0, false);

//        Tree tree = new Tree();
//        tree.setRoot(left);
//        tree.initAndValidate();


        System.out.println("Tree was " + tree.getRoot().toShortNewick(false) + ";");

        convertToFakeSATree(tree.getRoot(), new int[] {tree.getNodeCount()});

        System.out.println("Tree now " + tree.getRoot().toShortNewick(false) + ";");
    }

    public static String makeStandardString(Tree tree) {

        Node[] sampledNodeArray = tree.getExternalNodes().toArray(new Node[tree.getLeafNodeCount()]);

        Comparator<Node> comp = new NodeComparator();

        Arrays.sort(sampledNodeArray, comp);

        for(int i=0; i<sampledNodeArray.length; i++) {
            sampledNodeArray[i].setNr(i);
        }

        return tree.getRoot().toShortNewick(false);


    }

    public static void convertToFakeSATree(Node node, int[] nextNr) {
        if (!node.isLeaf()) {
            convertToFakeSATree(node.getLeft(), nextNr);
            if (node.getRight() != null) {
                convertToFakeSATree(node.getRight(), nextNr);
            }
        }
        if (node.getChildCount() == 1) {
            Node parent = new Node();
            parent.setHeight(node.getHeight());
            Node child = node.getLeft();
            parent.setLeft(child);
            child.setParent(parent);
            parent.setRight(node);
            node.setParent(parent);
            node.removeChild(child);
            parent.setNr(nextNr[0]);
            nextNr[0]++;
            if (!node.isRoot()) {
                Node grandparent = node.getParent();
                if (grandparent.getLeft().getNr() == node.getNr()) {
                    grandparent.setLeft(parent);
                }  else {
                    grandparent.setRight(parent);
                }
                parent.setParent(grandparent);
            }  else {
                node.getTree().setRoot(parent);
            }
        }
    }

}
