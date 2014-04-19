package beast.evolution.speciation;

import beast.app.beauti.BeautiDoc;
import beast.core.Input;
import beast.core.parameter.BooleanParameter;
import beast.core.parameter.RealParameter;
import beast.evolution.tree.Tree;
import beast.evolution.tree.TreeInterface;
import beast.evolution.tree.ZeroBranchSANode;
import beast.evolution.tree.ZeroBranchSATree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexandra Gavryushkina
 */
public class SABDSkylineModel extends BirthDeathSkylineModel {

    public Input<RealParameter> removalProbabilityChangeTimesInput =
            new Input<RealParameter>("removalProbabilityChangeTimes", "The times t_i specifying when removal probability changes occur", (RealParameter) null);

    Input<Boolean> removalProbabilityChangeTimesRelativeInput =
            new Input<Boolean>("removalProbabilityTimesRelative", "True if removal probability change times specified relative to tree height? Default false", false);

    BooleanParameter bpForSA = new BooleanParameter();
    {
        try {
            bpForSA.initByName("value", "false false false false false");
        } catch (Exception e) { //ignore
        }
    }

    public Input<BooleanParameter> reverseTimeArrays =
            new Input<BooleanParameter>("reverseTimeArrays", "True if the time arrays are given in backwards time (from the present back to root). Order: 1) birth 2) death 3) sampling 4) rho 5) r. Default false." +
                    "Careful, rate array must still be given in FORWARD time (root to tips). If rhosamplingTimes given, they should be backwards and this should be true.",
                    bpForSA);

    public Input<RealParameter> removalProbability =
            new Input<RealParameter>("removalProbability", "The probability of an individual to become noninfectious immediately after the sampling", Input.Validate.REQUIRED);

    protected Double[] r;

    protected int rChanges;

    protected List<Double> rChangeTimes = new ArrayList<Double>();

    Boolean rTimesRelative = false;

    @Override
    public void initAndValidate() throws Exception {
        if (origin.get() != null && origin.get().getValue() < treeInput.get().getRoot().getHeight()){
            throw new RuntimeException("Initial value of origin should be greater than initial root height");
        }

        birth = null;
        death = null;
        psi = null;
        r = null;
        rho = null;
        birthRateChangeTimes.clear();
        deathRateChangeTimes.clear();
        samplingRateChangeTimes.clear();
        rChangeTimes.clear();
        totalIntervals = 0;

        m_forceRateChange = forceRateChange.get();
        birthRateTimesRelative = birthRateChangeTimesRelativeInput.get();
        deathRateTimesRelative = deathRateChangeTimesRelativeInput.get();
        samplingRateTimesRelative = samplingRateChangeTimesRelativeInput.get();
        rTimesRelative = removalProbabilityChangeTimesRelativeInput.get();

        contempData = contemp.get();
        rhoSamplingCount = 0;
        printTempResults = false;


        if (birthRate.get() != null && deathRate.get() != null && samplingRate.get() != null) {

            transform = false;
            death = deathRate.get().getValues();
            psi = samplingRate.get().getValues();
            birth = birthRate.get().getValues();

        } else if (R0.get() != null && becomeUninfectiousRate.get() != null && samplingProportion.get() != null) {

            transform = true;

        } else {
            throw new RuntimeException("Either specify birthRate, deathRate and samplingRate OR specify R0, becomeUninfectiousRate and samplingProportion!");
        }

        r = removalProbability.get().getValues();

        if (transform) {

            if (birthChanges < 1) birthChanges = R0.get().getDimension() - 1;
            samplingChanges = samplingProportion.get().getDimension() - 1;
            deathChanges = becomeUninfectiousRate.get().getDimension() - 1;

        } else {

            if (birthChanges < 1) birthChanges = birthRate.get().getDimension() - 1;
            deathChanges = deathRate.get().getDimension() - 1;
            samplingChanges = samplingRate.get().getDimension() - 1;
        }

        rChanges = removalProbability.get().getDimension() -1;

        collectTimes();

        if (m_rho.get() != null) {

            constantRho = !(m_rho.get().getDimension() > 1);

            if (m_rho.get().getDimension() == 1) {
                if (!contempData && ((samplingProportion.get() != null && samplingProportion.get().getDimension() == 1 && samplingProportion.get().getValue() == 0.) ||
                        (samplingRate.get() != null && samplingRate.get().getDimension() == 1 && samplingRate.get().getValue() == 0.))) {
                    contempData = true;
                    if (printTempResults)
                        System.out.println("Parameters were chosen for contemporaneously sampled data. Setting contemp=true.");
                }
            }

            if (contempData) {
                if (m_rho.get().getDimension() != 1)
                    throw new RuntimeException("when contemp=true, rho must have dimension 1");

                else {
                    rho = new Double[totalIntervals];
                    Arrays.fill(rho, 0.);
                    rho[totalIntervals - 1] = m_rho.get().getValue();
                    rhoSamplingCount = 1;
                }
            } else {

                rho = new Double[totalIntervals];

                RealParameter rhoSampling = rhoSamplingTimes.get();
                if (rhoSampling != null) {
                    for (int i = 0; i < rhoSampling.getDimension(); i++) {
                        rho[index(reverseTimeArrays.get().getValue(3) ? (times[totalIntervals - 1] - rhoSampling.getValue(rhoSampling.getDimension() - i - 1)) : rhoSampling.getValue(i))]
                                = m_rho.get().getValue(constantRho ? 0 : i);
                    }
                    rhoSamplingCount = rho.length;
                }
            }
        } else {
            rho = new Double[totalIntervals];
            Arrays.fill(rho, 0.);
        }
        isRhoTip = new boolean[treeInput.get().getLeafNodeCount()];

        printTempResults = false;

    }

