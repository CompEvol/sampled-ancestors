package test.beast.evolution.speciation;

import beast.core.parameter.RealParameter;
import beast.evolution.speciation.SABDSkylineModel;
import beast.evolution.tree.Tree;
import beast.evolution.tree.ZeroBranchSATree;
import beast.util.TreeParser;
import beast.util.ZeroBranchSATreeParser;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Alexandra Gavryushkina
 */
public class SABDSkylineModelTest  extends TestCase {

    @Test
    public void testLikelihoodCalculation1() throws Exception {     //TODO make a test for 3 intervals
        ArrayList<String> taxa1 = new ArrayList<String>(Arrays.asList("1", "2", "3"));
        ZeroBranchSATree tree1 = new ZeroBranchSATreeParser(taxa1, "((1:1.0)2:1.0)3:0.0", 1);

        SABDSkylineModel model = new SABDSkylineModel();
        model.setInputValue("tree", tree1);
        model.setInputValue("origin", new RealParameter("3."));
        model.setInputValue("birthRate", new RealParameter("1.2 1."));
        model.setInputValue("deathRate", new RealParameter("0.4 0.3"));
        model.setInputValue("samplingRate", new RealParameter("0.1 0.4"));
        model.setInputValue("removalProbability", new RealParameter("0.8 0.7"));
        model.setInputValue("rho", new RealParameter("0.1 0.4"));
        model.setInputValue("reverseTimeArrays", "true true true true true");
        model.setInputValue("birthRateChangeTimes", new RealParameter("1. 0."));
        model.setInputValue("deathRateChangeTimes", new RealParameter("1. 0."));
        model.setInputValue("samplingRateChangeTimes", new RealParameter("1. 0."));
        model.setInputValue("rhoSamplingTimes", new RealParameter("1. 0."));
        model.setInputValue("removalProbabilityChangeTimes", new RealParameter("1. 0."));
        model.initAndValidate();

        assertEquals(-10.7246150935598, model.calculateTreeLogLikelihood(tree1), 1e-14);

        ArrayList<String> taxa2 = new ArrayList<String>(Arrays.asList("1", "2"));
        ZeroBranchSATree tree2 = new ZeroBranchSATreeParser(taxa2, "(1:2.0,2:1.0):0.0", 1);
        model.setInputValue("tree", tree2);

        assertEquals(-6.03094565272802, model.calculateTreeLogLikelihood(tree2), 1e-14);

    }


}
