package sa.beauti;

import beastfx.app.inputeditor.BEASTObjectInputEditor;
import beastfx.app.inputeditor.InputEditor;
import sa.evolution.operators.SampledNodeDateRandomWalker;

public class SampledNodeDataWalkerInputEditor extends BEASTObjectInputEditor {
	
	@Override
	public Class<?> type() {
		return SampledNodeDateRandomWalker.class;
	}
	
    public InputEditor createTaxonsetEditor() {
    	return new InputEditor.Base() {
			@Override
			public Class<?> type() {
				return null;
			}
		};
    }

}
