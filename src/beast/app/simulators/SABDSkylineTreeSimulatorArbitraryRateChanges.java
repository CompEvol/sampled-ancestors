package beast.app.simulators;

import beast.core.Description;
import beast.evolution.tree.Node;
import beast.evolution.tree.ZeroBranchSANode;
import beast.util.Randomizer;

import java.util.*;

/**
 *@author Alexandra Gavryushkina
 */


@Description("Simulate a tree under Sampled Ancestor Birth-death Skyline model")
public class SABDSkylineTreeSimulatorArbitraryRateChanges {

    double rhoSamplingTime;
    double[] psi, mu, lambda, r, rateChangeTimes;    // these are model parameters
    final static int BIRTH = 0;
    final static int DEATH = 1;
    final static int SAMPLING = 2;
    int epidemicSizeAtStartSampling=0;
    int currentInterval =0;


    int finalSampleCount;  // the number of sampled nodes in the simulated tree
    private int sampleCount; //a counter of sampled nodes that count nodes during simulation and also used for numbering
    //internal nodes when collecting nodes that will be presented in 'sampled tree' from the
    // full simulated tree
    private HashSet<Node> sampledNodes; // a set where sampled nodes are collected during the simulation
    double origin = 0; //is the distance between the origin (0) and the youngest sampled node
    private HashSet<Node> extinctNodes = new HashSet<Node>(); // nodes that died out
    boolean rhoSamplingTimeReached = false; //is true if at least one individual reached rhoSamplingTime

    /**
     * construct a skyline model tree simulator
     * @param newLambda  birth rates
     * @param newMu      death rates
     * @param newPsi     sampling rates
     * @param newR       removing parameters
     * @param newFSCount the number of sampled individuals
     * @param newRCTime  rate change times (as distances from the origin)
     */
    public SABDSkylineTreeSimulatorArbitraryRateChanges(double[] newLambda, double[] newMu, double[] newPsi, double[] newR, int newFSCount, double[] newRCTime) {
        if (newLambda.length != newMu.length || newLambda.length != newPsi.length || newLambda.length != newR.length || newLambda.length -1 != newRCTime.length) {
            System.out.println("The array lengths do not match");
            System.exit(0);
        }
        psi = newPsi;
        mu = newMu;
        lambda = newLambda;
        r= newR;
        rateChangeTimes = newRCTime;
        sampleCount = 0;
        sampledNodes = new HashSet<Node>();
        finalSampleCount = newFSCount;
    }

