package test.beast.evolution.speciation;

import beast.core.parameter.RealParameter;
import beast.evolution.speciation.BDSamplingThroughTimeSampledAncestorModel;
import beast.evolution.tree.Tree;
import beast.util.SampledAncestorTreeParser;
import junit.framework.TestCase;

import beast.core.Description;

import org.junit.Test;

import java.io.PrintStream;

/**
 * @author Alexandra Gavryushkina
 */

public class BDSamplingThroughTimeDirectAncestorModelTest extends TestCase {

    @Test
    public void testLikelihoodCalculation1() throws Exception {
        BDSamplingThroughTimeSampledAncestorModel model = new BDSamplingThroughTimeSampledAncestorModel();
        Tree tree = new SampledAncestorTreeParser(null, "3(2(1:1.0):1.0):0.0", 1);

        model.setInputValue("tree", tree);
        model.setInputValue("orig_root", new RealParameter("10."));
        model.setInputValue("birthRate", new RealParameter("2."));
        model.setInputValue("deathRate", new RealParameter("0.99"));
        model.setInputValue("samplingRate", new RealParameter("0.5") );
        model.setInputValue("becomeNoninfectiousAfterSamplingProbability", new RealParameter("0.9") );
        model.initAndValidate();

        assertEquals(-25.3707, model.calculateTreeLogLikelihood(tree), 1e-5);

    }



}
