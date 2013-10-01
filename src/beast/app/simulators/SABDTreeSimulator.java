package beast.app.simulators;

import beast.evolution.tree.Node;
import beast.util.Randomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 *
 */
public class SABDTreeSimulator {

    double psi, mu, lambda, r;    // these are model parameters
    final static int BIRTH = 0;
    final static int DEATH = 1;
    final static int SAMPLING = 2;

    int finalSampleCount;  // the number of sampled nodes in the simulated tree
    private int sampleCount; //a counter of sampled nodes that count nodes during simulation and also used for numbering
    //internal nodes when collecting nodes that will be presented in 'sampled tree' from the
    // full simulated tree
    private HashSet<Node> sampledNodes; // a set where sampled nodes collected during the simulation
    double origin = 0; //is the distance between the origin (0) and the youngest sampled node

    /**
     * construct a skyline model tree simulator with only sampling rate changing through
     * it changes from 0 to some positive rate at sampling start time (newSSTime)
     * @param newLambda  birth rate
     * @param newMu      death rate
     * @param newPsi     non-zero sampling rate after sampling start time
     * @param newR       removing parameter
     * @param newFSCount the number of sampled individuals
     */
    public SABDTreeSimulator(double newLambda, double newMu, double newPsi, double newR, int newFSCount) {
        psi = newPsi;
        mu = newMu;
        lambda = newLambda;
        r= newR;
        sampleCount = 0;
        sampledNodes = new HashSet<Node>();
        finalSampleCount = newFSCount;
    }

    /**
     * Simulate a tree under the model.
     * Note that nodes in the tree have negative heights (the origin node has height 0)
     */
    public void simulate() {
        //create an initial node (origin of tree)
        Node initial = new Node();
        initial.setNr(-1);
        initial.setHeight(0.0);
        ArrayList<Node> tipNodes = new ArrayList<Node>();    // an array of nodes at the previous stage of simulation
        tipNodes.add(initial);
        boolean stopCondition = false;
        //At each stage, for each node in TipNodes array simulate the next event and
        //collect children nodes resulting from this event to newTipNodes array.
        //After each stage, tipNodes represents tip nodes of the simulated tree that haven't died till this point
        do {
            Node node = tipNodes.get(0);
            double nextRate = Randomizer.nextDouble();
            int typeOfEvent;
            double timeInterval;
            //if the node is older than samplingStartTime then sampling event becomes possible
            timeInterval = Randomizer.nextExponential(lambda+mu+psi);
            if (nextRate < lambda/(lambda+mu+psi)) {
                typeOfEvent=BIRTH;
            } else {
                if (nextRate < (lambda+mu)/(lambda+mu+psi)) {
                    typeOfEvent=DEATH;
                } else typeOfEvent=SAMPLING;
            }
            tipNodes.remove(0);
            for (Node child: getNewNodes(node, typeOfEvent, timeInterval))  {
                int insPoint = -Collections.binarySearch(tipNodes, child, nodeComparator) - 1;
                tipNodes.add(insPoint, child);

            }
            stopCondition = evaluateStopCondition(tipNodes);
        }  while (!stopCondition && !tipNodes.isEmpty()); //stop simulating tree when either there are enough sampled nodes
        // or all the individuals died

        //Remove children added at the last stage because the process has stopped and we don't need to know
        //what happens after this point
        for (Node node:tipNodes) {
            Node parent = node.getParent();
            if (parent != null){
                if (parent.getRight() != null && parent.getRight().getNr() != -1) {
                    parent.setNr(parent.getRight().getNr());
                    sampledNodes.remove(parent.getRight());
                    sampledNodes.add(parent);
                }
                parent.removeAllChildren(false);
            }

        }

        //remove excess of sampled nodes
        if (!sampledNodes.isEmpty()) {
            removeSampleExcess();
        }

        HashSet<Node> parents;
        HashSet<Node> children = sampledNodes;
        while (children.size() > 1 ) {
            parents = collectParents(children);
            children = parents;
        }

        //the unique node remains in the array is the root of the sampled tree
        Node root = new Node();
        for (Node node:children) {
            root=node;
        }

        removeSingleChildNodes(root);

        if (root.getChildCount()==1) {
            Node newRoot = root.getLeft();
            root=newRoot;
        }

        System.out.println(root.toShortNewick(false) + ";");

        //collect information about the tree
    }

