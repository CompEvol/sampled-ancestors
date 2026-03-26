package sa.app.tools;


import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.parser.NexusParser;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    void convertTree(Node node) {

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

    /**
     * Class representing mapping from tip nodes to annotations
     * which are read from a file.  Contains a method for applying
     * these annotations to existing trees.
     *
     * This implements the functionality of the BEASTLabs class,
     * TreeAnnotation, avoiding that dependency.
     */
    static class TipAnnotations {
        Map<String, String> annotations = new HashMap<>();
        String traitName = null;

        /**
         * Create a new TipAnnotations object representing the annotations
         * contained in a file.  The file should be a two-column tab-separated
         * variable file where the first column contains taxon IDs and the
         * second the metadata to be associated with each taxon. The first
         * row must contain column headers. The header of the second column
         * is used as the "key" for the key-value pairs assigned to each tip.
         *
         * @param fileName name of file to read annotations from.
         */
        TipAnnotations(String fileName) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                boolean isHeader = true;
                for (String line : reader.lines().toList()) {
                    String[] fields = line.split("\t");
                    if (isHeader) {
                        traitName = fields[1];
                        isHeader = false;
                    } else {
                        annotations.put(fields[0], fields[1]);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Apply annotations to each tree in a list. A runtime exception is
         * thrown if the tree contains tips for which annotations are not
         * defined.
         *
         * @param trees list of trees to annotate.
         */
        void applyToTrees(List<Tree> trees) {
            for (Tree tree : trees) {
                for (Node node : tree.getNodesAsArray()) {
                    if (node.isLeaf()) {
                        if (!annotations.containsKey(node.getID()))
                            throw new IllegalArgumentException("Cannot find trait for tip " + node.getID());
                        node.metaDataString = traitName + "='" + annotations.get(node.getID()) + "'";
                    } else {
                        node.metaDataString = null;
                        node.lengthMetaDataString = null;
                    }
                }
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

        if (args.length > 1) {
            TipAnnotations tipAnnotations = new TipAnnotations(args[1]);
            tipAnnotations.applyToTrees(converter.trees);
            printMetaData = true;
        }

        converter.writeSATree(treeFilePath, printMetaData);
    }

}
