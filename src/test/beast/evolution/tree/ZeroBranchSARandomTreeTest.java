package test.beast.evolution.tree;

import beast.core.parameter.RealParameter;
import beast.evolution.alignment.Taxon;
import beast.evolution.alignment.TaxonSet;
import beast.evolution.tree.CladeConstraint;
import beast.evolution.tree.TreeUtils;
import beast.evolution.tree.ZeroBranchSARandomTree;
import beast.evolution.tree.ZeroBranchSATree;
import beast.evolution.tree.coalescent.ConstantPopulation;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Walter Xie
 */
public class ZeroBranchSARandomTreeTest {
    String treeTopology = "(((A,B),C),(D,E))";

    Taxon A;
    Taxon B;
    Taxon C;
    Taxon D;
    Taxon E;
    TaxonSet taxonSetAll;

    @Before
    public void setUp() throws Exception {
        A = new Taxon("A");
        B = new Taxon("B");
        C = new Taxon("C");
        D = new Taxon("D");
        E = new Taxon("E");
        taxonSetAll = new TaxonSet();
        taxonSetAll.initByName("taxon", A, "taxon", B, "taxon", C, "taxon", D, "taxon", E);
    }

    @Test
    public void testTreeTopologyNoDate() throws Exception {
//        Randomizer.setSeed(777);

        ZeroBranchSATree tree = new ZeroBranchSATree();
        tree.initByName("taxonset", taxonSetAll);

        // cladeConstraints
        List<CladeConstraint> cladeConstraints = new ArrayList<>();

        CladeConstraint clade = new CladeConstraint();
        clade.setID("constraint1");
        TaxonSet taxonSetIn = new TaxonSet();
        taxonSetIn.initByName("taxon", A, "taxon", B, "taxon", C);
        TaxonSet taxonSetOut = new TaxonSet();
        taxonSetOut.initByName("taxon", D);
        clade.initByName(
                "tree", tree,
                "taxonsetIn", taxonSetIn,
                "taxonsetOut", taxonSetOut
        );
        cladeConstraints.add(clade);

        clade = new CladeConstraint();
        clade.setID("constraint2");
        taxonSetIn = new TaxonSet();
        taxonSetIn.initByName("taxon", A, "taxon", B);
        clade.initByName(
                "tree", tree,
                "taxonsetIn", taxonSetIn
        );
        cladeConstraints.add(clade);

        clade = new CladeConstraint();
        clade.setID("constraint3");
        taxonSetIn = new TaxonSet();
        taxonSetIn.initByName("taxon", D, "taxon", E);
        clade.initByName(
                "tree", tree,
                "taxonsetIn", taxonSetIn
        );
        cladeConstraints.add(clade);

        ConstantPopulation popFunc = new ConstantPopulation();
        popFunc.initByName("popSize", new RealParameter("1.0"));

        ZeroBranchSARandomTree sARandomTree;
        for (int i=0; i<1000; i++) {
            sARandomTree = new ZeroBranchSARandomTree();
            sARandomTree.initByName(
                    "nodetype", "beast.evolution.tree.ZeroBranchSANode",
                    "initial", tree,
                    "taxonset", tree.getTaxonset(),
                    "populationModel", popFunc,
                    "cladeConstraint", cladeConstraints
            );

            String sortedNewickTopology = TreeUtils.sortedNewickTopology(sARandomTree.getRoot(), true);
//            System.out.println(sortedNewickTopology);
            assert sortedNewickTopology.contentEquals(treeTopology);
        }
    }

    @Test
    public void testTreeTopology() throws Exception {
//        Randomizer.setSeed(777);

        ZeroBranchSATree tree = new ZeroBranchSATree();
        tree.initByName("taxonset", taxonSetAll);

        // cladeConstraints
        List<CladeConstraint> cladeConstraints = new ArrayList<>();

        CladeConstraint clade = new CladeConstraint();
        clade.setID("constraint1");
        TaxonSet taxonSetIn = new TaxonSet();
        taxonSetIn.initByName("taxon", A, "taxon", B, "taxon", C);
        TaxonSet taxonSetOut = new TaxonSet();
        taxonSetOut.initByName("taxon", D);
        clade.initByName(
                "tree", tree,
                "taxonsetIn", taxonSetIn,
                "taxonsetOut", taxonSetOut,
                "stronglyMonophyletic", true
        );
        cladeConstraints.add(clade);

        clade = new CladeConstraint();
        clade.setID("constraint2");
        clade.initByName(
                "tree", tree,
                "taxonsetIn", taxonSetAll,
                "stronglyMonophyletic", true
        );
        cladeConstraints.add(clade);

        ConstantPopulation popFunc = new ConstantPopulation();
        popFunc.initByName("popSize", new RealParameter("1.0"));

        ZeroBranchSARandomTree sARandomTree;
        for (int i=0; i<1000; i++) {
            sARandomTree = new ZeroBranchSARandomTree();
            sARandomTree.initByName(
                    "nodetype", "beast.evolution.tree.ZeroBranchSANode",
                    "initial", tree,
                    "taxonset", tree.getTaxonset(),
                    "populationModel", popFunc,
                    "cladeConstraint", cladeConstraints
            );

            String sortedNewickTopology = TreeUtils.sortedNewickTopology(sARandomTree.getRoot(), true);
//            System.out.println(sortedNewickTopology);
            assert sortedNewickTopology.contentEquals(treeTopology);
        }
    }
}