    /**
     * Simulate a tree under the model. Simulation is stopped if
     * either the number of tips reaches the finalSampleCount or the population dies out.
     * Note that nodes in the tree have negative heights (the origin node has height 0)
     */
    public int simulate() {

        //create an initial node (origin of tree)
        Node initial = new ZeroBranchSANode();
        initial.setNr(-1);
        initial.setHeight(0.0);
        ArrayList<Node> tipNodes = new ArrayList<Node>();    // an array of nodes at the previous stage of simulation
        tipNodes.add(initial);
        double lineage_sampleCount;
        HashSet<Node> parents = new HashSet<Node>();
        int typeOfEvent;


        do{
            double commonRate = (lambda[currentInterval] + mu[currentInterval] + psi[currentInterval])*tipNodes.size();
            double timeInterval = Randomizer.nextExponential(commonRate);

            while (currentInterval < rateChangeTimes.length && tipNodes.get(0).getHeight() - timeInterval + rateChangeTimes[currentInterval] < 0) {
                double currentTimeToShiftTime = rateChangeTimes[currentInterval] + tipNodes.get(0).getHeight();  //distance between the current tip node time and the time of the next rate shift
                currentInterval++;
                commonRate = (lambda[currentInterval] + mu[currentInterval] + psi[currentInterval])*tipNodes.size();
                timeInterval = Randomizer.nextExponential(commonRate) + currentTimeToShiftTime;  //waiting time to the next event is counted from the last rate shift time
            }

            //pick the type of event
            double tmp = Randomizer.nextDouble() * (lambda[currentInterval] + mu[currentInterval] + psi[currentInterval]);
            if (tmp < lambda[currentInterval]) {
                typeOfEvent = BIRTH;
            } else if (tmp < lambda[currentInterval]+mu[currentInterval]) {
                typeOfEvent = DEATH;
            } else {
                typeOfEvent = SAMPLING;
            }
            int nodeNumber = Randomizer.nextInt(tipNodes.size());
            Node node = tipNodes.get(nodeNumber);
            tipNodes.remove(nodeNumber);
            tipNodes.addAll(getNewNodes(node, typeOfEvent, timeInterval));

            if (tipNodes.isEmpty()){
                return -1;
            }

            double newHeight = node.getHeight();
            //count the number of lineages at time of node1
            for (Node child:tipNodes) {
                child.setHeight(newHeight);
            }
//
//            if (typeOfEvent == BIRTH || typeOfEvent == SAMPLING) {
//                lineage_sampleCount = tipNodes.size()+sampledNodes.size() - 1;
//            }  else {
//                lineage_sampleCount = parents.size()+sampledNodes.size() + 1;
//            }
            lineage_sampleCount = sampledNodes.size();

        } while (lineage_sampleCount < finalSampleCount);

        origin = -tipNodes.get(tipNodes.size()-1).getHeight();

        for (int i=0; i<rateChangeTimes.length; i++) {
            if (origin - rateChangeTimes[i] < 0) {
                return -1;
            }
        }

        if (typeOfEvent == BIRTH) {
            Node child1 = tipNodes.get(tipNodes.size()-2);
            Node child2 = tipNodes.get(tipNodes.size()-1);
            if (!child1.getParent().equals(child2.getParent())) {
                System.out.println("Something is wrong in deleting children");
                System.exit(0);
            }
            Node parent = child1.getParent();
            tipNodes.remove(child1);
            tipNodes.remove(child2);
            tipNodes.add(parent);
            parent.removeAllChildren(false);
        }

        if (typeOfEvent == SAMPLING) {
            Node child = tipNodes.get(tipNodes.size()-1);
            Node parent = child.getParent();
            if (parent != null && child.getHeight() == parent.getHeight()) {
                int nodeNr = parent.getRight().getNr();
                parent.setNr(nodeNr);
                if (!sampledNodes.contains(parent.getRight())){
                    System.out.println("Wrong sampling");
                    System.exit(0);
                }
                sampledNodes.remove(parent.getRight());
                sampledNodes.add(parent);
                tipNodes.remove(child);
                parent.removeAllChildren(false);
            }
        }

//        for (Node tipNode:tipNodes) {
//            tipNode.setNr(sampleCount);
//            sampledNodes.add(tipNode);
//            sampleCount++;
//        }

        HashSet<Node> children = sampledNodes;
        while (children.size() > 1 ) {
            parents = collectParents(children);
            children = parents;
        }

        //the unique node remained in the array is the root of the sampled tree
        Node root = new ZeroBranchSANode();
        for (Node node:children) {
            root=node;
        }

        removeSingleChildNodes(root);

        if (root.getChildCount()==1) {
            Node newRoot = root.getLeft();
            root=newRoot;
        }
        System.out.println(root.toShortNewick(false)); // + ";");
        System.out.println(origin - rateChangeTimes[0]);
        System.out.println(origin - rateChangeTimes[1]);
        //System.out.println(root.getLeafNodeCount());
        return 1;

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
            node.setHeight(-rhoSamplingTime);
            node.setNr(sampleCount);
            sampledNodes.add(node);
            sampleCount++;
            rhoSamplingTimeReached = true;
            return newNodes;
        }
        node.setHeight(height);
        switch (typeOfEvent) {
            case BIRTH: {
                Node left = new ZeroBranchSANode();
                left.setNr(-1);
                Node right = new ZeroBranchSANode();
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
                if (r[currentInterval] < remain) {
                    Node left = new ZeroBranchSANode();
                    left.setNr(-1);
                    Node right = new ZeroBranchSANode();
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

    public void connectParentToChildren(Node parent, Node left, Node right) {
        parent.setLeft(left);
        parent.setRight(right);
        left.setParent(parent);
        right.setParent(parent);
    }

    public static void main (String[] args) {

        int treeCount = 50;
        double[] origins = new double[treeCount];
        double[] newLambda = {1.5, 1.2, 0.5};
        double[] newMu = {0.5, 0.6, 0.2};
        double[] newPsi = {0.4, 0.5, 0.1};
        double[] newR = {0.1, 0.5, 0.9};
        double[] newRateChangeTimes = {3., 6.};


        int index=0;
        do {
            SABDSkylineTreeSimulatorArbitraryRateChanges simulator = new SABDSkylineTreeSimulatorArbitraryRateChanges(newLambda, newMu, newPsi, newR, 200, newRateChangeTimes);
            if (simulator.simulate()>0) {
                origins[index] = simulator.origin;
                index++;
            }
        } while (index<treeCount);

//        for (int i=0; i<treeCount; i++) {
//            System.out.println(origins[i]);
//        }


    }


}