    /**
     * Collect all the times of parameter value changes and rho-sampling events
     */
    private void collectTimes() {

        timesSet.clear();

        if (isBDSIR()) {
            birthChanges = getSIRdimension() - 1;
        }

        getChangeTimes(birthRateChangeTimes,
                birthRateChangeTimesInput.get() != null && !isSeasonalBDSIR() ? birthRateChangeTimesInput.get() : intervalTimes.get(),
                birthChanges, birthRateTimesRelative, reverseTimeArrays.get().getValue(0));

        getChangeTimes(deathRateChangeTimes,
                deathRateChangeTimesInput.get() != null ? deathRateChangeTimesInput.get() : intervalTimes.get(),
                deathChanges, deathRateTimesRelative, reverseTimeArrays.get().getValue(1));

        getChangeTimes(samplingRateChangeTimes,
                samplingRateChangeTimesInput.get() != null ? samplingRateChangeTimesInput.get() : intervalTimes.get(),
                samplingChanges, samplingRateTimesRelative, reverseTimeArrays.get().getValue(2));

        getChangeTimes(rChangeTimes,
                removalProbabilityChangeTimesInput.get() != null ? removalProbabilityChangeTimesInput.get() : intervalTimes.get(),
                rChanges, rTimesRelative, reverseTimeArrays.get().getValue(4));


        //eventsSet.clear();
        for (Double time : birthRateChangeTimes) {
            //eventsSet.add(new BDSEvent(BDSEvent.Type.birth,time));
            timesSet.add(time);
        }
        for (Double time : deathRateChangeTimes) {
            //eventsSet.add(new BDSEvent(BDSEvent.Type.death,time));
            timesSet.add(time);

        }
        for (Double time : samplingRateChangeTimes) {
            //eventsSet.add(new BDSEvent(BDSEvent.Type.sampling,time));
            timesSet.add(time);
        }

        for (Double time : rChangeTimes) {
            timesSet.add(time);
        }

        RealParameter rhoSampling = rhoSamplingTimes.get();
        if (rhoSampling != null) {

            double maxTime = origin.get().getValue();
            int dim = rhoSampling.getDimension();

            for (int i = 0; i < dim; i++) {
                //eventsSet.add(new BDSEvent(BDSEvent.Type.rhoSampling, rhoSampling.getValue(i)));
                timesSet.add(reverseTimeArrays.get().getValue(3) ? (maxTime - rhoSampling.getValue(dim - i - 1)) : rhoSampling.getValue(i));
            }
        }

        if (printTempResults) System.out.println("times = " + timesSet);

        times = timesSet.toArray(new Double[timesSet.size()]);
        totalIntervals = times.length;

        if (printTempResults) System.out.println("total intervals = " + totalIntervals);

    }

