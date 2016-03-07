package beast.evolution.tree;

import beast.core.BEASTObject;
import beast.core.Input;
import beast.evolution.alignment.Taxon;
import beast.math.distributions.ParametricDistribution;
import beast.util.Randomizer;

/**
 * @author Alexandra Gavryushkina
 */
public class SamplingDate extends BEASTObject {
    public final Input<Taxon> taxonInput = new Input<Taxon>("taxon", "Name of the taxon for which the sampling date distribution is specified.",
            Input.Validate.REQUIRED);

    public Input<String> upperInput = new Input<>("upper", "Upper bound for the taxon sampling date");
    public Input<String> lowerInput = new Input<>("lower", "Lower bound for the taxon sampling date");

    private double upper, lower;

    public void initAndValidate() {
        upper=Double.parseDouble(upperInput.get());
        lower=Double.parseDouble(lowerInput.get());
        if (upper < 0 || lower < 0 || upper < lower) {
            throw new IllegalArgumentException("Upper and lower inputs of samplingDate should be both positive and upper should be greater than lower.");
        }
    }

    public double getUpper() {
        return upper;
    }

    public double getLower() {
        return lower;
    }

    public double getRandomFromTheRange() {
        return lower + Randomizer.nextDouble()*(upper - lower);
    }

}
