package beast.app.simulators;

import beast.evolution.tree.Node;
import beast.evolution.tree.ZeroBranchSANode;
import beast.util.Randomizer;

import java.util.*;

/**
 *
 */
public class SABDTreeSimulator {

    double psi, mu, lambda, r, rhoSamplingTime;    // these are model parameters
    final static int BIRTH = 0;
    final static int DEATH = 1;
    final static int SAMPLING = 2;

    int finalSampleCount;  // the number of sampled nodes in the simulated tree
    private int sampleCount; //a counter of sampled nodes that count nodes during simulation and also used for numbering
    //internal nodes when collecting nodes that will be presented in 'sampled tree' from the
    // full simulated tree
    private HashSet<Node> sampledNodes; // a set where sampled nodes collected during the simulation
    private HashSet<Node> extinctNodes = new HashSet<Node>(); // nodes that died out
    double origin = 0; //is the distance between the origin (0) and the youngest sampled node
    boolean rhoSamplingTimeReached = false; //is true if at least one individual reached rhoSamplingTime

    /**
     * construct a tree simulator under birth-death serially-sampled + one rho sampling time model
     * @param newLambda  birth rate
     * @param newMu      death rate
     * @param newPsi     sampling rate
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
     *
     * @param newLambda
     * @param newMu
     * @param newPsi
     * @param newR
     * @param newRSTime  rho sampling time
     */
    public SABDTreeSimulator(double newLambda, double newMu, double newPsi, double newR, double newRSTime) {
        psi = newPsi;
        mu = newMu;
        lambda = newLambda;
        r= newR;
        sampleCount = 0;
        sampledNodes = new HashSet<Node>();
        rhoSamplingTime = newRSTime;
    }

    /**
     * Simulate a tree under the model. The simulation is stopped when the number
     * of sampled nodes reaches finalSampleCount or if the process dies out.
     * Note that nodes in the tree have negative heights (the origin node has height 0)
     * @return 1 if simulated tree has finalSampleCount sampled nodes and -1 if the process stopped
     * (all the individuals died out) before the necessary number of samples had been reached.
     */
    public int simulate() {
        //create an initial node (origin of tree)
        Node initial = new ZeroBranchSANode();
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

        if (sampledNodes.isEmpty() || sampledNodes.size() < finalSampleCount) {
            return -1;
        }


        //remove excess of sampled nodes
        removeSampleExcess();

        HashSet<Node> parents;
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
        //System.out.println("tree");
        System.out.println(root.toShortNewick(false) + ";");
        //System.out.println(origin);
        return 1;
    }

