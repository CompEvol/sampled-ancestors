package beast.evolution.speciation;

import beast.core.CalculationNode;

/**
 * Created by alexei on 7/09/15.
 */
public abstract class SABDParameterization extends CalculationNode {

    /**
     * @return the death rate per unit time.
     */
    public abstract double mu();

    /**
     * @return the birth rate per unit time.
     */
    public abstract  double lambda();

    /**
     * @return the sampling rate per unit time.
     */
    public abstract double psi();

    /**
     * @return the time of the origin of the process before the present.
     */
    public abstract double origin();
}
