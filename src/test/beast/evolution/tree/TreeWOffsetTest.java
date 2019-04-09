package test.beast.evolution.tree;

import org.junit.Test;

import beast.evolution.tree.Tree;
import beast.evolution.tree.TreeWOffset;
import beast.util.ZeroBranchSATreeParser;
import junit.framework.TestCase;

public class TreeWOffsetTest extends TestCase {

	@Test
	public void testTree() throws Exception {
		Tree tree = new ZeroBranchSATreeParser("((t1:1.5,t2:0.5):0.5)3:0.0", true, true, 1);

		TreeWOffset treewoffset = new TreeWOffset();
		treewoffset.initByName("tree", tree, "offset", 25.0);

		assertEquals(treewoffset.getHeightOfNode(0), 25.0);
		assertEquals(treewoffset.getHeightOfNode(1), 26.0);
		assertEquals(treewoffset.getOffset(), 25.0);
	}

}
