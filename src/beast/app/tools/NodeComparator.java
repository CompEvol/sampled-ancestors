package beast.app.tools;

import beast.evolution.tree.Node;

/**
 * Created with IntelliJ IDEA.
 * User: agav755
 * Date: 22/05/13
 * Time: 9:24 AM
 * To change this template use File | Settings | File Templates.
 */

public abstract class NodeComparator implements Comparable<Node> {

    public int compare(Node node1, Node node2) {
        if (node1.getHeight() < node2.getHeight())
            return 1;
        else if (node1.getHeight() > node2.getHeight())
            return -1;
        else return 0;
    }
}
