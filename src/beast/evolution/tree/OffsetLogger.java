package beast.evolution.tree;

import java.io.PrintStream;

import beast.core.BEASTObject;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.Loggable;

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
