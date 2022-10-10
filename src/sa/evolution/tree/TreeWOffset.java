package sa.evolution.tree;

import beast.base.inference.CalculationNode;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;

public class TreeWOffset extends CalculationNode {
	
	public Input<Tree> treeInput = new Input<Tree> ("tree", "tree topology", Validate.REQUIRED);
	public Input<Double> offsetInput = new Input<Double>("offset", "Starting offset to present, default 0", 0.0);
	
	Tree tree;
	Double oldOffset, offset;
	Double[] oldHeights, leaves_heights;
	int oldMin_leaf, min_leaf;
	private boolean stored = false;
	
	@Override
	public void initAndValidate() {
		tree = treeInput.get();
		offset = offsetInput.get();
		
		leaves_heights = new Double[tree.getLeafNodeCount()];
		for(int i = 0; i < tree.getLeafNodeCount(); i++) {
			double h = tree.getNode(i).getHeight();
			if(h == 0) min_leaf = i;
			leaves_heights[i] = h + offset;
		}
	}
	
	public double getHeightOfNode(int nr) {
		return tree.getNode(nr).getHeight() + offset;
	}
	
	public void setHeightOfLeaf(int nr, double height) {
		store();
		
		double oldOffset = offset;
		if(height < offset) {
			offset = height;
			min_leaf = nr;
		}
		leaves_heights[nr] = height;
		// if we increased the height of the minimum leaf then which one it is may have changed
		if(height > offset && nr == min_leaf) {
			offset = height;
			for(int i = 0; i < leaves_heights.length; i++) {
				if(leaves_heights[i] < offset) {
					min_leaf = i;
					offset = leaves_heights[i];
				}
			}
		}
		// if offset has changed then all heights need to be updated to keep true heights the same
		if(Math.abs(oldOffset - offset) > 0) {
			for(Node n : tree.getNodesAsArray()) n.setHeight(n.getHeight() + oldOffset - offset);
		}
	}
	
	public double getOffset() {
		return offset;
	}

	public Tree getTree() {
		return tree;
	}

	public void setHeightOfNode(int nr, double height) {
		Node n = tree.getNode(nr);
		if(n.isLeaf()) this.setHeightOfLeaf(nr, height);
		n.setHeight(height - offset);
	}
	
	// for testing purposes
	public double getStoredHeightOfLeaf(int nr) {
		return leaves_heights[nr];
	}
	
	/* This entire section is to work around the fact that this class is modified DURING the operator proposal of SampledNodeDateRandomWalker
	 * whereas Beast2 assumes that only state nodes are modified, and that calculation nodes are only updated afterwards
	 * So, using the default mechanic will store/restore the modified state not the original one
	 * There is probably a better way to do this
	 */ 
	
	@Override
	public void store() {
		super.store();
		if(stored) return;
		oldMin_leaf = min_leaf;
		oldOffset = offset;
		oldHeights = leaves_heights.clone();
		stored = true;
	}
	
	@Override
	public void restore() {
		super.restore();
		min_leaf = oldMin_leaf;
		offset = oldOffset;
		leaves_heights = oldHeights.clone();
		stored = false;
	}
	
	@Override
	public void accept() {
		super.accept();
		stored = false;
	}
}
