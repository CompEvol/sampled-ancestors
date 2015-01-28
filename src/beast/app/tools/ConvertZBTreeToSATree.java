package beast.app.tools;

import java.io.*;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.NexusParser;

/**
 * @author Alexandra Gavryushkina
 */
public class ConvertZBTreeToSATree {


    ConvertZBTreeToSATree(String fileName) throws Exception {
        File file = new File(fileName);


        NexusParser parser = new NexusParser();
        parser.parseFile(file);

        PrintStream writer = null;




        FileReader reader = null;

        try {
            reader = new FileReader(file);
        }
        catch (IOException e) {
        }
        finally {
            if (reader != null) {
            }
            if (reader != null) {
            }
        }

        final BufferedReader fin;
        fin = new BufferedReader(reader);

        try {
            String name = fileName.substring(0, fileName.indexOf(".tree")-1);
            writer = new PrintStream(new File(fileName+"SA.tree"));

            while (fin.ready()) {
                final String line = fin.readLine();

                if (line.contains("tree TREE")) {
                    for (Tree tree : parser.trees) {
                        convertTree(tree.getRoot());
                        writer.println("tree TREE1 = " + tree.getRoot().toSortedNewick(new int[] {0}, false) + ";");
                    }

                } else {
                    writer.println(line);
                }

            }






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
