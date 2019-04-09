package beast.app.simulators;

import beast.core.Description;
import beast.evolution.tree.Node;
import beast.util.Randomizer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 *@author Alexandra Gavryushkina
 */


@Description("Simulate a tree under Sampled Ancestor Birth-death Skyline model")
public class SABDSkylineTreeSimulator {

    double psi, mu, lambda, r, samplingStartTime;    // these are model parameters
    final static int BIRTH = 0;
    final static int DEATH = 1;
    final static int SAMPLING = 2;
    int epidemicSizeAtStartSampling=0;


    int finalSampleCount;  // the number of sampled nodes in the simulated tree
    private int sampleCount; //a counter of sampled nodes that count nodes during simulation and also used for numbering
                             //internal nodes when collecting nodes that will be presented in 'sampled tree' from the
                             // full simulated tree
    private HashSet<Node> sampledNodes; // a set where sampled nodes are collected during the simulation
    double origin = 0; //is the distance between the origin (0) and the youngest sampled node
    /**
     * construct a skyline model tree simulator with only sampling rate changing through
     * it changes from 0 to some positive rate at sampling start time (newSSTime)
     * @param newLambda  birth rate
     * @param newMu      death rate
     * @param newPsi     non-zero sampling rate after sampling start time
     * @param newR       removing parameter
     * @param newFSCount the number of sampled individuals
     * @param newSSTime  sampling start time (as a distance from the origin)
     */
    public SABDSkylineTreeSimulator(double newLambda, double newMu, double newPsi, double newR, int newFSCount, double newSSTime) {
        psi = newPsi;
        mu = newMu;
        lambda = newLambda;
        r= newR;
        samplingStartTime = newSSTime;
        sampleCount = 0;
        sampledNodes = new HashSet<Node>();
        finalSampleCount = newFSCount;
    }

