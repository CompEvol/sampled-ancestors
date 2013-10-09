package beast;


import beast.evolution.tree.Tree;
import beast.evolution.tree.ZeroBranchSANode;
import beast.util.NexusParser;
import beast.util.TreeParser;
import beast.util.ZeroBranchSATreeParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * for testing
 */
public class forTesting {

    public static void main (String[] args) throws Exception {

        String sStr = "(((0:2.730768439033188,(((((5:0.06904757383754045,((13:1.3928072233274573,(18:0.4410536822351787,19:0.5579680166669654):2.2377095360543073):0.24584985038691975,(26:2.5201989318221436,(46:2.785718455919575,47:2.8044092388482653):2.3292079467544156):1.8630478162364295):1.069805555832346):0.2762800101194429,((10:0.6313458255163873,((15:0.06239869456491931,(17:0.6771370363306932,(35:0.31292122668332567,(39:0.3215630258558342,42:0.7857363153060142):0.25605716795970324):3.0966043780238426):0.45647179780621094):0.9627376142486757,21:2.4535455827542147):0.2785144841639351):1.1395196441606643,(11:0.9055467374519868,((((30:2.2712045875579,32:2.7530714320840666):0.21932469975144464)16:0.8297086196730277,((22:0.7042631571065137,45:4.159621924425988):0.49805757574863385,((27:0.3633302976872024,31:0.7552623621311909):0.1659436069923359,38:1.7865097828446395):1.889733380950391):0.7282152930738732):0.8986132290653241,24:3.483158068961732):0.2531527970212881):0.8991130945039458):0.6327865202864729):0.19941338987509027,((((12:0.8993422170491883,14:1.0232474242353113):0.8635994197292565)6:0.049665782595148755,(8:0.6375168881096851,(9:0.7852951910662522,(((((23:0.026813104851843406,(25:0.09454836561715041,(34:0.3509471487060267,49:2.0303613004602106):0.912287309345043):0.4054056217071995):0.3083268334695006,(41:1.9705462967703227,(52:0.7589848196274893,53:0.9775756251881695):2.5505670661329276):0.5394651543972309):1.3614181229936033,(54:2.2026655935510604,(57:0.4522187077703599,58:0.4615692758773058):1.8825605817104467):3.2855179063790327):1.308277997414132,(((28:0.3460116263822144,((33:0.25361354064860464,36:0.46995797052278476):0.6161699040151731,(55:2.3958828335460503,56:2.415192719090406):0.62691340555903):0.4213322464466671):0.8564913358051918,((40:0.2245824520275299,(43:0.8235842187060562,50:1.4274417377366255):0.02619186791379846):0.7560855837329683,(51:1.3718011685344038,59:1.8962511168472034):0.8815348508423817):1.6872603193796323):1.3256863715310683,44:4.651032650905785):1.1716895969502996):0.1428330501397408,(29:3.362077633267088,(37:0.9939268890755173,48:2.426240532772237):3.5156668795783617):0.5060480078651199):0.4419388191803364):0.30298820127154613):0.14285041488892247):0.054440369332040106,20:3.340998469238542):1.267141799692027):1.1367376010499903)1:1.3766466937030621,3:1.7843009545165032):1.9120686452459061):0.7543813819242899,2:4.175177800817782):2.2006796534184296,(4:1.633413439356513,7:2.932922480250298):5.963271624324975):0.0";
        ArrayList<String> taxa = new ArrayList<String>();
        for (int i=0; i<60; i++) {
            taxa.add(Integer.toString(i));
        }


        try {
            ZeroBranchSATreeParser parser = new ZeroBranchSATreeParser(taxa, sStr, 0);
            //TreeParser parser = new TreeParser(sStr, false, true, false, 0);
            System.out.println("The tree is " + parser.getRoot().toShortNewick(false));
            System.out.println("SACount " + parser.getDirectAncestorNodeCount());
            System.out.println(((ZeroBranchSANode) parser.getNode(1)).isDirectAncestor());
            for (int i=0; i<parser.getNodeCount(); i++) {
                System.out.println(i + " = " + parser.getNode(i).getHeight() + ",");
            }
        } catch (Exception e) {
            System.out.println("Exception");
        }

//        java.io.File file;
//
//        if (args != null && args.length > 0) {
//            file = new java.io.File(args[0]);
//        } else {
//            String message = "Choose file .trees";
//            java.awt.Frame frame = new java.awt.Frame();
//            java.awt.FileDialog chooser = new java.awt.FileDialog(frame, message,
//                    java.awt.FileDialog.LOAD);
//            chooser.setVisible(true);
//            if (chooser.getFile() == null) {
//                System.out.println("The file was not chosen.");
//                System.exit(0);
//            }
//            file = new java.io.File(chooser.getDirectory(), chooser.getFile());
//            chooser.dispose();
//            frame.dispose();
//        }
//
//        FileReader reader = null;

//        String[] codes = new String[54];
//        String[] dates = new String[54];
//        String[] am = new String[54];
//        int ind1=0;
//        int ind2=0;
//        int ind3=0;
//
//        try {
//            reader = new FileReader(file);
//            BufferedReader fin = new BufferedReader(reader);
//            while (fin.ready()) {
//                String str = fin.readLine();
//                str.trim();
//                int index3 = str.indexOf("LOCUS       AM");
//                if (index3 != -1) {
//                    am[ind3] = str.substring(index3 + 14, 20);
//                    ind3++;
//                }
//
//                int index1 = str.indexOf("/isolate=");
//                if (index1 != -1) {
//                    int end = str.length();
//                    codes[ind1] = str.substring(index1 + 10, end-1);
//                    ind1++;
//                }
//                int index2 = str.indexOf("/collection_date=");
//                if (index2 != -1) {
//                    int end = str.length();
//                    dates[ind2] = str.substring(index2 + 18, end-1);
//                    ind2++;
//                }
//            }
//        } catch (IOException e) {
//            //
//        }
//        finally {
//            if (reader != null) {
//                reader.close();
//            }
//        }
//        for (int i=0; i < 54; i++) {
//            System.out.println("B.GL." + dates[i].substring(7,11) + "." + codes[i] + ".AM" + am[i] + " = " +  dates[i]);
//        }
//
//
//
//        double[] alldates = new double[54];
//        int ind=0;
//
//        try {
//            reader = new FileReader(file);
//            BufferedReader fin = new BufferedReader(reader);
//            while (fin.ready()) {
//                String str = fin.readLine();
//                String toPrint = convert(str, alldates, ind);
//                System.out.println(toPrint + ",");
//                ind++;
//            }
//        } catch (IOException e) {
//            //
//        }
//        finally {
//            if (reader != null) {
//                reader.close();
//            }
//        }
//        Arrays.sort(alldates);
//        System.out.println("The first one is " + alldates[0] + " the second is " + alldates[1] + " the last one is " + alldates[53]);
//        System.out.println(alldates[53] - alldates[0]);
    }

