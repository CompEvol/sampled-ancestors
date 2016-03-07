package beast.evolution.operators;

import beast.core.Description;
import beast.core.Input;
import beast.evolution.tree.Node;
import beast.evolution.tree.SamplingDate;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Description("Randomly select a sampled node and shifts the date of the node within a given window")
public class SampledNodeDateRandomWalker extends TipDatesRandomWalker {

    public Input<List<SamplingDate>> samplingDatesInput = new Input<>("samplingDates",
            "List of sampling dates", new ArrayList<SamplingDate>());

    boolean useNodeNumbers;
    List<String> samplingDateTaxonNames = new ArrayList<>();


    @Override
    public void initAndValidate() {
        windowSize = windowSizeInput.get();
        useGaussian = useGaussianInput.get();

        for (SamplingDate taxon:samplingDatesInput.get()) {
            samplingDateTaxonNames.add(taxon.taxonInput.get().getID());
        }

        // determine taxon set to choose from
        if (m_taxonsetInput.get() != null) {
            useNodeNumbers = false;
            List<String> sTaxaNames = new ArrayList<String>();
            for (String sTaxon : treeInput.get().getTaxaNames()) {
                sTaxaNames.add(sTaxon);
            }

            List<String> set = m_taxonsetInput.get().asStringList();
            int nNrOfTaxa = set.size();
            taxonIndices = new int[nNrOfTaxa];
            int k = 0;
            for (String sTaxon : set) {
                int iTaxon = sTaxaNames.indexOf(sTaxon);
                if (iTaxon < 0) {
                    throw new IllegalArgumentException("Cannot find taxon " + sTaxon + " in tree");
                }
                taxonIndices[k++] = iTaxon;
            }
        } else {
            useNodeNumbers = true;
        }
    }

    @Override
    public double proposal() {

        // randomly select a leaf node
        Tree tree = treeInput.get();

        Node node;
        if (useNodeNumbers) {
            int leafNodeCount = tree.getLeafNodeCount();
            int i = Randomizer.nextInt(leafNodeCount);
            node = tree.getNode(i);
        }  else {
            int i = Randomizer.nextInt(taxonIndices.length);
            node = tree.getNode(taxonIndices[i]);
        }

        double value = node.getHeight();

        if (value == 0.0) {
            return Double.NEGATIVE_INFINITY;
        }
        double newValue = value;

        boolean drawFromDistribution = samplingDateTaxonNames.contains(node.getID());
        if (drawFromDistribution) {
            SamplingDate taxonSamplingDate = samplingDatesInput.get().get(samplingDateTaxonNames.indexOf(node.getID()));
            double range = taxonSamplingDate.getUpper() - taxonSamplingDate.getLower();
            newValue = taxonSamplingDate.getLower() + Randomizer.nextDouble() * range;
        }  else {
            if (useGaussian) {
                newValue += Randomizer.nextGaussian() * windowSize;
            } else {
                newValue += Randomizer.nextDouble() * 2 * windowSize - windowSize;
            }
        }


        Node fake = null;
        double lower, upper;

        if ((node).isDirectAncestor()) {
            fake = node.getParent();
            lower = getOtherChild(fake, node).getHeight();
            if (fake.getParent() != null) {
                upper = fake.getParent().getHeight();
            } else upper = Double.POSITIVE_INFINITY;
        } else {
            //lower = Double.NEGATIVE_INFINITY;
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
    public void optimize(double logAlpha) {
    }

}

// TODO transfer reflect, optimise and getPerformanceSuggestion from tipDatesRanodmWalker

