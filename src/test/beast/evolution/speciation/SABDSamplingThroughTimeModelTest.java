package test.beast.evolution.speciation;

import beast.core.parameter.RealParameter;
import beast.evolution.speciation.SABDSamplingThroughTimeModel;
import beast.evolution.tree.Tree;

import beast.util.ZeroBranchSATreeParser;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Alexandra Gavryushkina
 */

public class SABDSamplingThroughTimeModelTest extends TestCase {

    @Test
    public void testLikelihoodCalculation1() throws Exception {
        SABDSamplingThroughTimeModel model = new SABDSamplingThroughTimeModel();
        ZeroBranchSATreeParser tree = new ZeroBranchSATreeParser("((1:1.0)2:1.0)3:0.0", true, false, 1);

        model.setInputValue("tree", tree);
        model.setInputValue("origin", new RealParameter("10."));
        model.setInputValue("birthRate", new RealParameter("2."));
        model.setInputValue("deathRate", new RealParameter("0.99"));
        model.setInputValue("samplingRate", new RealParameter("0.5") );
        model.setInputValue("removalProbability", new RealParameter("0.9") );
        model.setInputValue("conditionOnSampling", true);
        model.initAndValidate();

        // these values ate calculated with Mathematica
        //assertEquals(-25.3707, model.calculateTreeLogLikelihood(tree), 1e-5); // likelihood conditioning only on parameters and origin time
        assertEquals(-24.92987, model.calculateTreeLogLikelihood(tree), 1e-5); // likelihood conditioning on at least one sampled individual

    }


    @Test
    public void testLikelihoodCalculation2() throws Exception {
        SABDSamplingThroughTimeModel model = new SABDSamplingThroughTimeModel();
        Tree tree = new ZeroBranchSATreeParser("((1:1.5,2:0.5):0.5)3:0.0", true, false, 1);

        model.setInputValue("tree", tree);
        model.setInputValue("origin", new RealParameter("10."));
        model.setInputValue("birthRate", new RealParameter("2."));
        model.setInputValue("deathRate", new RealParameter("0.99"));
        model.setInputValue("samplingRate", new RealParameter("0.5") );
        model.setInputValue("removalProbability", new RealParameter("0.9") );
        model.setInputValue("conditionOnSampling", true);
        model.initAndValidate();

        // this value is calculated with Mathematica
        assertEquals(-22.08332, model.calculateTreeLogLikelihood(tree), 1e-5); // likelihood conditioning on at least one sampled individual

    }
}
