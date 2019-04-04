package beast.evolution.speciation;

import beast.core.Input;
import beast.core.parameter.RealParameter;

/**
 * Created by alexei on 7/09/15.
 */
public class RateParameterization extends SABDParameterization {

    //'direct' parameters
    public Input<RealParameter> originInput =
            new Input<RealParameter>("origin", "The time when the process started", Input.Validate.REQUIRED);
    public Input<RealParameter> birthRateInput =
            new Input<RealParameter>("birthRate", "Birth rate", Input.Validate.REQUIRED);
    public Input<RealParameter> deathRateInput =
            new Input<RealParameter>("deathRate", "Death rate", Input.Validate.REQUIRED);
    public Input<RealParameter> samplingRateInput =
            new Input<RealParameter>("samplingRate", "Sampling rate per individual", Input.Validate.REQUIRED);

    public double mu() {
        return deathRateInput.get().getValue();
    }
    public double lambda() {
        return birthRateInput.get().getValue();
    }
    public double psi() {
        return samplingRateInput.get().getValue();
    }
    public double origin() {
        return originInput.get().getValue();
    }

    @Override
    public void initAndValidate() {}
}
