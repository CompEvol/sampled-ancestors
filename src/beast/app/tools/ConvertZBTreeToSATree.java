package beast.app.tools;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.NexusParser;

import java.io.*;
import java.util.List;

/**
 * @author Alexandra Gavryushkina
 */
public class ConvertZBTreeToSATree {


    public ConvertZBTreeToSATree(String fileName) throws Exception {
        File file = new File(fileName);

        NexusParser parser = new NexusParser();
        parser.parseFile(file);
        List<Tree> trees = parser.trees;

        System.out.println("Parse " + trees.size() + " zero-branch trees.");

        PrintStream writer = null;
        FileReader reader = null;

        try {
            reader = new FileReader(file);
        }
        catch (IOException e) {
        }
        finally {
            if (reader != null) {
                //TODO anything here
            }
        }

        final BufferedReader fin;
        fin = new BufferedReader(reader);

        try {
            String name = fileName.contains(".tree") ?
                    fileName.substring(0, fileName.indexOf(".tree")) : fileName;
            writer = new PrintStream(new File(name + ".SA.tree"));

            int treeline = 0;
            while (fin.ready()) {
                final String line = fin.readLine();

                if (line.contains("tree TREE") || line.contains("tree STATE")) {
                    // start to write tree
                    if (treeline == 0) {
                        for (int i = 0; i < trees.size(); i++) {
                            Tree tree = trees.get(i);
                            convertTree(tree.getRoot());
                            writer.println("tree SA_TREE_" + (i + 1) + " = " +
                                    tree.getRoot().toSortedNewick(new int[]{0}, false) + ";");
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
                        trees.size() + " found !");

            System.out.println("Convert " + treeline + " zero-branch trees into SA trees.");


        } catch (IOException e) {
            //
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("There have to be exactly one argument - a tree file to process");
            System.exit(0);
        }

        ConvertZBTreeToSATree processor = new ConvertZBTreeToSATree(args[0]);

    }

    private void convertTree(Node node) {

        if (!node.isLeaf()) {
            if (node.isFake()) {
                Node directAncestor = node.getDirectAncestorChild();
                node.removeChild(directAncestor);
                node.setNr(directAncestor.getNr());
                node.setID(directAncestor.getID());
                directAncestor.setParent(null);
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                Node childNode = node.getChild(i);
                convertTree(childNode);
            }
        }



    }


}
