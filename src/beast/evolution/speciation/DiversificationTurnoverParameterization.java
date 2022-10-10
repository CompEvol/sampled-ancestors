package beast.evolution.speciation;

import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;

/**
 * Created by alexei on 7/09/15.
 */
public class DiversificationTurnoverParameterization extends SABDParameterization {

    public Input<RealParameter> originInput =
            new Input<RealParameter>("origin", "The time when the process started", Input.Validate.REQUIRED);
    public Input<RealParameter> diversificationRateInput =
            new Input<RealParameter>("diversificationRate", "Net diversification rate. Birth rate - death rate", Input.Validate.REQUIRED);
    public Input<RealParameter> turnoverInput =
            new Input<RealParameter>("turnover", "Turnover. Death rate/birth rate", Input.Validate.REQUIRED);
    public Input<RealParameter> samplingProportionInput =
            new Input<RealParameter>("samplingProportion", "The probability of sampling prior to death. Sampling rate/(sampling rate + death rate)", Input.Validate.REQUIRED);

    public double mu() {

        return turnover()*lambda();
    }

    public double lambda() {

        return d()/(1.0-turnover());
    }

    public double psi() {

        return mu()*s()/(1.0-s());
    }

    public double origin() {
        return originInput.get().getValue();
    }

    public double turnover() {
        return turnoverInput.get().getValue();
    }

    public double d() {
        return diversificationRateInput.get().getValue();
    }

    public double s() {
        return samplingProportionInput.get().getValue();
    }

    @Override
    public void initAndValidate() {}
}
