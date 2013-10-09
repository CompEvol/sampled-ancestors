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
        model.setInputValue("orig_root", new RealParameter("8."));
        model.setInputValue("birthRate", new RealParameter("2."));
        model.setInputValue("deathRate", new RealParameter("0.99"));
        model.setInputValue("samplingRate", new RealParameter("0.5") );
        model.setInputValue("becomeNoninfectiousAfterSamplingProbability", new RealParameter("0.9") );
        model.initAndValidate();

        assertEquals(-25.3707, model.calculateTreeLogLikelihood(tree), 1e-5);
    }


    @Test
    public void testLikelihoodCalculation2() throws Exception {        //TODO make this test actually test something
        SABDSamplingThroughTimeModel model = new SABDSamplingThroughTimeModel();
        Tree tree = new ZeroBranchSATreeParser("(1:1.0,2:1.0)3:0.0", true, false, 1);

        model.setInputValue("tree", tree);
        model.setInputValue("orig_root", new RealParameter("1."));
        model.setInputValue("birthRate", new RealParameter("2."));
        model.setInputValue("deathRate", new RealParameter("0.99"));
        model.setInputValue("samplingRate", new RealParameter("0.5") );
        model.setInputValue("becomeNoninfectiousAfterSamplingProbability", new RealParameter("0.0") );

        model.initAndValidate();

        System.out.println(model.calculateTreeLogLikelihood(tree));

    }
}
