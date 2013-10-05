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


        //String sStr = "(((((0:0.7199609494021608,19:1.7789577032887163):1.002105226651608,52:4.148671537239016):0.42012001593072057,(14:1.0415269011763346,59:2.66690505222288):2.032184428869428):3.081312855570882,(((((((56:0.14902285468941834,48:0.03455794530993295):1.2967079642116897)16:1.7028821804155552,35:2.61781681150115):1.312046877045713,(31:0.7232254275320216)17:3.028962071088479):0.07869867312697565,(((32:1.8532849717532311,(22:0.038847439514722026,40:0.7793398582182771):1.3544770110833113):0.1851990248455948,20:1.3343083926188868):0.2176266966519691,45:2.6499532501338336):1.6070706992902863):0.3996983760833164,38:4.505780211387634):1.225165961182527,((8:0.2646977374555757,(26:0.3741300135315839,54:1.2662240442065844):0.8016093645094919):1.103349081686659,(10:0.3139054838163897,57:2.06224706630179):1.144788031202081):2.9679670310155446):1.5201517348382145):0.34123207807563993,((39:1.3443352824735708,24:0.7798191338106424):5.835325152646572,(((((2:0.12683177041249571,((23:0.7828048793856848,37:1.3383492846828347):8.767445219213243E-4,53:1.797926887634377):0.5682746489252732):0.0031430269324594917,3:0.23219291488638127):0.16110235990642252,33:1.9342695375953518):3.553010192328923,(12:2.0804147408219524,6:1.873052308066038):2.3714269438857514):0.01357536694281336,((((42:2.6270723304823793,9:1.1663507505989923):1.5809747452477225,1:2.1088526297312367):0.4569438538436099,(44:0.8476897327407524,34:0.5932942668499397):3.8369568984011995):1.0784812643009127,((((4:1.688595383736482,(((58:2.361388114641896,41:1.9239671670649994):0.020469408413900325,27:1.3941552906079213):0.6345319748701215,7:1.1100944095700953):0.756747285104419):1.4325687166674959,(28:0.18126624622320264,51:1.0271420686981987):4.042804453790093):0.1042304722712375,(((((11:0.015877090354578627,43:1.4195896680871556):0.007609351398727426,15:0.22422477337547697):2.140737813988588,((47:0.994073329254709)21:1.0305980390461205,50:2.0725079040120153):1.6999689545563026):0.7429038131477217,(46:1.303747212225021,18:0.03934445285489829):3.1571575592704146):0.3031983993457459,5:3.0158442143777897):0.31390668314354997):0.5056790757178664,(((30:0.34916158160304,29:0.2960813028934659):1.2444473596180394,(49:1.0187940583898616,(55:0.6831323628193946,36:0.17993256379934408):0.4467756916217098):1.2653592341259294):0.8182294272369646,(13:1.0916546410416252,25:1.7988641133705556):0.48503232969554055):2.526776165708192):0.31630140903892023):0.06367153517099577):1.4634662743531006):0.43964202547977216):0.0";

        String sStr= "(((((7:2.3260776384640725,33:2.8470185942687483):2.3745573217871154,12:4.7947769035705825):2.4209718588573983,(57:7.958958428113505,(38:2.214887688968382,(48:0.2671990897381953,53:0.3211748906966232):2.131536505360563):5.4254549281075715):0.09977816506299764):0.5541417979118792,((((((43:1.0997227695161413,41:1.0261782262367731):2.2084047117046923,(50:0.17037627759020602)39:3.2196597051427487):0.5389217692068389,(((46:2.22974283149588,59:2.426999121179888):0.06888861820867298,(14:0.47731468367496355,20:0.5918363766070147):1.1713117353756264):0.5762561317634916,25:2.5008920623277273):0.9929207955013597):0.405786544934295,(2:1.1038423139039981,49:2.0917902542201556):2.241892435637256):0.009553790186682676,(4:1.4572265526687662,24:1.8926858695922082):2.009855234111683):2.569202432342885,0:5.883169626840016):1.577555661637887):1.8894160339285753,((35:9.353479594408665,(47:5.99587847251648,((26:5.022302198976236,((((21:0.24700911182894458,15:0.15395522633246017):2.094974609685341,(9:1.5364484898552089,17:1.639004377573702):0.6341810480323744):0.023938132174583515,6:2.1315719748359854):0.3869775690851931,((42:1.8147064642393431,11:1.1642420822602357):0.9947208933319835,36:2.641390355435279):0.44774519159092563):2.104952300911876):0.06760570137212074,37:5.2877854138047615):0.5365201414249254):3.5802571960737986):0.10916410862572357,((((32:0.308581573218877,28:0.218576436218747):0.1246976575752683,30:0.35645681827954867):5.090600762682853,((((23:1.3463064646524376,58:1.9210590496750388):0.6859577154266958,(56:1.647075808335126,27:1.1510961285727355):0.9104327735566322):1.3978261322220176,((29:2.5746490386247416,16:2.2754877081310667):0.4025705685510337,19:2.7257884704626827):0.5151236578486778):0.8376914006630507,((44:3.1278608994883967,45:3.1334270030122457):1.4151433089300882,(3:1.1944175901671983,(8:0.4426136631725264,5:0.379382315061779):0.869952437973005):2.5503560613635345):0.08873668741922636):1.1060598977934095):0.7928257442120454,((((((55:1.065441319366652,13:0.2859029169824012):0.5416431233632792,51:1.5473952386927081):0.6931953697052844,1:1.2207485132497364):0.18169633152439602,(34:1.8801404089373097,(18:0.34293563604907185,10:0.24668038327652297):1.1757522236364562):0.2558729564859181):1.387320255305152,((22:0.8809917767183748,54:1.433528720654829):0.08202102575963721,(40:0.6405170918160863,31:0.4691659165705655):0.6592733097611454):2.3372253871368596):1.0524491269660903,52:4.896221469740613):1.7555162897572663):3.129465851815197):0.6397258182781375):0.0";
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
