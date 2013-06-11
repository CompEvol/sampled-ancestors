package beast.evolution.likelihood;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

import java.util.List;

/**
 *
 */
public class TreeLikelihoodForSampledAncestorTrees extends TreeLikelihood {

    protected BeerLikelihoodCore4ForSampledAncestorTrees m_likelihoodCore;

    @Override
    int traverse(final Node node) throws Exception {     //TODO complete this method
        int update = (node.isDirty() | m_nHasDirt);

        final int iNode = node.getNr();

        final double branchRate = m_branchRateModel.getRateForBranch(node);
        final double branchTime = node.getLength() * branchRate;

        // First update the transition probability matrix(ices) for this branch
        //if (!node.isRoot() && (update != Tree.IS_CLEAN || branchTime != m_StoredBranchLengths[iNode])) {
        if (!node.isRoot() && (update != Tree.IS_CLEAN || branchTime != m_branchLengths[iNode])) {
            m_branchLengths[iNode] = branchTime;
            final Node parent = node.getParent();
            m_likelihoodCore.setNodeMatrixForUpdate(iNode);
            for (int i = 0; i < m_siteModel.getCategoryCount(); i++) {
                final double jointBranchRate = m_siteModel.getRateForCategory(i, node) * branchRate;
                m_substitutionModel.getTransitionProbabilities(node, parent.getHeight(), node.getHeight(), jointBranchRate, m_fProbabilities);
                m_likelihoodCore.setNodeMatrix(iNode, i, m_fProbabilities);
            }
            update |= Tree.IS_DIRTY;
        }

        // If the node is internal, update the partial likelihoods.
        if (!node.isLeaf()) {

            // Traverse down the child nodes
            final List<Node> children= node.getAllChildNodes();
            final int[] updateChildren = new int[children.size()];
            boolean needUpdate = false;
            for  (Node child : children) {
                updateChildren[children.indexOf(child)] = traverse(child);
                needUpdate = needUpdate || (updateChildren[children.indexOf(child)] != Tree.IS_CLEAN );
            }

            // If some child node was updated then update this node too
            if (needUpdate) {

                final int[] childNum = new int[children.size()];

                for  (Node child : children) {
                    childNum[children.indexOf(child)] = child.getNr();
                }
                m_likelihoodCore.setNodePartialsForUpdate(iNode);
                for (int up : updateChildren) {
                    update |= (update | up);  // TODO check if this is what we need here
                }                            // it was like this before: update |= (update1 | update2);

                if (update >= Tree.IS_FILTHY) {
                    m_likelihoodCore.setNodeStatesForUpdate(iNode);
                }

                if (m_siteModel.integrateAcrossCategories()) {
                    if (children.size() == 2) {
                        m_likelihoodCore.calculatePartials(childNum[0], childNum[1], iNode);
                    }  else if (children.size() == 1) {
                        m_likelihoodCore.calculatePartials(childNum[0], iNode);
                    }

                } else {
                    throw new Exception("Error TreeLikelihood 201: Site categories not supported");
                    //m_pLikelihoodCore->calculatePartials(childNum1, childNum2, nodeNum, siteCategories);
                }

                if (node.isRoot()) {
                    // No parent this is the root of the beast.tree -
                    // calculate the pattern likelihoods
                    final double[] frequencies = //m_pFreqs.get().
                            m_substitutionModel.getFrequencies();

                    final double[] proportions = m_siteModel.getCategoryProportions(node);
                    m_likelihoodCore.integratePartials(node.getNr(), proportions, m_fRootPartials);

                    if (m_iConstantPattern != null) { // && !SiteModel.g_bUseOriginal) {
                        m_fProportionInvariant = m_siteModel.getProportianInvariant();
                        // some portion of sites is invariant, so adjust root partials for this
                        for (final int i : m_iConstantPattern) {
                            m_fRootPartials[i] += m_fProportionInvariant;
                        }
                    }

                    m_likelihoodCore.calculateLogLikelihoods(m_fRootPartials, frequencies, m_fPatternLogLikelihoods);
                }

            }
        }
        return update;
    }  // traverseWithBRM
}
