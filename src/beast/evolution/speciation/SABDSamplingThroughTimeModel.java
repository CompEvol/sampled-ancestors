package beast.evolution.speciation;

import beast.core.Description;
import beast.core.Input;
import beast.core.parameter.RealParameter;
import beast.evolution.tree.Tree;

/**
 * @author Alexandra Gavryushkina
 */

//The tree density is from: Tanja Stadler et al. "Estimating the Basic Reproductive Number from Viral Sequence Data"

@Description("Calculate tree density under Birth Death Sampling Through Time Model for Epidemics " +
        "that is the BDM where an individual is sampled at a time with a constant rate psi" +
        " and where an individual becomes noninfectious immediately after the sampling" +
        "with a constant probability r")
public class SABDSamplingThroughTimeModel extends SpeciesTreeDistribution {

    public Input<RealParameter> orig_root =
            new Input<RealParameter>("orig_root", "The time dictance between the most recent common ancestor time" +
                    " and the origin of infection", Input.Validate.REQUIRED);

    public Input<RealParameter> birthRate =
            new Input<RealParameter>("birthRate", "BirthRate", Input.Validate.REQUIRED);
    public Input<RealParameter> deathRate =
            new Input<RealParameter>("deathRate", "DeathRate", Input.Validate.REQUIRED);
    public Input<RealParameter> samplingRate =
            new Input<RealParameter>("samplingRate", "The sampling rate per individual", Input.Validate.REQUIRED);

    // r parameter
    public Input<RealParameter> becomeNoninfectiousAfterSamplingProbability =
            new Input<RealParameter>("becomeNoninfectiousAfterSamplingProbability", "The probability of an individual to become noninfectious immediately after the sampling", Input.Validate.REQUIRED);

    protected double r;
    protected double lambda;
    protected double mu;
    protected double psi;
    protected double c1;
    protected double c2;
    protected double origToRootDistance;

    public void initAndValidate() throws Exception {
        mu = deathRate.get().getValue();
        psi = samplingRate.get().getValue();
        lambda = birthRate.get().getValue();
        r = becomeNoninfectiousAfterSamplingProbability.get().getValue();
        c1 = Math.sqrt((lambda - mu - psi) * (lambda - mu - psi) + 4 * lambda * psi);
        c2 = -(lambda - mu - psi) / c1;
        origToRootDistance = orig_root.get().getValue();
    }

    private double p0s(double t, double c1, double c2) {
        double p0 = (lambda + mu + psi - c1 * ((1 + c2) - Math.exp(-c1 * t) * (1 - c2)) / ((1 + c2) + Math.exp(-c1 * t) * (1 - c2))) / (2 * lambda);
        return r + (1 - r) * p0;
    }

    private double q(double t, double c1, double c2) {
        return Math.exp(c1 * t) * (1 + c2) * (1 + c2) + Math.exp(-c1 * t) * (1 - c2) * (1 - c2) + 2 * (1 - c2 * c2);
    }

    private void updateParameters() {
        mu = deathRate.get().getValue();
        psi = samplingRate.get().getValue();
        lambda = birthRate.get().getValue();
        r = becomeNoninfectiousAfterSamplingProbability.get().getValue();
        c1 = Math.sqrt((lambda - mu - psi) * (lambda - mu - psi) + 4 * lambda * psi);
        c2 = -(lambda - mu - psi) / c1;
        origToRootDistance = orig_root.get().getValue();
    }

    @Override
    public double calculateTreeLogLikelihood(Tree tree) {
        int nodeCount = tree.getNodeCount();
        updateParameters();
        double x0 = tree.getRoot().getHeight() + origToRootDistance;
        //double x0 = origToRootDistance;
        if (x0 < tree.getRoot().getHeight()) {
            System.out.println("It is time to change the root height choice");
        }

        double logPost;
        logPost = -Math.log(q(x0, c1, c2));
        for (int i = 0; i < nodeCount; i++) {
            if (tree.getNode(i).isLeaf()) {
                if  (!tree.getNode(i).isDirectAncestor())
                    logPost += Math.log(psi) + Math.log(q(tree.getNode(i).getHeight(), c1, c2)) + Math.log(p0s(tree.getNode(i).getHeight(), c1, c2));
            } else {
                if (tree.getNode(i).isFake()) {
                    logPost += Math.log(psi) + Math.log(1 - r);
                } else {
                    logPost += Math.log(lambda) - Math.log(q(tree.getNode(i).getHeight(), c1, c2));
                }
            }
        }

        return logPost;
    }

    @Override
    protected boolean requiresRecalculation() {
        return true;
    }

}
