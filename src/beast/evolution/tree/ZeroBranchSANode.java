package beast.evolution.tree;

import beast.util.Randomizer;

import java.util.List;

/**
 * @author Alexandra Gavryushkina
 */

/**
 * @deprecated Use Node instead. Starting from v.1.1.1 Tree class supports sampled ancestors and
 * all the classes that contain ZeroBranch in their names (and some others) are replaced by similar classes.
 */
@Deprecated
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

    public String toSortedNewickWithZeroBranches(int[] iMaxNodeInClade) {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
            String sChild1 = ((ZeroBranchSANode)getLeft()).toSortedNewickWithZeroBranches(iMaxNodeInClade);
            int iChild1 = iMaxNodeInClade[0];
            if (getRight() != null) {
                String sChild2 = ((ZeroBranchSANode)getRight()).toSortedNewickWithZeroBranches(iMaxNodeInClade);
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
            if (getID() != null) {
                buf.append(labelNr+1);
            }
        } else {
            iMaxNodeInClade[0] = labelNr;
            buf.append(labelNr + 1);
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
    public void scale(double fScale, boolean scaleSNodes) {
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
                throw new IllegalArgumentException("Scale gives negative branch length");
            }
        }
    }

    public void scaleAndSlide(double fScale, int[] splitCount) throws Exception {
        startEditing();
        if (!isLeaf() && !isFake()) {
            height *= fScale;
            isDirty |= Tree.IS_DIRTY;
        }
        if (!isLeaf()) {
            ((ZeroBranchSANode)getLeft()).scaleAndSlide(fScale, splitCount);
            if (getRight() != null) {
                ((ZeroBranchSANode)getRight()).scaleAndSlide(fScale, splitCount);
            }
            if (height < getLeft().height || height < getRight().height) {
                if (isFake()){   // this can only be the case if we scale up

                    if (fScale < 1.0) {
                        throw new Exception("Scale and slide doesn't work properly");
                    }

                    Node child = this.getNonDirectAncestorChild();  //child is the node that has been scaled up causing
                    //this parent sampled ancestor node to be lower than its child

                    //this parent sampled ancestor node that has to be lowered down
                    Node sampledAncestor = this;
                    double saHeight = sampledAncestor.getHeight();

                    //apart from the direct parent of child there can be other sampled ancestor nodes lying on
                    //the branch going from child up to the first non-fake node
                    //search for such nodes and lower them down
                    do {
                        Node newChild = child;

                        //search for the right place to put the sampled ancestor node
                        do {
                            if (((ZeroBranchSANode)newChild).isFake()) {
                                newChild = ((ZeroBranchSANode) newChild).getNonDirectAncestorChild();
                            } else {
                                newChild = (Randomizer.nextInt(2) == 1)?newChild.getLeft():newChild.getRight();
                                splitCount[0]++;    //count how many levels the sa node goes down
                            }
                        } while (saHeight < newChild.getHeight());

                        //first remember the sampled ancestor node parent
                        Node newSampledAncestor = sampledAncestor.getParent();

                        //place the node above its newChild
                        placeSampledAncestorAboveItsNewChild(sampledAncestor, newChild);

                        //update sampled ancestor node
                        sampledAncestor = newSampledAncestor;
                        if (sampledAncestor != null) {
                            saHeight = sampledAncestor.getHeight();
                        }

                        //continue until you find all closely ancestral sampled ancestor nodes (those are nodes
                        //that lies on the branch going from this node up to the fist non-fake node).

                    } while (sampledAncestor != null && ((ZeroBranchSANode)sampledAncestor).isFake() && saHeight < child.getHeight());

                } else {  // this can only be the case if we scale down

                    if (fScale > 1.0) {
                        throw new Exception("Scale and slide doesn't work properly");
                    }

                    Node sampledAncestor = getFakeChild();
                    double saHeight = sampledAncestor.getHeight();

                    do {
                        Node newParent = this;
                        Node newChild;   //newChild remembers on which branch to place the sampled ancestor node
                                          //it will be a child of the sampled ancestor node
                        do {
                            newChild = newParent;
                            newParent = newParent.getParent();
                            if (newParent!= null && !((ZeroBranchSANode)newParent).isFake()) {
                                splitCount[0]++;
                            }
                        } while (newParent != null && saHeight > newParent.getHeight());


                        //first remember the sampled ancestor node child
                        Node newSampledAncestor = ((ZeroBranchSANode)sampledAncestor).getNonDirectAncestorChild();


                        //place the node above its newChild
                        placeSampledAncestorAboveItsNewChild(sampledAncestor, newChild);

                        //update sampled ancestor node
                        sampledAncestor = newSampledAncestor;
                        saHeight = sampledAncestor.getHeight();


                        //continue until you find all closely descendant sampled ancestor nodes (those are nodes
                        //that lies on the branch going from this node down to the first non-fake node).
                    } while (((ZeroBranchSANode)sampledAncestor).isFake() && saHeight > this.getHeight());
                }
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

    public Node getNonDirectAncestorChild(){
        if (((ZeroBranchSANode)this.getLeft()).isDirectAncestor()){
            return getRight();
        }
        if  (((ZeroBranchSANode)this.getRight()).isDirectAncestor()){
            return getLeft();
        }
        return null;
    }

    public Node getFakeChild(){

        if (((ZeroBranchSANode)this.getLeft()).isFake()){
            return getLeft();
        }
        if (((ZeroBranchSANode)this.getRight()).isFake()){
            return getRight();
        }
        return null;
    }

    public void placeSampledAncestorAboveItsNewChild(Node node, Node newChild){

        //reattach node from the branch it belongs to
        Node nodeChild = ((ZeroBranchSANode)node).getNonDirectAncestorChild();
        Node nodeParent = node.getParent();
        if (nodeParent != null) {
            int nodePosition = nodeParent.getChildren().indexOf(node);
            nodeParent.setChild(nodePosition, nodeChild);
            nodeChild.setParent(nodeParent);
        }  else {
            nodeChild.setParent(null);
        }
        node.removeChild(nodeChild); //you also need to set nodeChild as a new root but do this at the end

        //attach node to the branch above newChild
        Node newParent = newChild.getParent();
        if (newParent != null){
            int newChildPosition = newParent.getChildren().indexOf(newChild);
            newParent.setChild(newChildPosition, node);
            node.setParent(newParent);

        } else {
            node.setParent(null);
        }
        node.addChild(newChild);
        if (newParent == null){
            this.m_tree.setRoot(node);
        }

        //set the new root here because setting root automatically assigns the number of nodes below a new root
        // to tree.nodeCount field and if you were to set it early the nodeCount would not be correct
        if (nodeParent == null){
            this.m_tree.setRoot(nodeChild);
        }



    }
}
