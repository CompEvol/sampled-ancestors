package beast;


import beast.app.tools.FrequencySet;
import beast.app.tools.SampledAncestorTreeAnalysis;
import beast.evolution.tree.Node;
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
import java.util.Collections;
import java.util.List;

/**
 * for testing
 */
public class forTesting {

    public static void main (String[] args) throws Exception {

        for (int i = 0; i<200; i++) {
            System.out.println("<taxon spec='Taxon' id='" + i + "'/>");
        }


//        String newick ="(((((5:0.8902362809026343,(6:0.1964644496307244,(((13:0.03811232686804811,12:0.03811232686804811):0.29423216860779355,7:0.33234449547584166):0.2902775275118765,((9:0.3020074006768141,8:0.3020074006768141):0.03801084098027552,(11:0.09980912080240234,10:0.025995061624112914):0.08802526495519292):0.2826037813306286):0.18507979079693193):0.1444352181581552):0.7579351375743615,4:1.7100721695171668):1.3949873963625123)0:1.2947469974817283,3:1.8951815649105825):0.4044687594520455,(1:0.24162735218956133,2:0.3016925409307596):1.3220685682700517):0.0";
//            System.out.println("traits");
//
//            ArrayList<String> taxa = new ArrayList<String>();
//            for (int i=0; i<14; i++) {
//                taxa.add(Integer.toString(i));
//            }
//
//            try {
//                ZeroBranchSATreeParser parser = new ZeroBranchSATreeParser(taxa, newick, 0);
//                //TreeParser parser = new TreeParser(sStr, false, true, false, 0);
//                //System.out.println("The tree is " + parser.getRoot().toShortNewick(false));
//                for (int i=0; i<13; i++) {
//                    System.out.print(i + " = " + parser.getNode(i).getHeight() + ",");
//                }
//                System.out.println(13 + " = " + parser.getNode(13).getHeight());
//            } catch (Exception e) {
//                System.out.println("Tree parser isn't happy");
//            }

            //System.out.println("<parameter name=\"intervalTimes\" id=\"intervalTimes\" value=\""  + ssDate + " 0.\"/>");

//        String sStr = "((((((0:0.09554896065648677,((10:0.36294562554886767,((16:0.5686137865031391,24:1.2247065555914336):0.40202564926956086,((23:0.38837957397717204,(28:0.4607812462480485,(37:0.5703323848835193,38:0.5893561988037597):0.987426947454983):0.50085037547111):0.25017338081990204,(31:0.8565979293465684,50:2.880649529752697):0.5103594516934766):0.8783479297559733):0.2820057705705068):0.921345072617699,((12:1.4538720194594479,((43:0.7413051795981573)34:2.6644348625336924,((45:0.8628285454428948,(46:0.24400951112555624,48:0.6165153078895251):0.7646317965246059):1.7718954789760115,59:3.995712850363269):0.7954066489340583):0.8323178507340163):0.18587564411478752,(40:2.3040681091580613)18:1.811188181166214):0.3706098256550394):0.0960571008235398):1.3366704042736206,(8:2.1007254308984997,17:3.263649988346092):0.3481132529427615):0.17847757567835565,((3:0.3955721794768099)1:0.022326300150309564,((22:0.05936327741843783,30:0.7250894694008583):0.5983415519227027,49:3.3465663438839677):2.0636644392277166):1.599548189439556):0.3813201402015469,(4:1.6600777841780463,(5:0.2559906283915314,20:2.305591315909018):1.4642977261684162):0.826402933291261):1.1781620028890654,((2:0.10049486031791766,51:5.56657762077004):0.09017223046137168,(7:0.9129855267239488,((13:0.4315322150723162,((32:0.7575831854615558,(44:0.9942057986156989,53:1.8739607095928825):0.8759722920285178):0.5605149367458946,(55:2.1497445251592264,56:2.3135764859179577):1.2282799169532144):0.7714495703243234):1.380244652101819,((19:0.0019656678925610294,39:2.0275968385301724):1.0237244940283041,(((57:2.8736702432400705)27:0.6335228562295327,(47:1.5678550213041902,58:2.585514279484565):0.9816228135228062):0.1544129062700339,(42:0.9172014873882173,54:1.9882329403576602):1.3968079937431437):0.9514469946626054):1.180940237687782):0.3599290148493619):0.03103042624194252):2.995677048762442):0.262436322524108,((6:1.77920243679376,(9:0.8649644103682714,((((33:1.074859386378094)26:0.8071429762699296)14:0.4456373715348967,(21:0.005716381619212285,41:1.884705361547434):1.0845421792750898):0.7259595241765648,36:3.411936525736854):0.5673597654537428):1.4655637749889223):0.315803432812892,(11:0.43483520415628973,((29:1.1703105701403427)15:0.3645203730054689,((25:0.7261812959197851,35:2.0386696239539805):0.10106308117221019,52:3.533945892013673):0.2539451873250709):0.5581112929737078):2.684071926080531):1.974409854995594):0.0";
//
//        FrequencySet<String> pairs = new FrequencySet<String>();
//
//        ArrayList<String> taxa = new ArrayList<String>();
//        for (int i=0; i<60; i++) {
//            taxa.add(Integer.toString(i));
//        }
//
//
//        try {
//            ZeroBranchSATreeParser parser = new ZeroBranchSATreeParser(taxa, sStr, 0);
//            //TreeParser parser = new TreeParser(sStr, false, true, false, 0);
//            System.out.println("The tree is " + parser.getRoot().toShortNewick(false));
//            extractAllPairs(parser.getRoot(), pairs);
//            System.out.println("Pair frequencies");
//            System.out.println();
//            System.out.println("Count \t Pair");
//            System.out.println();
//            for (int i =0; i < pairs.size(); i++) {
//                System.out.println(pairs.getFrequency(i) + " \t " + pairs.get(i));
//            }
//            System.out.println();
//
//        } catch (Exception e) {
//            System.out.println("Exception");
//        }




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
//        ArrayList<Double> dates= new ArrayList<Double>();
//        int count=0;
//        try {
//            reader = new FileReader(file);
//            BufferedReader fin = new BufferedReader(reader);
//
//            while (fin.ready()) {
//                String str = fin.readLine();
//                if (str.contains("C1_1a_")) {
//                    int first = str.indexOf("C1_1a_");
//                    int second = str.indexOf("_", first+6);
//                    int last = str.indexOf("\"", second);
//                    count++;
//                    System.out.println(str.substring(first, second) + " = " + str.substring(second+1,last) + ",");
//                }
//                if (str.contains("value=\"")){
//                    int begin = str.indexOf("value=\"") + 7;
//                    int end = str.indexOf("\"", begin);
//                    String date = str.substring(begin, end);
//                    dates.add(Double.parseDouble(date));
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
//
//        Collections.sort(dates);
//        System.out.println("The first is " + dates.get(0) + " the last is " + dates.get(dates.size()-1) + "the difference is " + (dates.get(0) - dates.get(dates.size()-1)));
//        System.out.println(dates.size() + " " + count);
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


    private static ArrayList<String> listNodesUnder(Node node) {
        ArrayList<String> tmp = new ArrayList<String>();
        if (!node.isLeaf()) {
            for (Node child : node.getChildren()) {
                tmp.addAll(listNodesUnder(child));
            }
        } else tmp.add(node.getID());
        Collections.sort(tmp);
        return tmp;
    }


    public static void extractAllPairs(Node node, FrequencySet<String> pairs) {

        if ( node.isLeaf()) {
            String ancestor = node.getID();
            if (((ZeroBranchSANode)node).isDirectAncestor()) {
                ArrayList<String> descendants = listNodesUnder(node.getParent());
                for (String des:descendants) {
                    if (!des.equals(ancestor)) {
                        pairs.add(ancestor + "<" + des);
                    }
                }
            }
        }

        if (node.getLeft() != null){
            extractAllPairs(node.getLeft(), pairs);
        }

        if (node.getRight() != null) {
            extractAllPairs(node.getRight(), pairs);
        }
    }



}
