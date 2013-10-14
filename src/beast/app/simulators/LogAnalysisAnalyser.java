package beast.app.simulators;

import beast.util.LogAnalyser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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

    private void summarise(java.io.File file) throws Exception {

        BufferedReader fin = null;
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Integer> insideHPOCount = new ArrayList<Integer>();
        ArrayList<Integer> convergenceCount = new ArrayList<Integer>();
        ArrayList<String> noise = new ArrayList<String>(Arrays.asList(new String[] {"name", "jointTreeLikelihood", "samplingRate1", "posterior", "BDlikelihood", "rateAG"}));
        ArrayList<String> noTrueValue = new ArrayList<String>(Arrays.asList(new String[] {"treeHeight", "orig_root", "SACount"}));

        ArrayList<ArrayList<Double>> medians = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> medianMedians = new ArrayList<Double>();
        ArrayList<String> trueValues = new ArrayList<String>();


        try {
            fin = new BufferedReader(new FileReader(file));
            boolean run = true;
            while (fin.ready()) {
                String str = fin.readLine();
                String[] fields = str.split("\\s+");

                if (fields[0].equals("posterior") || fields[0].equals("BDlikelihood")) {
                    if (fields[1].equals("unconverged")){
                        run = false;
                    }
                }
                if (run && !noise.contains(fields[0])) {
                    int parameterIndex;
                    if (!names.contains(fields[0])) {
                        names.add(fields[0]);
                        insideHPOCount.add(0);
                        convergenceCount.add(0);
                        medians.add(new ArrayList<Double>());
                        trueValues.add("");
                    }
                    parameterIndex = names.indexOf(fields[0]);
                    if (fields[1].equals("converged")){
                        int convCount = convergenceCount.get(parameterIndex);
                        convergenceCount.set(parameterIndex, convCount+1);
                        if (fields[5].equals("inside")) {
                            int count = insideHPOCount.get(parameterIndex);
                            insideHPOCount.set(parameterIndex, count+1);
                        }
                        medians.get(parameterIndex).add(Double.parseDouble(fields[4]));
                        trueValues.set(parameterIndex, fields[2]);
                    }
                }
                if (fields[0].equals("rateGT")) {
                    run = true;
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

        for (ArrayList<Double> med:medians) {
            medianMedians.add(0.0);
        }
        for (ArrayList<Double> med:medians) {
            Collections.sort(med);
            double mm = med.get(med.size()/2);
            medianMedians.set(medians.indexOf(med), mm);
        }

        System.out.println("parameter \t % of times inside 95% HPD \t trueValue \t median");
        for (String parameter:names){
            int parameterIndex = names.indexOf(parameter);
            double percent = (double) 100 * insideHPOCount.get(parameterIndex)/convergenceCount.get(parameterIndex);
            //System.out.println(parameter + "\t inside HPO " + insideHPOCount.get(names.indexOf(parameter)) + "\t out of " + convergenceCount.get(names.indexOf(parameter)) + "\t" + percent);

            if (noTrueValue.contains(parameter)) {
                System.out.format(parameter + "\t %2.2f \t - \t -", percent);
                System.out.println();
            } else {
                System.out.format(parameter + "\t %2.2f \t" + trueValues.get(parameterIndex) + "\t %2.4f", percent, medianMedians.get(parameterIndex));
                System.out.println();
            }

        }


    }

    private ArrayList<ParameterInfo> pickUpInfo(java.io.File file) throws Exception{

        BufferedReader fin = null;
        ArrayList<ParameterInfo> parameters = new ArrayList<ParameterInfo>();

        try {
            fin = new BufferedReader(new FileReader(file));
            int seed = 0;
            while (fin.ready()) {
                String str = fin.readLine();
                if (str.length() <12) {
                    seed = Integer.parseInt(str);
                    continue;
                }
                if (!str.contains("item") && str.length() > 12 ) {
                    ParameterInfo parameter = new ParameterInfo(str, seed);
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

        boolean type = false;
        if (type) {
            String fileName = file.getName();
            int end = fileName.indexOf("_");
            String seed = fileName.substring(0, end);
            String xmlFileName = "/Users/agav755/Subversion/sampled-ancestors/simulationNew/xml/" + seed + "_SABDSKY.xml";
            xmlFile = new java.io.File(xmlFileName);
            LogAnalysisAnalyser analyser = new LogAnalysisAnalyser();
            analyser.setTreeInfo(xmlFile);
            LogAnalyser logAnalyser = new LogAnalyser(args, 2000, 10);
            outFile = new java.io.File("out.txt");

            PrintStream writer = null;

            try {
                writer = new PrintStream(outFile);
                writer.println(seed);
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
            System.out.println("name \t convergence \t trueValue \t mean \t median \t insideHPO");
            for (ParameterInfo parameter:parameters) {
                System.out.println(parameter.name+ "\t" + parameter.convergence + "\t" + parameter.trueValue + "\t" + parameter.mean + "\t" + parameter.median + "\t"+ parameter.insideHPO + "\t"+ parameter.seed );
            }
        } else {
            LogAnalysisAnalyser analyser = new LogAnalysisAnalyser();
            analyser.summarise(file);
        }

    }



//    String[] treeHeight, orig_root, birthRate, deathRate, samplingRate2, SACount, r, clock_rate, freqParameter1,
//            freqParameter2, freqParameter3, freqParameter4, rateAC, rateAT, rateCG, rateCT, rateGT;

    private class ParameterInfo {
        String name, insideHPO, convergence;
        double trueValue, mean, median, hpoLow, hpoHigh, ESS;
        int seed;

        public ParameterInfo(String logLine, int newSeed) {
            seed = newSeed;
            String[] fields = logLine.split("\\s+");
            name = fields[0];
            trueValue = assignTrueValue(name);
            mean = Double.parseDouble(fields[1]);
            median = Double.parseDouble(fields[4]);
            hpoLow = Double.parseDouble(fields[5]);
            hpoHigh = Double.parseDouble(fields[6]);
            ESS =  Double.parseDouble(fields[8]);
            if (hpoLow <= trueValue && hpoHigh >= trueValue) {
                insideHPO = "inside";
            }  else insideHPO = "outside";
            if (ESS>200) {
                convergence = "converged";
            } else if (ESS > 100) {
                convergence = "semiconverged";
            } else convergence = "unconverged";

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
        if (str.equals("clock.rate")){
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
