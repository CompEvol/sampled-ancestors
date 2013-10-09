package beast.app.tools;

import beast.core.Description;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.NexusParser;
import beast.util.TreeParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @AlexandraGavryushkina
 */

@Description("Compare the distribution of trees in two tree files. " +
        "If trees are in different format then it first convert trees to the same format.")

public class SampledAncestorTreeComparator {

    public static void main(String[] args) throws Exception{

        TreeParser tree = new TreeParser(null, "(((0:1.0,1:1.0):1.0)3:1.0,2:3.0):0.0", 0, false);
        System.out.println("Tree was " + tree.getRoot().toShortNewick(false) + ";");
        convertToFakeSATree(tree.getRoot(), new int[] {tree.getNodeCount()});
        System.out.println("Tree is now " + tree.getRoot().toShortNewick(false) + ";");

        java.io.File file1, file2;

        if (args != null && args.length > 0) {
            file1 = new java.io.File(args[0]);
            file2 = new java.io.File(args[1]);
        } else {
            String message = "Choose the first file .trees";
            java.awt.Frame frame = new java.awt.Frame();
            java.awt.FileDialog chooser = new java.awt.FileDialog(frame, message,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() == null) {
                System.out.println("The first file was not chosen.");
                System.exit(0);
            }
            file1 = new java.io.File(chooser.getDirectory(), chooser.getFile());
            chooser.dispose();
            frame.dispose();

            message = "Choose the second file .trees";
            frame = new java.awt.Frame();
            chooser = new java.awt.FileDialog(frame, message,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() == null) {
                System.out.println("The first file was not chosen.");
                System.exit(0);
            }
            file2 = new java.io.File(chooser.getDirectory(), chooser.getFile());
            chooser.dispose();
            frame.dispose();
        }

        FileReader reader1 = null;
        FileReader reader2 = null;

        try {
            reader1 = new FileReader(file1);
            NexusParser parser1 = new NexusParser();
            parser1.parseFile(file1);
            reader2 = new FileReader(file2);
            NexusParser parser2 = new NexusParser();
            parser2.parseFile(file2);
            SATreeComparingAnalysis analysis = new SATreeComparingAnalysis(parser1, parser2);

            analysis.perform();
        }
        catch (IOException e) {
            //
        }
        finally {
            if (reader1 != null) {
                reader1.close();
            }
            if (reader2 != null) {
                reader2.close();
            }
        }

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
            node.removeChild(child);
            parent.setRight(node);
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
            node.setParent(parent);
        }
    }

}
