package beast.app.tools;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.evolution.tree.TreeTraceAnalysis;
import beast.util.FrequencySet;
import beast.util.SANexusParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Alexandra Gavryushkina
 * @author Walter Xie
 */

public class SATreeTraceAnalysis extends TreeTraceAnalysis {
    FrequencySet<String> pairs = new FrequencySet<String>();

    public SATreeTraceAnalysis(List<Tree> posteriorTreeList, double burninFraction) {
        super(posteriorTreeList, burninFraction);
    }

    // This should calculate a credible set for the trees obtained after removing all fossils
    // WARNING this procedure needs testing.
    @Override
    public void analyze(double credSetProbability) {
        removeFossilsFromAllTrees();
        super.analyze(credSetProbability);

    }

    @Override
    public void analyze() {
        analyze(FrequencySet.DEFAULT_CRED_SET);
    }

    @Override
    public void report(PrintStream oStream) {
        super.report(oStream);
    }

    /**
     * Sorted Newick Trees
     * @param noLength
     * @return
     */
    public List<String> getNewickTrees (boolean noLength) {
        List<String> newickTrees = new ArrayList<>();
        for (Tree tree : treeInTrace) {
            String nTree = tree.getRoot().toSortedNewick(new int[] {1}, false);
            if (noLength) {
                nTree = Utils.convertToShortTree(nTree);
            }
            newickTrees.add(nTree);
        }
        return newickTrees;
    }

    /**
     * Sorted Newick Trees with length
     * @return
     */
    public List<String> getNewickTrees () {
        return getNewickTrees(false);
    }

