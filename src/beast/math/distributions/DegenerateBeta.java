package beast.math.distributions;

import beast.core.Input;
import beast.core.parameter.RealParameter;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BetaDistributionImpl;
import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.Distribution;

/**
 * @author Alexandra Gavryushkina
 */
public class DegenerateBeta extends ParametricDistribution {

    public Input<RealParameter> alphaInput = new Input<RealParameter>("alpha", "first shape parameter, defaults to 1", Input.Validate.REQUIRED);
    public Input<RealParameter> betaInput = new Input<RealParameter>("beta", "the other shape parameter, defaults to 1", Input.Validate.REQUIRED);
    public Input<Double> massInput = new Input<Double>("mass", "probability mass to put on a point", 0.5);
    public Input<Double> pointInput = new Input<Double>("point", "the point on which the probability mass is put", 1.0);

    DegenerateBetaImpl distr  = new DegenerateBetaImpl();
    org.apache.commons.math.distribution.BetaDistribution betaDistr = new BetaDistributionImpl(1, 1);

    double alpha, beta, point, mass, density;

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
        betaDistr.setAlpha(alpha);
        betaDistr.setBeta(beta);
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
        public double cumulativeProbability(double x) throws MathException {

            if (x < point) {
                return (1- mass) * betaDistr.cumulativeProbability(x);
            }  else {
                return mass + (1-mass) * betaDistr.cumulativeProbability(x);
            }

        }

        @Override
        public double cumulativeProbability(double x0, double x1) throws MathException {
            return cumulativeProbability(x1) - cumulativeProbability(x0);
        }

        @Override
        public double inverseCumulativeProbability(double p) throws MathException {
            return 0.0;      //TODO derive and implement formula for inverse cumulative probability
        }

        @Override
        public double density(double x) {
            if (x >= 0 && x <= 1 && x != point) {
                return (1-mass) * betaDistr.density(x);
            } else if (x == point) {
                return mass;
            } else  return 0;
        }

        @Override
        public double logDensity(double x) {
            return Math.log(density(x));
        }
    } // class DegenerateBetaImpl

    @Override
    public Distribution getDistribution() {
        return distr;
    }

    @Override
    public double density(double x) {
        return distr.density(x);
    }
}
