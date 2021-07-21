package beast.evolution.tree;

import java.util.List;
import java.util.Set;

import beast.core.Input;
import beast.core.util.Log;
import beast.evolution.alignment.TaxonSet;

public class RandomTreeWithSA extends RandomTree {

	final public Input<TaxonSet> startAsSampledAncestors = new Input<>("sampledAncestor",
			"Taxa to start as sampled ancestors");

	@Override
	public void initAndValidate() {
		if (!taxaInput.get().getTaxaNames().containsAll(startAsSampledAncestors.get().getTaxaNames())) {
			Set<String> remaining = startAsSampledAncestors.get().getTaxaNames();
			remaining.removeAll(taxaInput.get().getTaxaNames());
			Log.warning("WARNING: Not all sampledAncestor taxa were part of the alignment taxa. Missing taxa "
					+ remaining.toString() + " will not be forced to be sampled ancestors.");
		}
		super.initAndValidate();
	}

	@Override
	public void initStateNodes() {
		super.initStateNodes();

		List<String> sa = startAsSampledAncestors.get().asStringList();

		for (Node leaf : root.getAllLeafNodes()) {

			if (!sa.contains(this.getTaxonId(leaf))) {
				continue;
			}
			Node parent = leaf.getParent();

			double newHeight = leaf.getHeight();
			parent.setHeight(newHeight);
			// setHeight does not check temporal order of nodes, so we nudge all children
			// that are older than their parent down a bit. This can mess with other
			// assumptions.
			for (Node node : parent.getAllChildNodesAndSelf()) {
				if (node.getHeight() > node.parent.getHeight()) {
					if (sa.contains(getTaxonId(node))) {
						// Keep sampled ancestors as sampled ancestors. Yes, this is a bit redundant,
						// but at least it should be correct. The alternative is to recursively walk the
						// tree in a somewhat erratic order.
						node.setHeight(node.parent.getHeight());
					} else {
						// Put everything else down a notch.
						node.setHeight(node.parent.getHeight() - 1e-9);
					}
				}
			}
			assert leaf.isDirectAncestor();
		}
	}
}