    /**
     *
     * @param printCladeFrequencies
     * @param printPairs
     * @param printFrequencies
     * @param printTopologyCredibleSet
     * @param isHTML true if the report should be formatted as an HTML fragment
     * @return a report string
     * @throws Exception
     */
	public String toReportString(boolean printCladeFrequencies, boolean printPairs, boolean printFrequencies, boolean printTopologyCredibleSet, Double credSetProbability, boolean isHTML) throws Exception {
        FrequencySet<String> clades = new FrequencySet<String>();
        ArrayList<String> tmp = new ArrayList<String>();

        int treeWithSACount = 0;

        for (int i=0; i < getTotalTreesBurninRemoved(); i++) {
            Tree tree = treeInTrace.get(i);
            ArrayList<String> saClades =  extractAllSAClades(tree.getRoot());
            tmp.addAll(saClades);
            for (int j=0; j<tree.getNodeCount(); j++)
                if (tree.getNode(j).isFake()) {
                    treeWithSACount++;
                    break;
                }
        }

        for (int i=0; i < tmp.size(); i++) {
            clades.add(tmp.get(i));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ResultsOutput output = null;

        if (isHTML) {
            output = new ResultsOutput.HTML(ps);
        } else {
            output = new ResultsOutput.TabDelimitedPlainText(ps);
        }


        int treeCount = getTotalTreesBurninRemoved();
        if (printCladeFrequencies) {
	        output.beginTableOutput("Clade frequencies", new String[]{"Count", "Percent", "Clade"});
            for (int i =0; i < clades.size(); i++) {
	            double percent = (double) (clades.getFrequency(i) * 100)/(treeCount);

                output.outputRow(new String[] {
                        String.format("%-10d", clades.getFrequency(i)),
                        String.format("%-10.2f", percent),
                        clades.get(i)});

	        }
            output.endTableOutput();
        }

        if (printPairs) {
            output.beginTableOutput("Pair frequencies", new String[]{"Count", "Percent", "Pair"});
	        for (int i =0; i < pairs.size(); i++) {
	            int freq = pairs.getFrequency(i);
	            double percent = (double) (freq * 100)/(treeCount);

                output.outputRow(new String[] {
                        String.format("%-10d", freq),
                        String.format("%-10.2f", percent),
                        pairs.get(i)});
	        }
	        double a =  (double)treeWithSACount/ treeCount;
            output.endTableOutput();
            output.line(String.format(treeWithSACount + " trees (or %2.2f%%) have sampled internal nodes.", a * 100));
        }

        FrequencySet<String> sampledAncestors = new FrequencySet<String>();
        tmp = new ArrayList<String>();

        for (int i=0; i < getTotalTreesBurninRemoved(); i++) {
            Tree tree = treeInTrace.get(i);
            tmp.addAll(listSA(tree, false));
        }

        for (String ancestor : tmp) {
            sampledAncestors.add(ancestor);
        }

        if (printFrequencies) {
            output.beginTableOutput("SA frequencies", new String[]{"Count", "Percent", "SA"});
	        for (int i =0; i < sampledAncestors.size(); i++) {
	            double percent = (double) (sampledAncestors.getFrequency(i) * 100)/(getTotalTreesBurninRemoved());

                output.outputRow(new String[] {
                        String.format("%-10d", sampledAncestors.getFrequency(i)),
                        String.format("%-10.2f", percent),
                        sampledAncestors.get(i)});
            }
            output.endTableOutput();
        }

        if (printTopologyCredibleSet) {
            countTopologies(output, credSetProbability);
        }

        ps.flush();
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        ps.close();
        return content;
    }

    /**
     * prints the number of trees with sampled ancestors
     * to the standard output
     */
    public void countTreesWithSA() throws Exception {

        int treeWithSACount = 0;
        Tree tree;

        for (int i =0; i < getTotalTreesBurninRemoved(); i++) {
            tree = treeInTrace.get(i);
            int j;
            for (j=0; j<tree.getNodeCount(); j++)
                if (tree.getNode(j).isFake()) {
                    treeWithSACount++;
                    break;
                }
        }

        double a =  (double)treeWithSACount/ getTotalTreesBurninRemoved();
        System.out.format(treeWithSACount + " trees (or %2.2f%%) have sampled internal nodes.%n", a * 100);
        System.out.println();
    }

    /**
     * print the average number of sampled ancestors in all trees
     * to the standard output
     */
    public void countAverageSampledAncestorNumber(){
        Tree tree;
        int totalSACount = 0;


        for (int i =0; i < getTotalTreesBurninRemoved(); i++) {
            tree = treeInTrace.get(i);
            for (int j=0; j<tree.getNodeCount(); j++) {
                if (tree.getNode(j).isFake()) totalSACount++;
            }
        }

        System.out.format("The average number of sampled ancestors per tree is %2.2f", ((double) totalSACount / getTotalTreesBurninRemoved()));
        System.out.println();

    }

    /**
     * prints the frequency of each sampled ancestor clade to the standard output
     *
     * @param countPairs if true then the frequencies of sampled ancestors pairs are also counted
     * @throws Exception
     */
    public void countSAClades(boolean countPairs) throws Exception {

        FrequencySet<String> clades = new FrequencySet<String>();
        ArrayList<String> tmp = new ArrayList<String>();

        int treesWithSACount = 0;

        for (int i=0; i < getTotalTreesBurninRemoved(); i++) {
            Tree tree = treeInTrace.get(i);
            ArrayList<String> saClades =  extractAllSAClades(tree.getRoot());
            tmp.addAll(saClades);
            for (int j=0; j<tree.getNodeCount(); j++)
                if (tree.getNode(j).isFake()) {
                    treesWithSACount++;
                    break;
                }
        }

        for (int i=0; i < tmp.size(); i++)
            clades.add(tmp.get(i));

        System.out.println("Clade frequencies");
        System.out.println();
        System.out.println("Count \t Percent \t Clade");
        System.out.println();
        int treeCount = getTotalTreesBurninRemoved();
        for (int i =0; i < clades.size(); i++) {
            double percent = (double) (clades.getFrequency(i) * 100)/(treeCount);
            System.out.format("%-10d %-10.2f", clades.getFrequency(i), percent);
            System.out.println(clades.get(i));
        }
        System.out.println();

        if (countPairs) {
            System.out.println("Pair frequencies");
            System.out.println();
            System.out.println("Count \t Percent \t Pair");
            System.out.println();
            for (int i =0; i < pairs.size(); i++) {
            	int freq = pairs.getFrequency(i);
                double percent = (double) (freq * 100)/(treeCount);
                System.out.format("%-10d %-10.2f", freq, percent);
                System.out.println(pairs.get(i));
            }
            System.out.println();
            double a =  (double)treesWithSACount/ treeCount;
            System.out.format(treesWithSACount + " trees (or %2.2f%%) have sampled internal nodes.%n", a*100);
            System.out.println();
        }

    }

    

    
    /**
     * for each sampled node, it counts the percentage of trees in which this sampled node is a sampled ancestor
     * @param print  if is true then the percentages are printed
     * @param useRanking if is true then sampled nodes are distinguished by the time order of sampling
     *                   (that is, for each n in {1,..., #SampledNodes} it counts the number of
     *                   trees in which nth sampled node is a sampled ancestor)
     *                   if is false, labels are used
     * @return a set of sampled nodes with assigned frequencies
     */
    public FrequencySet<String> countSAFrequencies(boolean print, boolean useRanking) {
        FrequencySet<String> sampledAncestors = new FrequencySet<String>();
        ArrayList<String> tmp = new ArrayList<String>();

        for (int i=0; i < getTotalTreesBurninRemoved(); i++) {
            Tree tree = treeInTrace.get(i);
            tmp.addAll(listSA(tree, useRanking));
        }

        for (int i=0; i < tmp.size(); i++) {
            sampledAncestors.add(tmp.get(i));
        }

        if (print) {
            System.out.println("There are " + getTotalTreesBurninRemoved() + " trees in the file. " +
                    "The first " + getBurnin() + " are removed as a burn-in. " +
                    "Then sampled ancestors are counted");
            System.out.println();
            System.out.println("Count \t Percent \t SA");
            for (int i =0; i < sampledAncestors.size(); i++) {
                double percent = (double) (sampledAncestors.getFrequency(i) * 100)/(getTotalTreesBurninRemoved());
                System.out.format("%-10d %-10.2f", sampledAncestors.getFrequency(i), percent);
                System.out.println(sampledAncestors.get(i));
            }
        }

        return sampledAncestors;
    }

    /**
     *
     */
    public void countTopologies(ResultsOutput output, Double credSetProbability) {
        FrequencySet<String> topologies = new FrequencySet<String>();
        topologies.setCredSetProbability(credSetProbability);
        List<String> trees;
        trees = getNewickTrees(true);

        for (int i=0; i < trees.size(); i++) {
            topologies.add(trees.get(i));
        }

        output.line("The number of trees in the file = " + trees.size() + ".");

        double sumPercentage = 0;
        output.beginTableOutput(topologies.getCredSetProbability()*100 + "% credible set:", new String[] {"Count", "Percent", "Topology"});

        int i;

        for (i = 0; i < topologies.size(); i++)
            if (sumPercentage < topologies.getCredSetProbability()*100) {
                double percent = (double) (topologies.getFrequency(i) * 100)/(trees.size());

                output.outputRow(new String[] {
                        String.format("%-10d", topologies.getFrequency(i)),
                        String.format("%-10.2f", percent),
                        topologies.get(i)});

                sumPercentage += percent;
            } else {
                break;
            }
        output.endTableOutput();
        output.line("Total \t" + Math.round(sumPercentage) + "%");
        output.line(topologies.getCredSetProbability()*100 + "% credible set has " + i + " trees.");
    }

    /**
     *
     * @param node
     * @return all sampled ancestor clades descendant from node
     */
    public ArrayList<String> extractAllSAClades(Node node) {
        ArrayList<String> tmp = new ArrayList<String>();

        if (node.isLeaf())
            tmp.add(extractSAClade(node));
        if (node.getLeft() != null)
            tmp.addAll(extractAllSAClades(node.getLeft()));
        if (node.getRight() != null)
            tmp.addAll(extractAllSAClades(node.getRight()));

        return tmp;
    }


    /**
     * retern the list of sampled ancestors
     * @param tree
     * @return
     */
    public ArrayList<String> listSA(Tree tree, boolean useRanking){
        ArrayList<String> sampledAncestors = new ArrayList<String>();
        for (int i=0; i<tree.getLeafNodeCount(); i++){
            if (tree.getNode(i).isDirectAncestor()) {
                if (useRanking) {
                    sampledAncestors.add(Integer.toString(getRank(tree, tree.getNode(i))));
                } else {
                    sampledAncestors.add(tree.getNode(i).getID());
                }
            }
        }
        return  sampledAncestors;
    }

    public int getRank(Tree tree, Node node) {
        ArrayList<Node> nodes = new ArrayList<Node>(tree.getExternalNodes());
        Comparator<Node> comp = new NodeComparator();

        Collections.sort(nodes, comp);
        return nodes.size() - nodes.indexOf(node);
    }

    public void printTreeHeights(){
        Tree tree;

        System.out.print("heights <- c(");
        for (int i =0; i < getTotalTreesBurninRemoved()-1; i++) {
            tree = treeInTrace.get(i);
            System.out.print(tree.getRoot().getHeight() + ",");
        }
        tree = treeInTrace.get(getTotalTreesBurninRemoved()-1);
        System.out.println(tree.getRoot().getHeight() + ")");

        System.out.print("lengths <- c(");
        for (int i =0; i < getTotalTreesBurninRemoved()-1; i++) {
            tree = treeInTrace.get(i);
            double length = 0;
            for (int j=0; j< tree.getNodeCount(); j++){
                length += tree.getNode(j).getLength();
            }

            System.out.print(length + ",");
        }
        tree = treeInTrace.get(getTotalTreesBurninRemoved()-1);
        System.out.println(tree.getRoot().getHeight() + ")");
    }

    public void removeFossilsFromAllTrees() {
        Tree tree;
        for (int i =0; i < getTotalTreesBurninRemoved(); i++) {
            tree = treeInTrace.get(i);
            removeFossils(tree.getRoot(), tree, new ArrayList<>());
            System.out.println("tree STATE_"+ i*1000 + " = "+tree.getRoot().toSortedNewick(new int[]{0}, true) + ";");
        }

    }

    public void removeFossils(Node node, Tree tree, ArrayList<Node> removedNodes) {
        if (node.isLeaf()) {
            if (node.getHeight() > 0.00005) {
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

    //********* private ***********

    /**
     *
     * @param node
     * @return a sampled ancestor clade in the format:
     * [SA < A_1, A_n], where SA is the MRCA of (SA, A_1,..., A_n)
     */

    private String extractSAClade(Node node) {
        String tmp = new String();
        String ancestor = node.getID();
        tmp += ancestor + '<';
        if (node.isDirectAncestor()) {
            ArrayList<String> descendants = listSampledNodeIDsUnder(node.getParent());
            tmp+= descendants;
            for (String des:descendants) {
                if (!des.equals(ancestor)) {
                    pairs.add(ancestor + "<" + des);
                }
            }
        }
        return tmp;
    }

    private ArrayList<String> listSampledNodeIDsUnder(Node node) {
        ArrayList<String> tmp = new ArrayList<String>();
        if (!node.isLeaf()) {
            for (Node child : node.getChildren()) {
                tmp.addAll(listSampledNodeIDsUnder(child));
            }
        } else tmp.add(node.getID());
        Collections.sort(tmp);
        return tmp;
    }

    private ArrayList<Integer> listSampledNodeNumbersUnder(Node node) {

        ArrayList<Integer> tmp = new ArrayList<Integer>();
        if (node.isLeaf())
            tmp.add(node.getNr()+1);
        if (node.getLeft() != null)
            tmp.addAll(listSampledNodeNumbersUnder(node.getLeft()));
        if (node.getRight() != null)
            tmp.addAll(listSampledNodeNumbersUnder(node.getRight()));
        return tmp;
    }



    /**
     * static Utils
     */
    public static class Utils {
        /**
         * get list of SA trees from file
         * @param treeFile
         * @return
         * @throws Exception
         */
        public static List<Tree> getTrees (File treeFile) throws Exception {
            SANexusParser parser = new SANexusParser();
            parser.parseFile(treeFile);
            return parser.trees;
        }

        public static String convertToShortTree(String strIn) {
            StringBuilder buf = new StringBuilder();
            boolean skipping = false;
            boolean inComment = false;

            for (int i=0; i < strIn.length(); i++) {
                if (!skipping && !inComment) {

                    switch (strIn.charAt(i)) {
                        case ':':
                            if (strIn.charAt(i+1) != '(') {
                                skipping = true;
                                i += 1;
                            } else {
                                buf.append(strIn.charAt(i));
                            }
                            break;
                        case '[':
                            inComment = true;
                            break;
                        default:
                            buf.append(strIn.charAt(i));
                    }
                } else {
                    if (!inComment && (strIn.charAt(i) == ',' || strIn.charAt(i) == ')')) {
                        buf.append(strIn.charAt(i));
                        skipping = false;
                    }
                    if (inComment && strIn.charAt(i) == ']') {
                        inComment = false;
                    }
                }
            }

            return buf.toString();
        }


    }



}

