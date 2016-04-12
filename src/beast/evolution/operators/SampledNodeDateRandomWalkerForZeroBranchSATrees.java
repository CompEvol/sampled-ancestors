package beast.evolution.operators;

import beast.core.Description;
import beast.core.Distribution;
import beast.core.Input;
import beast.core.util.Log;
import beast.evolution.tree.Node;
import beast.evolution.tree.SamplingDate;
import beast.evolution.tree.Tree;
import beast.evolution.tree.ZeroBranchSANode;
import beast.math.distributions.*;
import beast.math.distributions.Uniform;
import beast.util.Randomizer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Description("Randomly select a sampled node and shifts the date of the node within a given window")
public class SampledNodeDateRandomWalkerForZeroBranchSATrees extends TipDatesRandomWalker {

    public Input<List<SamplingDate>> samplingDatesInput = new Input<>("samplingDates",
            "List of sampling dates", new ArrayList<>());

    public Input<String> weightFileInput = new Input<>("weightFile", "tab-delimited file (no header) containing relative weights to apply to each sampling date (and optionally ESS values for sampling dates from previous run.) " +
            "If the ESSs are included then they are assumed to be from a previous run using the given weights. They are used to calculate a new set of weights to even out the ESSs. " +
            "Each new relative weights is calculated to be proportional to old weight divided by corresponding ESS. " +
            "The weight file is only used if a taxon set is also provided. ");

    public Input<String> weightOutFileInput = new Input<>("weightOutFile", "tab-delimited file (no header) containing weights applied to each sampling date", (String)null);

    boolean useNodeNumbers;
    List<String> samplingDateTaxonNames = new ArrayList<>();

    double[] relativeWeights = null;

    @Override
    public void initAndValidate() {
        windowSize = windowSizeInput.get();
        useGaussian = useGaussianInput.get();

        for (SamplingDate taxon : samplingDatesInput.get()) {
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

            if (weightFileInput.get() != null) {
                relativeWeights = new double[nNrOfTaxa];
                double[] ess = new double[nNrOfTaxa];
                int count = 0;
                double essSum = 0.0;
                double prevWeightSum = 0.0;

                double[] prevWeights = new double[nNrOfTaxa];


                try {
                    BufferedReader reader = new BufferedReader(new FileReader(weightFileInput.get()));

                    String line = reader.readLine();
                    while (line != null ) {
                        String[] parts = line.split("\t");
                        int index = set.indexOf(parts[0]);
                        if (index < 0) {
                            Log.warning("taxon '" + parts[0] + "' in weight file '" + weightFileInput.get() + "' not found in taxa set.");
                        } else {
                            if (ess[index] > 0) {
                                throw new RuntimeException("Taxon '" + parts[0] + "' is duplicated in ESS file '" + weightFileInput.get() + "'");
                            }
                            count += 1;
                            ess[index] = Double.parseDouble(parts[2]);
                            essSum += ess[index];
                            prevWeights[index] = Double.parseDouble(parts[1]);
                            prevWeightSum += prevWeights[index];
                        }
                        line = reader.readLine();
                    }

                    if (count < ess.length) {
                        double meanESS = essSum / count;
                        double meanPrevWeight = prevWeightSum / count;
                        Log.warning("Only " + count + " out of " + ess.length + " taxa found in ESS file. The remainder will be set to the mean weight and ESS.");
                        for (int i = 0; i < ess.length; i++) {
                            if (ess[i] == 0) {
                                ess[i] = meanESS;
                                prevWeights[i] = meanPrevWeight;
                            }
                        }
                    }

                    double sum = 0.0;
                    for (int i = 0; i < relativeWeights.length; i++) {
                        relativeWeights[i] = prevWeights[i] / ess[i];
                        sum += relativeWeights[i];
                    }

                    PrintWriter writer = null;
                    if (weightOutFileInput.get() != null) {
                        writer = new PrintWriter(new FileWriter(weightOutFileInput.get()));
                    }

                    // normalize
                    for (int i = 0; i < relativeWeights.length; i++) {
                        relativeWeights[i] /= sum;
                        if (weightOutFileInput.get() != null) {

                            if (writer != null) {
                                writer.println(set.get(i) + "\t" + relativeWeights[i]);
                            } else {
                                Log.info(set.get(i) + "\t" + relativeWeights[i]);
                            }
                        }
                    }

                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }

                    //cumulative
                    for (int i = 1; i < relativeWeights.length; i++) {
                        relativeWeights[i] = relativeWeights[i-1] + relativeWeights[i];
                    }
                    Log.info("Last relative weight = " + relativeWeights[relativeWeights.length-1]);

                } catch (FileNotFoundException e) {
                    Log.warning("Weight file named '" + weightFileInput.get() + "' not found. Defaulting to equal weights.");
                    relativeWeights = null;
                } catch (IOException e) {
                    Log.warning("IO exception reading file named '" + weightFileInput.get() + "'. Defaulting to equal weights.");
                    relativeWeights = null;
                }
            }

        } else {
            useNodeNumbers = true;
        }


