package beast.util;

import beast.core.Input;
import beast.core.StateNode;
import beast.core.StateNodeInitialiser;
import beast.evolution.alignment.Alignment;
import beast.evolution.tree.Node;
import beast.evolution.tree.SampledAncestorNode;
import beast.evolution.tree.SampledAncestorTree;
import beast.evolution.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SampledAncestorTreeParser extends SampledAncestorTree implements StateNodeInitialiser {

    /**
     * default beast.tree branch length, used when that info is not in the Newick beast.tree
     */
    final static double DEFAULT_LENGTH = 0.001f;

    /**
     * labels of sampled nodes *
     */
    List<String> m_sLabels = null;
    /**
     * for memory saving, set to true *
     */
    boolean m_bSurpressMetadata = false;
    /**
     * if there is no translate block. This solves issues where the taxa labels are numbers e.g. in generated beast.tree data *
     */
    public Input<Boolean> m_bIsLabelledNewick = new Input<Boolean>("IsLabelledNewick", "Is the newick tree labelled? Default=false.", false);


    public Input<Alignment> m_oData = new Input<Alignment>("taxa", "Specifies the list of taxa represented by sampled nodes in the beast.tree");
    public Input<String> m_oNewick = new Input<String>("newick", "initial beast.tree represented in newick format");// not required, Beauti may need this for example
    public Input<Integer> m_nOffset = new Input<Integer>("offset", "offset if numbers are used for taxa (offset=the lowest taxa number) default=1", 1);
    public Input<Double> m_nThreshold = new Input<Double>("threshold", "threshold under which node heights (derived from lengths) are set to zero. Default=0.", 0.0);
    public Input<Boolean> adjustTipHeightsWhenMissingDateTraitsInput = new Input<Boolean>("adjustTipHeights", "flag to indicate if tipHeights shall be adjusted when date traits missing. Default=true.", false);
    public Input<Double> scale = new Input<Double>("scale", "scale used to multiply node heights during parsing." +
            "Useful for importing starting from external programs, for instance, RaxML rooted using Path-o-gen.", 1.0);

    /**
     * used to make sure all taxa only occur once in the tree *
     */
    List<Boolean> m_bTaxonIndexInUse = new ArrayList<Boolean>();

    boolean createUnrecognizedTaxa = false;

    // if true and no date traits available then tips heights will be adjusted to zero.
    private boolean adjustTipHeightsWhenMissingDateTraits; // = false;

    /**
     * op
     * assure the class behaves properly, even when inputs are not specified *
     */
    @Override
    public void initAndValidate() throws Exception {

        adjustTipHeightsWhenMissingDateTraits = adjustTipHeightsWhenMissingDateTraitsInput.get();

        if (m_oData.get() != null) {
            m_sLabels = m_oData.get().getTaxaNames();
        } else if (m_taxonset.get() != null) {
            m_sLabels = m_taxonset.get().asStringList();
        } else {
            if (m_bIsLabelledNewick.get()) {
                m_sLabels = new ArrayList<String>();
                createUnrecognizedTaxa = true;
            } else {
                if (m_initial.get() != null) {
                    // try to pick up taxa from initial tree
                    Tree tree = m_initial.get();
                    if (tree.m_taxonset.get() != null) {
                        m_sLabels = tree.m_taxonset.get().asStringList();
                    } else {
                        // m_sLabels = null;
                    }
                } else {
                    // m_sLabels = null;
                }
            }
//            m_bIsLabelledNewick = false;
        }
        String sNewick = m_oNewick.get();
        if (sNewick == null || sNewick.equals("")) {
            // can happen while initalising Beauti
            Node dummy = new SampledAncestorNode();
            setRoot(dummy);
        } else {
            setRoot(parseNewick(m_oNewick.get()));
        }

        super.initAndValidate();
        if (m_initial.get() != null && m_initial.get().m_trait.get() != null) {
            adjustTreeToNodeHeights(root, m_initial.get().m_trait.get());
        } else if (m_trait.get() == null && adjustTipHeightsWhenMissingDateTraits) {
            // all nodes should be at zero height if no date-trait is available
            for (int i = 0; i < getLeafNodeCount(); i++) {
                getNode(i).setHeight(0);
            }
        }
        initStateNodes();
    } // init

    public SampledAncestorTreeParser() {}

    public SampledAncestorTreeParser(List<String> taxaNames, String newick, int offset) throws Exception {
        m_sLabels = taxaNames;
        m_oNewick.setValue(newick, this);
        m_nOffset.setValue(offset, this);
        initAndValidate();
    }

    public SampledAncestorTreeParser(String newick) throws Exception {
        this(newick, true);
    }

    public SampledAncestorTreeParser(String newick, boolean adjustTipHeights) throws Exception {
        m_oNewick.setValue(newick, this);
        m_bIsLabelledNewick.setValue(true, this);
        adjustTipHeightsWhenMissingDateTraitsInput.setValue(adjustTipHeights, this);
        initAndValidate();
    }

    void processMetadata(Node node) throws Exception {
        if (node.m_sMetaData != null) {
            String[] sMetaData = node.m_sMetaData.split(",");
            for (int i = 0; i < sMetaData.length; i++) {
                try {
                    String[] sStrs = sMetaData[i].split("=");
                    if (sStrs.length != 2) {
                        throw new Exception("misformed meta data '" + node.m_sMetaData + "'. Expected name='value' pairs");
                    }
                    String sPattern = sStrs[0];
                    sStrs[1] = sStrs[1].replaceAll("[\"']", "");
                    try {
                        Double fValue = Double.parseDouble(sStrs[1]);
                        node.setMetaData(sPattern, fValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Meta data \"" + sPattern + "=" + sStrs[1] + "\" could not be interpreted as number. Storing as string.");
                        node.setMetaData(sPattern, sStrs[1]);
                    }
                } catch (Exception e) {
                    System.out.println("Warning 333: Attempt to parse metadata failed: " + node.m_sMetaData);
                    System.out.println(e.getMessage());

                }
            }
        }
        if (node.getLeft() != null) {
            processMetadata(node.getLeft());
            if (node.getRight() != null) {
                processMetadata(node.getRight());
            }
        }
    }

    void convertLengthToHeight(Node node) {
        double fTotalHeight = convertLengthToHeight(node, 0);
        offset(node, -fTotalHeight);
    }

    double convertLengthToHeight(Node node, double fHeight) {
        double fLength = node.getHeight();
        node.setHeight((fHeight - fLength) * scale.get());
        if (node.isLeaf()) {
            return node.getHeight();
        } else {
            double fLeft = convertLengthToHeight(node.getLeft(), fHeight - fLength);
            if (node.getRight() == null) {
                return fLeft;
            }
            double fRight = convertLengthToHeight(node.getRight(), fHeight - fLength);
            return Math.min(fLeft, fRight);
        }
    }

    void offset(Node node, double fDelta) {
        node.setHeight(node.getHeight() + fDelta);
        if (node.isLeaf()) {
            if (node.getHeight() < m_nThreshold.get()) {
                node.setHeight(0);
            }
        }
        if (!node.isLeaf()) {
            offset(node.getLeft(), fDelta);
            if (node.getRight() != null) {
                offset(node.getRight(), fDelta);
            }
        }
    }

    /**
     * Try to map sStr into an index. First, assume it is a number.
     * If that does not work, look in list of labels to see whether it is there.
     */

    void checkTaxaIsAvailable(String sStr, int nIndex) throws Exception {
        while (nIndex + 1 > m_bTaxonIndexInUse.size()) {
            m_bTaxonIndexInUse.add(false);
        }
        if (m_bTaxonIndexInUse.get(nIndex)) {
            throw new Exception("Duplicate taxon found: " + sStr);
        }
        m_bTaxonIndexInUse.set(nIndex, true);
    }


    char[] m_chars;
    int m_iTokenStart;
    int m_iTokenEnd;
    final static int COMMA = 1;
    final static int BRACE_OPEN = 3;
    final static int BRACE_CLOSE = 4;
    final static int COLON = 5;
    final static int SEMI_COLON = 8;
    final static int META_DATA = 6;
    final static int TEXT = 7;
    final static int UNKNOWN = 0;

    int nextToken() {
        m_iTokenStart = m_iTokenEnd;
        while (m_iTokenEnd < m_chars.length) {
            // skip spaces
            while (m_iTokenEnd < m_chars.length && (m_chars[m_iTokenEnd] == ' ' || m_chars[m_iTokenEnd] == '\t')) {
                m_iTokenStart++;
                m_iTokenEnd++;
            }
            if (m_chars[m_iTokenEnd] == '(') {
                m_iTokenEnd++;
                return BRACE_OPEN;
            }
            if (m_chars[m_iTokenEnd] == ':') {
                m_iTokenEnd++;
                return COLON;
            }
            if (m_chars[m_iTokenEnd] == ';') {
                m_iTokenEnd++;
                return SEMI_COLON;
            }
            if (m_chars[m_iTokenEnd] == ')') {
                m_iTokenEnd++;
                return BRACE_CLOSE;
            }
            if (m_chars[m_iTokenEnd] == ',') {
                m_iTokenEnd++;
                return COMMA;
            }
            if (m_chars[m_iTokenEnd] == '[') {
                m_iTokenEnd++;
                while (m_iTokenEnd < m_chars.length && m_chars[m_iTokenEnd - 1] != ']') {
                    m_iTokenEnd++;
                }
                return META_DATA;
            }
            while (m_iTokenEnd < m_chars.length && (m_chars[m_iTokenEnd] != ' ' && m_chars[m_iTokenEnd] != '\t'
                    && m_chars[m_iTokenEnd] != '(' && m_chars[m_iTokenEnd] != ')' && m_chars[m_iTokenEnd] != '['
                    && m_chars[m_iTokenEnd] != ':' && m_chars[m_iTokenEnd] != ',' && m_chars[m_iTokenEnd] != ';')) {
                m_iTokenEnd++;
            }
            return TEXT;
        }
        return UNKNOWN;
    }

    public Node parseNewick(String sStr) throws Exception {
        // get rid of initial and terminal spaces
        sStr = sStr.replaceAll("^\\s+", "");
        sStr = sStr.replaceAll("\\s+$", "");

        try {
            m_chars = sStr.toCharArray();
            if (sStr == null || sStr.length() == 0) {
                return null;
            }
            m_iTokenStart = 0;
            m_iTokenEnd = 0;
            Vector<Node> stack = new Vector<Node>();
            Vector<Boolean> isFirstChild = new Vector<Boolean>();
            stack.add(new SampledAncestorNode());
            stack.lastElement().setNr(-1);
            isFirstChild.add(true);
            stack.lastElement().setHeight(DEFAULT_LENGTH);
            boolean bIsLabel = true;
            int[] sampleSize = new int[1];
            sampleSize[0] = 0;
            while (m_iTokenEnd < m_chars.length) {
                switch (nextToken()) {
                    case BRACE_OPEN: {
                        Node node2 = new SampledAncestorNode();
                        node2.setNr(-1);
                        node2.setHeight(DEFAULT_LENGTH);
                        stack.add(node2);
                        isFirstChild.add(true);
                        bIsLabel = true;
                    }
                    break;
                    case BRACE_CLOSE: {
                        if (isFirstChild.lastElement()) {

                            // process single child nodes
                            Node left = stack.lastElement();
                            stack.remove(stack.size() - 1);
                            isFirstChild.remove(isFirstChild.size() - 1);
                            Node parent = stack.lastElement();
                            parent.setLeft(left);
                            //parent.setRight(null);
                            left.setParent(parent);
                            bIsLabel = true;
                            break;
                        }
                        // last two nodes on stack merged into single parent node
                        Node right = stack.lastElement();
                        stack.remove(stack.size() - 1);
                        isFirstChild.remove(isFirstChild.size() - 1);
                        Node left = stack.lastElement();
                        stack.remove(stack.size() - 1);
                        isFirstChild.remove(isFirstChild.size() - 1);
                        Node parent = stack.lastElement();
                        parent.setLeft(left);
                        left.setParent(parent);
                        parent.setRight(right);
                        right.setParent(parent);
                        bIsLabel = true;
                    }
                    break;
                    case COMMA: {
                        Node node2 = new SampledAncestorNode();
                        node2.setNr(-1);
                        node2.setHeight(DEFAULT_LENGTH);
                        stack.add(node2);
                        isFirstChild.add(false);
                        bIsLabel = true;
                    }
                    break;
                    case COLON:
                        bIsLabel = false;
                        break;
                    case TEXT:
                        if (bIsLabel) {
                            String sLabel = sStr.substring(m_iTokenStart, m_iTokenEnd);
                            int index =getLabelIndex(sLabel);
                            stack.lastElement().setNr(index);
                            if (m_sLabels != null)
                                stack.lastElement().setID(m_sLabels.get(index));
                            else stack.lastElement().setID(Integer.toString(index));
                            sampleSize[0]++;
                        } else {
                            String sLength = sStr.substring(m_iTokenStart, m_iTokenEnd);
                            stack.lastElement().setHeight(Double.parseDouble(sLength));
                        }
                        break;
                    case META_DATA:
                        if (stack.lastElement().m_sMetaData == null) {
                            stack.lastElement().m_sMetaData = sStr.substring(m_iTokenStart + 1, m_iTokenEnd - 1);
                        } else {
                            stack.lastElement().m_sMetaData += " " + sStr.substring(m_iTokenStart + 1, m_iTokenEnd - 1);
                        }
                        break;
                    case SEMI_COLON:
                        //System.err.println(stack.lastElement().toString());
                        Node tree = stack.lastElement();

                        // at this stage, all heights are actually lengths
                        convertLengthToHeight(tree);

                        //label unlabeled nodes
                        sampleSize[0] --;
                        labelNonLabeledNodes(tree, sampleSize);

                        if (!m_bSurpressMetadata) {
                            processMetadata(tree);
                        }
                        System.out.println(tree.toShortNewick(false));
                        return stack.lastElement();
                    default:
                        throw new Exception("parseNewick: unknown token");
                }
            }
            Node tree = stack.lastElement();

            // at this stage, all heights are actually lengths
            convertLengthToHeight(tree);

            //label unlabeled nodes
            sampleSize[0] --;
            labelNonLabeledNodes(tree, sampleSize);

            //System.out.println("Tree after parse is " + tree.toShortNewick(false));

            if (!m_bSurpressMetadata) {
                processMetadata(tree);

            }
            return tree;
        } catch (Exception e) {
            System.err.println(e.getClass().toString() + "/" + e.getMessage() + ": " + sStr.substring(Math.max(0, m_iTokenStart - 100), m_iTokenStart) + " >>>" + sStr.substring(m_iTokenStart, m_iTokenEnd) + " <<< ...");
            throw new Exception(e.getMessage() + ": " + sStr.substring(Math.max(0, m_iTokenStart - 100), m_iTokenStart) + " >>>" + sStr.substring(m_iTokenStart, m_iTokenEnd) + " <<< ...");
        }
//        return node;
    }

    private int getLabelIndex(String sStr) throws Exception {
        if (!m_bIsLabelledNewick.get() && m_sLabels == null) {
            try {
                int nIndex = Integer.parseInt(sStr) - m_nOffset.get();
                checkTaxaIsAvailable(sStr, nIndex);
                return nIndex;
            } catch (Exception e) {
                System.out.println(e.getClass().getName() + " " + e.getMessage() + ". Perhaps taxa or taxonset is not specified?");
            }
        }
        // look it up in list of taxa
        for (int nIndex = 0; nIndex < m_sLabels.size(); nIndex++) {
            if (sStr.equals(m_sLabels.get(nIndex))) {
                checkTaxaIsAvailable(sStr, nIndex);
                return nIndex;
            }
        }

        // if createUnrecognizedTaxa==true, then do it now, otherwise labels will not be populated and
        // out of bounds error will occur in m_sLabels later.
        if (createUnrecognizedTaxa) {
            m_sLabels.add(sStr);
            int nIndex = m_sLabels.size() - 1;
            checkTaxaIsAvailable(sStr, nIndex);
            System.out.println("index is " + nIndex);
            return nIndex;
        }

        // finally, check if its an integer number indicating the taxon id
        try {
            int nIndex = Integer.parseInt(sStr) - m_nOffset.get();
            checkTaxaIsAvailable(sStr, nIndex);
            return nIndex;
        } catch (NumberFormatException e) {
            // apparently not a number
        }
        throw new Exception("Label '" + sStr + "' in Newick beast.tree could not be identified. Perhaps taxa or taxonset is not specified?");
    }

    public void labelNonLabeledNodes(Node node, int[] lastLabel) {

        if (node.getLeft() != null) {
            labelNonLabeledNodes(node.getLeft(), lastLabel);
        }
        if (node.getRight() != null) {
            labelNonLabeledNodes(node.getRight(), lastLabel);
        }

        if (node.getNr() == -1) {
            node.setNr(lastLabel[0]+1);
            lastLabel[0]++;
        }
    }

    public void initStateNodes() {
        if (m_initial.get() != null) {
            m_initial.get().assignFrom(this);
        }
    }

    public List<StateNode> getInitialisedStateNodes() {
        List<StateNode> stateNodes = new ArrayList<StateNode>();
        if (m_initial.get() != null) {
            stateNodes.add(m_initial.get());
        }
        return stateNodes;
    }
}