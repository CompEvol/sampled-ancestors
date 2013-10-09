package beast.evolution.operators;

import beast.core.Description;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.evolution.tree.ZeroBranchSANode;
import beast.util.Randomizer;

import java.util.ArrayList;
import java.util.List;

@Description("Randomly select a sampled node and shifts the date of the node within a given window")
public class SampledNodeDateRandomWalkerForFakeSATrees extends TipDatesRandomWalker {


    boolean useNodeNumbers;


    @Override
    public void initAndValidate() throws Exception {
        windowSize = windowSizeInput.get();
        useGaussian = useGaussianInput.get();

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
                    throw new Exception("Cannot find taxon " + sTaxon + " in tree");
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
//            do {
//                int i = Randomizer.nextInt(leafNodeCount);
//                node = tree.getNode(i);
//            }  while (!node.isLeaf());
        }  else {
            int i = Randomizer.nextInt(taxonIndices.length);
            node = tree.getNode(taxonIndices[i]);
        }

        double value = node.getHeight();
        double newValue = value;
        if (useGaussian) {
            newValue += Randomizer.nextGaussian() * windowSize;
        } else {
            newValue += Randomizer.nextDouble() * 2 * windowSize - windowSize;
        }

        Node fake = null;
        double lower, upper;

        if (((ZeroBranchSANode)node).isDirectAncestor()) {
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

        //tree.setEverythingDirty(true);

        return 0.0;
    }
}

// TODO 1. Should the height of a leaf be greater than zero?
// TODO 2. Should we use reflect if a chosen value is out of the bounds?
// TODO 3. Look at the optimise and getPerformanceSuggestion, can we leave them as they are?
