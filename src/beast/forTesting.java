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

        String sStr = "(((((24:1.6989824191139684,7:1.53582784970481):1.7606461436152907,44:3.5388959897208405):1.6777779975401867,(((40:2.6988191616257113,(42:1.982427651363662,57:2.07045973295834):0.7171253234759529):1.7345651059009395,((51:3.2536162353424576,16:3.0895633360211274):0.5198340513643549,58:3.825762393734485):0.7038145256316053):0.7287137872286373,26:5.090078667422286):0.051424714513101044):1.1380877516320798,(((((6:3.3607837085216836,2:3.345079599813058):0.029906814974532736,((((((9:0.8081745399016791,43:1.0257820465395247):0.6443790555103543,3:1.4095499995263845):0.43550031716874926,(19:0.05743969990727571,56:0.24904676542474746):1.9385490261892082):0.2558649595667113,32:2.3218771098033963):0.7057540090786327,41:3.0667639661692387):0.3698834900224357,(((46:1.348649944870422,15:1.204153443861097):0.2989996511952375,11:1.4579664898366014):1.6328309988484166,(50:1.3871705286716152,12:1.171697483140906):1.9271137320807048):0.16467449748852392):0.19856638059688603):1.249325262729677,(28:0.22735867917526598,1:0.03313846309026758):4.589129957683731):0.04272005102263021,(((5:3.5498264956893415,((47:0.08926879928805498)25:0.6243854854600741,4:0.44375113058932314):3.094900775893535):0.6040246372539833,(45:4.1033223981869416,(((((48:1.73386539701003,(31:0.3177275551918415,55:0.4371199032562876):1.356849355974421):0.1946523530758606,35:1.8759084842197735):0.23833672407091822,52:2.2079277734802165):0.6736039790420012,(((14:1.9174733465368172,(22:1.5906787105352738,39:1.6685448315145255):0.37980206864604593):0.37279571777590537,49:2.451877447720319):0.11229837571137669,59:2.6369139940093502):0.2807368896787965):0.14848294277520768,34:2.935212532505953):1.1307195959284444):0.29897470163727524):0.514212581281611,29:4.847970148047107):0.014532107598326682):0.9046312611842033,(53:2.919448335383806,((((33:0.04699831702943236)23:1.0297500541174731,27:1.0433608685216562):0.232979424416631,17:1.2349180258287822):0.7795883253737186,(37:0.5526391318155106,18:0.46261480931703325):1.5519254906638906):0.7180039826306333):2.987941697386445):0.5200741203618717):1.129490891498407,(((38:2.4486977142278787,10:2.263767046985101):0.7615469423121333,(54:1.5837531406023695,8:1.280549650162338):1.7124518679753322):3.277454487654743,(((30:3.7523529748197166,(0:1.9234697985775107,21:2.0875336797032595):1.6133362541119123):1.1689894564931107,36:4.948037457577095):0.08298643693464314,(13:0.18777966111054134,20:0.2294698401975559):4.722853296354773):1.4385820859280247):0.9838012985446141):0.0";
        ArrayList<String> taxa = new ArrayList<String>();
        for (int i=0; i<60; i++) {
            taxa.add(Integer.toString(i));
        }


        try {
            TreeParser parser = new TreeParser(taxa, sStr, 0, false);
            //TreeParser parser = new TreeParser(sStr, false, true, false, 0);
            System.out.println("The tree is " + parser.getRoot().toShortNewick(false));
            for (int i=0; i<60; i++) {
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
