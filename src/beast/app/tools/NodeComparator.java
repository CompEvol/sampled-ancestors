package beast.app.tools;

import beast.evolution.tree.Node;

import java.util.Comparator;

/**
 *
 */

public class NodeComparator implements Comparator<Node> {

    public int compare(Node node1, Node node2) {
        if (node1.getHeight() < node2.getHeight())
            return 1;
        else if (node1.getHeight() > node2.getHeight())
            return -1;
        else return 0;
    }

}
