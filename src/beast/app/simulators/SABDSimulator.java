package beast.app.simulators;

import beast.base.evolution.tree.Node;
import beast.base.util.Randomizer;

import java.io.*;
import java.util.*;

/**
 *
 */
public class SABDSimulator {

    double psi, mu, lambda, r, rho, rhoSamplingTime;    // these are model parameters
    final static int BIRTH = 0;
    final static int DEATH = 1;
    final static int SAMPLING = 2;

    //int lowCount=0, highCount =0;

    private int sampleCount; //a counter of sampled nodes that counts nodes during simulation and also used for numbering
    //internal nodes when collecting nodes that will be presented in 'sampled tree' from the
    // full simulated tree
    private HashSet<Node> sampledNodes; // a set where sampled nodes collected during the simulation
    private HashSet<Node> extinctNodes = new HashSet<Node>(); // nodes that died out
    double origin = 0; //is the distance between the origin (0) and the youngest sampled node
    boolean rhoSamplingTimeReached = false; //is true if at least one individual reached rhoSamplingTime
    Node root; // the root of the tree
    int sampledNodeNumber;
    int rhoSampledNodeNumber;

    /**
     *
     * @param newLambda as above
     * @param newMu     as above
     * @param newPsi    as above
     * @param newR      as above
     * @param newRho    as above
     * @param transform as above
     * @param newRSTime rho sampling time
     */
    public SABDSimulator(double newLambda, double newMu, double newPsi, double newR, double newRho, boolean transform, double newRSTime) {
        if (!transform) {
            psi = newPsi;
            mu = newMu;
            lambda = newLambda;
        } else {
            lambda = newLambda/(1-newMu);
            mu = newMu*lambda;
            psi = mu*newPsi/(1-newPsi);
        }
        r= newR;
        rho = newRho;
        sampleCount = 0;
        sampledNodes = new HashSet<Node>();
        rhoSamplingTime = newRSTime;
    }

    private void clear() {
        sampleCount = 0;
        sampledNodes.clear();
        extinctNodes = new HashSet<Node>();
        origin = 0;
        rhoSamplingTimeReached = false;
        root = null;
        sampledNodeNumber = 0;
        rhoSampledNodeNumber = 0;
    }

