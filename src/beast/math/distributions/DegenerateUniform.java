package beast.math.distributions;

import beast.core.Input;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.Distribution;

/**
 * @author Alexandra Gavryushkina
 */
public class DegenerateUniform extends ParametricDistribution {

    public Input<Double> lowerInput = new Input<Double>("lower", "lower bound on the interval, defaul 0", 0.0);
    public Input<Double> upperInput = new Input<Double>("upper", "lower bound on the interval, defaul 1", 1.0);
    public Input<Double> massInput = new Input<Double>("mass", "probability mass to put on a point", 0.5);
    public Input<Double> pointInput = new Input<Double>("point", "the point on which the probability mass is put", 1.0);

    DegenerateUniformImpl distr = new DegenerateUniformImpl();

    double lower, upper, point, mass, density;

    @Override
    public void initAndValidate() {
        lower = lowerInput.get();
        upper = upperInput.get();
        point = pointInput.get();
        if (lower >= upper || point < lower || upper < point) {
            throw new IllegalArgumentException("Upper value should be higher than lower value and a mass point should be between lower and upper bound (inclusive)");
        }
        mass = massInput.get();
        if (mass <= 0 || mass >= 1) {
            throw new IllegalArgumentException("Mass value should be between 0 and 1");
        }
        distr.setParameters(lower, upper, point, mass);
        density = (1-mass)/(upper - lower);
    }

    class DegenerateUniformImpl implements ContinuousDistribution {
        private double lower;
        private double upper;
        private double point;
        private double mass;

        public void setParameters(double lower, double upper, double point, double mass) {
            this.lower = lower;
            this.upper = upper;
            this.point = point;
            this.mass = mass;
        }

        @Override
        public double cumulativeProbability(double x) throws MathException {

            if (x < point) {
                x = Math.max(x, lower);
                return (1- mass) * (x - lower) / (upper - lower);
            }  else {
                x = Math.min(x, upper);
                return mass + (1-mass) * (x - lower)/(upper - lower);
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
            if (x >= lower && x <= upper && x != point) {
                return density;
            } else if (x == point) {
                    return mass;
                } else  return 0;
        }

        @Override
        public double logDensity(double x) {
            return Math.log(density(x));
        }
    } // class DegenerateUniformImpl

    @Override
    public Distribution getDistribution() {
        return distr;
    }

    @Override
    public double density(double x) {
        return distr.density(x);
    }
}
