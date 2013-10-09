package beast.evolution.tree;

import java.util.List;

/**
 * @author Alexandra Gavryushkina
 */
public class ZeroBranchSANode extends Node {

    /**
     * @return (deep) copy of node
     */
    public Node copy() {
        final Node node = new ZeroBranchSANode();
        node.height = height;
        node.labelNr = labelNr;
        node.metaDataString = metaDataString;
        node.parent = null;
        node.setID(ID);

        for (final Node child : getChildren()) {
            node.addChild(child.copy());
        }
        return node;
    } // copy

    /**
     * Writes a short newick format.
     * Note: if bPrintInternalNodeNumbers=false this method suppresses internal
     * nodes that have a zero branch length child (replacing them with the child in the
     * newick string).
     *
     * @return beast.tree in Newick format, with length and meta data
     *         information. Unlike toNewick(), here Nodes are numbered, instead of
     *         using the node labels.
     *         All internal nodes are labelled if bPrintInternalNodeNumbers
     *         is set true. This is useful for example when storing a State to file
     *         so that it can be restored.
     */
    @Override
    public String toShortNewick(boolean bPrintInternalNodeNumbers) {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
            if (isFake() && !bPrintInternalNodeNumbers) {
                Node directAncestor;
                Node otherChild;
                if (((ZeroBranchSANode)getLeft()).isDirectAncestor()) {
                    directAncestor = getLeft();
                    otherChild = getRight();
                } else {
                    directAncestor = getRight();
                    otherChild = getLeft();
                }
                buf.append(otherChild.toShortNewick(bPrintInternalNodeNumbers));
                buf.append(")");
                buf.append(directAncestor.getNr());
            } else {
                buf.append(getLeft().toShortNewick(bPrintInternalNodeNumbers));
                if (getRight() != null) {
                    buf.append(',');
                    buf.append(getRight().toShortNewick(bPrintInternalNodeNumbers));
                }
                buf.append(")");
                if (bPrintInternalNodeNumbers) {
                    buf.append(getNr());
                }
            }
        } else {
            buf.append(getNr());
        }
        buf.append(getNewickMetaData());
        buf.append(":").append(getLength());
        return buf.toString();
    }

    /**
     * Note: this method suppresses internal nodes that have a zero branch length child
     * (replacing them with the child in the newick string).
     *
     * @param iMaxNodeInClade
     * @param printMetaData
     * @return
     */
    public String toSortedNewick(int[] iMaxNodeInClade, boolean printMetaData) {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
            if (isFake()) {
                Node directAncestor;
                Node otherChild;
                if (((ZeroBranchSANode)getLeft()).isDirectAncestor()) {
                    directAncestor = getLeft();
                    otherChild = getRight();
                } else {
                    directAncestor = getRight();
                    otherChild = getLeft();
                }
                buf.append(otherChild.toSortedNewick(iMaxNodeInClade, printMetaData));
                buf.append(")");
                buf.append(directAncestor.getNr() + 1);
            } else {
                String sChild1 = getLeft().toSortedNewick(iMaxNodeInClade, printMetaData);
                int iChild1 = iMaxNodeInClade[0];
                if (getRight() != null) {
                    String sChild2 = getRight().toSortedNewick(iMaxNodeInClade, printMetaData);
                    int iChild2 = iMaxNodeInClade[0];
                    if (iChild1 > iChild2) {
                        buf.append(sChild2);
                        buf.append(",");
                        buf.append(sChild1);
                    } else {
                        buf.append(sChild1);
                        buf.append(",");
                        buf.append(sChild2);
                        iMaxNodeInClade[0] = iChild1;
                    }
                } else {
                    buf.append(sChild1);
                }
                buf.append(")");
            }
        } else {
            iMaxNodeInClade[0] = labelNr;
            buf.append(labelNr + 1);
        }

        if (printMetaData) {
            buf.append(getNewickMetaData());
        }
        buf.append(":").append(getLength());
        return buf.toString();
    }


    /**                                        //TODO make this method work for ZeroBranchSATree
     * @return beast.tree in Newick format with taxon labels for labelled tip nodes
     * and labeled (having non-null ID) internal nodes.
     * If a tip node doesn't have an ID (taxon label) then node number (m_iLabel) is printed.
     */
    public String toNewick() {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
            buf.append(getLeft().toNewick());
            if (getRight() != null) {
                buf.append(',');
                buf.append(getRight().toNewick());
            }
            buf.append(")");
        } else {
            if (getID() == null) {
                buf.append(labelNr);
            } else {
                buf.append(getID());
            }
        }
        buf.append(getNewickMetaData());
        buf.append(":").append(getLength());
        return buf.toString();
    }

    /**
     * @param sLabels      //TODO make this method work for ZeroBranchSATree
     * @return beast.tree in long Newick format, with all length and meta data
     *         information, but with leafs labelled with their names
     */
    public String toString(List<String> sLabels) {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
            buf.append(getLeft().toString(sLabels));
            if (getRight() != null) {
                buf.append(',');
                buf.append(getRight().toString(sLabels));
            }
            buf.append(")");
        } else {
            buf.append(sLabels.get(labelNr));
        }
        if (metaDataString != null) {
            buf.append('[');
            buf.append(metaDataString);
            buf.append(']');
        }
        buf.append(":").append(getLength());
        return buf.toString();
    }

    public String toString() {
        return toShortNewick(true);
    }

    /**
     * Scales this node and all its descendants (either all descendants, or only non-sampled descendants)
     *
     * @param fScale    the scalar to multiply each scaled node age by
     * @param scaleSNodes true if sampled nodes should be scaled as well as internal nodes, false if only non-sampled
     *                  internal nodes should be scaled.
     * @throws Exception throws exception if resulting tree would have negative branch lengths.
     */
    public void scale(double fScale, boolean scaleSNodes) throws Exception {
        startEditing();
        isDirty |= Tree.IS_DIRTY;
        if (scaleSNodes || (!isLeaf() && !isFake())) {
            height *= fScale;
        }
        if (!isLeaf()) {
            ((ZeroBranchSANode)getLeft()).scale(fScale, scaleSNodes);
            if (getRight() != null) {
                ((ZeroBranchSANode)getRight()).scale(fScale, scaleSNodes);
            }
            if (height < getLeft().height || height < getRight().height) {
                throw new Exception("Scale gives negative branch length");
            }
        }
    }


    /**
     * @return true if this leaf actually represent a direct ancestor
     * (i.e. is on the end of a zero-length branch)
     */
    public boolean isDirectAncestor() {
        return (isLeaf() && !isRoot() && this.getParent().getHeight() == this.getHeight());
    }

    /**
     * @return true if this is a "fake" internal node (i.e. one of its children is a direct ancestor)
     */
    public boolean isFake() {
        if (this.isLeaf())
            return false;
        return (((ZeroBranchSANode)this.getLeft()).isDirectAncestor() || (this.getRight() != null && ((ZeroBranchSANode)this.getRight()).isDirectAncestor()));
    }
}
