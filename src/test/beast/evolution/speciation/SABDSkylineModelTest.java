package test.beast.evolution.speciation;

import beast.core.parameter.RealParameter;
import beast.evolution.speciation.SABDSkylineModel;
import beast.evolution.tree.Tree;
import beast.util.TreeParser;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * @author Alexandra Gavryushkina
 */
public class SABDSkylineModelTest  extends TestCase {

    @Test
    public void testLikelihoodCalculation1() throws Exception {        //TODO make this test actually test something
        SABDSkylineModel model = new SABDSkylineModel();
        Tree tree = new TreeParser("((1:1.0)2:1.0)3:0.0", false, true, false, 1);

        model.setInputValue("tree", tree);
        model.setInputValue("orig_root", new RealParameter("1."));
        model.setInputValue("R0", new RealParameter("2."));
        model.setInputValue("becomeUninfectiousRate", new RealParameter("0.4"));
        model.setInputValue("samplingProportion", new RealParameter("0. 0.4"));
        model.setInputValue("reverseTimeArrays", "false false true false");

        model.setInputValue("becomeNoninfectiousAfterSamplingProbability", new RealParameter("0.0") );
        model.setInputValue("samplingRateChangeTimes", new RealParameter("3.6 0."));
        model.initAndValidate();

        System.out.println(model.calculateTreeLogLikelihood(tree));

    }

}
