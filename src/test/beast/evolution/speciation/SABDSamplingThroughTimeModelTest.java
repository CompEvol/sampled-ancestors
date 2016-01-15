package test.beast.evolution.speciation;

import beast.core.parameter.RealParameter;
import beast.evolution.speciation.BirthDeathSkylineModel;
import beast.evolution.speciation.SABDSamplingThroughTimeModel;
import beast.evolution.tree.Tree;

import beast.util.TreeParser;
import beast.util.ZeroBranchSATreeParser;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Alexandra Gavryushkina
 */

public class SABDSamplingThroughTimeModelTest extends TestCase {



    @Test
     public void testLikelihoodCalculationSimple() throws Exception {

        SABDSamplingThroughTimeModel bdssm =  new SABDSamplingThroughTimeModel();

         Tree tree = new ZeroBranchSATreeParser("((3 : 1.5, 4 : 0.5) : 1 , (1 : 2, 2 : 1) : 3);",false,true,0);
         bdssm.setInputValue("tree", tree);
         bdssm.setInputValue("origin", new RealParameter("10."));
//        bdssm.setInputValue("conditionOnSurvival", true);
        bdssm.setInputValue("removalProbability", "1");


        bdssm.setInputValue("birthRate", new RealParameter("2.25"));
        bdssm.setInputValue("deathRate", new RealParameter("1.05"));
        bdssm.setInputValue("samplingRate", new RealParameter("0.45") );

         bdssm.initAndValidate();

         assertEquals(-33.7573, bdssm.calculateTreeLogLikelihood(tree), 1e-4);
     }
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
//        model.setInputValue("conditionOnSampling", true);
        model.initAndValidate();

        // this value is calculated with Mathematica
        assertEquals(-22.08332, model.calculateTreeLogLikelihood(tree), 1e-5); // likelihood conditioning on at least one sampled individual

    }

    @Test
     public void testLikelihoodCalculationWithoutAncestors() throws Exception {
         BirthDeathSkylineModel model = new BirthDeathSkylineModel();
         Tree tree = new ZeroBranchSATreeParser("((3 : 1.5, 4 : 0.5) : 1 , (1 : 2, 2 : 1) : 3);",false);

         model.setInputValue("tree", tree);
         model.setInputValue("origin", new RealParameter("10."));
         model.setInputValue("R0", new RealParameter("1.5"));
         model.setInputValue("becomeUninfectiousRate", new RealParameter("1.5"));
         model.setInputValue("samplingProportion", new RealParameter("0.3") );
         model.setInputValue("removalProbability", new RealParameter("1") );
         model.setInputValue("conditionOnSurvival", true);
         model.initAndValidate();

         // likelihood conditioning on at least one sampled individual    - "true" result from BEAST 09 June 2015 (DK)
         assertEquals(-25.671303367076007, model.calculateTreeLogLikelihood(tree), 1e-5);

     }



}
