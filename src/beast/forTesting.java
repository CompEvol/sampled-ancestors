package beast;


import beast.evolution.tree.Tree;
import beast.util.NexusParser;
import beast.util.TreeParser;

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

        String sStr ="(((((15:0.9072117015322814)5:2.344954805931658,8:2.555746538150742):3.5142478689380257,((29:0.9245446168771885)24:0.3882787277119544,26:0.6887571305654259):7.398320466425792):0.037086832505206946,(((14:1.1992219324855915,16:1.3107447845913667):5.116260351192503,0:5.29920798325698):0.3471661031356965,1:5.697695243717641):0.08353354362752086):1.1601142400847981,(((28:4.047418176969043,2:1.1748554485602645):0.1542887610529391,(((22:0.9632745788120705,20:0.5192622525808339):0.1075697340747368)10:1.5031024967077764,(18:0.4062215672232341)11:1.533548304754766):0.4331934410768703):1.5258272297761808,((((21:0.3039618290915733)17:0.6300310116485406,(27:1.1452329241703278)23:1.2924829756018568):1.1855883878998474,12:1.5293925069503382):1.1631142615640906,(((25:1.7450071199157016,19:0.7209133127843366):0.3517976222075845)4:1.67964840200941,(((13:0.3009537579938577)9:0.7051470183258033,(7:0.19708572826965742)3:0.2801585747979054):0.7749055179641084,6:1.1610810225875179):0.6160918463315141):0.30869688906978876):0.9113380444042924):4.137074764985794):0.0";

        ArrayList<String> taxa = new ArrayList<String>();
        for (int i=0; i<30; i++) {
            taxa.add(Integer.toString(i));
        }


        try {
            TreeParser parser = new TreeParser(taxa, sStr, 0, false);
            //TreeParser parser = new TreeParser(sStr, false, true, false, 0);
            System.out.println("The tree is " + parser.getRoot().toShortNewick(false));
            for (int i=0; i<30; i++) {
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
