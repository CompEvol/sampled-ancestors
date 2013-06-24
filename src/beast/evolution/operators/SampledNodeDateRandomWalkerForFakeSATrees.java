package beast.evolution.operators;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Description("Randomly moves sampled node dates on a tree by randomly selecting one either from a subset of taxa or from leaves")
public class SampledNodeDateRandomWalkerForFakeSATrees extends TreeOperator {

    public Input<Double> windowSizeInput =
            new Input<Double>("windowSize", "the size of the window both up and down when using uniform interval OR standard deviation when using Gaussian", Input.Validate.REQUIRED);
    public Input<TaxonSet> m_taxonsetInput = new Input<TaxonSet>("taxonset", "limit scaling to a subset of taxa. By default all tips are scaled.");
    public Input<Boolean> useGaussianInput =
            new Input<Boolean>("useGaussian", "Use Gaussian to move instead of uniform interval. Default false.", false);

    /**
     * node indices of taxa to choose from *
     */
    int[] m_iTaxa;

    double windowSize = 1;
    boolean m_bUseGaussian;
    boolean useNodeNumbers;


    @Override
    public void initAndValidate() throws Exception {
        windowSize = windowSizeInput.get();
        m_bUseGaussian = useGaussianInput.get();

        // determine taxon set to choose from
        if (m_taxonsetInput.get() != null) {
            useNodeNumbers = false;
            List<String> sTaxaNames = new ArrayList<String>();
            for (String sTaxon : m_tree.get().getTaxaNames()) {
                sTaxaNames.add(sTaxon);
            }

            List<String> set = m_taxonsetInput.get().asStringList();
            int nNrOfTaxa = set.size();
            m_iTaxa = new int[nNrOfTaxa];
            int k = 0;
            for (String sTaxon : set) {
                int iTaxon = sTaxaNames.indexOf(sTaxon);
                if (iTaxon < 0) {
                    throw new Exception("Cannot find taxon " + sTaxon + " in tree");
                }
                m_iTaxa[k++] = iTaxon;
            }
        } else {
            useNodeNumbers = true;
        }
    }

    @Override
    public double proposal() {
        // randomly select leaf node
        Tree tree = m_tree.get();
        Node node;
        if (useNodeNumbers) {
            int leafNodeCount = tree.getLeafNodeCount();
            do {
                int i = Randomizer.nextInt(leafNodeCount);
                node = tree.getNode(i);
            }  while (!node.isLeaf());

        }  else {
            int i = Randomizer.nextInt(m_iTaxa.length);
            node = tree.getNode(m_iTaxa[i]);
        }

        double value = node.getHeight();
        double newValue = value;
        if (m_bUseGaussian) {
            newValue += Randomizer.nextGaussian() * windowSize;
        } else {
            newValue += Randomizer.nextDouble() * 2 * windowSize - windowSize;
        }

        Node fake = null;
        double lower, upper;

        if (node.isDirectAncestor()) {
            fake = node.getParent();
            lower = getOtherChild(fake, node).getHeight();
            if (fake.getParent() != null) {
                upper = fake.getParent().getHeight();
            } else upper = Double.POSITIVE_INFINITY;
        } else {
            lower = 0.0;
            upper = node.getParent().getHeight();
        }

        if (newValue < lower || newValue > upper) {
            return Double.NEGATIVE_INFINITY;
        }

        if (newValue == value) {
            // this saves calculating the posterior
            return Double.NEGATIVE_INFINITY;
        }

        if (fake != null) {
            fake.setHeight(newValue);
        }
        node.setHeight(newValue);

        return 0.0;
    }

   @Override
    public double getCoercableParameterValue() {
        return windowSize;
    }

    @Override
    public void setCoercableParameterValue(double fValue) {
        windowSize = fValue;
    }

//    @Override
//    public void optimize(double logAlpha) {
//        // must be overridden by operator implementation to have an effect
//        double fDelta = calcDelta(logAlpha);
//        fDelta += Math.log(windowSize);
//        windowSize = Math.exp(fDelta);
//    }

//    @Override
//    public final String getPerformanceSuggestion() {
//        double prob = m_nNrAccepted / (m_nNrAccepted + m_nNrRejected + 0.0);
//        double targetProb = getTargetAcceptanceProbability();
//
//        double ratio = prob / targetProb;
//        if (ratio > 2.0) ratio = 2.0;
//        if (ratio < 0.5) ratio = 0.5;
//
//        // new scale factor
//        double newWindowSize = windowSize * ratio;
//
//        DecimalFormat formatter = new DecimalFormat("#.###");
//        if (prob < 0.10) {
//            return "Try setting window size to about " + formatter.format(newWindowSize);
//        } else if (prob > 0.40) {
//            return "Try setting window size to about " + formatter.format(newWindowSize);
//        } else return "";
//    }
}

// TODO 1. Should the height of a leaf be greater than zero?
// TODO 2. Should we use reflect if a chosen value is out of the bounds?
// TODO 3. Look at the optimise and getPerformanceSuggestion, can we leave them as they are?
