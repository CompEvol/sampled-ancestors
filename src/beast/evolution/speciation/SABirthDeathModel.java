package beast.evolution.speciation;

import beast.core.Citation;
import beast.core.Description;
import beast.core.Input;
import beast.core.parameter.RealParameter;
import beast.evolution.tree.*;
import beast.util.ZeroBranchSATreeParser;

/**
 * @author Alexandra Gavryushkina
 */

//The tree density is from: Tanja Stadler et al. "Estimating the Basic Reproductive Number from Viral Sequence Data"

@Description("Calculate tree density under Birth Death Sampling Through Time Model for Epidemics " +
        "that is the BDM where an individual is sampled at a time with a constant rate psi" +
        " and where an individual becomes noninfectious immediately after the sampling" +
        "with a constant probability r")
@Citation("Gavryushkina A, Welch D, Stadler T, Drummond AJ (2014) \n" +
        "Bayesian inference of sampled ancestor trees for epidemiology and fossil calibration. \n" +
        "PLoS Comput Biol 10(12): e1003919. doi:10.1371/journal.pcbi.1003919")
public class SABirthDeathModel extends SpeciesTreeDistribution {

    public Input<RealParameter> originInput =
            new Input<RealParameter>("origin", "The origin of infection",(RealParameter)null);

    //'direct' parameters
    public Input<RealParameter> birthRateInput =
            new Input<RealParameter>("birthRate", "Birth rate");
    public Input<RealParameter> deathRateInput =
            new Input<RealParameter>("deathRate", "Death rate");
    public Input<RealParameter> samplingRateInput =
            new Input<RealParameter>("samplingRate", "Sampling rate per individual");

    //transformed parameters:
    public Input<RealParameter> diversificationRateInput =
            new Input<RealParameter>("diversificationRate", "Net diversification rate. Birth rate - death rate", Input.Validate.XOR, birthRateInput);
    public Input<RealParameter> turnoverInput =
            new Input<RealParameter>("turnover", "Turnover. Death rate/birth rate", Input.Validate.XOR, deathRateInput);
    public Input<RealParameter> samplingProportionInput =
            new Input<RealParameter>("samplingProportion", "The probability of sampling prior to death. Sampling rate/(sampling rate + death rate)", Input.Validate.XOR, samplingRateInput);


    // r parameter
    public Input<RealParameter> removalProbability =
            new Input<RealParameter>("removalProbability", "The probability that an individual is removed from the process after the sampling", Input.Validate.REQUIRED);

    public Input<RealParameter> rhoProbability =
            new Input<RealParameter>("rho", "Probability of an individual to be sampled at present", (RealParameter)null);

    // if the tree likelihood is condition on sampling at least one individual then set to true one of the inputs:
    public Input<Boolean> conditionOnSamplingInput = new Input<Boolean>("conditionOnSampling", "the tree " +
            "likelihood is conditioned on sampling at least one individual", false);
    public Input<Boolean> conditionOnRhoSamplingInput = new Input<Boolean>("conditionOnRhoSampling", "the tree " +
            "likelihood is conditioned on sampling at least one individual in present", false);

    public Input<Boolean> conditionOnRootInput = new Input<Boolean>("conditionOnRoot", "the tree " +
            "likelihood is conditioned on the root height otherwise on the time of origin", false);


    protected double r;
    protected double lambda;
    protected double mu;
    protected double psi;
    protected double c1;
    protected double c2;
    protected double origin;
    protected double rho;
    protected boolean transform; //is true if the model is parametrised through transformed parameters
    private boolean lambdaExceedsMu = false;

    public void initAndValidate() throws Exception {

        if (originInput.get() == null && !conditionOnRootInput.get()) {
            throw new RuntimeException("Either specify origin input or set conditionOnRoot input to \"true\"");
        }

        if (originInput.get() != null && conditionOnRootInput.get()){
            throw new RuntimeException("Either don't specify origin input or set conditionOnRoot input to \"false\"");
        }


        if (conditionOnSamplingInput.get() && conditionOnRhoSamplingInput.get()){
            throw new RuntimeException("Either set to \"true\" only one of conditionOnSampling and conditionOnRhoSampling inputs or don't specify both!");
        }

        if (birthRateInput.get() != null && deathRateInput.get() != null && samplingRateInput.get() != null) {

            transform = false;
            //mu = deathRateInput.get().getValue();
            //psi = samplingRateInput.get().getValue();
            //lambda = birthRateInput.get().getValue();

        } else if (diversificationRateInput.get() != null && turnoverInput.get() != null && samplingProportionInput.get() != null) {

            transform = true;

        } else {
            throw new RuntimeException("Either specify birthRate, deathRate and samplingRate OR specify diversificationRate, turnover and samplingProportion!");
        }

        if (treeInput.get() instanceof ZeroBranchSATreeParser && originInput.get() != null && originInput.get().getValue() < treeInput.get().getRoot().getHeight()){
            throw new RuntimeException("Initial value of origin should be greater than initial root height");

        }


//        r = becomeNoninfectiousAfterSamplingProbability.get().getValue();
//        if (rhoProbability.get() != null ) {
//            rho = rhoProbability.get().getValue();
//        } else {
//            rho = 0.;
//        }
//        c1 = Math.sqrt((lambda - mu - psi) * (lambda - mu - psi) + 4 * lambda * psi);
//        c2 = -(lambda - mu - 2*lambda*rho - psi) / c1;
//        origin = originInput.get().getValue();
    }

