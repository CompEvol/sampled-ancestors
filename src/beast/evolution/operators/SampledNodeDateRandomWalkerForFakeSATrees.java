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

@Description("Randomly select a sampled node and shifts the date of the node within a given window")
public class SampledNodeDateRandomWalkerForFakeSATrees extends TipDatesRandomWalker {


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

//        System.out.println("Tree was before Random = " + tree.getRoot().toShortNewick(false));
//        for (int i=0; i< tree.getNodeCount(); i ++) {
//            if (tree.getNode(i).isDirectAncestor()) {
//                System.out.println("Node " + i + " is direct ancestor");
//            }
//        }

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

//        System.out.println("Proposed tree = " + tree.getRoot().toShortNewick(false));
//        for (int inx=0; inx< tree.getNodeCount(); inx ++) {
//            if (tree.getNode(inx).isDirectAncestor()) {
//                System.out.println("Node " + inx + " is direct ancestor");
//            }
//        }

        //tree.setEverythingDirty(true);

        return 0.0;
    }
}

// TODO 1. Should the height of a leaf be greater than zero?
// TODO 2. Should we use reflect if a chosen value is out of the bounds?
// TODO 3. Look at the optimise and getPerformanceSuggestion, can we leave them as they are?
