package test.beast.evolution.speciation;

import junit.framework.TestCase;
import sa.evolution.speciation.SABirthDeathModel;
import sa.evolution.tree.TreeWOffset;
import sa.util.ZeroBranchSATreeParser;

import org.junit.Test;

import beast.base.inference.parameter.RealParameter;
import beast.base.evolution.tree.Tree;

/**
 * @author Alexei Drummond
 */
public class SABirthDeathModelTest extends TestCase {

	@Test
	public void testLikelihoodCalculationSimple() throws Exception {

		Tree tree = new ZeroBranchSATreeParser("((3 : 1.5, 4 : 0.5) : 1 , (1 : 2, 2 : 1) : 3);", false, true, 0);

		SABirthDeathModel sabdm = new SABirthDeathModel();
		sabdm.setInputValue("tree", tree);
		sabdm.setInputValue("origin", new RealParameter("10."));
		sabdm.setInputValue("removalProbability", "1");
		sabdm.setInputValue("conditionOnSampling", true);

		sabdm.setInputValue("birthRate", new RealParameter("2.25"));
		sabdm.setInputValue("deathRate", new RealParameter("1.05"));
		sabdm.setInputValue("samplingRate", new RealParameter("0.45"));

		sabdm.initAndValidate();

		// the true value is as calculated in beast on 23 May 2017
		assertEquals(-25.6651, sabdm.calculateLogP(), 1e-4);


		TreeWOffset treewoffset = new TreeWOffset();
		treewoffset.setInputValue("tree", tree);
		treewoffset.initAndValidate();

		sabdm = new SABirthDeathModel();
		sabdm.setInputValue("treeWOffset", treewoffset);
		sabdm.setInputValue("origin", new RealParameter("10."));
		sabdm.setInputValue("removalProbability", "1");
		sabdm.setInputValue("conditionOnSampling", true);

		sabdm.setInputValue("birthRate", new RealParameter("2.25"));
		sabdm.setInputValue("deathRate", new RealParameter("1.05"));
		sabdm.setInputValue("samplingRate", new RealParameter("0.45"));

		sabdm.initAndValidate();

		// the true value is as calculated in beast on 23 May 2017
		assertEquals(-25.6651, sabdm.calculateLogP(), 1e-4);
	}

	@Test
	public void testLikelihoodCalculation1() throws Exception {
		ZeroBranchSATreeParser tree = new ZeroBranchSATreeParser("((1:1.0)2:1.0)3:0.0", true, false, 1);

		SABirthDeathModel sabd = new SABirthDeathModel();
		sabd.setInputValue("tree", tree);
		sabd.setInputValue("origin", new RealParameter("10."));
		sabd.setInputValue("birthRate", new RealParameter("2."));
		sabd.setInputValue("deathRate", new RealParameter("0.99"));
		sabd.setInputValue("samplingRate", new RealParameter("0.5"));
		sabd.setInputValue("removalProbability", new RealParameter("0.9"));
		sabd.initAndValidate();

		// these values are calculated with Mathematica
		assertEquals(-25.3707, sabd.calculateLogP(), 1e-5); // likelihood conditioning only on parameters and origin time

		sabd.setInputValue("conditionOnSampling", true);
		sabd.initAndValidate();
		// these values are calculated with Mathematica
		assertEquals(-24.92987, sabd.calculateLogP(), 1e-5); // likelihood conditioning on at least one sampled individual

		TreeWOffset treewoffset = new TreeWOffset();
		treewoffset.setInputValue("tree", tree);
		treewoffset.initAndValidate();

		sabd = new SABirthDeathModel();
		sabd.setInputValue("treeWOffset", treewoffset);
		sabd.setInputValue("origin", new RealParameter("10."));
		sabd.setInputValue("birthRate", new RealParameter("2."));
		sabd.setInputValue("deathRate", new RealParameter("0.99"));
		sabd.setInputValue("samplingRate", new RealParameter("0.5"));
		sabd.setInputValue("removalProbability", new RealParameter("0.9"));
		sabd.setInputValue("conditionOnSampling", true);
		sabd.initAndValidate();

		// these values are calculated with Mathematica
		assertEquals(-24.92987, sabd.calculateLogP(), 1e-5); // likelihood conditioning on at least one sampled individual

	}


	@Test
	public void testLikelihoodCalculation2() throws Exception {
		Tree tree = new ZeroBranchSATreeParser("((1:1.5,2:0.5):0.5)3:0.0", true, false, 1);
		
		SABirthDeathModel sabd = new SABirthDeathModel();
		sabd.setInputValue("tree", tree);
		sabd.setInputValue("origin", new RealParameter("10."));
		sabd.setInputValue("birthRate", new RealParameter("2."));
		sabd.setInputValue("deathRate", new RealParameter("0.99"));
		sabd.setInputValue("samplingRate", new RealParameter("0.5"));
		sabd.setInputValue("removalProbability", new RealParameter("0.9"));
		sabd.setInputValue("conditionOnSampling", true);
		sabd.initAndValidate();

		// this value is calculated with Mathematica
		assertEquals(-22.08332, sabd.calculateLogP(), 1e-5); // likelihood conditioning on at least one sampled individual

		TreeWOffset treewoffset = new TreeWOffset();
		treewoffset.setInputValue("tree", tree);
		treewoffset.initAndValidate();

		sabd = new SABirthDeathModel();
		sabd.setInputValue("treeWOffset", treewoffset);
		sabd.setInputValue("origin", new RealParameter("10."));
		sabd.setInputValue("birthRate", new RealParameter("2."));
		sabd.setInputValue("deathRate", new RealParameter("0.99"));
		sabd.setInputValue("samplingRate", new RealParameter("0.5"));
		sabd.setInputValue("removalProbability", new RealParameter("0.9"));
		sabd.setInputValue("conditionOnSampling", true);
		sabd.initAndValidate();

		// this value is calculated with Mathematica
		assertEquals(-22.08332, sabd.calculateLogP(), 1e-5); // likelihood conditioning on at least one sampled individual

	}
}
