package beast.evolution.tree;

import beast.core.Input;
import beast.core.Logger;

/**
 * Alexandra Gavryushkina
 */
/**
 * @deprecated This option is not supported. Starting from v.1.1.1 Tree class supports sampled ancestors and
 * some of the classes that work with ZeroBranchSATree are not used anymore others are replaced.
 * */
@Deprecated
public class SATreeLogger extends Logger {

    public Input<Boolean> logWithZeroBranchesInput = new Input<Boolean>("logWithZeroBranches", "If it is true than sampled ancestors " +
            "are logged as tips on zero branches otherwise they are logged as single child nodes", false);

    @Override
    public void initAndValidate() {
        super.initAndValidate();
        ZeroBranchSATree tree = (ZeroBranchSATree)loggersInput.get().get(0);
        tree.logWithZeroBranches = logWithZeroBranchesInput.get();
    }

}
