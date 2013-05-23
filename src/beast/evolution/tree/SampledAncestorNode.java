/*
* File Node.java
*
* Copyright (C) 2010 Remco Bouckaert remco@cs.auckland.ac.nz
*
* This file is part of BEAST2.
* See the NOTICE file distributed with this work for additional
* information regarding copyright ownership and licensing.
*
* BEAST is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
*  BEAST is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with BEAST; if not, write to the
* Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
* Boston, MA  02110-1301  USA
*/
package beast.evolution.tree;

import beast.core.Description;

import java.util.List;

@Description("Nodes in building beast.tree data structure.")
public class SampledAncestorNode extends Node {

    /**
     * @return (deep) copy of node
     */
    @Override
    public Node copy() {
        Node node = new SampledAncestorNode();
        node.m_fHeight = m_fHeight;
        node.m_iLabel = m_iLabel;
        node.m_sMetaData = m_sMetaData;
        node.m_Parent = null;
        node.setID(m_sID);

        for (Node child : getChildren()) {
            node.addChild(child.copy());
        }
        return node;
    } // copy


    @Override
    public String toSortedNewick(int[] iMaxNodeInClade, boolean printMetaData) {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
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
        } else {
            iMaxNodeInClade[0] = m_iLabel;
        }
        if (getID() != null) {
            buf.append(m_iLabel + 1);
        }
        if (printMetaData) {
            buf.append(getNewickMetaData());
        }
        buf.append(":").append(getLength());
        return buf.toString();
    }

    /**
     * @param sLabels names of the taxa
     * @return beast.tree in Newick format with taxon labels for sampled nodes.
     */
    @Override
    public String toNewick(List<String> sLabels) {
        StringBuilder buf = new StringBuilder();
        if (getLeft() != null) {
            buf.append("(");
            buf.append(getLeft().toNewick(sLabels));
            if (getRight() != null) {
                buf.append(',');
                buf.append(getRight().toNewick(sLabels));
            }
            buf.append(")");
        } else {
            if (sLabels == null) {
                buf.append(m_iLabel);
            } else {
                buf.append(sLabels.get(m_iLabel));
            }
        }
        buf.append(getNewickMetaData());
        if (getChildCount() == 1)
            if (sLabels == null) {
                buf.append(m_iLabel);
            } else {
                buf.append(sLabels.get(m_iLabel));
            }
        buf.append(":").append(getLength());
        return buf.toString();
    }
} // class Node
