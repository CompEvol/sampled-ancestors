package sa.math.distributions;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.spec.domain.Real;
import beast.base.spec.inference.distribution.ScalarDistribution;
import beast.base.spec.type.RealScalar;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.statistics.distribution.ContinuousDistribution;

import java.util.List;

/**
 * Uniform distribution on [lower, upper] with an additional point mass at
 * {@code point}. Density is {@code mass} at the point and a flat density
 * elsewhere, scaled so the total measure integrates to 1.
 *
 * @author Alexandra Gavryushkina
 */
@Description("Uniform on [lower, upper] with a probability point mass at `point`. "
        + "Density is mass at the point and (1 - mass) / (upper - lower) elsewhere.")
public class DegenerateUniform extends ScalarDistribution<RealScalar<Real>, Double> {

    public final Input<Double> lowerInput = new Input<>(
            "lower", "lower bound on the interval, default 0", 0.0);
    public final Input<Double> upperInput = new Input<>(
            "upper", "upper bound on the interval, default 1", 1.0);
    public final Input<Double> massInput  = new Input<>(
            "mass", "probability mass to put on a point", 0.5);
    public final Input<Double> pointInput = new Input<>(
            "point", "the point on which the probability mass is put", 1.0);

    private DegenerateUniformImpl dist = new DegenerateUniformImpl(0.0, 1.0, 1.0, 0.5);
    private ContinuousDistribution.Sampler sampler;

    public DegenerateUniform() {}

    @Override
    public void initAndValidate() {
        refresh();
        super.initAndValidate();
    }

    @Override
    public void refresh() {
        double lower = lowerInput.get();
        double upper = upperInput.get();
        double point = pointInput.get();
        double mass  = massInput.get();

        if (lower >= upper || point < lower || point > upper) {
            throw new IllegalArgumentException(
                    "Need lower < upper and point in [lower, upper], got "
                            + "lower=" + lower + ", upper=" + upper + ", point=" + point);
        }
        if (mass <= 0.0 || mass >= 1.0) {
            throw new IllegalArgumentException("mass must be in (0, 1), got " + mass);
        }

        if (isNotEqual(dist.lower, lower)
                || isNotEqual(dist.upper, upper)
                || isNotEqual(dist.point, point)
                || isNotEqual(dist.mass, mass)) {
            dist = new DegenerateUniformImpl(lower, upper, point, mass);
            sampler = null;
        }
    }

    @Override
    public double calculateLogP() {
        logP = getApacheDistribution().logDensity(param.get());
        return logP;
    }

    @Override
    public List<Double> sample() {
        if (sampler == null) {
            sampler = dist.createSampler(rng);
        }
        return List.of(sampler.sample());
    }

    @Override
    protected DegenerateUniformImpl getApacheDistribution() {
        refresh();
        return dist;
    }

    static final class DegenerateUniformImpl implements ContinuousDistribution {

        final double lower;
        final double upper;
        final double point;
        final double mass;
        final double flatDensity;

        DegenerateUniformImpl(double lower, double upper, double point, double mass) {
            this.lower = lower;
            this.upper = upper;
            this.point = point;
            this.mass = mass;
            this.flatDensity = (1.0 - mass) / (upper - lower);
        }

        @Override
        public double density(double x) {
            if (x == point) return mass;
            if (x >= lower && x <= upper) return flatDensity;
            return 0.0;
        }

        @Override
        public double logDensity(double x) {
            return Math.log(density(x));
        }

        @Override
        public double cumulativeProbability(double x) {
            if (x < point) {
                double clamped = Math.max(x, lower);
                return (1.0 - mass) * (clamped - lower) / (upper - lower);
            }
            double clamped = Math.min(x, upper);
            return mass + (1.0 - mass) * (clamped - lower) / (upper - lower);
        }

        @Override
        public double inverseCumulativeProbability(double p) {
            // TODO derive and implement formula for inverse CDF
            throw new UnsupportedOperationException("inverseCumulativeProbability not implemented for DegenerateUniform");
        }

        @Override
        public double getMean() {
            return mass * point + (1.0 - mass) * 0.5 * (lower + upper);
        }

        @Override
        public double getVariance() {
            double m = getMean();
            double range = upper - lower;
            double uniformEx2 = (lower * lower + lower * upper + upper * upper) / 3.0;
            double ex2 = mass * point * point + (1.0 - mass) * uniformEx2;
            return ex2 - m * m;
        }

        @Override public double getSupportLowerBound() { return lower; }
        @Override public double getSupportUpperBound() { return upper; }

        @Override
        public Sampler createSampler(UniformRandomProvider rng) {
            return () -> rng.nextDouble() < mass
                    ? point
                    : lower + rng.nextDouble() * (upper - lower);
        }
    }
}