    /**
     * Simulate a tree under the model with one sampling rate change time. Simulation is stopped if
     * either the number of tips reaches the finalSampleCount or the population dies out.
     * Note that nodes in the tree have negative heights (the origin node has height 0)
     */
    public int simulate(PrintStream writer) {

        //create an initial node (origin of tree)
        Node initial = new Node();
        initial.setNr(-1);
        initial.setHeight(0.0);
        ArrayList<Node> tipNodes = new ArrayList<Node>();    // an array of temporary tip nodes sorted by their heights
        tipNodes.add(initial);
        boolean stopCondition;
        //At each stage, take the oldest node in tipNodes array, simulate the next event for this node, remove
        // this node from the tipNodes array and add its new children (if any) to tipNodes
        //After each stage, tipNodes represents tip nodes of the simulated tree that haven't died till this point
        do {
            Node node = tipNodes.get(0);
            int typeOfEvent;
            double timeInterval;
            double[] timeIntervals = new double[3];
            double[] sortedTimeIntervals = new double[3];

            //if the node is older than samplingStartTime then sampling event becomes possible
            if (node.getHeight() + samplingStartTime < 0) {
                timeIntervals[0] = Randomizer.nextExponential(lambda);
                timeIntervals[1] = Randomizer.nextExponential(mu);
                timeIntervals[2] = Randomizer.nextExponential(psi);
                System.arraycopy(timeIntervals, 0, sortedTimeIntervals, 0, 3);
                Arrays.sort(sortedTimeIntervals);
                timeInterval = sortedTimeIntervals[0];
                if (timeInterval == timeIntervals[0]) {
                    typeOfEvent=BIRTH;
                } else {
                    if (timeInterval == timeIntervals[1]) {
                        typeOfEvent=DEATH;
                    } else typeOfEvent=SAMPLING;
                }
            }  else {
                timeIntervals[0] = Randomizer.nextExponential(lambda);
                timeIntervals[1] = Randomizer.nextExponential(mu);
                if (timeIntervals[0] < timeIntervals[1]) {
                    timeInterval = timeIntervals[0];
                }  else timeInterval = timeIntervals[1];

                if (timeInterval == timeIntervals[0]) {
                    typeOfEvent=BIRTH;
                } else {
                    typeOfEvent=DEATH;
                }

                // if the next event happens after samplingStartTime when we should account fot the possibility
                // of sampling event happens after samplingStartTime and the proposed node age
                // (which is -(node.Height - timeInterval)). Note that samplingStartTime is given as a distance from the origin
                // and is positive, while node.Height - timeInterval is negative
                if (node.getHeight() - timeInterval +samplingStartTime < 0) {
                    double startTime_samplingEvent = Randomizer.nextExponential(psi);
                    if (samplingStartTime + startTime_samplingEvent + node.getHeight()-timeInterval < 0) {
                        typeOfEvent = SAMPLING;
                        timeInterval = samplingStartTime + startTime_samplingEvent + node.getHeight();
                    }
                }
            }
            tipNodes.remove(0);
            ArrayList<Node> newNodes = getNewNodes(node, typeOfEvent, timeInterval);
            for (Node child: newNodes)  {
                int insPoint = -Collections.binarySearch(tipNodes, child, nodeComparator) - 1;
                tipNodes.add(insPoint, child);

            }
            stopCondition = evaluateStopCondition(tipNodes);
        }  while (!stopCondition && !tipNodes.isEmpty()); //stop simulation either then the number of sampled nodes reach
                                                              //the given sample size (finalSampleCount)
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

        if (sampledNodes.size()<finalSampleCount) {
            return -1;
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

        root.setParent(null);

        writer.println("tree");
        writer.println(root.toShortNewick(false));// + ";");
        writer.println("traits");
        printTraits(root, writer);
        writer.println("parameters");
        writer.println(origin);
        writer.println(origin + root.getHeight());
        writer.println(countSA(root));

        writer.println(origin - samplingStartTime);
        return 1;
//        for (Node node:sampledNodes) {
//            System.out.println(node.getNr() + " = " + (origin+node.getHeight()) + ",");
//        }

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
        if (node.getHeight() + samplingStartTime > 0 && node.getHeight()-timeInterval +samplingStartTime >0) {
            epidemicSizeAtStartSampling++;
        }
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

                if (node.getHeight() + samplingStartTime >0 ) {
                    System.out.println("Sampling before sampling start time");
                    System.exit(0);
                }

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

        PrintStream writer = null;

        int treeCount = 100;
        double[] origins = new double[treeCount];
        int[] epidemicSizes = new int[treeCount];
        int index=0;

        try {
            writer = new PrintStream(new File("trees.txt"));
            //writer = System.out;
            do {
                double [] rates = {0.8, 0.4, 0.2, 0.8};
                SABDSkylineTreeSimulator simulator = new SABDSkylineTreeSimulator(rates[0], rates[1], rates[2], rates[3], 200, 10.0);
                //double[] rates = simulateParameters(3.0, 1.0, 0.8, 0.7);

                if (simulator.simulate(writer)>0) {
                    writer.println(rates[0]);
                    writer.println(rates[1]);
                    writer.println(rates[2]);
                    writer.println(rates[3]);
                    //origins[index] = simulator.origin;
                    //epidemicSizes[index]=simulator.epidemicSizeAtStartSampling;
                    index++;
                }
            } while (index<treeCount);
        } catch (IOException e) {
            //
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }

//        int sum1 = 0;
//        for (int i=0; i<treeCount; i++) {
//            System.out.println("Origin " + origins[i]);
//            sum1 += epidemicSizes[i];
//        }
//        System.out.println("mean epidemic size = " +(double)sum1/treeCount);

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
     * assess if the stop simulation condition is met. It is met if the sample size exceeds finalSampleCount
     * and all the nodes are younger then the age of the last sampled node (to find the last sampled node, sampled nodes
     * are sorted with respect to their heights and the node with finalSampleCount-1 number is the last sampled node).
     * @param tipNodes  current nodes that have to be younger then  the last sampled node
     * @return true if the stop condition is met and false otherwise
     */
    private boolean evaluateStopCondition(ArrayList<Node> tipNodes){

        if (sampledNodes.size() > finalSampleCount) {
            ArrayList<Node> sampledNodeList = new ArrayList<Node>(sampledNodes);
            Collections.sort(sampledNodeList, nodeComparator);
            double height=sampledNodeList.get(finalSampleCount-1).getHeight();

            for (int i=0; i<tipNodes.size(); i++) {
                if (tipNodes.get(i).getHeight() > height) {
                    return false;
                }
            }
            return true;
        } else return false;
    }


    /**
     * sort nodes by their heights and then keep only finalSampleCount of nodes that have been sampled first
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

    private void printTraits(Node node, PrintStream writer){
        if (node.isLeaf()){
            writer.println(node.getNr() + "=" + (origin + node.getHeight()) + ',');
        } else {
            printTraits(node.getLeft(), writer);
            printTraits(node.getRight(), writer);
        }
    }

    private int countSA(Node node){
        if (!node.isLeaf()) {
            return countSA(node.getLeft()) + countSA(node.getRight());
        } else {
            if (node.isDirectAncestor()) {
                return 1;
            } else return 0;
        }
    }


}
