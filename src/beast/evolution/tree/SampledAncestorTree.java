/*
* @author Alexandra Gavryushkina
*/
package beast.evolution.tree;


import beast.core.Description;
import beast.util.SampledAncestorTreeParser;

/*
* Note that leaf nodes are always numbered 0,...,nodeCount-1
* Internal nodes are numbered higher, but the root has no guaranteed 
* number.
*/

@Description("Tree (the T in BEAST) representing gene beast.tree, species beast.tree, language history, or " +
        "other time-beast.tree relationships among sequence data.")
public class SampledAncestorTree extends Tree {

    public SampledAncestorTree() {
    }

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

    public Node createNode() {
        return new SampledAncestorNode();
    }


} // class SampledAncestorTree
