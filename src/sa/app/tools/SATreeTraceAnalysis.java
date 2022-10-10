package sa.app.tools;

import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beastfx.app.tools.TreeTraceAnalysis;
import beast.base.util.FrequencySet;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Parser cannot parse SA trees, so that input has to be zero-branch trees.
 * usage: SATreeTraceAnalysis(filePath, outputFilePath)
 *
 * @author Alexandra Gavryushkina
 * @author Walter Xie
 */

public class SATreeTraceAnalysis extends TreeTraceAnalysis {

    FrequencySet<String> pairs = new FrequencySet<>();
    FrequencySet<String> clades = new FrequencySet<>();
    FrequencySet<String> sampledAncestors = new FrequencySet<>();

    int treesWithSACount = 0;
    int totalSACount = 0;

    public SATreeTraceAnalysis() {
        super();
    }

    public SATreeTraceAnalysis(List<Tree> posteriorTreeList, double burninFraction) {
        this();

        addAllTrees(posteriorTreeList, burninFraction);
    }

    @Override
    public String uniqueNewick(Tree tree) {
        return Utils.getSortedNewickWithSAs(tree.getRoot());
    }

    private int getSACount(Tree tree) {
        int count = 0;

        for (Node node : tree.getNodesAsArray())
            if (node.isFake())
                count += 1;

        return count;
    }

    @Override
    public void analyzeTree(Tree tree) {
        super.analyzeTree(tree);

        for (String clade : extractAllSAClades(tree.getRoot()))
            clades.add(clade);

        int saCount = getSACount(tree);
        totalSACount += saCount;
        treesWithSACount += saCount > 0 ? 1 : 0;

        for (String ancestor :listSA(tree, false))
            sampledAncestors.add(ancestor);
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
	public String toReportString(boolean printCladeFrequencies, boolean printPairs, boolean printFrequencies, boolean printTopologyCredibleSet, boolean isHTML) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ResultsOutput output = null;

        if (isHTML) {
            output = new ResultsOutput.HTML(ps);
        } else {
            output = new ResultsOutput.TabDelimitedPlainText(ps);
        }


        if (printCladeFrequencies) {
	        output.beginTableOutput("Clade frequencies", new String[]{"Count", "Percent", "Clade"});
            for (int i =0; i < clades.size(); i++) {
	            double percent = (double) (clades.getFrequency(i) * 100)/(nTrees);

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
	            double percent = (double) (freq * 100)/(nTrees);

                output.outputRow(new String[] {
                        String.format("%-10d", freq),
                        String.format("%-10.2f", percent),
                        pairs.get(i)});
	        }
	        double a =  (double)treesWithSACount/ nTrees;
            output.endTableOutput();
            output.line(String.format(treesWithSACount + " trees (or %2.2f%%) have sampled internal nodes.", a * 100));
        }

        if (printFrequencies) {
            output.beginTableOutput("SA frequencies", new String[]{"Count", "Percent", "SA"});
	        for (int i =0; i < sampledAncestors.size(); i++) {
	            double percent = (double) (sampledAncestors.getFrequency(i) * 100)/nTrees;

                output.outputRow(new String[] {
                        String.format("%-10d", sampledAncestors.getFrequency(i)),
                        String.format("%-10.2f", percent),
                        sampledAncestors.get(i)});
            }
            output.endTableOutput();
        }

        if (printTopologyCredibleSet) {
            countTopologies(output);
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
        double fraction = (double)treesWithSACount/nTrees;
        System.out.format(treesWithSACount + " trees (or %2.2f%%) have sampled internal nodes.%n", fraction * 100);
        System.out.println();
    }

    /**
     * print the average number of sampled ancestors in all trees
     * to the standard output
     */
    public void countAverageSampledAncestorNumber(){
        System.out.format("The average number of sampled ancestors per tree is %2.2f", ((double) totalSACount / nTrees));
        System.out.println();

    }

    /**
     * prints the frequency of each sampled ancestor clade to the standard output
     *
     * @param countPairs if true then the frequencies of sampled ancestors pairs are also counted
     * @throws Exception
     */
    public void countSAClades(boolean countPairs) throws Exception {

        System.out.println("Clade frequencies");
        System.out.println();
        System.out.println("Count \t Percent \t Clade");
        System.out.println();
        for (int i =0; i < clades.size(); i++) {
            double percent = (double) (clades.getFrequency(i) * 100)/(nTrees);
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
                double percent = (double) (freq * 100)/(nTrees);
                System.out.format("%-10d %-10.2f", freq, percent);
                System.out.println(pairs.get(i));
            }
            System.out.println();
            double a =  (double)treesWithSACount/ nTrees;
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
        if (print) {
            System.out.println("There are " + nTrees + " trees in the file. " +
                    "Then sampled ancestors are counted");
            System.out.println();
            System.out.println("Count \t Percent \t SA");
            for (int i =0; i < sampledAncestors.size(); i++) {
                double percent = (double) (sampledAncestors.getFrequency(i) * 100)/(nTrees);
                System.out.format("%-10d %-10.2f", sampledAncestors.getFrequency(i), percent);
                System.out.println(sampledAncestors.get(i));
            }
        }

        return sampledAncestors;
    }

    /**
     * Report the distribution of topologies in tree trace.
     */
    public void countTopologies(ResultsOutput output) {
        output.line("The number of trees in the file = " + nTrees + ".");

        double sumPercentage = 0;
        output.beginTableOutput(topologiesFrequencySet.getCredSetProbability()*100 + "% credible set:", new String[] {"Count", "Percent", "Topology"});

        int i;

        for (i = 0; i < topologiesFrequencySet.size(); i++)
            if (sumPercentage < topologiesFrequencySet.getCredSetProbability()*100) {
                double percent = (double) (topologiesFrequencySet.getFrequency(i) * 100)/(nTrees);

                output.outputRow(new String[] {
                        String.format("%-10d", topologiesFrequencySet.getFrequency(i)),
                        String.format("%-10.2f", percent),
                        topologiesFrequencySet.get(i)});

                sumPercentage += percent;
            } else {
                break;
            }
        output.endTableOutput();
        output.line("Total \t" + Math.round(sumPercentage) + "%");
        output.line(topologiesFrequencySet.getCredSetProbability()*100 + "% credible set has " + i + " trees.");
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


    /**
     * Static class containing utility methods
     */
    public static class Utils {
        /**
         * Get tree topology in Newick that is sorted by taxon labels.
         *
         * @param node root of tree
         * @return newick string
         */
        public static String getSortedNewickWithSAs(Node node) {
            if (node.isLeaf()) {
                return String.valueOf(node.getID());
            } else {
                StringBuilder builder = new StringBuilder("(");

                List<String> subTrees = new ArrayList<>();
                for (Node child : node.getChildren()) {
                    if (!child.isDirectAncestor())
                        subTrees.add(getSortedNewickWithSAs(child));
                }

                Collections.sort(subTrees);

                for (int i = 0; i < subTrees.size(); i++) {
                    builder.append(subTrees.get(i));
                    if (i < subTrees.size() - 1) {
                        builder.append(",");
                    }
                }
                builder.append(")");

                if (node.isFake())
                    builder.append(node.getDirectAncestorChild().getID());

                return builder.toString();
            }
        }

    }
}

