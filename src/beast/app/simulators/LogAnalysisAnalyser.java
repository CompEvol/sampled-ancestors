package beast.app.simulations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Alexandra Gavryushkina
 */
public class LogAnalysisAnalyser {

    double treeHeight, orig_root, SACount;

    private void setTreeInfo(){
        treeHeight = 10;
        orig_root = 2;
        SACount = 2;
    }

    private void pickUpInfo(java.io.File file) throws Exception{

        BufferedReader fin = null;
        String[] titles = null;
        String ssDate = null;

        try {    //TODO finish this
            fin = new BufferedReader(new FileReader(file));
            ArrayList<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
            while (fin.ready()) {
                String str = fin.readLine();
                if (!str.contains("item")) {
                    setTreeInfo();
                } else {
                    ParameterInfo parameter = new ParameterInfo(str);
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

        for (int i = 0; i<titles.length; i++) {
            System.out.println(titles[i]);
        }

    }


    public static void main(String[] args) throws Exception {
        java.io.File file;

        if (args != null && args.length > 0) {
            file = new java.io.File(args[0]);
        } else {
            throw new Exception("there is no file");
        }
        LogAnalysisAnalyser analyser = new LogAnalysisAnalyser();
        analyser.pickUpInfo(file);

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
