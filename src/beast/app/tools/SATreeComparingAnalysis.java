package beast.app.tools;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.evolution.tree.ZeroBranchSANode;
import beast.evolution.tree.ZeroBranchSATree;
import beast.util.NexusParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alexandra Gavryushkina
 */


public class SATreeComparingAnalysis {

    private NexusParser parser1;
    private NexusParser parser2;

    public SATreeComparingAnalysis(NexusParser newParser1, NexusParser newParser2) {
        parser1 = newParser1;
        parser2 = newParser2;
    }

    public SATreeComparingAnalysis() {
    }

    public void perform() {
        //int labelCount = parser1.translationMap.keySet().size();
        List<Tree> trees1 = parser1.trees;
        List<Tree> trees2 = parser2.trees;
        int treeCount1 = trees1.size();
        int treeCount2 = trees2.size();
//        if (labelCount != parser2.translationMap.keySet().size()) {
//            System.out.println("The nubmer of sampled nodes are different in two files");
//            System.exit(0);
//        }

        SATreeComparingAnalysis.TreeSummary[] treeSummary1 = makeTreeSummaryForAllTrees(trees1, treeCount1);
        SATreeComparingAnalysis.TreeSummary[] treeSummary2 = makeTreeSummaryForAllTrees(trees2, treeCount2);

    };

    private SATreeComparingAnalysis.TreeSummary[] makeTreeSummaryForAllTrees(List<Tree> trees, int treeCount){
        SATreeComparingAnalysis.TreeSummary[] treeSummary = new TreeSummary[treeCount];
        for(int treeIndex=0; treeIndex < treeCount; treeIndex++) {
            Tree tree = trees.get(treeIndex);
            ArrayList<Integer> dAPattern = new ArrayList<Integer>();
            for (int i=tree.getLeafNodeCount(); i< tree.getNodeCount(); i++){
                if (((ZeroBranchSANode)tree.getNode(i)).isFake()) {
                    int descendantsCount = tree.getNode(i).getLeafNodeCount() - 1;
                    if (descendantsCount > 0) {
                        for(int j=dAPattern.size(); j<descendantsCount; j++){
                            dAPattern.add(0);
                        }
                        int tmp = dAPattern.get(descendantsCount-1);
                        tmp++;
                        dAPattern.set(descendantsCount - 1, tmp);
                    }
                }
            }
            treeSummary[treeIndex] = new TreeSummary(tree.getRoot().getHeight(), tree.getLeafNodeCount() - ((ZeroBranchSATree)tree).getDirectAncestorNodeCount(), dAPattern);
        }
        return treeSummary;
    }

    private class TreeSummary {
        double rootHeight;
        int tipsCount;
        ArrayList<Integer> dAPattern;     //the ith element of this list is the number of direct ancestors in the tree
                                          // which have i sampled descendants

        public TreeSummary(double newRootHeight, int newTipsCount, ArrayList<Integer> newDAPattern) {
            this.rootHeight = newRootHeight;
            this.tipsCount = newTipsCount;
            this.dAPattern = newDAPattern;
        }
    }

    public static void main (String[] arg) throws Exception{


        Node root = new Node();
        root.setNr(4);
        root.setHeight(2.0);
        Node left = new Node();
        left.setNr(3);
        left.setHeight(1.0);
        root.setLeft(left);
        left.setParent(root);
        Node right = new Node();
        right.setNr(2);
        right.setHeight(2.0);
        root.setRight(right);
        right.setParent(root);
        Node leftLeft = new Node();
        leftLeft.setNr(0);
        leftLeft.setHeight(0.0);
        left.setLeft(leftLeft);
        leftLeft.setParent(left);
        Node leftRight = new Node();
        leftRight.setNr(1);
        leftRight.setHeight(1.0);
        left.setRight(leftRight);
        leftRight.setParent(left);
        Tree tree = new Tree(root);

        System.out.println("The tree is " + tree.getRoot().toShortNewick(false));


        SATreeComparingAnalysis analysis = new SATreeComparingAnalysis();
        //process tree. consider Fake SA trees
        ArrayList<Integer> dAPattern = new ArrayList<Integer>();
        for (int i=tree.getLeafNodeCount(); i< tree.getNodeCount(); i++){
            if (((ZeroBranchSANode)tree.getNode(i)).isFake()) {
                int descendantsCount = tree.getNode(i).getLeafNodeCount() - 1;
                if (descendantsCount > 0) {
                    for(int j=dAPattern.size(); j<descendantsCount; j++){
                        dAPattern.add(0);
                    }
                    int tmp = dAPattern.get(descendantsCount-1);
                    tmp++;
                    dAPattern.set(descendantsCount - 1, tmp);
                }
            }
        }

        SATreeComparingAnalysis.TreeSummary treeSummary = analysis.new TreeSummary(tree.getRoot().getHeight(), tree.getLeafNodeCount() - ((ZeroBranchSATree)tree).getDirectAncestorNodeCount(), dAPattern);

        System.out.println(treeSummary.dAPattern.toString());
        ArrayList<Integer> a = new ArrayList<Integer> (Arrays.asList(new Integer[]{1, 1}));
        System.out.println(treeSummary.dAPattern.equals(a));
    }
}
