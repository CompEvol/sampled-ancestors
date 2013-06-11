package beast.evolution.likelihood;

/**
 *
 */

public class BeerLikelihoodCore4ForSampledAncestorTrees extends BeerLikelihoodCore4 {

    @Override
    public void calculatePartials(int iChild1Index, int iChild2Index, int iParentIndex) {
        if (m_iStates[iChild1Index] != null) {
            if (m_iStates[iChild2Index] != null) {
                calculateStatesStatesPruning(
                        m_iStates[iChild1Index], m_fMatrices[m_iCurrentMatrices[iChild1Index]][iChild1Index],
                        m_iStates[iChild2Index], m_fMatrices[m_iCurrentMatrices[iChild2Index]][iChild2Index],
                        m_fPartials[m_iCurrentPartials[iParentIndex]][iParentIndex]);
            } else {
                calculateStatesPartialsPruning(m_iStates[iChild1Index], m_fMatrices[m_iCurrentMatrices[iChild1Index]][iChild1Index],
                        m_fPartials[m_iCurrentPartials[iChild2Index]][iChild2Index], m_fMatrices[m_iCurrentMatrices[iChild2Index]][iChild2Index],
                        m_fPartials[m_iCurrentPartials[iParentIndex]][iParentIndex]);
            }
        } else {
            if (m_iStates[iChild2Index] != null) {
                calculateStatesPartialsPruning(m_iStates[iChild2Index], m_fMatrices[m_iCurrentMatrices[iChild2Index]][iChild2Index],
                        m_fPartials[m_iCurrentPartials[iChild1Index]][iChild1Index], m_fMatrices[m_iCurrentMatrices[iChild1Index]][iChild1Index],
                        m_fPartials[m_iCurrentPartials[iParentIndex]][iParentIndex]);
            } else {
                calculatePartialsPartialsPruning(m_fPartials[m_iCurrentPartials[iChild1Index]][iChild1Index], m_fMatrices[m_iCurrentMatrices[iChild1Index]][iChild1Index],
                        m_fPartials[m_iCurrentPartials[iChild2Index]][iChild2Index], m_fMatrices[m_iCurrentMatrices[iChild2Index]][iChild2Index],
                        m_fPartials[m_iCurrentPartials[iParentIndex]][iParentIndex]);
            }
        }

        if (m_bUseScaling) {
            scalePartials(iParentIndex);
        }

    }

    public void calculatePartials(int iChildIndex, int iParentIndex) {

        int[] parentStates = m_iStates[iParentIndex];

        if (m_iStates[iChildIndex] != null) {
            calculateStatesPruning(parentStates, m_iStates[iChildIndex], m_fMatrices[m_iCurrentMatrices[iChildIndex]][iChildIndex],
                    m_fPartials[m_iCurrentPartials[iParentIndex]][iParentIndex]);
        } else {
            calculatePartialsPruning(parentStates, m_fPartials[m_iCurrentPartials[iChildIndex]][iChildIndex], m_fMatrices[m_iCurrentMatrices[iChildIndex]][iChildIndex],
                        m_fPartials[m_iCurrentPartials[iParentIndex]][iParentIndex]);

        }

        if (m_bUseScaling) {
            scalePartials(iParentIndex);
        }

    }

    protected void calculateStatesPruning(int[] parentStates, int[] iStates, double[] fMatrices,
                                                double[] fPartialsParent) {
        int v = 0;

        for (int l = 0; l < m_nMatrices; l++) {

            for (int k = 0; k < m_nPatterns; k++) {

                int state = iStates[k];

                int parentState = parentStates[k];

                int w = l * m_nMatrixSize;

                if (state < 4) {
                    if (parentState < 4) {
                        if (0 == parentState)
                            fPartialsParent[v] = fMatrices[w + state];
                        else fPartialsParent[v] = 0;
                        v++;
                        w += 4;
                        if (1 == parentState)
                            fPartialsParent[v] = fMatrices[w + state];
                        else fPartialsParent[v] = 0;
                        v++;
                        w += 4;
                        if (2 == parentState)
                            fPartialsParent[v] = fMatrices[w + state];
                        else fPartialsParent[v] = 0;
                        v++;
                        w += 4;
                        if (3 == parentState)
                            fPartialsParent[v] = fMatrices[w + state];
                        else fPartialsParent[v] = 0;
                        v++;
                        w += 4;
                    }
                    else {
                        // parent has a gap or unknown state and child has a state   //TODO finish this case

                    }

                } 
                else {
                    if (parentState < 4) {
                        // parent has a state and child has a gap or unknown state     //TODO finish this case

                        if (0 == parentState)
                            fPartialsParent[v] = 1;
                        else fPartialsParent[v] = 0;
                        v++;
                        w += 4;
                        if (1 == parentState)
                            fPartialsParent[v] = 1;
                        else fPartialsParent[v] = 0;
                        v++;
                        w += 4;
                        if (2 == parentState)
                            fPartialsParent[v] = 1;
                        else fPartialsParent[v] = 0;
                        v++;
                        w += 4;
                        if (3 == parentState)
                            fPartialsParent[v] = 1;
                        else fPartialsParent[v] = 0;
                        v++;
                        w += 4;

                    }
                    else {
                        // both parent and child have gaps or unknown states     //TODO finish this case
                    }
                }
            }
        }   
    }

    protected void calculatePartialsPruning( int[] parentStates, double[] fPartialsChild, double[] fMatrices,
                                                double[] fPartialsParent) {

        double sum;

        int u = 0;
        int v = 0;

        for (int l = 0; l < m_nMatrices; l++) {

            for (int k = 0; k < m_nPatterns; k++) {

                int parentState = parentStates[k];

                int w = l * m_nMatrixSize;

                if (parentState < 4) {
                    if (parentState == 0) {
                        sum = fMatrices[w] * fPartialsChild[v];
                        sum += fMatrices[w + 1] * fPartialsChild[v + 1];
                        sum += fMatrices[w + 2] * fPartialsChild[v + 2];
                        sum += fMatrices[w + 3] * fPartialsChild[v + 3];
                        fPartialsParent[u] = sum;
                    }  else fPartialsParent[u] = 0;
                    u++;

                    if (parentState == 1) {
                        sum = fMatrices[w + 4] * fPartialsChild[v];
                        sum += fMatrices[w + 5] * fPartialsChild[v + 1];
                        sum += fMatrices[w + 6] * fPartialsChild[v + 2];
                        sum += fMatrices[w + 7] * fPartialsChild[v + 3];
                        fPartialsParent[u] = sum;
                    }  else fPartialsParent[u] = 0;
                    u++;

                    if (parentState == 2) {
                        sum = fMatrices[w + 8] * fPartialsChild[v];
                        sum += fMatrices[w + 9] * fPartialsChild[v + 1];
                        sum += fMatrices[w + 10] * fPartialsChild[v + 2];
                        sum += fMatrices[w + 11] * fPartialsChild[v + 3];
                        fPartialsParent[u] = sum;
                    }  else fPartialsParent[u] = 0;
                    u++;

                    if (parentState == 3) {
                        sum = fMatrices[w + 12] * fPartialsChild[v];
                        sum += fMatrices[w + 13] * fPartialsChild[v + 1];
                        sum += fMatrices[w + 14] * fPartialsChild[v + 2];
                        sum += fMatrices[w + 15] * fPartialsChild[v + 3];
                        fPartialsParent[u] = sum;
                    }  else fPartialsParent[u] = 0;
                    u++;
                }
                else {
                    // parent has a gap or unknown state  //TODO finish this case
                }
                v += 4;
            }
        }
        
    }

}
