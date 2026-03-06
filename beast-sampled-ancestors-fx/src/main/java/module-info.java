open module sa.fx {
    requires sa;
    requires beast.base;
    requires beast.pkgmgmt;
    requires beast.fx;
    requires javafx.controls;

    exports sa.beauti;
    exports sa.app.tools.fx;

    provides beast.base.core.BEASTInterface with
        sa.app.tools.fx.FullToExtantTreeConverter,
        sa.app.tools.fx.SampledAncestorTreeAnalyser;

    provides beastfx.app.inputeditor.InputEditor with
        sa.beauti.SAMRCAPriorInputEditor;

    provides beastfx.app.beauti.PriorProvider with
        sa.beauti.SAMRCAPriorProvider;
}
