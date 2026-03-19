package sa.evolution.tree;

import org.junit.jupiter.api.Test;

import beast.base.evolution.tree.Tree;
import sa.util.ZeroBranchSATreeParser;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeWOffsetTest {

	@Test
	public void testTree() throws Exception {
		Tree tree = new ZeroBranchSATreeParser("((t1:1.5,t2:0.5):0.5)3:0.0", true, true, 1);

		TreeWOffset treewoffset = new TreeWOffset();
		treewoffset.initByName("tree", tree, "offset", 25.0);

		assertEquals(25.0, treewoffset.getHeightOfNode(0), 1e-10);
		assertEquals(26.0, treewoffset.getHeightOfNode(1), 1e-10);
		assertEquals(25.0, treewoffset.getOffset(), 1e-10);
	}

}
