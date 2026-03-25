package sa.evolution.speciation;

import beast.base.core.Input;
import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.domain.PositiveReal;
import beast.base.spec.type.RealScalar;

/**
 * Created by alexei on 7/09/15.
 */
public class RateParameterization extends SABDParameterization {

    //'direct' parameters
    public Input<RealScalar<? extends PositiveReal>> originInput =
            new Input<>("origin", "The time when the process started", Input.Validate.REQUIRED);
    public Input<RealScalar<? extends PositiveReal>> birthRateInput =
            new Input<>("birthRate", "Birth rate", Input.Validate.REQUIRED);
    public Input<RealScalar<? extends NonNegativeReal>> deathRateInput =
            new Input<>("deathRate", "Death rate", Input.Validate.REQUIRED);
    public Input<RealScalar<? extends NonNegativeReal>> samplingRateInput =
            new Input<>("samplingRate", "Sampling rate per individual", Input.Validate.REQUIRED);

    public double mu() {
        return deathRateInput.get().get();
    }
    public double lambda() {
        return birthRateInput.get().get();
    }
    public double psi() {
        return samplingRateInput.get().get();
    }
    public double origin() {
        return originInput.get().get();
    }

    @Override
    public void initAndValidate() {}
}
