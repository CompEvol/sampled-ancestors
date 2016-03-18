package beast.evolution.speciation;

import beast.core.Input;
import beast.core.parameter.RealParameter;

/**
 * Created by alexei on 7/09/15.
 */
public class DiversificationTurnoverPsiExpectedNParameterization extends SABDParameterization {

    public Input<RealParameter> expectedNInput =
            new Input<RealParameter>("expectedN", "The expected number of species at the present", Input.Validate.REQUIRED);
    public Input<RealParameter> diversificationRateInput =
            new Input<RealParameter>("diversificationRate", "Net diversification rate. Birth rate - death rate", Input.Validate.REQUIRED);
    public Input<RealParameter> turnoverInput =
            new Input<RealParameter>("turnover", "Turnover. Death rate/birth rate", Input.Validate.REQUIRED);
    public Input<RealParameter> samplingRateInput =
            new Input<RealParameter>("samplingRate", "Sampling rate per individual", Input.Validate.REQUIRED);

    public double mu() {

        return turnover()*lambda();
    }
    public double lambda() {

        return d()/(1.0-turnover());
    }
    public double psi() {

        return samplingRateInput.get().getValue();
    }

    public double origin() {

        double N = expectedNInput.get().getValue();
        return Math.log((1.0 - turnover())*N + turnover())/d();
    }

    public double turnover() {
        return turnoverInput.get().getValue();
    }

    public double d() {
        return diversificationRateInput.get().getValue();
    }

    @Override
    public void initAndValidate() {}
}