    /**
     * Simulate a tree under the model with rhoSamplingTime.
     * The simulation is stopped at rho sampling time and all existing lineages are cut
     * at this time.
     * Note that nodes in the tree have negative heights (the origin node has height 0)
     * @return 1 if simulated tree has finalSampleCount sampled nodes and -1 if the process stopped
     * (all the individuals died out) before the necessary number of samples had been reached.
     */
    public int simulate() { // (PrintStream treeWriter, PrintStream writer, int[] leafCount) {
        //clear old values
        clear();

        //create an initial node (origin of tree)
        Node initial = new Node();
        initial.setNr(-1);
        initial.setHeight(0.0);
        ArrayList<Node> tipNodes = new ArrayList<Node>();    // an array of nodes at the previous stage of simulation
        tipNodes.add(initial);

        //At each stage, for each node in TipNodes array simulate the next event and
        //collect children nodes resulting from this event to newTipNodes array.
        //After each stage, tipNodes represents tip nodes of the simulated tree that haven't died till this point
        do {
            Node node = tipNodes.get(0);
            int typeOfEvent;
            double[] timeIntervals = new double[3];
            double[] sortedTimeIntervals = new double[3];
            timeIntervals[0] = Randomizer.nextExponential(lambda);
            timeIntervals[1] = Randomizer.nextExponential(mu);
            timeIntervals[2] = Randomizer.nextExponential(psi);
            System.arraycopy(timeIntervals, 0, sortedTimeIntervals, 0, 3);
            Arrays.sort(sortedTimeIntervals);
            double timeInterval = sortedTimeIntervals[0];
            if (timeInterval == timeIntervals[0]) {
                typeOfEvent=BIRTH;
            } else {
                if (timeInterval == timeIntervals[1]) {
                    typeOfEvent=DEATH;
                } else typeOfEvent=SAMPLING;
            }
            tipNodes.remove(0);
            for (Node child: getNewNodes(node, typeOfEvent, timeInterval))  {
                int insPoint = -Collections.binarySearch(tipNodes, child, nodeComparator) - 1;
                tipNodes.add(insPoint, child);
            }
        }  while (!tipNodes.isEmpty()); //stop simulating tree when either
        // all the tip nodes are younger than rhoSamplingTime or all the individuals died



        if (sampledNodes.isEmpty() || !rhoSamplingTimeReached) {
            return -1;
        }

        // traverse the tree from sampled nodes towards the root and mark
        // the nodes that belong to the sampled tree
        HashSet<Node> parents;
        HashSet<Node> children = sampledNodes;
        while (children.size() > 1 ) {
            parents = collectParents(children);
            children = parents;
        }

        //the unique node remained in the array is the root of the sampled tree
        root = new Node();
        for (Node node:children) {
            root=node;
        }

        removeSingleChildNodes(root);

        if (root.getChildCount()==1) {
            Node newRoot = root.getLeft();
            root=newRoot;
        }

//        if (root.getLeafNodeCount() > 250){
//            highCount++;
//            return -1;
//        }
//        if (root.getLeafNodeCount() < 5){
//            lowCount++;
//            return -3;
//        }
        //writer.println("tree");
        //writer.println(root.toShortNewick(false));
        //treeWriter.println(root.toShortNewick(false) + ";");
        //writer.println("traits");
//        double minSampleAge = 0.0;
//        if (rho != 0.0) {
//            printTraitsWithRhoSamplingTime(root, writer);
//        }  else {
//            printTraitsWithRhoSamplingTime(root, writer);
//            writer.println("sampled ancestors");
//            minSampleAge = printSAWithRhoSamplingTime(root, writer);
//        }
//        writer.println("parameters");
//        if (rho != 0.0) {
//            writer.println(origin+rhoSamplingTime);
//            writer.println(root.getHeight()+rhoSamplingTime);
//        }   else {
//            writer.println(origin+rhoSamplingTime - minSampleAge);
//            writer.println(root.getHeight()+rhoSamplingTime  - minSampleAge);
//        }
//        writer.println(countSA(root));
        //rootHeight[0] = origin+root.getHeight();
        //System.out.println(origin);
        sampledNodeNumber = root.getLeafNodeCount();
        //writer.println(leafCount[0]);
        return 1;
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
        if (rhoSamplingTime != 0 && height + rhoSamplingTime <= 0) {
            double randomFrom01 = Randomizer.nextDouble();
            if (randomFrom01 <= rho) {
                node.setHeight(-rhoSamplingTime);
                node.setNr(sampleCount);
                sampledNodes.add(node);
                node.setID("A"+sampleCount);
                sampleCount++;
                rhoSampledNodeNumber++;
                rhoSamplingTimeReached = true;
            }
            if (rho == 0.0) {
                rhoSamplingTimeReached = true;
            }
            return newNodes;
        }
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
                extinctNodes.add(node);
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
                    right.setID("A"+sampleCount);
                } else {
                    node.setNr(sampleCount);
                    sampledNodes.add(node);
                    node.setID("A"+sampleCount);
                }
                sampleCount++;
            }
            break;

        }
        return newNodes;
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
     * Node1 is less than node2 if node1 is closer to the root (or to the origin)
     * than node2. Note that, since the nodes heights are negative, node1 is less than node2 if
     * node1 height is greater than node2 height.
     */
    private Comparator<Node> nodeComparator = new Comparator<Node>() {
        public int compare(Node node1, Node node2) {
            return (node2.getHeight() - node1.getHeight() < 0)?-1:1;
        }
    };

    public int countSA(Node node){
        if (!node.isLeaf()) {
            return countSA(node.getLeft()) + countSA(node.getRight());
        } else {
            if (node.isDirectAncestor()) {
                return 1;
            } else return 0;
        }
    }

    private void printTraits(Node node, PrintStream writer){
        if (node.isLeaf()){
            writer.println(node.getID() + "=" + (origin + node.getHeight()) + ',');
        } else {
            printTraits(node.getLeft(), writer);
            printTraits(node.getRight(), writer);
        }
    }

    public void printTraitsWithRhoSamplingTime(Node node, PrintStream writer){
        if (node.isLeaf()){
            writer.println(node.getID() + "=" + (rhoSamplingTime + node.getHeight()) + ',');
        } else {
            printTraitsWithRhoSamplingTime(node.getLeft(), writer);
            printTraitsWithRhoSamplingTime(node.getRight(), writer);
        }
    }

    private double printSAWithRhoSamplingTime(Node node, PrintStream writer){
        if (node.isLeaf()){
            if (node.isDirectAncestor()) {
                writer.println(node.getID() + "=1");
            }   else {
                writer.println(node.getID() + "=0");
            }
            return rhoSamplingTime + node.getHeight();
        } else {
            double minLeft = printSAWithRhoSamplingTime(node.getLeft(), writer);
            double minRight = printSAWithRhoSamplingTime(node.getRight(), writer);
            return Math.min(minLeft, minRight);
        }
    }

    public HashSet<String> printInternalNodeAges(Node node, PrintStream writer) {

        HashSet<String> extantDescendants = new HashSet<>();

        if (!node.isLeaf()) {
            HashSet<String> leftExtantDescendants = printInternalNodeAges(node.getLeft(), writer);
            HashSet<String> rightExtantDescendants = printInternalNodeAges(node.getRight(), writer);
            extantDescendants.addAll(leftExtantDescendants);
            extantDescendants.addAll(rightExtantDescendants);

            if (!leftExtantDescendants.isEmpty() && !rightExtantDescendants.isEmpty()) {
                writer.println(rhoSamplingTime + node.getHeight() + extantDescendants.toString());
            }
        } else {
            if (node.getHeight() + rhoSamplingTime == 0.0) {
                extantDescendants.add(node.getID());
            }
        }

        return extantDescendants;
    }

}
