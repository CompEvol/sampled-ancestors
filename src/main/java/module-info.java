open module sampled.ancestors {
    requires beast.pkgmgmt;
    requires beast.base;
    requires static beast.fx;
    requires static javafx.controls;
    requires org.apache.commons.statistics.distribution;
    requires org.apache.commons.rng.api;
    requires java.desktop;

    exports sa.app.simulators;
    exports sa.app.tools;
    exports sa.app.tools.fx;
    exports sa.beauti;
    exports sa.evolution.operators;
    exports sa.evolution.speciation;
    exports sa.evolution.tree;
    exports sa.math.distributions;
    exports sa.util;

    provides beast.base.core.BEASTInterface with
        sa.evolution.operators.Binary,
        sa.evolution.operators.IntRandomWalkWithExclusion,
        sa.evolution.operators.IntSwapWithExclusion,
        sa.evolution.operators.IntUniformWithExclusion,
        sa.evolution.operators.JumpToPoint,
        sa.evolution.operators.LeafToSampledAncestorJump,
        sa.evolution.operators.SAExchange,
        sa.evolution.operators.SampledNodeDateRandomWalker,
        sa.evolution.operators.SAScaleOperator,
        sa.evolution.operators.SAUniform,
        sa.evolution.operators.SAWilsonBalding,
        sa.evolution.operators.TreeDimensionJump,
        sa.evolution.operators.WilsonBaldingWithRateCategories,
        sa.evolution.speciation.DiversificationTurnoverParameterization,
        sa.evolution.speciation.DiversificationTurnoverPsiExpectedNParameterization,
        sa.evolution.speciation.ParameterizedSABirthDeathModel,
        sa.evolution.speciation.ProbabilitySA,
        sa.evolution.speciation.RateParameterization,
        sa.evolution.speciation.SABirthDeathModel,
        sa.evolution.tree.AncestryConstraint,
        sa.evolution.tree.CladeConstraint,
        sa.evolution.tree.OffsetLogger,
        sa.evolution.tree.SampledAncestorLogger,
        sa.evolution.tree.SamplingDate,
        sa.evolution.tree.TreeWOffset,
        sa.math.distributions.DegenerateBeta,
        sa.math.distributions.DegenerateUniform,
        sa.math.distributions.SpecialMRCAPrior,
        sa.math.distributions.SAMRCAPrior,
        sa.util.ClusterZBSATree,
        sa.util.ZeroBranchSATreeParser,
        sa.app.tools.fx.FullToExtantTreeConverter,
        sa.app.tools.fx.SampledAncestorTreeAnalyser;

    provides beastfx.app.inputeditor.InputEditor with
        sa.beauti.SAMRCAPriorInputEditor;

    provides beastfx.app.beauti.PriorProvider with
        sa.beauti.SAMRCAPriorProvider;
}
