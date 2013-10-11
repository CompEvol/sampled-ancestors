package beast.app.simulators;

import beast.util.LogAnalyser;

import java.io.*;
import java.util.ArrayList;

/**
 * @author Alexandra Gavryushkina
 */
public class LogAnalysisAnalyser {

    double treeHeight, orig_root, SACount;

    private void setTreeInfo(java.io.File file) throws Exception{
        BufferedReader fin = null;

        try {
            fin = new BufferedReader(new FileReader(file));
            while (fin.ready()) {
                String str = fin.readLine();
                if (str.contains("Tree height")) {
                    treeHeight = Double.parseDouble(fin.readLine());
                    fin.readLine();
                    SACount = Double.parseDouble(fin.readLine());
                    System.out.println("SA " + SACount);
                    fin.readLine();
                    String lastLine = fin.readLine();
                    int end = lastLine.indexOf(" -->");
                    orig_root = Double.parseDouble(lastLine.substring(0, end));
                }
            }
        } catch (IOException e) {
            //
        }
        finally {
            if (fin != null) {
                fin.close();
            }
        }
    }

    private ArrayList<ParameterInfo> pickUpInfo(java.io.File file) throws Exception{

        BufferedReader fin = null;
        ArrayList<ParameterInfo> parameters = new ArrayList<ParameterInfo>();

        try {
            fin = new BufferedReader(new FileReader(file));

            while (fin.ready()) {
                String str = fin.readLine();
                if (!str.contains("item")) {
                    ParameterInfo parameter = new ParameterInfo(str);
                    parameters.add(parameter);
                }
            }
        } catch (IOException e) {
            //
        }
        finally {
            if (fin != null) {
                fin.close();
            }
        }

        return parameters;

    }


    public static void main(String[] args) throws Exception {
        java.io.File file, xmlFile, outFile;

        if (args != null && args.length > 0) {
            file = new java.io.File(args[0]);
        } else {
            throw new Exception("there is no file");
        }

        boolean type = true;
        if (type) {
            String fileName = file.getName();
            int end = fileName.indexOf("_");
            String xmlFileName = "/Users/agav755/Subversion/sampled-ancestors/simulationNew/xml/" + fileName.substring(0, end) + "_SABDSKY.xml";
            xmlFile = new java.io.File(xmlFileName);
            LogAnalysisAnalyser analyser = new LogAnalysisAnalyser();
            analyser.setTreeInfo(xmlFile);
            LogAnalyser logAnalyser = new LogAnalyser(args, 2000, 10);
            outFile = new java.io.File("out.txt");

            PrintStream writer = null;

            try {
                writer = new PrintStream(outFile);
                logAnalyser.print(writer);

            } catch (IOException e) {
                //
            }
            finally {
                if (writer != null) {
                    writer.close();
                }
            }

            ArrayList<ParameterInfo> parameters = analyser.pickUpInfo(outFile);

            for (ParameterInfo parameter:parameters) {
                System.out.println("The true value of " + parameter.name + " is " + parameter.insideHPO + " 95% HPO");
            }
        } else {

        }

    }



//    String[] treeHeight, orig_root, birthRate, deathRate, samplingRate2, SACount, r, clock_rate, freqParameter1,
//            freqParameter2, freqParameter3, freqParameter4, rateAC, rateAT, rateCG, rateCT, rateGT;

    private class ParameterInfo {
        String name, insideHPO;
        double trueValue, mean, median, hpoLow, hpoHigh;

        public ParameterInfo(String logLine) {
            String[] fields = logLine.split("\\s+");
            name = fields[0];
            trueValue = assignTrueValue(name);
            mean = Double.parseDouble(fields[1]);
            median = Double.parseDouble(fields[4]);
            hpoLow = Double.parseDouble(fields[5]);
            hpoHigh = Double.parseDouble(fields[6]);
            if (hpoLow < trueValue && hpoHigh > trueValue) {
                insideHPO = "inside";
            }  else insideHPO = "outside";

        }
    }

    private double assignTrueValue(String str){
        if (str.equals("treeHeight")){
            return treeHeight;
        }
        if (str.equals("orig_root")){
            return orig_root;
        }
        if (str.equals("birthRate")){
            return 0.8;
        }
        if (str.equals("deathRate")){
            return 0.4;
        }
        if (str.equals("samplingRate2")){
            return 0.2;
        }
        if (str.equals("SACount")){
            return SACount;
        }
        if (str.equals("r")){
            return 0.8;
        }
        if (str.equals("clock_rate")){
            return 0.0023;
        }
        if (str.equals("freqParameter1")){
            return 0.25;
        }
        if (str.equals("freqParameter2")){
            return 0.25;
        }
        if (str.equals("freqParameter3")){
            return 0.25;
        }
        if (str.equals("freqParameter4")){
            return 0.25;
        }
        if (str.equals("rateAC")){
            return 0.4;
        }
        if (str.equals("rateAT")){
            return 0.1;
        }
        if (str.equals("rateCG")){
            return 0.15;
        }
        if (str.equals("rateCT")){
            return 1.04;
        }
        if (str.equals("rateGT")){
            return 0.15;
        }
        return 0.;
    }


}
