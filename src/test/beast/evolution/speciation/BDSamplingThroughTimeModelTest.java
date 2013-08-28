package test.beast.evolution.speciation;

import beast.core.parameter.RealParameter;
import beast.evolution.speciation.BDSamplingThroughTimeModel;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;

import beast.util.TreeParser;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Alexandra Gavryushkina
 */

public class BDSamplingThroughTimeModelTest extends TestCase {

    @Test
    public void testLikelihoodCalculation1() throws Exception {
        BDSamplingThroughTimeModel model = new BDSamplingThroughTimeModel();
        Tree tree = new TreeParser("((1:1.0)2:1.0)3:0.0", false, true, false, 1);


        model.setInputValue("tree", tree);
        model.setInputValue("orig_root", new RealParameter("8."));
        model.setInputValue("birthRate", new RealParameter("2."));
        model.setInputValue("deathRate", new RealParameter("0.99"));
        model.setInputValue("samplingRate", new RealParameter("0.5") );
        model.setInputValue("becomeNoninfectiousAfterSamplingProbability", new RealParameter("0.9") );
        model.initAndValidate();

        assertEquals(-25.3707, model.calculateTreeLogLikelihood(tree), 1e-5);
    }

}
