package sa.evolution.operators;

import beast.base.core.Description;
import beast.base.evolution.tree.Tree;
import beast.base.spec.evolution.operator.ScaleTreeOperator;

/**
 * Tree-scale operator for trees that may contain sampled ancestors.
 * <p>
 * The whole-tree path of spec {@link ScaleTreeOperator} relies on
 * {@code Tree.scale -> Node.intervalScale}, which currently has a defect on
 * fake-rooted SA trees: it scales the non-direct-ancestor subtree but leaves
 * the fake root pinned, so {@code scale > 1} can lift a child above its
 * parent. The {@code rootOnly} path has the inverse problem: scaling the
 * fake root height up breaks the SA invariant (root height pinned to the
 * SA leaf's height). See <a href="https://github.com/CompEvol/beast3/issues/78">CompEvol/beast3#78</a>.
 * <p>
 * Until that issue is fixed in spec, we override {@code proposal()} to add
 * the legacy SA guard back: reject any {@code rootOnly} move when the root
 * is a fake node. Once #78 lands, this override can be deleted and the
 * class can extend {@link ScaleTreeOperator} with no body (kept only for
 * the XML element name {@code sa.evolution.operators.SAScaleOperator}).
 *
 * @author Alexandra Gavryushkina
 */
@Description("Scale operator for trees with sampled ancestors. Defers to spec "
        + "ScaleTreeOperator and rejects rootOnly moves when the root is a fake "
        + "(sampled-ancestor) node. See CompEvol/beast3#78.")
public class SAScaleOperator extends ScaleTreeOperator {

    @Override
    public double proposal() {
        if (rootOnlyInput.get()) {
            final Tree tree = treeInput.get();
            if (tree.getRoot().isFake()) {
                return Double.NEGATIVE_INFINITY;
            }
        }
        return super.proposal();
    }
}
