package beast.app.tools;

import beast.evolution.tree.Node;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Alexandra Gavryushkina
 */

public class SampledAncestorTreeTrace {

    private int labelCount;

    private int treeCount;

    private String[] trees;

    private String[] shortTrees;

    private String[] rankedTrees;

    private HashSet<Integer> labelSet;

    public  String[] getTrees() {
        return trees;
    }

    public  String[] getShortTrees() {
        return shortTrees;
    }

    public int getTreeCount() {
        return treeCount;
    }

    public SampledAncestorTreeTrace(String[] newTrees, int newLabelCount) throws Exception {

        labelCount = newLabelCount;
        trees = newTrees;
        treeCount = trees.length;

        Integer[] tmp = new Integer[labelCount];
        for (int i=0; i< labelCount; i++)
            tmp[i] = i+1;

        labelSet = new HashSet<Integer>(Arrays.asList(tmp));

        shortTrees = new String[treeCount];

        for (int i =0; i < treeCount; i++) {
            shortTrees[i] = convertToShortTree(trees[i]);
        }
    }

    public SampledAncestorTreeTrace(int newLabelCount) {

        labelCount = newLabelCount;

        Integer[] tmp = new Integer[labelCount];
        for (int i=0; i< labelCount; i++)
            tmp[i] = i+1;

        labelSet = new HashSet<Integer>(Arrays.asList(tmp));
    }

    public static String convertToShortTree(String strIn) {

        StringBuilder buf = new StringBuilder();
        boolean skipping = false;

        for (int i=0; i < strIn.length(); i++) {
            if (!skipping) {
                if (strIn.charAt(i) != ':') {
                    buf.append(strIn.charAt(i));
                } else {
                    if (strIn.charAt(i+1) != '(') {
                        skipping = true;
                        i++;
                    } else {
                        buf.append(strIn.charAt(i+1));
                        i++;
                    }
                }
            } else {
                if (strIn.charAt(i) == ',' || strIn.charAt(i) == ')') {
                    buf.append(strIn.charAt(i));
                    skipping = false;
                }
            }
        }

        return buf.toString();
    }

//    private SampledAncestorNode makeNewNode(String strTree, int[] currentPosition, int[] nodeLabel) {
//
//        SampledAncestorNode node = new SampledAncestorNode();
//        String label = new String();
//
//        for (int i = currentPosition[0]; i < strTree.length(); i++) {
//            if (strTree.charAt(i) != '(' && strTree.charAt(i) != ':') {
//                label += strTree.charAt(i);
//            } else {
//                currentPosition[0] = i;
//                break;
//            }
//        }
//
//        char ch;
//
//        if (label.equals("")) {
//            node.setNr(nodeLabel[0]);
//            nodeLabel[0]++;
//        }  else {
//            if (labelSet.contains(Integer.parseInt(label)))
//                node.setNr(Integer.parseInt(label)-1);
//            else {
//                System.out.println("Incorrect string tree representation (label set is incorrect)");
//                return null;
//            }
//        }
//
//        ch = strTree.charAt(currentPosition[0]);
//        currentPosition[0]++;
//
//        // checking if the node has label
//
//
//        //checking if the node has children
//        if (ch == '(') {
//            SampledAncestorNode left = makeNewNode(strTree, currentPosition, nodeLabel);
//            node.setLeft(left);
//            left.setParent(node);
//            ch = strTree.charAt(currentPosition[0]);
//            currentPosition[0]++;
//            if ( ch == ',') {
//                SampledAncestorNode right = makeNewNode(strTree, currentPosition, nodeLabel);
//                node.setRight(right);
//                right.setParentOnly(node);
//                ch = strTree.charAt(currentPosition[0]);
//                currentPosition[0]++;
//            }
//            if (ch == ')') {
//                ch = strTree.charAt(currentPosition[0]);
//                currentPosition[0]++;
//            }  else {
//                System.out.println("Incorrect string tree representation");
//                return null;
//            }
//
//        }
//
//        //  reading length
//        if (ch == ':') {
//            node.setHeight(-readLength(strTree, currentPosition));
//        }
//
//        shiftHeights(node, node.getHeight());
//
//        return node;
//    }

    private void shiftHeights(Node node, double shift) {
        if (node.getLeft() != null) {
            double tmp = node.getLeft().getHeight();
            node.getLeft().setHeight(tmp + shift);
            shiftHeights(node.getLeft(), shift);
        }
        if (node.getRight() != null) {
            double tmp = node.getRight().getHeight();
            node.getRight().setHeight(tmp + shift);
            shiftHeights(node.getRight(), shift);
        }
    }

    private double readLength(String strTree, int[] currentPosition){

        String tmp = new String();
        int start = currentPosition[0];

        currentPosition[0] = strTree.length();

        for (int i = start; i< strTree.length(); i++) {
            if (strTree.charAt(i) != ')' && strTree.charAt(i) != ',')
                tmp += strTree.charAt(i);
            else {
                currentPosition[0] = i;
                break;
            }
        }

        return Double.parseDouble(tmp);

    }

    /*public static void main (String[] arg) throws Exception {
        String exTree = "9(10(8(7(6(5(4(3(2((1:1.0,11:1.0):1.0):1.0):1.0):1.0):1.0):1.0):1.0):1.0):1.0):0.0";
        //String exTree = "1(2(3:1.0):1.0):0.0";
        SampledAncestorTreeTrace analysis = new SampledAncestorTreeTrace(11);

        TreeParser tree = new TreeParser(exTree, false, true, false, 1);

        //ArrayList<String> tmp = TraceFRSTree.extractAllDClades((TraceNode)tree.getRoot());

        System.out.println("Tree was = " + exTree);
        System.out.println("Tree now = " + tree.getRoot().toShortNewick(false));
        //for (int i=0; i< tmp.size(); i++)
        //  System.out.println("Clade " + i + " is " + tmp.get(i));
    }        */


}

