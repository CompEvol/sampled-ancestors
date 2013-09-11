package beast.util;

import beast.core.Description;
import beast.core.Input;
import beast.core.StateNode;
import beast.core.StateNodeInitialiser;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author Alexandr Gavryushkina
 */

@Description("Create beast.tree by parsing from a specification of a beast.tree (which is a fake SA tree) in Newick" +
        " format (includes parsing of any meta data in the Newick string).")
public class FakeSATreeParser extends Tree {
    /**
     * default beast.tree branch length, used when that info is not in the Newick beast.tree
     */
    final static double DEFAULT_LENGTH = 0.001f;

    /**
     * labels of leafs *
     */
    List<String> labels = new ArrayList<String>();
    /**
     * for memory saving, set to true *
     */
    boolean surpressMetadata = false;

    /**
     * if there is no translate block. This solves issues where the taxa labels are numbers e.g. in generated beast.tree data *
     */
//    public Input<Boolean> isLabelledNewickInput = new Input<Boolean>("IsLabelledNewick", "Is the newick tree labelled? Default=false.", false);

//    public Input<Alignment> dataInput = new Input<Alignment>("taxa", "Specifies the list of taxa represented by leaves in the beast.tree");
    public Input<String> newickInput = new Input<String>("newick", "initial beast.tree represented in newick format");// not required, Beauti may need this for example
//    public Input<String> nodeTypeInput = new Input<String>("nodetype", "type of the nodes in the beast.tree", Node.class.getName());
    public Input<Integer> offsetInput = new Input<Integer>("offset", "offset if numbers are used for taxa (offset=the lowest taxa number) default=1", 1);
    public Input<Double> thresholdInput = new Input<Double>("threshold", "threshold under which node heights (derived from lengths) are set to zero. Default=0.", 0.0);
//    public Input<Boolean> allowSingleChildInput = new Input<Boolean>("singlechild", "flag to indicate that single child nodes are allowed. Default=true.", true);
//    public Input<Boolean> adjustTipHeightsWhenMissingDateTraitsInput = new Input<Boolean>("adjustTipHeights", "flag to indicate if tipHeights shall be adjusted when date traits missing. Default=true.", true);
//    public Input<Double> scaleInput = new Input<Double>("scale", "scale used to multiply internal node heights during parsing." +
//            "Useful for importing starting from external programs, for instance, RaxML tree rooted using Path-o-gen.", 1.0);



    // if true and no date traits available then tips heights will be adjusted to zero.

    /**
     * op
     * assure the class behaves properly, even when inputs are not specified *
     */
    @Override
    public void initAndValidate()  throws Exception {
        labels = new ArrayList<String>();

        String sNewick = newickInput.get();
        if (sNewick == null || sNewick.equals("")) {
            // can happen while initalising Beauti
            Node dummy = new Node();
            setRoot(dummy);
        } else {
            setRoot(parseNewick(newickInput.get()));
        }

        super.initAndValidate();
    } // init

    /**
     * used to make sure all taxa only occur once in the tree *
     */
    List<Boolean> m_bTaxonIndexInUse = new ArrayList<Boolean>();

    public FakeSATreeParser() {
    }

    /**
     * Create a tree from the given newick format
     *
     * @param newick    the newick of the tree
     * @param offset    the offset to map node numbers in newick format to indices in taxaNames.
     *                  so, name(node with nodeNumber) = taxaNames[nodeNumber-offset]
     * @throws Exception
     */
    public FakeSATreeParser(String newick, int offset) throws Exception {
        newickInput.setValue(newick, this);
        offsetInput.setValue(offset, this);
        initAndValidate();
    }

    void convertLengthToHeight(Node node) {
        double fTotalHeight = convertLengthToHeight(node, 0);
        offset(node, -fTotalHeight);
    }

