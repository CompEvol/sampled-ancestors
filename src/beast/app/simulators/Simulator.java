package beast.app.simulators;

import beast.util.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Alexandra Gavryushkina
 */

public class Simulator {

    double[] parameters = new double[6];

    public Simulator(String[] parametersStr) {
        for (int i=0; i<6; i++) {
            parameters[i] = Double.parseDouble(parametersStr[i]); 
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 6) {
            System.out.println("There have to be six arguments for parameters: d, nu, s, r, rho, and t_origin");
        } else {
            Simulator simulator = new Simulator(args);
            simulator.simulateForTotalEvidence();
        }

    }

    private void simulateForTotalEvidence() throws Exception {

        PrintStream writer = null;
        //PrintStream treeWriter = null;

        int treeCount = 1;

        int index=0;
        int count=0;


        try {
            writer = new PrintStream(new File("trees_and_pars.txt"));
            //treeWriter = new PrintStream(new File("trees.txt"));


            for (int i=0; i< treeCount; i++) {
                //int meanLeafCount = 0;
                //int low=0;
                //int high=0;

                //parameters = {0.04, 0.6, 0.2, 0.0, 0.8};
                
                SABDSimulator simulator = new SABDSimulator(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4], true, parameters[5]);
                int result;
                do {
                    result = simulator.simulate();
//                    if (simulator.sampledNodeNumber < 5 || simulator.sampledNodeNumber > 250) {
//                        result = -1;
//                    }
                    count++;
                } while (result < 0);

                count--;

                writer.println("tree");
                writer.println(simulator.root.toShortNewick(false));
                //treeWriter.println(simulator.root.toShortNewick(false) + ";");

                writer.println("traits");
//              double minSampleAge = 0.0;
                simulator.printTraitsWithRhoSamplingTime(simulator.root, writer);

                writer.println("parameters");
                writer.println(simulator.rhoSamplingTime);
                writer.println(simulator.root.getHeight()+simulator.rhoSamplingTime);

                for (int j=0; j<3; j++) {
                    writer.println(parameters[j]);
                }
                writer.println(parameters[4]);
                //writer.println(simulator.countSA(simulator.root));
                writer.println("divergence times");
                simulator.printInternalNodeAges(simulator.root, writer);

                writer.println("fossil count");
                writer.println(simulator.sampledNodeNumber - simulator.rhoSampledNodeNumber);
                writer.println("rho sample count");
                writer.println(simulator.rhoSampledNodeNumber);
                writer.println("total sample count");
                writer.println(simulator.sampledNodeNumber);
                if (simulator.sampledNodeNumber < 5 || simulator.sampledNodeNumber > 250) {
                    System.out.print("Too few or too many sampled nodes: " + simulator.sampledNodeNumber +" in tree ");
                }


            }
            //System.out.print(meanLeafCount/(treeCount+count));
            //System.out.println();
            //System.out.println("Number of trees rejected: " + count);


        } catch (IOException e) {
            //
        }
        finally {
            if (writer != null) {
                writer.close();
            }
//            if (treeWriter != null) {
//                treeWriter.close();
//            }
        }

    }
}
