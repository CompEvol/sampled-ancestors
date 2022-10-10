package test.beast.evolution.operators;

import org.junit.Test;

import beast.base.evolution.alignment.Taxon;
import beast.base.evolution.alignment.TaxonSet;
import beast.evolution.operators.SampledNodeDateRandomWalker;
import beast.evolution.tree.SamplingDate;
import beast.base.evolution.tree.Tree;
import beast.evolution.tree.TreeWOffset;
import beast.base.util.Randomizer;
import beast.util.ZeroBranchSATreeParser;
import junit.framework.TestCase;

public class SampledNodeDateRandomWalkerTest extends TestCase {

	@Test
	public void testHeightWOffset() throws Exception {
		
		Tree tree = new ZeroBranchSATreeParser("((t1:1.5,t2:0.5):0.5)3:0.0", true, true, 1);
		TaxonSet tx = new TaxonSet(), txs = new TaxonSet();
		tx.initByName("taxon", new Taxon("t1"), "taxon", new Taxon("t2"));
		tree.setInputValue("taxonset", tx);

		TreeWOffset treewoffset = new TreeWOffset();
		treewoffset.initByName("tree", tree, "offset", 25.0);

		SamplingDate sd1 = new SamplingDate();
		sd1.initByName("taxon", tree.getTaxonset().taxonsetInput.get().get(0), "lower", "24.0", "upper", "30.0");
		
		txs.initByName("taxon", new Taxon("t1"));
		SampledNodeDateRandomWalker op = new SampledNodeDateRandomWalker();
		op.initByName("tree", tree, "treeWOffset", treewoffset, "samplingDates", sd1, 
				"windowSize", 10.0, "weight", 1.0, "taxonset", txs);
		
		Randomizer.setSeed(2);
		// test that tip t2 doesn't move
		for(int i = 0; i < 25; i++) {
			op.proposal();
			assertEquals(treewoffset.getHeightOfNode(1), 26.0);
		}
	}

	@Test
	public void testSwitchWOffset() throws Exception {

		Tree tree = new ZeroBranchSATreeParser("((t1:1.5,t2:0.5):0.5)3:0.0", true, true, 1);
		TaxonSet tx = new TaxonSet(), txs = new TaxonSet();
		tx.initByName("taxon", new Taxon("t1"), "taxon", new Taxon("t2"));
		tree.setInputValue("taxonset", tx);

		TreeWOffset treewoffset = new TreeWOffset();
		treewoffset.initByName("tree", tree, "offset", 25.0);

		SamplingDate sd1 = new SamplingDate();
		sd1.initByName("taxon", tree.getTaxonset().taxonsetInput.get().get(0), "lower", "24.0", "upper", "30.0");

		txs.initByName("taxon", new Taxon("t1"));
		SampledNodeDateRandomWalker op = new SampledNodeDateRandomWalker();
		op.initByName("tree", tree, "treeWOffset", treewoffset, "samplingDates", sd1, 
				"windowSize", 10.0, "weight", 1.0, "taxonset", txs);

		// test that switching leaves happens correctly
		Randomizer.setSeed(2);
		op.proposal();
		assertEquals(treewoffset.getOffset(), 26.0);
		assertEquals(treewoffset.getTree().getNode(1).getHeight(), 0.0);
		assertTrue(treewoffset.getTree().getNode(0).getHeight() > 0);

		treewoffset.restore();
		assertEquals(treewoffset.getOffset(), 25.0);
		assertEquals(treewoffset.getStoredHeightOfLeaf(0), 25.0);
		assertTrue(treewoffset.getStoredHeightOfLeaf(1) > 25.0);

	}

}
