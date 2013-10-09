package beast.evolution.tree;

import beast.core.CalculationNode;
import beast.core.Function;
import beast.core.Input;
import beast.core.Loggable;

import java.io.PrintStream;

/**
 * @author Alexandra Gavryushkina
 */
public class SALogger extends CalculationNode implements Loggable, Function {
    public Input<Tree> treeInput = new Input<Tree>("tree", "tree to report height for.", Input.Validate.REQUIRED);

    @Override
    public void initAndValidate() {
        // nothing to do
    }

    @Override
    public void init(PrintStream out) throws Exception {
        final Tree tree = treeInput.get();
        if (getID() == null || getID().matches("\\s*")) {
            out.print(tree.getID() + ".SAcount\t");
        } else {
            out.print(getID() + "\t");
        }
    }

    @Override
    public void log(int nSample, PrintStream out) {
        final Tree tree = treeInput.get();
        out.print(((ZeroBranchSATree)tree).getDirectAncestorNodeCount() + "\t");
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
        return ((ZeroBranchSATree)treeInput.get()).getDirectAncestorNodeCount();
    }

    @Override
    public double getArrayValue(int iDim) {
        return ((ZeroBranchSATree)treeInput.get()).getDirectAncestorNodeCount();
    }

}
