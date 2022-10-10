package beast.evolution.speciation;

        import beast.base.inference.Distribution;
        import beast.base.core.Input;
        import beast.base.inference.State;
        import beast.base.inference.parameter.IntegerParameter;
        import beast.base.inference.parameter.RealParameter;

        import java.util.List;
        import java.util.Random;

/**
 * @author Alexandra Gavryushkina
 */
public class ProbabilitySA extends Distribution {

    public final Input<IntegerParameter> SAInput = new Input<IntegerParameter>("SA", "A binary parameter that describe " +
            "the fact that a lineage has no sampled descendants (zero) or it has sampled descendants (one)", Input.Validate.REQUIRED);

    public Input<RealParameter> timeInput =
            new Input<RealParameter>("time", "The time of the lineage",Input.Validate.REQUIRED);

    public Input<RealParameter> originInput =
            new Input<RealParameter>("origin", "The time when the process started",(RealParameter)null);

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

    @Override
    public List<String> getArguments() {
        return null;
    }

    @Override
    public List<String> getConditions() {
        return null;
    }

    @Override
    public void sample(State state, Random random) {

    }

    public void initAndValidate() {

        if (originInput.get() == null && !conditionOnRootInput.get()) {
            throw new IllegalArgumentException("Either specify origin input or set conditionOnRoot input to \"true\"");
        }

        if (originInput.get() != null && conditionOnRootInput.get()){
            throw new IllegalArgumentException("Either don't specify origin input or set conditionOnRoot input to \"false\"");
        }


        if (conditionOnSamplingInput.get() && conditionOnRhoSamplingInput.get()){
            throw new IllegalArgumentException("Either set to \"true\" only one of conditionOnSampling and conditionOnRhoSampling inputs or don't specify both!");
        }

        if (birthRateInput.get() != null && deathRateInput.get() != null && samplingRateInput.get() != null) {

            transform = false;
            //mu = deathRateInput.get().getValue();
            //psi = samplingRateInput.get().getValue();
            //lambda = birthRateInput.get().getValue();

        } else if (diversificationRateInput.get() != null && turnoverInput.get() != null && samplingProportionInput.get() != null) {

            transform = true;

        } else {
            throw new IllegalArgumentException("Either specify birthRate, deathRate and samplingRate OR specify diversificationRate, turnover and samplingProportion!");
        }
    }


    private double oneMinusP0(double t, double c1, double c2) {
        return 1 - (lambda + mu + psi - c1 * ((1 + c2) - Math.exp(-c1 * t) * (1 - c2)) / ((1 + c2) + Math.exp(-c1 * t) * (1 - c2))) / (2 * lambda);
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
    public double calculateLogP()
    {
        updateParameters();

        if (lambdaExceedsMu && lambda <= mu) {
            return Double.NEGATIVE_INFINITY;
        }
        double t=timeInput.get().getValue();
        if (SAInput.get().getValue() == 0) {
            logP = Math.log(1 - oneMinusP0(t, c1, c2));
        } else {
            logP = Math.log(oneMinusP0(t, c1, c2));
        }

        return logP;
    }

    @Override
    protected boolean requiresRecalculation() {
        return true;
    }
}
