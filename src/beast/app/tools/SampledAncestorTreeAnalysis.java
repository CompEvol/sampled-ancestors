package beast.app.tools;

import beast.core.util.ESS;
import beast.evolution.tree.Node;
import beast.util.TreeParser;

import java.util.ArrayList;
import java.util.Arrays;

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

    public void perform(boolean useNumbers) throws Exception {
        countTopologies(useNumbers);
        //countTreesWithDClades();
        //countClades();
    }

    public void countTreesWithDClades() throws Exception {
        TreeParser tree;


        int dCladeCount = 0;

        for (int i =0; i < trace.getTreeCount(); i++) {
            tree = new TreeParser(trace.getTrees()[i], false, true, false, 1);
            int j;
            for (j=0; j<tree.getNodeCount(); j++)
                if (tree.getNode(j).getChildCount() == 1) {
                    dCladeCount++;
                    break;
                }
        }

        double a =  (double)dCladeCount/trace.getTreeCount();
        System.out.format(dCladeCount + " trees (or %2.2f%%) have sampled internal nodes.%n", a*100);
        System.out.println();
    }

    public void countClades()throws Exception {

        FrequencySet<String> clades = new FrequencySet<String>();
        ArrayList<String> tmp = new ArrayList<String>();

        int dCladeCount = 0;

        for (int i=0; i < trace.getTreeCount(); i++) {
            TreeParser tree = new TreeParser(trace.getTrees()[i], false, true, false, 1);
            ArrayList<String> dClades =  extractAllDClades(tree.getRoot());
            tmp.addAll(dClades);
            for (int j=0; j<tree.getNodeCount(); j++)
                if (tree.getNode(j).getChildCount() == 1) {
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
            double percent = (double) (clades.getFrequency(i) * 100)/(trace.getTreeCount());
            System.out.format("%-10d %-10.2f", clades.getFrequency(i), percent);
            System.out.println(clades.get(i));
        }
        System.out.println();

        System.out.println("Pair frequencies");
        System.out.println();
        System.out.println("Count \t Percent \t Pair");
        System.out.println();
        for (int i =0; i < pairs.size(); i++) {
            double percent = (double) (pairs.getFrequency(i) * 100)/(trace.getTreeCount());
            System.out.format("%-10d %-10.2f", pairs.getFrequency(i), percent);
            System.out.println(pairs.get(i));
        }
        System.out.println();
        double a =  (double)dCladeCount/trace.getTreeCount();
        System.out.format(dCladeCount + " trees (or %2.2f%%) have sampled internal nodes.%n", a*100);
        System.out.println();
    }

    public void countTopologies(boolean useNumbers) {
        FrequencySet<String> topologies = new FrequencySet<String>();
        String[] trees;
        if (useNumbers) {
            trees = trace.getShortTrees();
        } else {
            trees = trace.getLabeledTrees();
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

        String strForPlottingx = new String();
        String strForPlottingy = new String();

        for (i =0; i < topologies.size(); i++)
            if (sumPercentage < percentCredSet) {
                double percent = (double) (topologies.getFrequency(i) * 100)/(trees.length);
                System.out.format("%-10d %-10.2f", topologies.getFrequency(i), percent);
                System.out.println(topologies.get(i));
                strForPlottingx += i + ", ";
                strForPlottingy += (double) (topologies.getFrequency(i))/(trees.length) + ", ";
                sumPercentage += percent;
            } else {
                break;
            }
        System.out.println();
        System.out.println("Total \t" + Math.round(sumPercentage) + "%");
        System.out.println();
        System.out.println(percentCredSet + "% credible set has " + i + " trees.");
        System.out.println();
        System.out.println("X = " + strForPlottingx);
        System.out.println("Y = " + strForPlottingy);
        System.out.println();

        /*System.out.println("Numeric tree representation");
        for(int j =0; j < 50; j++)
            System.out.print(j + ", ");
        System.out.println();
        for (int j =0; j < 50; j++)
            System.out.print(numericTrees[j] + ", ");  */

    }


    public void countTopologiesTest() {
        FrequencySet<String> topologies = new FrequencySet<String>();
        String[] trees = trace.getShortTrees();

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
                double trueVal = SDE(topologies.get(i));
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

    private double SDE(String tree) {

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

    private String extractDClade(Node node) {
        String tmp = new String();
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

        return tmp;
    }

    public ArrayList<String> extractAllDClades(Node node) {
        ArrayList<String> tmp = new ArrayList<String>();

        if (node.getChildCount() < 2)
            tmp.add(extractDClade(node));
        if (node.getLeft() != null)
            tmp.addAll(extractAllDClades(node.getLeft()));
        if (node.getRight() != null)
            tmp.addAll(extractAllDClades(node.getRight()));

        return tmp;
    }



}