    private double p0s(double t, double c1, double c2) {
        double p0 = (lambda + mu + psi - c1 * ((1 + c2) - Math.exp(-c1 * t) * (1 - c2)) / ((1 + c2) + Math.exp(-c1 * t) * (1 - c2))) / (2 * lambda);
        return r + (1 - r) * p0;
    }

    private double oneMinusP0(double t, double c1, double c2) {
        return 1 - (lambda + mu + psi - c1 * ((1 + c2) - Math.exp(-c1 * t) * (1 - c2)) / ((1 + c2) + Math.exp(-c1 * t) * (1 - c2))) / (2 * lambda);
    }

    private double oneMinusP0Hat(double t, double c1, double c2) {
        return rho*(lambda-mu)/(lambda*rho + (lambda*(1-rho) - mu)* Math.exp((mu-lambda) * t)) ;
    }

    private double pS(double t) {
        return psi*Math.exp((lambda - mu - psi*r) * t);
    }

    private double q(double t, double c1, double c2) {
        return Math.exp(c1 * t) * (1 + c2) * (1 + c2) + Math.exp(-c1 * t) * (1 - c2) * (1 - c2) + 2 * (1 - c2 * c2);
    }

    private void transformParameters() {
        double d = diversificationRateInput.get().getValue();
        double r_turnover = turnoverInput.get().getValue();
        double s = samplingProportionInput.get().getValue();
        lambda = d/(1-r_turnover);
        mu = r_turnover*lambda;
        psi = mu*s/(1-s);
    }

    private void updateParameters() {

        if (transform) {
            transformParameters();
        } else {
            lambda = birthRateInput.get().getValue();
            mu = deathRateInput.get().getValue();
            psi = samplingRateInput.get().getValue();
        }

        r = removalProbability.get().getValue();
        if (rhoProbability.get() != null ) {
            rho = rhoProbability.get().getValue();
        } else {
            rho = 0.;
        }
        c1 = Math.sqrt((lambda - mu - psi) * (lambda - mu - psi) + 4 * lambda * psi);
        c2 = -(lambda - mu - 2*lambda*rho - psi) / c1;
        if (originInput.get() != null){
            origin = originInput.get().getValue();
        }  else {
            origin = Double.POSITIVE_INFINITY;
        }
    }

    @Override
    public double calculateTreeLogLikelihood(TreeInterface tree)
    {
        int nodeCount = tree.getNodeCount();
        updateParameters();
        if (lambdaExceedsMu && lambda <= mu) {
            return Double.NEGATIVE_INFINITY;
        }
        //double x0 = tree.getRoot().getHeight() + origToRootDistance;
        double x0 = origin;
        double x1=tree.getRoot().getHeight();

        if (x0 < x1 || r==1 && ((Tree)tree).getDirectAncestorNodeCount() > 0) {
            return Double.NEGATIVE_INFINITY;
        }

        double logPost;
        if (!conditionOnRootInput.get()){
            logPost = -Math.log(q(x0, c1, c2));
        } else {
            if (tree.getRoot().isFake()){   //when conditioning on the root we assume the process
                //starts at the time of the first branching event and
                //that means that the root can not be a sampled ancestor
                return Double.NEGATIVE_INFINITY;
            } else {
                logPost = -Math.log(q(x1, c1, c2));
            }
        }

        if (conditionOnSamplingInput.get()) {
            logPost -= Math.log(oneMinusP0(x0, c1, c2));
        }

        if (conditionOnRhoSamplingInput.get()) {
            if (conditionOnRootInput.get()) {
                logPost -= Math.log(lambda*oneMinusP0Hat(x1, c1, c2)* oneMinusP0Hat(x1, c1, c2));
            }  else {
                logPost -= Math.log(oneMinusP0Hat(x0, c1, c2));
            }
        }

        int internalNodeCount = tree.getLeafNodeCount() - ((Tree)tree).getDirectAncestorNodeCount() - 1;

        logPost += internalNodeCount*Math.log(2);

        for (int i = 0; i < nodeCount; i++) {
            if (tree.getNode(i).isLeaf()) {
                if  (!tree.getNode(i).isDirectAncestor())  {
                    if (tree.getNode(i).getHeight() > 0.000000000005 || rho == 0.) {
                        logPost += Math.log(psi) + Math.log(q(tree.getNode(i).getHeight(), c1, c2)) + Math.log(p0s(tree.getNode(i).getHeight(), c1, c2));
                    } else {
                        logPost += Math.log(4*rho);
                    }
                }
            } else {
                if (tree.getNode(i).isFake()) {
                    if (r == 1) {
                        System.out.println("r = 1 but there are sampled ancestors in the tree");
                        System.exit(0);
                    }
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

