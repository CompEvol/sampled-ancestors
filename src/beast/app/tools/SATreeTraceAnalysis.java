package beast.app.tools;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.evolution.tree.TreeTraceAnalysis;
import beast.evolution.tree.ZeroBranchSANode;
import beast.util.FrequencySet;
import beast.util.Randomizer;
import beast.util.SANexusParser;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

/**
 * @author Alexandra Gavryushkina
 * @author Walter Xie
 */
//TODO labeledTrees
public class SATreeTraceAnalysis extends TreeTraceAnalysis {

    FrequencySet<String> pairs = new FrequencySet<String>();

    public SATreeTraceAnalysis(List<Tree> posteriorTreeList) {
        super(posteriorTreeList);
    }

    public SATreeTraceAnalysis(List<Tree> posteriorTreeList, double burninFraction) {
        super(posteriorTreeList, burninFraction);
    }

    @Override
    public void analyze(double credSetProbability) {
        try {
            countClades(true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        removeFossils();

        // this should be after removeFossils
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

    public List<String> getTaxaLabelTrees () {
        // why toSortedNewick not in Tree class to make it easy, instead to use labelNr + 1
        throw new UnsupportedOperationException("toSortedNewick not support taxa label");
    }


    /**
     * @parameter isTaxaLabel is false if the indices of sampled nodes should be use as labels
     * and taxa names are used otherwise
     */
    @Deprecated
    public void perform(boolean isTaxaLabel) throws Exception {
        countClades(true, true);
        countSampledAncestors(true);
        countSAFrequencies(true, false, 0.445);
        printTreeHeights();
        countTopologies(false);
    }


    public void countTreesWithDClades() throws Exception {

        int dCladeCount = 0;
        Tree tree;

        for (int i =0; i < getTotalTreesBurninRemoved(); i++) {
            tree = treeInTrace.get(i);
            int j;
            for (j=0; j<tree.getNodeCount(); j++)
                if (tree.getNode(j).getChildCount() == 1) {
                    dCladeCount++;
                    break;
                }
        }

        double a =  (double)dCladeCount/ getTotalTreesBurninRemoved();
        System.out.format(dCladeCount + " trees (or %2.2f%%) have sampled internal nodes.%n", a*100);
        System.out.println();
    }

    /**
     * print the number of sampled ancestor in each tree
     *
     * @param zeroBranchTrees
     */
    public void countSampledAncestors(boolean zeroBranchTrees){
        Tree tree;
        int sAcount;
        int totalsAcount = 0;

        if (zeroBranchTrees) {
            //System.out.println("Sample \t Prior \t Posterior");
            for (int i =0; i < getTotalTreesBurninRemoved()-1; i++) {
                tree = treeInTrace.get(i);
                sAcount=0;
                for (int j=0; j<tree.getNodeCount(); j++) {
                    if (((ZeroBranchSANode)tree.getNode(j)).isFake()) sAcount++;
                }
                totalsAcount += sAcount;
                //System.out.println(i + "\t" + sAcount + "\t" + Randomizer.nextDouble());
            }

            tree = treeInTrace.get(getTotalTreesBurninRemoved()-1);
            sAcount=0;
            for (int j=0; j<tree.getNodeCount(); j++) {
                if (((ZeroBranchSANode)tree.getNode(j)).isFake()) sAcount++;
            }
            totalsAcount += sAcount;
            //System.out.println(getTotalTreesBurninRemoved()-1 + "\t" + sAcount + "\t" + Randomizer.nextDouble());
            System.out.format("The average number of sampled ancestors per tree is %2.2f", ((double) totalsAcount / getTotalTreesBurninRemoved()));
            System.out.println();

        }

    }

    /**
     * print the quantity of each sampled ancestor clade encountered in trees
     *
     * @param zeroBranchTrees  if true then beast trees are treated as zero branch SA trees
     * @param countPairs       if true then sampled ancestors pairs are also counted and printed
     * @throws Exception
     */
    public void countClades(boolean zeroBranchTrees, boolean countPairs) throws Exception {

        FrequencySet<String> clades = new FrequencySet<String>();
        ArrayList<String> tmp = new ArrayList<String>();

        int dCladeCount = 0;

        for (int i=0; i < getTotalTreesBurninRemoved(); i++) {
            Tree tree = treeInTrace.get(i);
            ArrayList<String> dClades =  extractAllDClades(tree.getRoot(),zeroBranchTrees);
            tmp.addAll(dClades);
            for (int j=0; j<tree.getNodeCount(); j++)
                if ((!zeroBranchTrees && tree.getNode(j).getChildCount() == 1) || (zeroBranchTrees && ((ZeroBranchSANode)tree.getNode(j)).isFake())) {
                    dCladeCount++;
                    break;
                }
        }

        for (int i=0; i < tmp.size(); i++)
            clades.add(tmp.get(i));

        System.out.println("Clade frequencies");
        System.out.println();
        System.out.println("Count \t Percent \t Clade");
        System.out.println();
        for (int i =0; i < clades.size(); i++) {
            double percent = (double) (clades.getFrequency(i) * 100)/(getTotalTreesBurninRemoved());
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
                double percent = (double) (pairs.getFrequency(i) * 100)/(getTotalTreesBurninRemoved());
                System.out.format("%-10d %-10.2f", pairs.getFrequency(i), percent);
                System.out.println(pairs.get(i));
            }
            System.out.println();
            double a =  (double)dCladeCount/ getTotalTreesBurninRemoved();
            System.out.format(dCladeCount + " trees (or %2.2f%%) have sampled internal nodes.%n", a*100);
            System.out.println();
        }

    }

    /**
     * for each sampled node, it counts the number of trees in which this sampled node is a sampled ancestor
     * @param print  if is true then the counts are printed
     * @param useRanking if is true then sampled nodes are distinguished by the time order of sampling
     *                   (that is, for each n in {1,..., #SampledNodes} it counts the number of
     *                   trees in which nth sampled node is a sampled ancestor)
     *                   if is false, labels are used
     * @return a set of sampled nodes with assigned frequencies
     */
    public FrequencySet<String> countSAFrequencies(boolean print, boolean useRanking, double cutoff) {
        FrequencySet<String> sampledAncestors = new FrequencySet<String>();
        ArrayList<String> tmp = new ArrayList<String>();
        ArrayList<String> predictedSA = new ArrayList<String>();

        int burnIn = getTotalTreesBurninRemoved()/10;

        for (int i=burnIn; i < getTotalTreesBurninRemoved(); i++) {
            Tree tree = treeInTrace.get(i);
            tmp.addAll(listSA(tree, useRanking));
        }

        for (int i=0; i < tmp.size(); i++) {
            sampledAncestors.add(tmp.get(i));
        }

        if (print) {
            System.out.println("There are " + getTotalTreesBurninRemoved() + " trees in the file. The first " + burnIn + " are removed as a burn-in. " +
                    "Then sampled ancestors are counted");
            System.out.println();
            System.out.println("Count \t Percent \t SA");
            System.out.println("flag1");
            for (int i =0; i < sampledAncestors.size(); i++) {
                double percent = (double) (sampledAncestors.getFrequency(i) * 100)/(getTotalTreesBurninRemoved() - burnIn);
                if (percent >= cutoff*100) {
                    predictedSA.add(sampledAncestors.get(i));
                }
                System.out.format("%-10d %-10.2f", sampledAncestors.getFrequency(i), percent);
                System.out.println(sampledAncestors.get(i));
            }
            System.out.println("flag2");
        }

        int trueSACount;
        boolean falseSADetected;
        Tree randomTree;
        int randomIndex;
        do {
            randomIndex = Randomizer.nextInt(getTotalTreesBurninRemoved());
            randomTree = treeInTrace.get(randomIndex);
            trueSACount=0;
            falseSADetected = false;
            for (int i=0; i<randomTree.getExternalNodes().size(); i++){
                if (((ZeroBranchSANode)randomTree.getNode(i)).isDirectAncestor()) {
                    if (predictedSA.contains(randomTree.getNode(i).getID())){
                        trueSACount++;
                    }  else {
                        falseSADetected = true;
                        break;
                    }
                }
            }
        } while (falseSADetected || trueSACount != predictedSA.size());

        System.out.println("Random tree with all the predicted sampled ancestors and no other sampled ancestors ");
        System.out.println(randomTree.getRoot().toShortNewick(false));
        System.out.println("Random tree index " + randomIndex);

        return sampledAncestors;
    }

    /**
     *
     * @param isTaxaLabel  false if the indices of sampled nodes should be use as labels and taxa names are used otherwise
     */
    public void countTopologies(boolean isTaxaLabel) {
        FrequencySet<String> topologies = new FrequencySet<String>();
        List<String> trees;
        if (isTaxaLabel) {
            trees = getTaxaLabelTrees();
        } else {
            trees = getNewickTrees(true);
        }

        for (int i=0; i < trees.size(); i++) {
            topologies.add(trees.get(i));
        }

//        Double[] numericTrees = new Double[trees.size()];
//
//        Arrays.fill(numericTrees, 0.0);
//
//        for (int i=0; i< topologies.size(); i++) {
//            for (int j=0; j < trees.size(); j++) {
//                if (trees.get(j).equals(topologies.get(i))) {
//                    numericTrees[j] = (double)i+1;
//                }
//            }
//        }
//
//        int nSampleInterval = 100;
//        // calc effective sample size
//        double ACT = ESS.ACT(numericTrees, nSampleInterval);
//        double ESS = numericTrees.length / (ACT / nSampleInterval);
//
        System.out.println("The number of trees in the file = " + trees.size() + ".");
//        System.out.println("ESS = " + ESS);
//        System.out.println();

        double sumPercentage = 0;
        System.out.println(getCredSetProbability()*100 + "% credible set: ");
        System.out.println();
        System.out.println("Count \t Percent \t Topology");
        System.out.println();
        int i;

        for (i =0; i < topologies.size(); i++)
            if (sumPercentage < getCredSetProbability()*100) {
                double percent = (double) (topologies.getFrequency(i) * 100)/(trees.size());
                System.out.format("%-10d %-10f ", topologies.getFrequency(i), percent);
                System.out.println(topologies.get(i));
                sumPercentage += percent;
            } else {
                break;
            }
        System.out.println();
        System.out.println("Total \t" + Math.round(sumPercentage) + "%");
        System.out.println();
        System.out.println(getCredSetProbability()*100 + "% credible set has " + i + " trees.");
        System.out.println();

    }

    public ArrayList<String> extractAllDClades(Node node, boolean zeroBranchTrees) {
        ArrayList<String> tmp = new ArrayList<String>();

        if ((!zeroBranchTrees && node.getChildCount() < 2) || (zeroBranchTrees && node.isLeaf()))
            tmp.add(extractDClade(node, zeroBranchTrees));
        if (node.getLeft() != null)
            tmp.addAll(extractAllDClades(node.getLeft(), zeroBranchTrees));
        if (node.getRight() != null)
            tmp.addAll(extractAllDClades(node.getRight(), zeroBranchTrees));

        return tmp;
    }

    /**
     * retern the list of sampled ancestors, WARNING works only for zeroBranchTrees
     * @param tree
     * @return
     */
    public ArrayList<String> listSA(Tree tree, boolean useRanking){
        ArrayList<String> sampledAncestors = new ArrayList<String>();
        int count = 0;
        for (int i=0; i<tree.getLeafNodeCount(); i++){
            if (((ZeroBranchSANode)tree.getNode(i)).isDirectAncestor()) {
                if (useRanking) {
                    sampledAncestors.add(Integer.toString(getRank(tree, tree.getNode(i))));
                } else {
                    sampledAncestors.add(tree.getNode(i).getID());
                    if (tree.getNode(i).getID().equals("0")) {
                        count++;
                    }
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

//    public void removeFossils(){
//        Tree tree;
//
//        for (int i =0; i < getTotalTreesBurninRemoved(); i++) {
//            tree = treeInTrace.get(i);
//            for (int j=0; j<tree.getNodeCount(); j++) {
//                Node fake = tree.getNode(j);
//                if (((ZeroBranchSANode)fake).isFake()) {
//                    Node parent = fake.getParent();
//                    Node otherChild = ((ZeroBranchSANode)fake.getLeft()).isDirectAncestor()?fake.getRight():fake.getLeft();
//                    parent.removeChild(fake);
//                    parent.addChild(otherChild);
//                } else if (!fake.isLeaf() && ((fake.getLeft().isLeaf() && fake.getLeft().getHeight()>0.0000000005) || (fake.getRight().isLeaf() && fake.getRight().getHeight() > 0.0000000005))) {
//                    Node parent = fake.getParent();
//                    Node otherChild= (fake.getLeft().isLeaf() && fake.getLeft().getHeight()>0.0000000005)?fake.getRight():fake.getLeft();
//                    parent.removeChild(fake);
//                    parent.addChild(otherChild);
//                }
//            }
//            System.out.println("tree STATE_"+ i*1000 + " = "+tree.getRoot().toSortedNewick(new int[]{0}, false) + ";");
//        }
//
//    }

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
//            if (!removedNodes.contains(left)) {
//                removeFossils(left, tree, removedNodes);
//            }
//            if (!removedNodes.contains(right)) {
//                removeFossils(right, tree, removedNodes);
//            }


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

    private ArrayList<Integer> listNodesUnder(Node node) {

        ArrayList<Integer> tmp = new ArrayList<Integer>();
        if (node.getChildCount() < 2)
            tmp.add(node.getNr()+1);
        if (node.getLeft() != null)
            tmp.addAll(listNodesUnder(node.getLeft()));
        if (node.getRight() != null)
            tmp.addAll(listNodesUnder(node.getRight()));
        return tmp;
    }

    private ArrayList<String> listNodesUnder(Node node, boolean useID) {
        ArrayList<String> tmp = new ArrayList<String>();
        if (!node.isLeaf()) {
            for (Node child : node.getChildren()) {
                tmp.addAll(listNodesUnder(child, useID));
            }
        } else tmp.add(node.getID());
        Collections.sort(tmp);
        return tmp;
    }

    private String extractDClade(Node node, boolean zeroBranchTrees) {
        String tmp = new String();
        if (zeroBranchTrees) {
            String ancestor = node.getID();
            tmp += ancestor + '<';
            if (((ZeroBranchSANode)node).isDirectAncestor()) {
                ArrayList<String> descendants = listNodesUnder(node.getParent(), true);
                tmp+= descendants;
                for (String des:descendants) {
                    if (!des.equals(ancestor)) {
                        pairs.add(ancestor + "<" + des);
                    }
                }
            }
        } else {
            String ancestor = Integer.toString(node.getNr() + 1);
            tmp += ancestor + '<';
            if (node.getChildCount() == 1) {
                Integer[] descendants = listNodesUnder(node.getLeft()).toArray(new Integer[0]);
                Arrays.sort(descendants);
                for (int i=0; i < descendants.length; i++){
                    String pair = ancestor + '<' + descendants[i];
                    pairs.add(pair);
                }
                tmp += Arrays.toString(descendants);
            }
        }


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