    /**
     * First it change the height of this node and then define children (depending on the type of event) of this node.
     * New nodes get -1 number except of sampled nodes that get the next number defined by sampleCount.
     * @param node
     * @param typeOfEvent if = BIRTH then two children added to this node,
     *                    if = DEATH or SAMPLING+removing then no children added
     *                    if = SAMPLING and no-removing then one child added
     * @param timeInterval the node height decrease (i.e. this nodes become closer to present) by this value
     * @return the children of this node that the event results in (is empty in case of death event or sampling+removing
     * event). At this point, children have the same heights as the node and will get their actual heights when an event
     * happens to them.
     */
    private ArrayList<Node> getNewNodes(Node node, int typeOfEvent, double timeInterval) {
        ArrayList<Node> newNodes = new ArrayList<Node>();
        double height = node.getHeight() - timeInterval;
        node.setHeight(height);

        switch (typeOfEvent) {
            case BIRTH: {
                Node left = new Node();
                left.setNr(-1);
                Node right = new Node();
                right.setNr(-1);
                left.setHeight(height);
                right.setHeight(height);
                connectParentToChildren(node, left, right);
                newNodes.add(left);
                newNodes.add(right);
            }
            break;
            case DEATH: {

            }
            break;
            case SAMPLING: {
                double remain = Randomizer.nextDouble();
                if (r < remain) {
                    Node left = new Node();
                    left.setNr(-1);
                    Node right = new Node();
                    right.setNr(sampleCount);
                    left.setHeight(height);
                    right.setHeight(height);
                    connectParentToChildren(node, left, right);
                    newNodes.add(left);
                    sampledNodes.add(right);
                } else {
                    node.setNr(sampleCount);
                    sampledNodes.add(node);
                }
                sampleCount++;
            }
            break;

        }
        return newNodes;
    }

    public static void main (String[] args) {

        int treeCount = 10;
        double[] origins = new double[10];

        for (int i=0; i<treeCount; i++) {
            SABDSkylineTreeSimulator simulator = new SABDSkylineTreeSimulator(1.0, 0.1, 0.5, 0.1, 30, 8.0);
            simulator.simulate();
            origins[i] = simulator.origin;
        }

        for (int i=0; i<treeCount; i++) {
            System.out.println(origins[i]);
        }


    }

    public void connectParentToChildren(Node parent, Node left, Node right) {
        parent.setLeft(left);
        parent.setRight(right);
        left.setParent(parent);
        right.setParent(parent);
    }

    /**
     * collect parents of nodes in children set. During the collection all visited nodes get non-negative numbers
     * in order to extract the sampled tree later.
     * @param children set of nodes
     * @return  parents of nodes in children set
     */
    public HashSet<Node> collectParents(HashSet<Node> children) {
        HashSet<Node> parents = new HashSet<Node>();
        for (Node node:children) {
            if (node.getParent() != null) {
                if (node.getParent().getNr() == -1){
                    node.getParent().setNr(sampleCount);
                    sampleCount++;
                }
                parents.add(node.getParent());
            } else {
                parents.add(node);
            }
        }
        return parents;
    }

    /**
     * Extract the sampled tree by discarding all the nodes that have -1 number simultaneously suppress single
     * child nodes (nodes with only one child numbered by non-negative number)
     * @param node
     */
    public void removeSingleChildNodes(Node node) {
        if (!node.isLeaf()) {
            Node left = node.getLeft();
            Node right = node.getRight();

            removeSingleChildNodes(left);
            removeSingleChildNodes(right);
            for (Node child:node.getChildren()) {
                if (child.getNr() == -1) {
                    node.removeChild(child);
                }
            }
            if (node.getChildCount() == 1 && node.getParent() != null) {
                Node parent = node.getParent();
                Node newChild =  node.getLeft();
                parent.removeChild(node);
                parent.addChild(newChild);
                newChild.setParent(parent);
                //node.setParent(null);
            }
        }
    }

    /**
     * sort nodes by their heights and then only keep finalSampleCount of nodes that have been sampled first
     * also change the sampleCount to represent the actual number of sampled nodes.
     */
    public void removeSampleExcess() {
        ArrayList<Node> sampledNodeList = new ArrayList<Node>(sampledNodes);
        Collections.sort(sampledNodeList, nodeComparator);
        if (sampledNodeList.size() > finalSampleCount) {
            for (int i=0; i<finalSampleCount; i++) {
                sampledNodeList.get(i).setNr(i);
            }
            for (int i=finalSampleCount; i<sampledNodeList.size(); i++) {
                sampledNodeList.get(i).setNr(-1);
            }
            sampledNodeList.subList(finalSampleCount, sampledNodes.size()).clear();
            sampledNodes = new HashSet<Node>(sampledNodeList);
            sampleCount = finalSampleCount;
            origin = - sampledNodeList.get(finalSampleCount-1).getHeight();
        } else {
            origin = - sampledNodeList.get(sampleCount-1).getHeight();
        }


    }

    public boolean evaluateStopCondition(ArrayList<Node> tipNodes){
        if (sampleCount >= finalSampleCount) {
            ArrayList<Node> sampledNodeList = new ArrayList<Node>(sampledNodes);
            Collections.sort(sampledNodeList, nodeComparator);
            double height = sampledNodeList.get(finalSampleCount - 1).getHeight();
            for (Node node:tipNodes) {
                if (node.getHeight() > height) {
                    return false;
                }
            }
            return true;
        }   else return false;
    }

    private Comparator<Node> nodeComparator = new Comparator<Node>() {
        public int compare(Node node1, Node node2) {
            return (node2.getHeight() - node1.getHeight() < 0)?-1:1;
        }
    };
}
