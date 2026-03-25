package sa.evolution.speciation;

import sa.evolution.tree.TreeWOffset;
import sa.util.ZeroBranchSATreeParser;

import org.junit.jupiter.api.Test;

import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.domain.PositiveReal;
import beast.base.spec.domain.UnitInterval;
import beast.base.spec.inference.parameter.RealScalarParam;
import beast.base.evolution.tree.Tree;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexei Drummond
 */
public class SABirthDeathModelTest {

	@Test
	public void testLikelihoodCalculationSimple() throws Exception {

		Tree tree = new ZeroBranchSATreeParser("((3 : 1.5, 4 : 0.5) : 1 , (1 : 2, 2 : 1) : 3);", false, true, 0);

		SABirthDeathModel sabdm = new SABirthDeathModel();
		sabdm.setInputValue("tree", tree);
		sabdm.setInputValue("origin", new RealScalarParam<>(10.0, PositiveReal.INSTANCE));
		sabdm.setInputValue("removalProbability", new RealScalarParam<>(1.0, UnitInterval.INSTANCE));
		sabdm.setInputValue("conditionOnSampling", true);

		sabdm.setInputValue("birthRate", new RealScalarParam<>(2.25, PositiveReal.INSTANCE));
		sabdm.setInputValue("deathRate", new RealScalarParam<>(1.05, NonNegativeReal.INSTANCE));
		sabdm.setInputValue("samplingRate", new RealScalarParam<>(0.45, NonNegativeReal.INSTANCE));

		sabdm.initAndValidate();

		// the true value is as calculated in beast on 23 May 2017
		assertEquals(-25.6651, sabdm.calculateLogP(), 1e-4);


		TreeWOffset treewoffset = new TreeWOffset();
		treewoffset.setInputValue("tree", tree);
		treewoffset.initAndValidate();

		sabdm = new SABirthDeathModel();
		sabdm.setInputValue("treeWOffset", treewoffset);
		sabdm.setInputValue("origin", new RealScalarParam<>(10.0, PositiveReal.INSTANCE));
		sabdm.setInputValue("removalProbability", new RealScalarParam<>(1.0, UnitInterval.INSTANCE));
		sabdm.setInputValue("conditionOnSampling", true);

		sabdm.setInputValue("birthRate", new RealScalarParam<>(2.25, PositiveReal.INSTANCE));
		sabdm.setInputValue("deathRate", new RealScalarParam<>(1.05, NonNegativeReal.INSTANCE));
		sabdm.setInputValue("samplingRate", new RealScalarParam<>(0.45, NonNegativeReal.INSTANCE));

		sabdm.initAndValidate();

		// the true value is as calculated in beast on 23 May 2017
		assertEquals(-25.6651, sabdm.calculateLogP(), 1e-4);
	}

	@Test
	public void testLikelihoodCalculation1() throws Exception {
		ZeroBranchSATreeParser tree = new ZeroBranchSATreeParser("((1:1.0)2:1.0)3:0.0", true, false, 1);

		SABirthDeathModel sabd = new SABirthDeathModel();
		sabd.setInputValue("tree", tree);
		sabd.setInputValue("origin", new RealScalarParam<>(10.0, PositiveReal.INSTANCE));
		sabd.setInputValue("birthRate", new RealScalarParam<>(2.0, PositiveReal.INSTANCE));
		sabd.setInputValue("deathRate", new RealScalarParam<>(0.99, NonNegativeReal.INSTANCE));
		sabd.setInputValue("samplingRate", new RealScalarParam<>(0.5, NonNegativeReal.INSTANCE));
		sabd.setInputValue("removalProbability", new RealScalarParam<>(0.9, UnitInterval.INSTANCE));
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
		sabd.setInputValue("origin", new RealScalarParam<>(10.0, PositiveReal.INSTANCE));
		sabd.setInputValue("birthRate", new RealScalarParam<>(2.0, PositiveReal.INSTANCE));
		sabd.setInputValue("deathRate", new RealScalarParam<>(0.99, NonNegativeReal.INSTANCE));
		sabd.setInputValue("samplingRate", new RealScalarParam<>(0.5, NonNegativeReal.INSTANCE));
		sabd.setInputValue("removalProbability", new RealScalarParam<>(0.9, UnitInterval.INSTANCE));
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
		sabd.setInputValue("origin", new RealScalarParam<>(10.0, PositiveReal.INSTANCE));
		sabd.setInputValue("birthRate", new RealScalarParam<>(2.0, PositiveReal.INSTANCE));
		sabd.setInputValue("deathRate", new RealScalarParam<>(0.99, NonNegativeReal.INSTANCE));
		sabd.setInputValue("samplingRate", new RealScalarParam<>(0.5, NonNegativeReal.INSTANCE));
		sabd.setInputValue("removalProbability", new RealScalarParam<>(0.9, UnitInterval.INSTANCE));
		sabd.setInputValue("conditionOnSampling", true);
		sabd.initAndValidate();

		// this value is calculated with Mathematica
		assertEquals(-22.08332, sabd.calculateLogP(), 1e-5); // likelihood conditioning on at least one sampled individual

		TreeWOffset treewoffset = new TreeWOffset();
		treewoffset.setInputValue("tree", tree);
		treewoffset.initAndValidate();

		sabd = new SABirthDeathModel();
		sabd.setInputValue("treeWOffset", treewoffset);
		sabd.setInputValue("origin", new RealScalarParam<>(10.0, PositiveReal.INSTANCE));
		sabd.setInputValue("birthRate", new RealScalarParam<>(2.0, PositiveReal.INSTANCE));
		sabd.setInputValue("deathRate", new RealScalarParam<>(0.99, NonNegativeReal.INSTANCE));
		sabd.setInputValue("samplingRate", new RealScalarParam<>(0.5, NonNegativeReal.INSTANCE));
		sabd.setInputValue("removalProbability", new RealScalarParam<>(0.9, UnitInterval.INSTANCE));
		sabd.setInputValue("conditionOnSampling", true);
		sabd.initAndValidate();

		// this value is calculated with Mathematica
		assertEquals(-22.08332, sabd.calculateLogP(), 1e-5); // likelihood conditioning on at least one sampled individual

	}
}