    private static String convert(String str, double[] alldates, int ind) {
        int index = str.indexOf("=");
        int indexEnd = index-1;
        index++;
        index++;
        String date = str.substring(index);
        String label = str.substring(0, indexEnd);
        int day = Integer.parseInt(date.substring(0,2));
        int year = Integer.parseInt(date.substring(7,11));
        String monthStr = date.substring(3,6);
        int days = 0;

        if (monthStr.equals("Jan")){
            days = day;
        }
        if (monthStr.equals("Feb")) {
            days = 31 + day;
        }
        int daysBefore;
        if (year % 4 != 0) {
            daysBefore = 59;
        } else {
            daysBefore = 60;
        }
        if (monthStr.equals("Mar")) {
            days = daysBefore + day;
        }
        daysBefore += 31;
        if (monthStr.equals("Apr")){
           days = daysBefore + day;
        }
        daysBefore +=30;
        if (monthStr.equals("May")) {
            days = daysBefore + day;
        }
        daysBefore += 31;
        if (monthStr.equals("Jun")) {
            days = daysBefore + day;
        }
        daysBefore +=30;
        if (monthStr.equals("Jul")) {
            days = daysBefore + day;
        }
        daysBefore += 31;
        if (monthStr.equals("Aug")){
            days = daysBefore + day;
        }
        daysBefore += 31;
        if (monthStr.equals("Sep")) {
            days = daysBefore + day;
        }
        daysBefore +=30;
        if (monthStr.equals("Oct")) {
            days = daysBefore + day;
        }
        daysBefore += 31;
        if (monthStr.equals("Nov")) {
            days = daysBefore + day;
        }
        daysBefore +=30;
        if (monthStr.equals("Dec")) {
            days = daysBefore + day;
        }
        double dateDouble;
        if (year % 4 != 0) {
            dateDouble = (double)days/365 + year;
        } else {
            dateDouble = (double)days/366 + year;
        }
        alldates[ind] = dateDouble;
        return label + " = " + Double.toString(dateDouble);
    }



}
