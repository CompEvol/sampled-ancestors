package beast.app.tools;

import beast.evolution.tree.Tree;
import beast.evolution.tree.TreeTraceAnalysis;
import beast.util.FrequencySet;
import beast.util.SANexusParser;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexandra Gavryushkina
 * @author Walter Xie
 */
//TODO combine SampledAncestorTreeAnalysis
public class SATreeTraceAnalysis extends TreeTraceAnalysis {

//    public String[] labeledTrees;

    public SATreeTraceAnalysis(List<Tree> posteriorTreeList) {
        super(posteriorTreeList);
    }

    public SATreeTraceAnalysis(List<Tree> posteriorTreeList, double burninFraction) {
        super(posteriorTreeList, burninFraction);
    }

    public SATreeTraceAnalysis(List<Tree> posteriorTreeList, double burninFraction, double credSetProbability) {
        super(posteriorTreeList, burninFraction, credSetProbability);
    }

    @Override
    public void analyze(double credSetProbability) {
        super.analyze(credSetProbability);
    }

    @Override
    public void analyze() {
        super.analyze();
    }

    @Override
    public void report(PrintStream oStream) {
        super.report(oStream);
    }

    public List<String> getNewickTrees (boolean noLength) {
        List<String> newickTrees = new ArrayList<>();
        for (Tree tree : treeInCredSetList) {
            String nTree = tree.getRoot().toSortedNewick(new int[] {1}, false);
            if (noLength) {
                nTree = convertToShortTree(nTree);
            }
            newickTrees.add(nTree);
        }
        return newickTrees;
    }

    public List<String> getNewickTrees () {
        return getNewickTrees(false);
    }


    @Override
    protected void analyze(FrequencySet<String> topologiesFrequencySet) {
        super.analyze(topologiesFrequencySet);
    }

    protected static String convertToShortTree(String strIn) {

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

    }
}

