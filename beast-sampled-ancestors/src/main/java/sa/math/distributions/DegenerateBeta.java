package sa.math.distributions;


import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;
import org.apache.commons.statistics.distribution.BetaDistribution;
import org.apache.commons.statistics.distribution.ContinuousDistribution;

import beast.base.inference.distribution.ParametricDistribution;
import org.apache.commons.rng.UniformRandomProvider;

/**
 * @author Alexandra Gavryushkina
 */
public class DegenerateBeta extends ParametricDistribution {

    public Input<RealParameter> alphaInput = new Input<RealParameter>("alpha", "first shape parameter, defaults to 1", Input.Validate.REQUIRED);
    public Input<RealParameter> betaInput = new Input<RealParameter>("beta", "the other shape parameter, defaults to 1", Input.Validate.REQUIRED);
    public Input<Double> massInput = new Input<Double>("mass", "probability mass to put on a point", 0.5);
    public Input<Double> pointInput = new Input<Double>("point", "the point on which the probability mass is put", 1.0);

    DegenerateBetaImpl distr = new DegenerateBetaImpl();
    BetaDistribution betaDistr = BetaDistribution.of(1, 1);

    double alpha, beta, point, mass;

    @Override
    public void initAndValidate() {
        alpha = alphaInput.get().getValue();
        beta = betaInput.get().getValue();
        point = pointInput.get();
        if (point < 0 || 1 < point) {
            throw new IllegalArgumentException("Point should be between 0 and 1 (inclusive)");
        }
        mass = massInput.get();
        if (mass <= 0 || mass >= 1) {
            throw new IllegalArgumentException("Mass value should be between 0 and 1");
        }
        betaDistr = BetaDistribution.of(alpha, beta);
        distr.setParameters(point, mass);
    }

    class DegenerateBetaImpl implements ContinuousDistribution {
        private double point;
        private double mass;

        public void setParameters(double point, double mass) {
            this.point = point;
            this.mass = mass;
        }

        @Override
        public double cumulativeProbability(double x) {
            if (x < point) {
                return (1 - mass) * betaDistr.cumulativeProbability(x);
            } else {
                return mass + (1 - mass) * betaDistr.cumulativeProbability(x);
            }
        }

        @Override
        public double inverseCumulativeProbability(double p) {
            return 0.0;      //TODO derive and implement formula for inverse cumulative probability
        }

        @Override
        public double density(double x) {
            if (x >= 0 && x <= 1 && x != point) {
                return (1 - mass) * betaDistr.density(x);
            } else if (x == point) {
                return mass;
            } else return 0;
        }

        @Override
        public double logDensity(double x) {
            return Math.log(density(x));
        }

        @Override public double getMean() { return betaDistr.getMean(); }
        @Override public double getVariance() { return betaDistr.getVariance(); }
        @Override public double getSupportLowerBound() { return 0; }
        @Override public double getSupportUpperBound() { return 1; }
        @Override public ContinuousDistribution.Sampler createSampler(UniformRandomProvider rng) {
            return betaDistr.createSampler(rng);
        }
    } // class DegenerateBetaImpl

    @Override
    public Object getDistribution() {
        return distr;
    }

    @Override
    public double density(double x) {
        return distr.density(x);
    }
}
