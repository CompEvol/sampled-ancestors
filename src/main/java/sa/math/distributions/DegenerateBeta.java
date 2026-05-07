package sa.math.distributions;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.spec.domain.PositiveReal;
import beast.base.spec.domain.UnitInterval;
import beast.base.spec.inference.distribution.ScalarDistribution;
import beast.base.spec.type.RealScalar;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.statistics.distribution.BetaDistribution;
import org.apache.commons.statistics.distribution.ContinuousDistribution;

import java.util.List;

/**
 * Beta distribution on [0,1] with an additional point mass at {@code point}.
 * Density is {@code mass} at the point and {@code (1 - mass) * Beta(alpha, beta)}
 * elsewhere, so the total measure integrates to 1.
 *
 * @author Alexandra Gavryushkina
 */
@Description("Beta(alpha, beta) on [0,1] with a probability point mass at `point`. "
        + "Density is mass at the point and (1 - mass) * Beta(alpha, beta) elsewhere.")
public class DegenerateBeta extends ScalarDistribution<RealScalar<UnitInterval>, Double> {

    public final Input<RealScalar<? extends PositiveReal>> alphaInput = new Input<>(
            "alpha", "first shape parameter, defaults to 1", Input.Validate.REQUIRED);
    public final Input<RealScalar<? extends PositiveReal>> betaInput = new Input<>(
            "beta", "the other shape parameter, defaults to 1", Input.Validate.REQUIRED);
    public final Input<Double> massInput = new Input<>(
            "mass", "probability mass to put on a point", 0.5);
    public final Input<Double> pointInput = new Input<>(
            "point", "the point on which the probability mass is put", 1.0);

    private DegenerateBetaImpl dist = new DegenerateBetaImpl(BetaDistribution.of(1, 1), 1.0, 0.5);
    private ContinuousDistribution.Sampler sampler;

    public DegenerateBeta() {}

    @Override
    public void initAndValidate() {
        refresh();
        super.initAndValidate();
    }

    @Override
    public void refresh() {
        double alpha = alphaInput.get().get();
        double beta  = betaInput.get().get();
        double point = pointInput.get();
        double mass  = massInput.get();

        if (point < 0.0 || point > 1.0) {
            throw new IllegalArgumentException("point must be in [0, 1], got " + point);
        }
        if (mass <= 0.0 || mass >= 1.0) {
            throw new IllegalArgumentException("mass must be in (0, 1), got " + mass);
        }

        if (isNotEqual(dist.beta.getAlpha(), alpha)
                || isNotEqual(dist.beta.getBeta(), beta)
                || isNotEqual(dist.point, point)
                || isNotEqual(dist.mass, mass)) {
            dist = new DegenerateBetaImpl(BetaDistribution.of(alpha, beta), point, mass);
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
    protected DegenerateBetaImpl getApacheDistribution() {
        refresh();
        return dist;
    }

    static final class DegenerateBetaImpl implements ContinuousDistribution {

        final BetaDistribution beta;
        final double point;
        final double mass;

        DegenerateBetaImpl(BetaDistribution beta, double point, double mass) {
            this.beta = beta;
            this.point = point;
            this.mass = mass;
        }

        @Override
        public double density(double x) {
            if (x == point) return mass;
            if (x >= 0.0 && x <= 1.0) return (1.0 - mass) * beta.density(x);
            return 0.0;
        }

        @Override
        public double logDensity(double x) {
            return Math.log(density(x));
        }

        @Override
        public double cumulativeProbability(double x) {
            if (x < point) return (1.0 - mass) * beta.cumulativeProbability(x);
            return mass + (1.0 - mass) * beta.cumulativeProbability(x);
        }

        @Override
        public double inverseCumulativeProbability(double p) {
            // TODO derive and implement formula for inverse CDF
            throw new UnsupportedOperationException("inverseCumulativeProbability not implemented for DegenerateBeta");
        }

        @Override
        public double getMean() {
            return mass * point + (1.0 - mass) * beta.getMean();
        }

        @Override
        public double getVariance() {
            double m = getMean();
            double exVar = beta.getVariance() + beta.getMean() * beta.getMean();
            double ex2 = mass * point * point + (1.0 - mass) * exVar;
            return ex2 - m * m;
        }

        @Override public double getSupportLowerBound() { return 0.0; }
        @Override public double getSupportUpperBound() { return 1.0; }

        @Override
        public Sampler createSampler(UniformRandomProvider rng) {
            final Sampler betaSampler = beta.createSampler(rng);
            return () -> rng.nextDouble() < mass ? point : betaSampler.sample();
        }
    }
}
