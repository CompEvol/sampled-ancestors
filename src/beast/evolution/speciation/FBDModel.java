package beast.evolution.speciation;

import beast.core.Input;
import beast.core.parameter.RealParameter;
import beast.evolution.tree.TreeInterface;
import beast.evolution.tree.ZeroBranchSANode;
import beast.evolution.tree.ZeroBranchSATree;

/**
 * Created by Gavra on 28/01/14.
 */
public class FBDModel extends SpeciesTreeDistribution {

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


    public Input<RealParameter> rhoInput =
            new Input<RealParameter>("rho", "Probability of an individual to be sampled at present", (RealParameter)null);

    protected double lambda;
    protected double mu;
    protected double psi;
    protected double rho;
    protected double c1;
    protected double c2;
    protected boolean transform; //is true if the model is parametrised through transformed parameters

    public void initAndValidate() throws Exception {

        if (birthRateInput.get() != null && deathRateInput.get() != null && samplingRateInput.get() != null) {

            transform = false;

        } else if (diversificationRateInput.get() != null && turnoverInput.get() != null && samplingProportionInput.get() != null) {

            transform = true;

        } else {
            throw new RuntimeException("Either specify birthRate, deathRate and samplingRate OR specify diversificationRate, turnover and samplingProportion!");
        }

    }

    private void transformParameters() {
        double d = diversificationRateInput.get().getValue();
        double r_turnover = turnoverInput.get().getValue();
        double s = samplingProportionInput.get().getValue();
        lambda = d/(1-r_turnover);
        mu = r_turnover * d/(1-r_turnover);
        psi =  (s/(1-s)) *  r_turnover * d/(1-r_turnover);
    }

    private void updateParameters() {

        if (transform) {
            transformParameters();
        } else {
            lambda = birthRateInput.get().getValue();
            mu = deathRateInput.get().getValue();
            psi = samplingRateInput.get().getValue();
        }

        rho = rhoInput.get().getValue();
        c1 =  Math.abs(Math.sqrt((lambda-mu-psi)*(lambda-mu-psi)+4*lambda*psi));
        c2 = -(lambda- mu - 2 * lambda*rho - psi)/c1;
    }

    private double q(double t){
        return (1+c2)*(1+c2)*Math.exp(c1*t) + 2*(1-c2*c2) + (1-c2)*(1-c2)*Math.exp(-c1*t);
    }

    private double p0(double t){
        return 1 + (-(lambda-mu-psi)- c1*(1+c2 - (1-c2)*Math.exp(-c1*t))/(1+c2 + (1-c2)*Math.exp(-c1*t)))/(2*lambda);
    }

    private double oneMinusP0Hat(double t){
        return rho*(lambda - mu)/(lambda*rho + (lambda*(1-rho) - mu)*Math.exp(-(lambda-mu)*t));
    }

    @Override
    public double calculateTreeLogLikelihood(TreeInterface tree){

        if (tree.getRoot().getHeight() > 10000) {
            return Double.NEGATIVE_INFINITY;
        }

        double logPost=0.0;

        updateParameters();

        logPost -= 2*Math.log(lambda*oneMinusP0Hat(tree.getRoot().getHeight()));
        logPost += Math.log(lambda) - Math.log(q(tree.getRoot().getHeight()));

        for (int i=0; i< tree.getNodeCount(); i++) {
            ZeroBranchSANode node = (ZeroBranchSANode)tree.getNode(i);
            if (node.isLeaf()){
                 if (node.getHeight() > 0.000000000001) {
                     if (node.isDirectAncestor()){
                         logPost += Math.log(psi);
                     }  else {
                         logPost += Math.log(psi*2*p0(node.getHeight())*q(node.getHeight()));
                     }
                 }  else {
                      logPost += Math.log(4*rho);
                 }
            }  else {
                 if (!node.isFake()) {
                     logPost += Math.log(lambda) - Math.log(q(node.getHeight()));
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
