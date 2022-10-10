package sa.evolution.tree;

import java.io.PrintStream;

import beast.base.inference.CalculationNode;
import beast.base.core.Function;
import beast.base.core.Input;
import beast.base.core.Loggable;
import beast.base.evolution.tree.Tree;

/**
 * @author Alexandra Gavryushkina
 */
public class SampledAncestorLogger extends CalculationNode implements Loggable, Function {
    public Input<Tree> treeInput = new Input<Tree>("tree", "tree to report SA count for.", Input.Validate.REQUIRED);

    @Override
    public void initAndValidate() {
        // nothing to do
    }

    @Override
    public void init(PrintStream out) {
        final Tree tree = treeInput.get();
        if (getID() == null || getID().matches("\\s*")) {
            out.print(tree.getID() + ".SAcount\t");
        } else {
            out.print(getID() + "\t");
        }
    }

    @Override
    public void log(long nSample, PrintStream out) {
        final Tree tree = treeInput.get();
        out.print(tree.getDirectAncestorNodeCount() + "\t");
    }

    @Override
    public void close(PrintStream out) {
        // nothing to do
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public double getArrayValue() {
        return treeInput.get().getDirectAncestorNodeCount();
    }

    @Override
    public double getArrayValue(int iDim) {
        return treeInput.get().getDirectAncestorNodeCount();
    }

}

