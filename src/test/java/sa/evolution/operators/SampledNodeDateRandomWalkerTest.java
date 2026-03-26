package sa.evolution.operators;

import org.junit.jupiter.api.Test;

import beast.base.evolution.alignment.Taxon;
import beast.base.evolution.alignment.TaxonSet;
import beast.base.evolution.tree.Tree;
import beast.base.util.Randomizer;
import sa.evolution.tree.SamplingDate;
import sa.evolution.tree.TreeWOffset;
import sa.util.ZeroBranchSATreeParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampledNodeDateRandomWalkerTest {

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
			assertEquals(26.0, treewoffset.getHeightOfNode(1), 1e-10);
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
		assertEquals(26.0, treewoffset.getOffset(), 1e-10);
		assertEquals(0.0, treewoffset.getTree().getNode(1).getHeight(), 1e-10);
		assertTrue(treewoffset.getTree().getNode(0).getHeight() > 0);

		treewoffset.restore();
		assertEquals(25.0, treewoffset.getOffset(), 1e-10);
		assertEquals(25.0, treewoffset.getStoredHeightOfLeaf(0), 1e-10);
		assertTrue(treewoffset.getStoredHeightOfLeaf(1) > 25.0);

	}

}
