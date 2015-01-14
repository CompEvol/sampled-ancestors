package beast.app.simulators;

import beast.util.*;

/**
 * Created by agav755 on 19/12/14.
 */
public class Simulator {

    public static void main(String[] args) throws Exception{
//        System.out.print("num <- c(");
//        for (int i =0; i< 100; i++) {
//            SABDTreeSimulator.main(new String[] {});
//            if (i!= 99) {
//                System.out.print(", ");
//            }
//        }
//        System.out.print(")");

        //SABDSimulator simulator = new SABDSimulator(0.0042, 0.924, 0.114, 0.0, 0.81, true, 60.0);
        SABDSimulator simulator = new SABDSimulator(0.0042, 0.924, 0.114, 0.0, 0.81, true, 63.0);
        int result = simulator.simulate(System.out);



        // Simulate a tree under a set of parameters d=0.0042, v=0.924, s=0.114, rho=0.81, r = 0, t_origin=60
        // print the parameters: d, v, s, rho, t_origin
        // print the traits (psi-sampled nodes)
        // print the divergence times + clades
    }
}
