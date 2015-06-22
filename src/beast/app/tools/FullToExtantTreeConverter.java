package beast.app.tools;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.evolution.tree.TreeTraceAnalysis;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexandra Gavryushkina
 */
public class FullToExtantTreeConverter {

    ArrayList<String> extantTaxa = new ArrayList<>();

    double extantHeightThreshold = 1e-8;

    public FullToExtantTreeConverter() {
    }

    public FullToExtantTreeConverter(double threshold) {
        extantHeightThreshold = threshold;
    }

    public FullToExtantTreeConverter(ArrayList<String> newExtantTaxa) {
        extantTaxa = newExtantTaxa;
    }

    private void printConvertedTrees(List<Tree> trees, String outputFile) throws Exception {
        new File(outputFile);
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

        Tree oldTree = trees.get(0);

        if (extantTaxa.isEmpty()) {
            for (Node node:oldTree.getExternalNodes()) {
                if (node.getHeight() < extantHeightThreshold) {
                    extantTaxa.add(node.getID());
                }
            }
        }


        removeFossils(oldTree.getRoot(), oldTree, new ArrayList<>());
        numberNodes(oldTree.getRoot(), new int[] {extantTaxa.size()});
        Tree tree = new Tree(oldTree.getRoot());

        tree.init(out);
        out.println();
        tree.log(0, out);
        out.println();

        for (int i=1; i< trees.size(); i++) {
            oldTree = trees.get(i);
            removeFossils(oldTree.getRoot(), oldTree, new ArrayList<>());
            numberNodes(oldTree.getRoot(), new int[] {extantTaxa.size()});
            tree = new Tree(oldTree.getRoot());
            tree.log(i, out);
            out.println();
        }
        out.println("End;");
        out.close();

    }

    public void numberNodes(Node node, int[] nextNr) {

        if (node.isLeaf()) {
            node.setNr(extantTaxa.indexOf(node.getID()));
        } else {
            for (Node child:node.getChildren()) {
                numberNodes(child, nextNr);
            }
            node.setNr(nextNr[0]);
            nextNr[0]++;
        }

    }

    public void removeFossils(Node node, Tree tree, ArrayList<Node> removedNodes) {
        if (node.isLeaf()) {
            if (!extantTaxa.contains(node.getID())) {
                node.getParent().removeChild(node);
                node.setParent(null);
                removedNodes.add(node);
            }
        }  else {
            Node left = node.getLeft();
            Node right = node.getRight();
            removeFossils(left, tree, removedNodes);
            removeFossils(right, tree, removedNodes);


            if (node.isLeaf()) {
                node.getParent().removeChild(node);
                node.setParent(null);
                removedNodes.add(node);
            }
            if (node.getChildCount() == 1) {
                Node child = node.getChild(0);
                if (node.getParent() != null) {
                    Node grandParent = node.getParent();
                    grandParent.removeChild(node);
                    grandParent.addChild(child);
                    child.setParent(grandParent);
                    node.setParent(null);
                }
                else {
                    child.setParent(null);
                    tree.setRootOnly(child);
                }
                removedNodes.add(node);
            }

        }
    }

    public static void main(String[] args) throws Exception {

        java.io.File file, file_out;
        String outputFile = "";
        double customThreshold = -1.;
        ArrayList<String> taxa = null;
        if (args != null && args.length > 0) {
            file = new java.io.File(args[0]);
            if (args.length == 2) {
                outputFile = args[1];
            }
            if (args.length ==3) {
                customThreshold = Double.parseDouble(args[2]);
            }
        } else {
            String message = "Choose input file .trees";
            java.awt.Frame frame = new java.awt.Frame();
            java.awt.FileDialog chooser = new java.awt.FileDialog(frame, message,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() == null) {
                System.out.println("The file was not chosen.");
                System.exit(0);
            }
            file = new java.io.File(chooser.getDirectory(), chooser.getFile());
            chooser.dispose();
            frame.dispose();
            String strThreshold = JOptionPane.showInputDialog("Choose the threshold for identifying extant taxa", "1e-8");
            customThreshold = Double.parseDouble(strThreshold);

        }

        if (outputFile.isEmpty()) {
            String inputFileName = file.getName();
            if (inputFileName.contains(".trees")) {
                inputFileName = inputFileName.substring(0, inputFileName.indexOf(".trees"));
            }
            outputFile = inputFileName +"_extant.trees";
        }

        FileReader reader = null;

        try {
            System.err.println("Reading file " + file.getName());
            reader = new FileReader(file);
            List<Tree> trees = TreeTraceAnalysis.Utils.getTrees(file);
            FullToExtantTreeConverter converter;
            if (customThreshold >= 0.0) {
                converter = new FullToExtantTreeConverter(customThreshold);
            } else if (taxa != null) {
                converter = new FullToExtantTreeConverter(taxa);
            } else {
                converter = new FullToExtantTreeConverter();
            }
            converter.printConvertedTrees(trees, outputFile);
        }
        catch (IOException e) {
            //
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

    }

}
