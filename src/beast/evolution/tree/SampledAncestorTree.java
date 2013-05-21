/*
* @author Alexandra Gavryushkina
*/
package beast.evolution.tree;


import beast.core.Description;
import beast.core.StateNodeInitialiser;
import beast.util.SampledAncestorTreeParser;
import beast.util.TreeParser;

import java.util.List;

/*
* Note that leaf nodes are always numbered 0,...,nodeCount-1
* Internal nodes are numbered higher, but the root has no guaranteed 
* number.
*/

@Description("Tree (the T in BEAST) representing gene beast.tree, species beast.tree, language history, or " +
        "other time-beast.tree relationships among sequence data.")
public class SampledAncestorTree extends Tree {

    public SampledAncestorTree() {}

    public SampledAncestorTree(Node rootNode) {
        setRootOnly(rootNode);
        nodeCount = this.root.getNodeCount();
        initArrays();
    }

    /**
     * Construct a tree from newick string -- will not automatically adjust tips to zero.
     */
    public SampledAncestorTree(String sNewick) throws Exception {
        this(new SampledAncestorTreeParser(sNewick).getRoot());
    }

    @Override
    public void initAndValidate() throws Exception {
        if (m_initial.get() != null && !(this instanceof StateNodeInitialiser)) {
            Tree other = m_initial.get();
            root = other.root.copy();
            nodeCount = other.nodeCount;
            internalNodeCount = other.internalNodeCount;
            leafNodeCount = other.leafNodeCount;
        }

        if (nodeCount < 0) {
            if (m_taxonset.get() != null) {
                // make a caterpillar
                List<String> sTaxa = m_taxonset.get().asStringList();
                Node left = new SampledAncestorNode();
                left.m_iLabel = 0;
                left.m_fHeight = 0;
                left.setID(sTaxa.get(0));
                for (int i = 1; i < sTaxa.size(); i++) {
                    Node right = new SampledAncestorNode();
                    right.m_iLabel = i;
                    right.m_fHeight = 0;
                    right.setID(sTaxa.get(i));
                    Node parent = new SampledAncestorNode();
                    parent.m_iLabel = sTaxa.size() + i - 1;
                    parent.m_fHeight = i;
                    left.m_Parent = parent;
                    parent.setLeft(left);
                    right.m_Parent = parent;
                    parent.setRight(right);
                    left = parent;
                }
                root = left;
                leafNodeCount = sTaxa.size();
                nodeCount = leafNodeCount * 2 - 1;
                internalNodeCount = leafNodeCount - 1;

            } else {
                // make dummy tree with a single root node
                root = new SampledAncestorNode();
                root.m_iLabel = 0;
                root.m_fHeight = 0;
                root.m_tree = this;
                nodeCount = 1;
                internalNodeCount = 0;
                leafNodeCount = 1;
            }
        }
        if (m_trait.get() != null) {
            adjustTreeToNodeHeights(root, m_trait.get());
        }

        if (nodeCount >= 0) {
            initArrays();
        }
    }

    @Override
    protected void adjustTreeToNodeHeights(Node node, TraitSet trait) {
        if (node.isLeaf()) {
            node.setMetaData(trait.getTraitName(), trait.getValue(node.getNr()));
        } else {
            adjustTreeToNodeHeights(node.getLeft(), trait);
            if (node.getRight() != null)
                adjustTreeToNodeHeights(node.getRight(), trait);
            if (node.m_fHeight < node.getLeft().getHeight() + EPSILON) {
                node.m_fHeight = node.getLeft().getHeight() + EPSILON;
            }
            if (node.getRight()!= null && node.m_fHeight < node.getRight().getHeight() + EPSILON) {
                node.m_fHeight = node.getRight().getHeight() + EPSILON;
            }
        }
    }

    @Override
    public Tree copy() {
        Tree tree = new SampledAncestorTree();
        tree.setID(m_sID);
        tree.index = index;
        tree.root = root.copy();
        tree.nodeCount = nodeCount;
        tree.internalNodeCount = internalNodeCount;
        tree.leafNodeCount = leafNodeCount;
        return tree;
    }

    /**
     * reconstruct tree from XML fragment in the form of a DOM node *
     */
    @Override
    public void fromXML(org.w3c.dom.Node node) {
        String sNewick = node.getTextContent();
        SampledAncestorTreeParser parser = new SampledAncestorTreeParser();
        try {
            parser.m_nThreshold.setValue(1e-10, parser);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        try {
            parser.m_nOffset.setValue(0, parser);
            setRoot(parser.parseNewick(sNewick));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        initArrays();
    }

    /**
     * StateNode implementation *
     */
    @Override
    protected void store() {
        if (m_storedNodes.length != nodeCount) {
            Node[] tmp = new Node[nodeCount];
            for (int i = 0; i < m_storedNodes.length - 1; i++) {
                tmp[i] = m_storedNodes[i];
            }
            if (nodeCount > m_storedNodes.length) {
                tmp[m_storedNodes.length - 1] = m_storedNodes[m_storedNodes.length - 1];
                tmp[nodeCount - 1] = new SampledAncestorNode();
                tmp[nodeCount - 1].setNr(nodeCount - 1);
            }
            m_storedNodes = tmp;
        }
        storeNodes(0, nodeCount);
        storedRoot = m_storedNodes[root.getNr()];
    }

    private void storeNodes(int iStart, int iEnd) {
        for (int i = iStart; i < iEnd; i++) {
            Node sink = m_storedNodes[i];
            Node src = m_nodes[i];
            sink.m_fHeight = src.m_fHeight;
            if (src.m_Parent != null) {
                sink.m_Parent = m_storedNodes[src.m_Parent.getNr()];
            } else {
                ((SampledAncestorNode)sink).reattachFromParent();
            }
            if (src.getLeft() != null) {
                sink.setLeft(m_storedNodes[src.getLeft().getNr()]);
                if (src.getRight() != null) {
                    sink.setRight(m_storedNodes[src.getRight().getNr()]);
                } else {
                    sink.removeChild(sink.getRight());
                }
            } else {
                ((SampledAncestorNode)sink).removeAllChildren();
            }
        }
    }

    @Override
    public void restore() {
        nodeCount = m_storedNodes.length;
        Node[] tmp = m_storedNodes;
        m_storedNodes = m_nodes;
        m_nodes = tmp;
        root = m_nodes[storedRoot.getNr()];
        leafNodeCount = root.getLeafNodeCount();
        m_bHasStartedEditing = false;
    }

    public void removeNode(int i) {
        Node[] tmp = new Node[nodeCount - 1];
        for (int j = 0; j < i; j++) {
            tmp[j] = m_nodes[j];
        }
        for (int j = i; j < nodeCount - 1; j++) {
            tmp[j] = m_nodes[j + 1];
            tmp[j].setNr(j);
        }
        m_nodes = tmp;
        nodeCount--;
        leafNodeCount--;
    }

    public void addNode(Node newNode) {
        Node[] tmp = new Node[nodeCount + 1];
        for (int j = 0; j < nodeCount; j++) {
            tmp[j] = m_nodes[j];
        }
        tmp[nodeCount] = newNode;
        newNode.setNr(nodeCount);
        m_nodes = tmp;
        nodeCount++;
        leafNodeCount++;
    }

    public void setRootOnly(Node root) {
        this.root = root;
    }

} // class SampledAncestorTree
