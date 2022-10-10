package sa.evolution.tree;

import java.io.PrintStream;

import beast.base.core.BEASTObject;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.core.Loggable;
import beast.base.evolution.tree.Tree;

public class OffsetLogger extends BEASTObject implements Loggable {
	
	public Input<TreeWOffset> treeWOffsetInput =
            new Input<TreeWOffset>("treeWOffset", "Fully extinct tree", Validate.REQUIRED);

	@Override
	public void initAndValidate() {}

	@Override
	public void init(PrintStream out) {
		final Tree tree = treeWOffsetInput.get().getTree();
        if (getID() == null || getID().matches("\\s*")) {
            out.print(tree.getID() + ".offset\t");
        } else {
            out.print(getID() + "\t");
        }
	}

	@Override
	public void log(long sample, PrintStream out) {
		final TreeWOffset tree = treeWOffsetInput.get();
        out.print(tree.getOffset() + "\t");
	}

	@Override
	public void close(PrintStream out) {}

}
