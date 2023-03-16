package sa.beauti;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.evolution.alignment.TaxonSet;
import beast.base.inference.Operator;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BooleanInputEditor;
import beastfx.app.inputeditor.InputEditor;
import beastfx.app.inputeditor.MRCAPriorInputEditor;
import beastfx.app.util.FXUtils;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import sa.evolution.operators.SampledNodeDateRandomWalker;
import sa.math.distributions.SAMRCAPrior;

public class SAMRCAPriorInputEditor extends MRCAPriorInputEditor {

	public SAMRCAPriorInputEditor(BeautiDoc doc) {
		super(doc);
	}

	public SAMRCAPriorInputEditor() {
		super();
	}

	@Override
	public Class<?> type() {
		return SAMRCAPrior.class;
	}

	//InputEditor tipsonlyEditor;
    
    public InputEditor createTipsonlyEditor() throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        BooleanInputEditor e = new BooleanInputEditor (doc) {

			@Override
        	public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption,
        			boolean addButtons) {
        		super.init(input, beastObject, itemNr, isExpandOption, addButtons);
        		for (Node o1 : getChildren()) {
        			if (o1 instanceof HBox) {
	            		for (Node o : ((HBox)o1).getChildren()) {
		        			if (o instanceof CheckBox) {
		        				((CheckBox)o).setOnAction(e -> {
				                	CheckBox src = (CheckBox) e.getSource();
				                	if (src.isSelected()) {
				                		enableTipSampling();
				                	} else {
				                		disableTipSampling(m_beastObject, doc);
				                	}
		        				});
		        			}
		    			}
		    		}
		    	}
			}
        };

        SAMRCAPrior prior = (SAMRCAPrior) m_beastObject;
        Input<?> input = prior.onlyUseTipsInput;
        e.init(input, prior, -1, ExpandOption.FALSE, false);
        return e;
    }

    public class NoEditor extends InputEditor.Base {
		@Override
		public Class<?> type() {
			// TODO Auto-generated method stub
			return null;
		}
    }
    
    public InputEditor createTreeEditor() {
    	System.err.println(">>>>>> createTreeEditor");
    	return new NoEditor();
    }

    public InputEditor createMonophyleticEditor() {
    	System.err.println(">>>>>> createMonophyleticEditor");
    	return new NoEditor();
    }

    public InputEditor createTaxonsetEditor() {
    	System.err.println(">>>>>> createTaxonsetEditor");
    	return new NoEditor();
    }
    
    // add TipDatesRandomWalker (if not present) and add to list of operators
    private void enableTipSampling() {
    	// First, create/find the operator
    	SampledNodeDateRandomWalker operator = null;
    	SAMRCAPrior prior = (SAMRCAPrior) m_beastObject;
    	TaxonSet taxonset = prior.taxonsetInput.get();
    	taxonset.initAndValidate();
    	
    	// see if an old operator still hangs around -- happens when toggling the TipsOnly checkbox a few times
    	for (BEASTInterface o : taxonset.getOutputs()) {
    		if (o instanceof SampledNodeDateRandomWalker) {
    			operator = (SampledNodeDateRandomWalker) o;
    		}
    	}
    	
    	if (operator == null) {
    		operator = new SampledNodeDateRandomWalker();
    		operator.initByName("tree", prior.treeInput.get(), "taxonset", taxonset, "windowSize", 1.0, "weight", 1.0);
    	}
   		operator.setID("tipDatesSampler." + taxonset.getID());
   	    	
    	doc.mcmc.get().setInputValue("operator", operator);
	}

    // remove TipDatesRandomWalker from list of operators
	private static void disableTipSampling(BEASTInterface m_beastObject, BeautiDoc doc) {
    	// First, find the operator
		SampledNodeDateRandomWalker operator = null;
    	SAMRCAPrior prior = (SAMRCAPrior) m_beastObject;
    	TaxonSet taxonset = prior.taxonsetInput.get();
    	
    	// We cannot rely on the operator ID created in enableTipSampling()
    	// since the taxoneset name may have changed.
    	// However, if there is an TipDatesRandomWalker with taxonset as input, we want to remove it.
    	for (BEASTInterface o : taxonset.getOutputs()) {
    		if (o instanceof SampledNodeDateRandomWalker) {
    			operator = (SampledNodeDateRandomWalker) o;
    		}
    	}
    	
    	if (operator == null) {
    		// should never happen
    		return;
    	}
    	
    	// remove from list of operators
    	Object o = doc.mcmc.get().getInput("operator");
    	if (o instanceof Input<?>) {
    		Input<List<Operator>> operatorInput = (Input<List<Operator>>) o;
    		List<Operator> operators = operatorInput.get();
    		operators.remove(operator);
    	}
	}

}
