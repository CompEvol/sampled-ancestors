package beast.app.simulators;

import beast.util.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Alexandra Gavryushkina
 */

public class Simulator {

    public static void main(String[] args) throws Exception{

        PrintStream writer = null;
        PrintStream treeWriter = null;

        int treeCount = 1;

        int index=0;
        int count=0;


        try {
            writer = System.out; //new PrintStream(new File("trees_and_pars.txt"));
            treeWriter = new PrintStream(new File("trees.txt"));


            for (int i=0; i< treeCount; i++) {
                //int meanLeafCount = 0;
                //int low=0;
                //int high=0;

                //double[] rates = simulateParameters(0.0, 0.0, 0.0, 0.0);
                double [] rates = {0.0042, 0.924, 0.114, 0.0, 0.81};
                double rhoSamplingTime=63.0;
                SABDSimulator simulator = new SABDSimulator(rates[0], rates[1], rates[2], rates[3], rates[4], true, rhoSamplingTime);
                int result;
                do {
                    result = simulator.simulate();
                    count++;
                } while (result < 0);

                count--;

                writer.println("tree");
                writer.println(simulator.root.toShortNewick(false));
                treeWriter.println(simulator.root.toShortNewick(false) + ";");

                writer.println("traits");
//              double minSampleAge = 0.0;
                simulator.printTraitsWithRhoSamplingTime(simulator.root, writer);

                writer.println("parameters");
                writer.println(rhoSamplingTime);
                writer.println(simulator.root.getHeight()+simulator.rhoSamplingTime);

                for (int j=0; j<3; j++) {
                    writer.println(rates[j]);
                }
                writer.println(rates[4]);
                //writer.println(simulator.countSA(simulator.root));
                writer.println("divergence times");
                simulator.printInternalNodeAges(simulator.root, writer);

            }
            //System.out.print(meanLeafCount/(treeCount+count));
            //System.out.println();
            System.out.println("Number of trees rejected due to process died out: " + count);

        } catch (IOException e) {
            //
        }
        finally {
            if (writer != null) {
                writer.close();
            }
            if (treeWriter != null) {
                treeWriter.close();
            }
        }




        // Simulate a tree under a set of parameters d=0.0042, v=0.924, s=0.114, rho=0.81, r = 0, t_origin=60
        // print the parameters: d, v, s, rho, t_origin
        // print the traits (psi-sampled nodes)
        // print the divergence times + clades
    }
}