    double convertLengthToHeight(Node node, double fHeight) {
        double fLength = node.getHeight();
        node.setHeight((fHeight - fLength));
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
            if (node.getHeight() < thresholdInput.get()) {
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
    private int getLabelIndex(String sStr) throws Exception {

        try {
            int nIndex = Integer.parseInt(sStr) - offsetInput.get();
            return nIndex;
        } catch (NumberFormatException e) {
            // apparently not a number
        }
        throw new Exception("Label '" + sStr + "' in Newick beast.tree could not be identified. Perhaps taxa or taxonset is not specified?");
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
            stack.add(new Node());
            stack.lastElement().setNr(-1);
            isFirstChild.add(true);
            stack.lastElement().setHeight(DEFAULT_LENGTH);
            boolean bIsLabel = true;
            int[] sampleSize = new int[1];
            sampleSize[0] = 0;
            while (m_iTokenEnd < m_chars.length) {
                switch (nextToken()) {
                    case BRACE_OPEN: {
                        Node node2 = new Node();
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
                    }
                    break;
                    case COMMA: {
                        Node node2 = new Node();
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
                            stack.lastElement().setNr(getLabelIndex(sLabel));
                            sampleSize[0]++;
                        } else {
                            String sLength = sStr.substring(m_iTokenStart, m_iTokenEnd);
                            stack.lastElement().setHeight(Double.parseDouble(sLength));
                            bIsLabel = true;
                        }
                        break;
                    case META_DATA:
                        if (stack.lastElement().metaDataString == null) {
                            stack.lastElement().metaDataString = sStr.substring(m_iTokenStart + 1, m_iTokenEnd - 1);
                        } else {
                            stack.lastElement().metaDataString += " " + sStr.substring(m_iTokenStart + 1, m_iTokenEnd - 1);
                        }
                        break;
                    case SEMI_COLON:
                        return finishNewickParsing(stack.lastElement(), sampleSize);
                    default:
                        throw new Exception("parseNewick: unknown token");
                }
            }
            return finishNewickParsing(stack.lastElement(), sampleSize);
        } catch (Exception e) {
            System.err.println(e.getClass().toString() + "/" + e.getMessage() + ": " + sStr.substring(Math.max(0, m_iTokenStart - 100), m_iTokenStart) + " >>>" + sStr.substring(m_iTokenStart, m_iTokenEnd) + " <<< ...");
            throw new Exception(e.getMessage() + ": " + sStr.substring(Math.max(0, m_iTokenStart - 100), m_iTokenStart) + " >>>" + sStr.substring(m_iTokenStart, m_iTokenEnd) + " <<< ...");
        }
//        return node;
    }

    private Node finishNewickParsing(Node root, int[] sampleSize) throws Exception{

        // at this stage, all heights are actually lengths
        convertLengthToHeight(root);

        //insert a fake parent node for each sampled internal node
        convertToFakeSATree(root);
        if (root.getParent() != null) {
            Node tmp = root.getParent();
            root = tmp;
        }

        root.sort();

        sampleSize[0]--;
        labelNonLabeledNodes(root, sampleSize);

        return root;

    }

    private void labelNonLabeledNodes(Node node, int[] lastLabel) {

        for (Node child : node.getChildren()) {
            labelNonLabeledNodes(child, lastLabel);
        }
        if (node.getNr() == -1) {
            node.setNr(lastLabel[0] + 1);
            lastLabel[0] += 1;
        } else if (labels != null && node.getNr() < labels.size()) {
            node.setID(labels.get(node.getNr()));
        }
    }

    private void convertToFakeSATree(Node node) throws Exception {
        if (!node.isLeaf()) {
            convertToFakeSATree(node.getLeft());
            if (node.getRight() != null) {
                convertToFakeSATree(node.getRight());
            }
        }
        if (node.getChildCount() == 1) {
            Node parent = new Node();
            parent.setNr(-1);
            parent.setHeight(node.getHeight());
            Node child = node.getLeft();
            parent.setLeft(child);
            child.setParent(parent);
            node.removeChild(child);
            parent.setRight(node);
            if (!node.isRoot()) {
                Node grandparent = node.getParent();

                if (grandparent.getLeft().getNr() == node.getNr()) {
                    grandparent.setLeft(parent);
                }  else {
                    grandparent.setRight(parent);
                }
                parent.setParent(grandparent);
            }
            node.setParent(parent);
        }
    }
} // class FakeSATreeParser