        // check that all nodes are within bounds.
        List<Node> nodes = getNodesToOperateOn();
        boolean err = false;
        for (Node node : nodes) {
            double age = node.getHeight();
            boolean drawFromDistribution = samplingDateTaxonNames.contains(node.getID());
            if (drawFromDistribution) {
                SamplingDate taxonSamplingDate = samplingDatesInput.get().get(samplingDateTaxonNames.indexOf(node.getID()));
                double lower = taxonSamplingDate.getLower();
                double upper = taxonSamplingDate.getUpper();
                if (age > upper || age < lower) {
                    err = true;
                    Log.err("Node " + node.getID() + " has an age (" + age + ") outside the sampling date range (" + lower + ", " + upper + ").");
                }
            }
        }
        if (err) {
            throw new RuntimeException("Error: Stopping because nodes found out of sampling date range.");
        }
    }

    private List<Node> getNodesToOperateOn() {
        Tree tree = treeInput.get();

        ArrayList<Node> nodeList = new ArrayList<>();

        if (useNodeNumbers) {
            int leafNodeCount = tree.getLeafNodeCount();
        }  else {
            for (int i = 0; i < taxonIndices.length; i++) {
                nodeList.add(tree.getNode(taxonIndices[i]));
            }
        }
        return nodeList;
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

            if (relativeWeights != null) {
                double X = Randomizer.nextDouble();
                int i = 0;
                while (relativeWeights[i] < X) {
                    i += 1;
                }
                node = tree.getNode(taxonIndices[i]);
            } else {
                int i = Randomizer.nextInt(taxonIndices.length);
                node = tree.getNode(taxonIndices[i]);
            }
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

        if (node.isDirectAncestor()) {
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

        if (newValue < 0) {
            for (int i=0; i<tree.getNodeCount(); i++){
                double oldHeight = tree.getNode(i).getHeight();
                tree.getNode(i).setHeight(oldHeight-newValue);
            }
        }  else {
            boolean dateShiftDown = true;
            for (int i=0; i< tree.getLeafNodeCount(); i++){
                if (tree.getNode(i).getHeight() == 0){
                    dateShiftDown = false;
                    break;
                }
            }
            if (dateShiftDown) {
                ArrayList<Double> tipNodeHeights= new ArrayList<Double>();
                for (int i=0; i<tree.getLeafNodeCount(); i++){
                    tipNodeHeights.add(tree.getNode(i).getHeight());
                }
                Collections.sort(tipNodeHeights);
                double shiftDown = tipNodeHeights.get(0);
                for (int i=0; i<tree.getNodeCount(); i++){
                    double oldHeight = tree.getNode(i).getHeight();
                    tree.getNode(i).setHeight(oldHeight-shiftDown);
                }
            }
        }

        boolean check = true;
        for (int i=0; i<tree.getNodeCount(); i++){
            if (tree.getNode(i).getHeight() < 0) {
                System.out.println("Negative height found");
                System.exit(0);
            }
            if (tree.getNode(i).getHeight() == 0) {
                check = false;
            }
        }
        if (check) {
            System.out.println("There is no 0 height node");
            System.exit(0);
        }

        //tree.setEverythingDirty(true);

        return 0.0;
    }

    @Override
    public void optimize(double logAlpha) {
    }

}

// TODO 1. Should the height of a leaf be greater than zero?
// TODO 2. Should we use reflect if a chosen value is out of the bounds?
// TODO 3. Look at the optimise and getPerformanceSuggestion, can we leave them as they are?
