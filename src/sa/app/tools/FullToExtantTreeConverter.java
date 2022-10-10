package sa.app.tools;


import beastfx.app.tools.Application;
import beastfx.app.util.TreeFile;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beastfx.app.tools.TreeTraceAnalysis;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexandra Gavryushkina
 */
@Description("utility for creating a tree set with extant nodes from a set of ancestral node trees")
/*
 * Usage: /path/to/beast/appstore FullToExtantTreeConverter -trees <treefile.trees> [-output <output.file>] [-threshold <threshold>]
 * Opens a report in a webbrowser containing statistics on how much support there is
 * for an internal node to be ancestral.
 *
 */

public class FullToExtantTreeConverter extends beast.base.inference.Runnable {
	public Input<TreeFile> outputInput = new Input<>("output","file to store converted tree set. If not specified, _extant is aded to input file name");
	public Input<TreeFile> treesInput = new Input<>("trees","tree set file to be converted");
	public Input<Double> thresholdInput= new Input<>("threshold", "the threshold for identifying extant taxa. Ignored if negative.", -1.0);

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
        tree.log((long) 0, out);
        out.println();

        for (int i=1; i< trees.size(); i++) {
            oldTree = trees.get(i);
            removeFossils(oldTree.getRoot(), oldTree, new ArrayList<>());
            numberNodes(oldTree.getRoot(), new int[] {extantTaxa.size()});
            tree = new Tree(oldTree.getRoot());
            tree.log((long) i, out);
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
    	new Application(new FullToExtantTreeConverter(), "FullToExtantTreeConverter", args);
    }

	@Override
	public void initAndValidate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() throws Exception {
	       java.io.File file;
	        String outputFile = "";
	        double customThreshold = -1.;
//	        ArrayList<String> taxa = null;
	      
	        file = treesInput.get();
	        if (file == null || !file.exists()) {
	        	throw new IllegalArgumentException("tree file must be specified and file must exist");
	        }
	        if (outputInput.get() != null) {
	        	outputFile = outputInput.get().getPath();
	        }
	        customThreshold = thresholdInput.get();

	        if (outputFile.isEmpty()) {
	            String inputFileName = file.getPath();
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
//	            } else if (taxa != null) {
//	                converter = new FullToExtantTreeConverter(taxa);
	            } else {
	                converter = new FullToExtantTreeConverter();
	            }
	            System.err.println("Writing file " + outputFile);
	            converter.printConvertedTrees(trees, outputFile);
	            System.err.println("Done");
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