    @Override
    protected void transformParameters() {

        Double[] R = R0.get().getValues();
        Double[] b = becomeUninfectiousRate.get().getValues();
        Double[] p = samplingProportion.get().getValues();
        Double[] r = removalProbability.get().getValues();

        birth = new Double[totalIntervals];
        death = new Double[totalIntervals];
        psi = new Double[totalIntervals];

        for (int i = 0; i < totalIntervals; i++) {
            birth[i] = R[birthChanges > 0 ? index(times[i], birthRateChangeTimes) : 0] * b[deathChanges > 0 ? index(times[i], deathRateChangeTimes) : 0];
            psi[i] = (p[samplingChanges > 0 ? index(times[i], samplingRateChangeTimes) : 0] * b[deathChanges > 0 ? index(times[i], deathRateChangeTimes) : 0])/r[rChanges > 0 ? index(times[i], rChangeTimes) : 0];
            death[i] = b[deathChanges > 0 ? index(times[i], deathRateChangeTimes) : 0] - p[samplingChanges > 0 ? index(times[i], samplingRateChangeTimes) : 0] * b[deathChanges > 0 ? index(times[i], deathRateChangeTimes) : 0];
        }
    }


    @Override
    protected Double updateRatesAndTimes(TreeInterface tree) {

        collectTimes();

        t_root = tree.getRoot().getHeight();

        if (m_forceRateChange && timesSet.last() > origin.get().getValue()) {
            return Double.NEGATIVE_INFINITY;
        }

        if (transform)
            transformParameters();
        else {

            Double[] birthRates = birthRate.get().getValues();
            Double[] deathRates = deathRate.get().getValues();
            Double[] samplingRates = samplingRate.get().getValues();

            birth = new Double[totalIntervals];
            death = new Double[totalIntervals];
            psi = new Double[totalIntervals];

            birth[0] = birthRates[0];

            for (int i = 0; i < totalIntervals; i++) {
                if (!isBDSIR()) birth[i] = birthRates[index(times[i], birthRateChangeTimes)];
                death[i] = deathRates[index(times[i], deathRateChangeTimes)];
                psi[i] = samplingRates[index(times[i], samplingRateChangeTimes)];

                if (printTempResults) {
                    if (!isBDSIR()) System.out.println("birth[" + i + "]=" + birth[i]);
                    System.out.println("death[" + i + "]=" + death[i]);
                    System.out.println("psi[" + i + "]=" + psi[i]);
                }
            }
        }

        Double[] removalProbabilities = removalProbability.get().getValues();
        r =  new Double[totalIntervals];

        for (int i = 0; i < totalIntervals; i++) {
            r[i] = removalProbabilities[index(times[i], rChangeTimes)];
            if (printTempResults) {
                System.out.println("r[" + i + "]=" + r[i]);
            }
        }

        if (m_rho.get() != null && rhoSamplingTimes.get() != null) {

            Double[] rhos = m_rho.get().getValues();
            RealParameter rhoSampling = rhoSamplingTimes.get();

            for (int i = 0; i < totalIntervals; i++) {

                for (int j = 0; i < rhos.length; i++) {
                    if (times[i].equals(reverseTimeArrays.get().getValue(3) ? (origin.get().getValue() - rhoSampling.getValue(rhoSampling.getDimension() - j - 1)) : rhoSampling.getValue(j)))
                        rho[i] = rhos[j];
                }
            }
        }

        return 0.;

    }

