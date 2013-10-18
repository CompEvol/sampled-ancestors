package beast.app.tools;

import beast.core.util.ESS;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.evolution.tree.ZeroBranchSANode;
import beast.evolution.tree.ZeroBranchSATree;
import beast.util.Randomizer;
import beast.util.TreeParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexandra Gavryushkina
 */

public class SampledAncestorTreeAnalysis {

    SampledAncestorTreeTrace trace;
    int percentCredSet;

    FrequencySet<String> pairs = new FrequencySet<String>();

    public SampledAncestorTreeAnalysis(SampledAncestorTreeTrace newTrace, int percent) {
        trace = newTrace;
        percentCredSet = percent;
    }

    /**
     * @parameter useNumbers is true if the indices of sampled nodes should be use as labels
     * and taxa names are used otherwise
     */
    public void perform(boolean useNumbers) throws Exception {
       //countClades(true, false);
       //countSampledAncestors(true);
        countSAFrequencies(true);
    }

    public void countTreesWithDClades() throws Exception {

        int dCladeCount = 0;
        Tree tree;

        for (int i =0; i < trace.treeCount; i++) {
            tree = trace.beastTrees.get(i);
            int j;
            for (j=0; j<tree.getNodeCount(); j++)
                if (tree.getNode(j).getChildCount() == 1) {
                    dCladeCount++;
                    break;
                }
        }

        double a =  (double)dCladeCount/trace.treeCount;
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
            for (int i =0; i < trace.treeCount-1; i++) {
                tree = trace.beastTrees.get(i);
                sAcount=0;
                for (int j=0; j<tree.getNodeCount(); j++) {
                    if (((ZeroBranchSANode)tree.getNode(j)).isFake()) sAcount++;
                }
                totalsAcount += sAcount;
                //System.out.println(i + "\t" + sAcount + "\t" + Randomizer.nextDouble());
            }

            tree = trace.beastTrees.get(trace.treeCount-1);
            sAcount=0;
            for (int j=0; j<tree.getNodeCount(); j++) {
                if (((ZeroBranchSANode)tree.getNode(j)).isFake()) sAcount++;
            }
            totalsAcount += sAcount;
            //System.out.println(trace.treeCount-1 + "\t" + sAcount + "\t" + Randomizer.nextDouble());
            System.out.format("The average number of sampled ancestors per tree is %2.2f", ((double) totalsAcount / trace.treeCount));
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

        for (int i=0; i < trace.treeCount; i++) {
            Tree tree = trace.beastTrees.get(i);
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
            double percent = (double) (clades.getFrequency(i) * 100)/(trace.treeCount);
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
                double percent = (double) (pairs.getFrequency(i) * 100)/(trace.treeCount);
                System.out.format("%-10d %-10.2f", pairs.getFrequency(i), percent);
                System.out.println(pairs.get(i));
            }
            System.out.println();
            double a =  (double)dCladeCount/trace.treeCount;
            System.out.format(dCladeCount + " trees (or %2.2f%%) have sampled internal nodes.%n", a*100);
            System.out.println();
        }
    }

    public FrequencySet<String> countSAFrequencies(Boolean print) {
        FrequencySet<String> sampledAncestors = new FrequencySet<String>();
        ArrayList<String> tmp = new ArrayList<String>();

        for (int i=0; i < trace.treeCount; i++) {
            Tree tree = trace.beastTrees.get(i);
            tmp.addAll(listSA(tree));
        }

        for (int i=0; i < tmp.size(); i++) {
            sampledAncestors.add(tmp.get(i));
        }

        if (print) {
            System.out.println("Sampled ancestors frequencies");
            System.out.println();
            System.out.println("Count \t Percent \t Clade");
            System.out.println();
            for (int i =0; i < sampledAncestors.size(); i++) {
                double percent = (double) (sampledAncestors.getFrequency(i) * 100)/(trace.treeCount);
                System.out.format("%-10d %-10.2f", sampledAncestors.getFrequency(i), percent);
                System.out.println(sampledAncestors.get(i));
            }
            System.out.println();
        }

        return sampledAncestors;
    }

    public void countTopologies(boolean useNumbers) {
        FrequencySet<String> topologies = new FrequencySet<String>();
        String[] trees;
        if (useNumbers) {
            trees = trace.shortTrees;
        } else {
            trees = trace.labeledTrees;
        }

        for (int i=0; i < trees.length; i++) {
            topologies.add(trees[i]);
        }

        Double[] numericTrees = new Double[trees.length];

        Arrays.fill(numericTrees, 0.0);

        for (int i=0; i< topologies.size(); i++) {
            for (int j=0; j < trees.length; j++) {
                if (trees[j].equals(topologies.get(i))) {
                    numericTrees[j] = (double)i+1;
                }
            }
        }

        int nSampleInterval = 100;
        // calc effective sample size
        double ACT = ESS.ACT(numericTrees, nSampleInterval);
        double ESS = numericTrees.length / (ACT / nSampleInterval);

        System.out.println("The number of trees in the file = " + trees.length + ".");
        //System.out.println("ACT = " + ACT);
        System.out.println("ESS = " + ESS);
        System.out.println();

        double sumPercentage = 0;
        System.out.println(percentCredSet + "% credible set: ");
        System.out.println();
        System.out.println("Count \t Percent \t Topology");
        System.out.println();
        int i;

        for (i =0; i < topologies.size(); i++)
            if (sumPercentage < percentCredSet) {
                double percent = (double) (topologies.getFrequency(i) * 100)/(trees.length);
                System.out.format("%-10d %-10.2f", topologies.getFrequency(i), percent);
                System.out.println(topologies.get(i));
                sumPercentage += percent;
            } else {
                break;
            }
        System.out.println();
        System.out.println("Total \t" + Math.round(sumPercentage) + "%");
        System.out.println();
        System.out.println(percentCredSet + "% credible set has " + i + " trees.");
        System.out.println();

        /*System.out.println("Numeric tree representation");
        for(int j =0; j < 50; j++)
            System.out.print(j + ", ");
        System.out.println();
        for (int j =0; j < 50; j++)
            System.out.print(numericTrees[j] + ", ");  */

    }

    //test for sampling from prior.
    // testType = 1 --- test with fixed time of origin.
    // testType = 2 --- with logNormal prior on distance between root height and origin time.
    public void countTopologiesTest(int testType) {
        FrequencySet<String> topologies = new FrequencySet<String>();
        String[] trees = trace.shortTrees;

        for (int i=0; i < trees.length; i++) {
            topologies.add(trees[i]);
        }

        Double[] treesNumberRepresentation = new Double[trees.length];

        Arrays.fill(treesNumberRepresentation, 0.0);

        for (int i=0; i< topologies.size(); i++) {
            for (int j=0; j < trees.length; j++) {
                if (trees[j] == topologies.get(i)) {
                    treesNumberRepresentation[j] = (double)i;
                }
            }
        }

        int nSampleInterval = 100;
        // calc effective sample size
        double ACT = ESS.ACT(treesNumberRepresentation, nSampleInterval);
        double ESS = treesNumberRepresentation.length / (ACT / nSampleInterval);

        System.out.println("ACT = " + ACT);
        System.out.println("ESS = " + ESS);

        System.out.println("The number of trees in the file = " + trees.length + ".");
        System.out.println();

        /*if (0 >= percentageCredSet || percentageCredSet >= 100) {
            percentageCredSet = 95;
        }  */

        int percentageCredSet = 100;

        double sumPercentage = 0;
        System.out.println(percentageCredSet + "% credible set: ");
        System.out.println();
        System.out.println("Count \t Percent \t Topology");
        System.out.println();
        int i;

        for (i =0; i < topologies.size(); i++)
            if (sumPercentage < percentageCredSet) {
                double percent = (double) (topologies.getFrequency(i) * 100)/(trees.length);
                double trueVal=0;
                switch (testType) {
                    case 1: trueVal = SDE1(topologies.get(i));
                            break;
                    case 2: trueVal = SDE2(topologies.get(i));
                            break;
                }
                double sde = 200 * Math.sqrt((trueVal * (1-trueVal))/ESS);
                String correctness;
                if ((100 * trueVal < percent && percent < 100 * trueVal + sde) || (100 * trueVal -sde < percent && percent < 100 * trueVal))
                    correctness = "correct";
                else correctness = "incorrect";
                System.out.println(topologies.getFrequency(i) + "\t" + percent + "%\t" + topologies.get(i) + "\t" + correctness);
                sumPercentage += percent;
            } else {
                break;
            }
        System.out.println();
        System.out.println("Total \t" + Math.round(sumPercentage) + "%");
        System.out.println();
        System.out.println(percentageCredSet + "% credible set has " + i + " trees.");
        System.out.println();

    }

    private double SDE1(String tree) {

        if (tree.equals("((1,2),3)")) {
            return 0.7133;
        } else if (tree.equals("((1,2))3")) {
            return 0.0702;
        } else if (tree.equals("((1)2,3)")) {
            return 0.0689;
        } else if (tree.equals("((1,3),2)") || tree.equals("(1,(2,3))")) {
            return 0.0577;
        } else if (tree.equals("(1,(2)3)") || tree.equals("((1)3,2)")) {
            return 0.0124;
        } else if (tree.equals("((1)2)3")) {
            return 0.0074;
        } else return 0.0;
    }

    private double SDE2(String tree) {

        if (tree.equals("((1,2),3)")) {
            return 0.6805;
        } else if (tree.equals("((1,2))3")) {
            return 0.1377;
        } else if (tree.equals("((1)2,3)")) {
            return 0.0676;
        } else if (tree.equals("((1,3),2)") || tree.equals("(1,(2,3))")) {
            return 0.0377;
        } else if (tree.equals("(1,(2)3)") || tree.equals("((1)3,2)")) {
            return 0.0121;
        } else if (tree.equals("((1)2)3")) {
            return 0.0145;
        } else return 0.0;
    }

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
                tmp+= listNodesUnder(node.getParent(), true);
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
    public ArrayList<String> listSA(Tree tree){
        ArrayList<String> sampledAncestors = new ArrayList<String>();
        for (int i=0; i<tree.getLeafNodeCount(); i++){
            if (((ZeroBranchSANode)tree.getNode(i)).isDirectAncestor()) {
                sampledAncestors.add(tree.getNode(i).getID());
            }
        }
        return  sampledAncestors;
    }




}