    public int simulateWithRho1() {
        Node initial = new ZeroBranchSANode();
        initial.setNr(-1);
        initial.setHeight(0.0);
        ArrayList<Node> tipNodes = new ArrayList<Node>();    // an array of nodes at the previous stage of simulation
        tipNodes.add(initial);
        double lineage_sampleCount;
        HashSet<Node> parents = new HashSet<Node>();
        int typeOfEvent;

        do{
            double commonRate = (lambda + mu + psi)*tipNodes.size();
            double timeInterval = Randomizer.nextExponential(commonRate);

            //pick the type of event
            double tmp = Randomizer.nextDouble() * (lambda + mu + psi);
            if (tmp < lambda) {
                typeOfEvent = BIRTH;
            } else if (tmp < lambda+mu) {
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

            if (typeOfEvent == BIRTH || typeOfEvent == SAMPLING) {
                lineage_sampleCount = tipNodes.size()+sampledNodes.size() - 1;
            }  else {
                lineage_sampleCount = parents.size()+sampledNodes.size() + 1;
            }

        } while (lineage_sampleCount < finalSampleCount);

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

        for (Node tipNode:tipNodes) {
            tipNode.setNr(sampleCount);
            sampledNodes.add(tipNode);
            sampleCount++;
        }

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
        //System.out.println(root.getLeafNodeCount());
        return 1;
    }

    /**
     * Simulate a tree under the model with rho parameter. The simulation is stopped when the number
     * of sampled nodes reaches finalSampleCount or if the process dies out.
     * Note that nodes in the tree have negative heights (the origin node has height 0)
     * @return 1 if simulated tree has finalSampleCount sampled nodes and -1 if the process stopped
     * (all the individuals died out) before the necessary number of samples had been reached.
     */
    public int simulateWithRho() {
        //create an initial node (origin of tree)
        Node initial = new ZeroBranchSANode();
        initial.setNr(-1);
        initial.setHeight(0.0);
        ArrayList<Node> tipNodes = new ArrayList<Node>();    // an array of nodes at the previous stage of simulation
        tipNodes.add(initial);
        int ancestorCount;
        double time;
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

            if (tipNodes.isEmpty()){
                return -1;
            }

            Node node1 = tipNodes.get(0);
            //count the number of lineages at time of node1
            HashSet<Node> parents = new HashSet<Node>();
            for (Node child:tipNodes) {
                parents.add(child.getParent());
            }
            ancestorCount = parents.size();

            //if the other child of node parent is in tipNodes then decrease lineage number by one
            Node parent = node1.getParent();
            Node otherChild;
            if (node1.equals(parent.getLeft())){
                otherChild = parent.getRight();
            } else {
                otherChild = parent.getLeft();
            }
            if (parents.contains(otherChild)){
                ancestorCount--;
            }

            time = tipNodes.get(0).getHeight();
            for (Node extNode:extinctNodes){
                if (extNode.getHeight() < time){
                    ancestorCount++;
                }
            }

            ancestorCount += sampledNodes.size();

        }  while (ancestorCount<finalSampleCount);

        if (ancestorCount != finalSampleCount) {
            System.out.println("Something is wrong");
        }

        //Cut the process at the last node time and remove children added at
        // the last stage because the process has stopped and we don't need to know
        //what happens after this point
        for (Node node1:tipNodes) {
            Node parent = node1.getParent();
            if (parent != null){
                parent.setHeight(time);
                parent.setNr(sampleCount);
                sampledNodes.add(parent);
                sampleCount++;
                parent.removeAllChildren(false);
            }
        }

        for (Node extNode:extinctNodes) {
            if (extNode.getHeight() < time) {
                extNode.setHeight(time);
                extNode.setNr(sampleCount);
                sampledNodes.add(extNode);
            }
        }


        HashSet<Node> parents;
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
        System.out.println(root.toShortNewick(false) + ";");
        //System.out.println(root.getLeafNodeCount());
        return 1;
    }

    /**
     * Simulate a tree under the model with rhoSamplingTime.
     * The simulation is stopped at rho sampling time and all existing lineages are cut
     * at this time.
     * Note that nodes in the tree have negative heights (the origin node has height 0)
     * @return 1 if simulated tree has finalSampleCount sampled nodes and -1 if the process stopped
     * (all the individuals died out) before the necessary number of samples had been reached.
     */
    public int simulateWithRhoSamplingTime() {
        //create an initial node (origin of tree)
        Node initial = new ZeroBranchSANode();
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
        Node root = new ZeroBranchSANode();
        for (Node node:children) {
            root=node;
        }

        removeSingleChildNodes(root);

        if (root.getChildCount()==1) {
            Node newRoot = root.getLeft();
            root=newRoot;
        }
        //System.out.println("tree");
        System.out.println(root.toShortNewick(false) + ";");
        //System.out.println(origin);
        System.out.println(root.getLeafNodeCount());
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
                if (r < remain) {
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

    public static void main (String[] args) {

        int treeCount = 50;
        double[] origins = new double[treeCount];

        int index=0;
        do {
            SABDTreeSimulator simulator = new SABDTreeSimulator(1.0, 0.1, 0.4, 0.5, 200);
            if (simulator.simulate()>0) {
                origins[index] = simulator.origin;
                index++;
            }
        } while (index<treeCount);

//        for (int i=0; i<treeCount; i++) {
//            System.out.println(origins[i]);
//        }


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

    /**
     *
     * @param tipNodes
     * @return true if the number of sampled nodes exceeds finalSampleCount
     * and all the nodes in TipNodes array are younger than the youngest sampled node.
     */

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

//    private void printNodeHeights(Node node) {
//        for (Node child:node.getChildren()) {
//            if (!child.isLeaf()) {
//                printNodeHeights(child);
//            }
//            System.out.println(child.getHeight());
//        }
//    }

}
