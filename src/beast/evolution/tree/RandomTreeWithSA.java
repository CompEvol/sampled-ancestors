package beast.evolution.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.util.Log;
import beast.evolution.alignment.Taxon;
import beast.evolution.alignment.TaxonSet;

public class RandomTreeWithSA extends RandomTree {

	final public Input<List<Taxon>> startAsSampledAncestors = new Input<>("sampledAncestor",
			"Taxa to start as sampled ancestors", new ArrayList<Taxon>());

	@Override
	public void initAndValidate() {
		taxa = new LinkedHashSet<>();
		if (taxaInput.get() != null) {
			taxa.addAll(taxaInput.get().getTaxaNames());
		} else {
			taxa.addAll(m_taxonset.get().asStringList());
		}
		
		Set<String> startTaxa = new LinkedHashSet<>();
		for (Taxon taxon: startAsSampledAncestors.get()) {
			startTaxa.add(taxon.getID());
		}
		if (!taxa.containsAll(startTaxa)) {
			Set<String> remaining = startTaxa;
			remaining.removeAll(taxa);
			Log.warning("WARNING: Not all sampledAncestor taxa were part of the alignment taxa. Missing taxa "
					+ remaining.toString() + " will not be forced to be sampled ancestors.");
		}
		super.initAndValidate();
	}

	@Override
	public void initStateNodes() {
		super.initStateNodes();

		List<String> sa = new ArrayList<String>();
				
		for (Taxon taxon: startAsSampledAncestors.get()) {
			sa.add(taxon.getID());
		}

		for (Node leaf : root.getAllLeafNodes()) {

			if (!sa.contains(getTaxonId(leaf))) {
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
					if (!node.isLeaf()) {
						// Not a leaf, so cannot be a sampled ancestor which needs to be handled with
						// care
						node.setHeight(node.parent.getHeight() - 1e-9);
					} else if (sa.contains(getTaxonId(node))) {
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

		if (m_initial.get() != null) {
			m_initial.get().assignFromWithoutID(this);
		}
	}
}
