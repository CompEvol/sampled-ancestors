package test.beast.evolution.speciation;

import beast.core.parameter.RealParameter;
import beast.evolution.speciation.SABirthDeathModel;
import beast.evolution.tree.Tree;
import beast.util.ZeroBranchSATreeParser;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * @author Alexei Drummond
 */
public class SABirthDeathModelTest extends TestCase {

    @Test
    public void testLikelihoodCalculationSimple() throws Exception {

        SABirthDeathModel sabdm = new SABirthDeathModel();

        Tree tree = new ZeroBranchSATreeParser("((3 : 1.5, 4 : 0.5) : 1 , (1 : 2, 2 : 1) : 3);", false, true, 0);

        System.out.println(tree.toString());

        sabdm.setInputValue("tree", tree);
        sabdm.setInputValue("origin", new RealParameter("10."));
        sabdm.setInputValue("removalProbability", "1");
        sabdm.setInputValue("conditionOnSampling", true);


        sabdm.setInputValue("birthRate", new RealParameter("2.25"));
        sabdm.setInputValue("deathRate", new RealParameter("1.05"));
        sabdm.setInputValue("samplingRate", new RealParameter("0.45"));

        sabdm.initAndValidate();

        assertEquals(-33.7573, sabdm.calculateTreeLogLikelihood(tree), 1e-4);
    }

    @Test
    public void testLikelihoodCalculation1() throws Exception {
        SABirthDeathModel model = new SABirthDeathModel();
        ZeroBranchSATreeParser tree = new ZeroBranchSATreeParser("((1:1.0)2:1.0)3:0.0", true, false, 1);

        System.out.println(tree.toString());

        model.setInputValue("tree", tree);
        model.setInputValue("origin", new RealParameter("10."));
        model.setInputValue("birthRate", new RealParameter("2."));
        model.setInputValue("deathRate", new RealParameter("0.99"));
        model.setInputValue("samplingRate", new RealParameter("0.5"));
        model.setInputValue("removalProbability", new RealParameter("0.9"));
        model.setInputValue("conditionOnSampling", true);
        model.initAndValidate();

        // these values ate calculated with Mathematica
        //assertEquals(-25.3707, model.calculateTreeLogLikelihood(tree), 1e-5); // likelihood conditioning only on parameters and origin time
        assertEquals(-24.92987, model.calculateTreeLogLikelihood(tree), 1e-5); // likelihood conditioning on at least one sampled individual

    }


    @Test
    public void testLikelihoodCalculation2() throws Exception {
        SABirthDeathModel model = new SABirthDeathModel();
        Tree tree = new ZeroBranchSATreeParser("((1:1.5,2:0.5):0.5)3:0.0", true, false, 1);

        model.setInputValue("tree", tree);
        model.setInputValue("origin", new RealParameter("10."));
        model.setInputValue("birthRate", new RealParameter("2."));
        model.setInputValue("deathRate", new RealParameter("0.99"));
        model.setInputValue("samplingRate", new RealParameter("0.5"));
        model.setInputValue("removalProbability", new RealParameter("0.9"));
        model.setInputValue("conditionOnSampling", true);
        model.initAndValidate();

        System.out.println(tree.toString());

        // this value is calculated with Mathematica
        assertEquals(-22.08332, model.calculateTreeLogLikelihood(tree), 1e-5); // likelihood conditioning on at least one sampled individual

    }
}
