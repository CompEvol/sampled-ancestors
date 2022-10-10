package beast.app.tools;


import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.parser.NexusParser;
import beastlabs.evolution.tree.TreeAnnotation;

import java.io.*;
import java.util.List;

/**
 * Convert zero-branch tree(s) to "real" SA trees.
 *
 * usage: ConvertZBTreeToSATree(filePath)
 * keywords "tree TREE" or "tree STATE" in the tree log
 * are used to trigger writing SA trees.
 *
 * @author Alexandra Gavryushkina
 */
public class ConvertZBTreeToSATree {

    public List<Tree> trees;

    /**
     * parse zero-branch tree(s) from a Nexus file
     * @param treeFilePath Nexus tree file path
     * @throws Exception
     */
    public ConvertZBTreeToSATree(String treeFilePath) throws Exception {

        final File file = new File(treeFilePath);

        NexusParser parser = new NexusParser();
        parser.parseFile(file);
        trees = parser.trees;

        System.out.println("Parsed " + trees.size() + " zero-branch trees.");
    }

    /**
     * convert zero-branch tree(s) into sampled ancestor tree(s),
     * and write to a new tree file in Nexus format.
     * @param treeFilePath Nexus tree file path
     * @param printMetaData
     * @throws Exception
     */
    public void writeSATree(String treeFilePath, boolean printMetaData) {
        FileReader reader = null;
        PrintStream writer = null;
        // add .SA before postfix .tree .trees
        String name = treeFilePath.contains(".tree") ?
                treeFilePath.substring(0, treeFilePath.indexOf(".tree")) + ".SA" +
                        treeFilePath.substring(treeFilePath.indexOf(".tree"), treeFilePath.length()) : treeFilePath;
        try {
            reader = new FileReader(new File(treeFilePath));
            writer = new PrintStream(new File(name));
        }
        catch (IOException e) {
        }
        finally {
            if (reader != null) {
                //TODO anything here ?
            }
        }

        assert reader != null;
        assert writer != null;

        final BufferedReader fin = new BufferedReader(reader);
        try {
            int treeline = 0;
            while (fin.ready()) {
                final String line = fin.readLine();

                // keywords to trigger writing SA trees
                if (line.contains("tree TREE") || line.contains("tree STATE")) {
                    // start to write tree
                    if (treeline == 0) {
                        for (int i = 0; i < trees.size(); i++) {
                            Tree tree = trees.get(i);
                            convertTree(tree.getRoot());
                            writer.println("tree SA_TREE_" + (i + 1) + " = " +
                                    tree.getRoot().toSortedNewick(new int[]{0}, printMetaData) + ";");
                        }
                    }
                    treeline ++;
                } else {
                    // write other lines
                    writer.println(line);
                }
            }

            if (treeline != trees.size())
                throw new RuntimeException("There are " + treeline + " trees in log file, but " +
                        trees.size() + " parsed !");

            System.out.println("Convert " + trees.size() + " zero-branch trees into SA trees.");
            System.out.println("Output " + name);

        } catch (IOException e) {
            //
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    // convert ZB node into SA node
    private void convertTree(Node node) {

        if (!node.isLeaf()) {
            if (node.isFake()) {
                Node directAncestor = node.getDirectAncestorChild();
                node.removeChild(directAncestor);
                node.setNr(directAncestor.getNr());
                node.setID(directAncestor.getID());
                // copy meta data
                node.metaDataString = directAncestor.metaDataString;
                node.lengthMetaDataString = directAncestor.lengthMetaDataString;
                directAncestor.setParent(null);
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                Node childNode = node.getChild(i);
                convertTree(childNode);
            }
        }
    }

    // Usage: ConvertZBTreeToSATree tree_file [annotation_file]
    public static void main(String[] args) throws Exception {

        if (args.length > 2 || args.length == 0) {
            System.out.println("Usage: ConvertZBTreeToSATree tree_file [annotation_file]");
            System.out.println("arg1: a tree log file to process");
            System.out.println("arg2: optional, a tab delimited file to import annotation");
            System.exit(0);
        }

        String treeFilePath = args[0];
        ConvertZBTreeToSATree converter = new ConvertZBTreeToSATree(treeFilePath);
        boolean printMetaData = false;

//        String traitName = "group";
        if (args.length > 1) {
            TreeAnnotation treeAnnotation = new TreeAnnotation(null, args[1]);
            treeAnnotation.annotateNodes(converter.trees, true, true);
            printMetaData = true;
        }

        converter.writeSATree(treeFilePath, printMetaData);
    }

}
