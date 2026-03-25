package sa.evolution.speciation;

import beast.base.core.Input;
import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.domain.PositiveReal;
import beast.base.spec.domain.Real;
import beast.base.spec.domain.UnitInterval;
import beast.base.spec.type.RealScalar;

/**
 * Created by alexei on 7/09/15.
 */
public class DiversificationTurnoverPsiExpectedNParameterization extends SABDParameterization {

    public Input<RealScalar<? extends PositiveReal>> expectedNInput =
            new Input<>("expectedN", "The expected number of species at the present", Input.Validate.REQUIRED);
    public Input<RealScalar<? extends Real>> diversificationRateInput =
            new Input<>("diversificationRate", "Net diversification rate. Birth rate - death rate", Input.Validate.REQUIRED);
    public Input<RealScalar<? extends UnitInterval>> turnoverInput =
            new Input<>("turnover", "Turnover. Death rate/birth rate", Input.Validate.REQUIRED);
    public Input<RealScalar<? extends NonNegativeReal>> samplingRateInput =
            new Input<>("samplingRate", "Sampling rate per individual", Input.Validate.REQUIRED);

    public double mu() {

        return turnover()*lambda();
    }
    public double lambda() {

        return d()/(1.0-turnover());
    }
    public double psi() {

        return samplingRateInput.get().get();
    }

    public double origin() {

        double N = expectedNInput.get().get();
        return Math.log((1.0 - turnover())*N + turnover())/d();
    }

    public double turnover() {
        return turnoverInput.get().get();
    }

    public double d() {
        return diversificationRateInput.get().get();
    }

    @Override
    public void initAndValidate() {}
}