    @Override
    public double calculateTreeLogLikelihood(TreeInterface tree) {

        //r = removalProbability.get().getValues();

        int nTips = tree.getLeafNodeCount();

        if (preCalculation((Tree)tree) < 0)
            return Double.NEGATIVE_INFINITY;


        // number of lineages at each time ti
        int[] n = new int[totalIntervals];

        double x0 = 0;
        int index = 0;

        double temp;

        // the first factor for origin
        if (!conditionOnSurvival.get())
            temp = Math.log(g(index, times[index], x0));  // NOT conditioned on at least one sampled individual
        else {
            temp = p0(index, times[index], x0);
            if (temp == 1)
                return Double.NEGATIVE_INFINITY;
            temp = Math.log(g(index, times[index], x0) / (1 - temp));   // DEFAULT: conditioned on at least one sampled individual
        }

        logP = temp;
        if (Double.isInfinite(logP))
            return logP;

        if (printTempResults) System.out.println("first factor for origin = " + temp);

        // first product term in f[T]
        for (int i = 0; i < tree.getInternalNodeCount(); i++) {
            double x = times[totalIntervals - 1] - tree.getNode(nTips + i).getHeight();
            index = index(x);
            if (!((ZeroBranchSANode)tree.getNode(nTips + i)).isFake()) {
                temp = Math.log(birth[index] * g(index, times[index], x));
                logP += temp;
                if (printTempResults) System.out.println("1st pwd" +
                        " = " + temp + "; interval = " + i);
                if (Double.isInfinite(logP)) {
                    return logP;
                }
            } else {
                if (r[index] != 1) {
                    logP += Math.log((1 - r[index])*psi[index]);
                    if (Double.isInfinite(logP)) {
                        return logP;
                    }
                } else {
                    //throw new Exception("There is a sampled ancestor in the tree while r parameter is 1");
                    System.out.println("There is a sampled ancestor in the tree while r parameter is 1");
                    System.exit(0);
                }
            }
        }

        // middle product term in f[T]
        for (int i = 0; i < nTips; i++) {
            if ((!isRhoTip[i] || m_rho.get() == null) && !((ZeroBranchSANode)tree.getNode(i)).isDirectAncestor()) {
                double y = times[totalIntervals - 1] - tree.getNode(i).getHeight();
                index = index(y);
                double t = 0;
                temp = Math.log(psi[index] * (r[index] + (1-r[index])*p0(index, times[index], y))) - Math.log(g(index, times[index], y));
                logP += temp;
                if (printTempResults) System.out.println("2nd PI = " + temp);
                if (Double.isInfinite(logP)){
                    return logP;
                }
            }
        }

        // last product term in f[T], factorizing from 1 to m
        double time;
        for (int j = 0; j < totalIntervals; j++) {
            time = j < 1 ? 0 : times[j - 1];
            n[j] = ((j == 0) ? 0 : lineageCountAtTime(times[totalIntervals - 1] - time, (Tree)tree));

            if (n[j] > 0) {
                temp = n[j] * (Math.log(g(j, times[j], time)) + Math.log(1-rho[j]));
                logP += temp;
                if (printTempResults)
                    System.out.println("3rd factor (nj loop) = " + temp + "; interval = " + j + "; n[j] = " + n[j]);//+ "; Math.log(g(j, times[j], time)) = " + Math.log(g(j, times[j], time)));
                if (Double.isInfinite(logP)) {
                    return logP;
                }
            }
            if (rho[j] > 0 && N[j] > 0) {
                temp = N[j] * Math.log(rho[j]);    // term for contemporaneous sampling
                logP += temp;
                if (printTempResults)
                    System.out.println("3rd factor (Nj loop) = " + temp + "; interval = " + j + "; N[j] = " + N[j]);
                if (Double.isInfinite(logP)){
                    return logP;
                }
            }
        }

        int internalNodeCount = tree.getLeafNodeCount() - ((ZeroBranchSATree)tree).getDirectAncestorNodeCount() - 1;

        logP +=  Math.log(Math.pow(2, internalNodeCount));

        return logP;
    }
    
    
}
