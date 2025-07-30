package sa.app.tools;

import beast.base.evolution.tree.Tree;
import org.junit.Assert;
import org.junit.Test;

public class ConvertZBTreeToSATreeTest {


    @Test
    public void test() throws Exception {
        String treeFile = "test/sa/app/tools/testTree.tree";
        ConvertZBTreeToSATree converter = new ConvertZBTreeToSATree(treeFile);

        String annotationFile = "test/sa/app/tools/testAnnotations.tsv";

        ConvertZBTreeToSATree.TipAnnotations tipAnnotations =
                new ConvertZBTreeToSATree.TipAnnotations(annotationFile);

        tipAnnotations.applyToTrees(converter.trees);

        // Test whether annotations are correctly applied:
        Tree tree = converter.trees.get(0);
        Assert.assertEquals("mass='1.2'", tree.getNode(0).metaDataString);
        Assert.assertEquals("mass='78'", tree.getNode(1).metaDataString);
        Assert.assertEquals("mass='13'", tree.getNode(2).metaDataString);
        Assert.assertEquals("mass='52'", tree.getNode(3).metaDataString);
        Assert.assertEquals("mass='3.14'", tree.getNode(4).metaDataString);

        // Test whether ZBTree is converted properly to an SATree:
        converter.convertTree(tree.getRoot());
        Assert.assertEquals("(((A[&mass='1.2']:1.0,B[&mass='78']:1.0):1.0,C[&mass='13']:2.0):1.0,(D[&mass='52']:1.5)E[&mass='3.14']:1.5):0.0",
                tree.getRoot().toNewick());
    }
}
