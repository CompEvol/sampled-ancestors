package beast.stuff;

import beast.util.LogAnalyser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
                    int end = lastLine.indexOf("-->");
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
        ArrayList<Integer> insideHPDCount = new ArrayList<Integer>();
        ArrayList<Integer> convergenceCount = new ArrayList<Integer>();
        ArrayList<String> noise = new ArrayList<String>(Arrays.asList(new String[] {"name", "jointTreeLikelihood", "samplingRate1", "posterior", "BDlikelihood", "rateAG"}));
        ArrayList<String> noTrueValue = new ArrayList<String>(Arrays.asList(new String[] {"treeHeight", "orig_root", "SACount"}));

        ArrayList<ArrayList<Double>> medians = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<Double>> errors = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<Double>> biases = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> medianMedians = new ArrayList<Double>();
        ArrayList<Double> medianErrors = new ArrayList<Double>();
        ArrayList<Double> medianBiases = new ArrayList<Double>();
        ArrayList<Double> medianLengths = new ArrayList<Double>();
        ArrayList<String> trueValues = new ArrayList<String>();
        ArrayList<ArrayList<Double>> hpdLengths = new ArrayList<ArrayList<Double>>();


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
                        insideHPDCount.add(0);
                        convergenceCount.add(0);
                        medians.add(new ArrayList<Double>());
                        errors.add(new ArrayList<Double>());
                        biases.add(new ArrayList<Double>());
                        trueValues.add("");
                        hpdLengths.add(new ArrayList<Double>());
                    }
                    parameterIndex = names.indexOf(fields[0]);
                    if (fields[1].equals("converged")){
                        int convCount = convergenceCount.get(parameterIndex);
                        convergenceCount.set(parameterIndex, convCount+1);
                        if (fields[6].equals("inside")) {
                            int count = insideHPDCount.get(parameterIndex);
                            insideHPDCount.set(parameterIndex, count+1);
                        }
                        double median = Double.parseDouble(fields[4]);
                        double trueValue = Double.parseDouble(fields[2]);
                        double bais = (median - trueValue)/trueValue;
                        double error = Math.abs(bais);
                        medians.get(parameterIndex).add(median);
                        errors.get(parameterIndex).add(error);
                        biases.get(parameterIndex).add(bais);
                        trueValues.set(parameterIndex, fields[2]);
                        hpdLengths.get(parameterIndex).add(Double.parseDouble(fields[5]));
                    }
                }
                if (fields[0].equals("birthsampling")) {
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

        for (ArrayList<Double> err:errors) {
            medianErrors.add(0.0);
        }
        for (ArrayList<Double> err:errors) {
            Collections.sort(err);
            double me = err.get(err.size()/2);
            medianErrors.set(errors.indexOf(err), me);
        }

        for (ArrayList<Double> bias:biases) {
            medianBiases.add(0.0);
        }
        for (ArrayList<Double> bias:biases) {
            Collections.sort(bias);
            double mb = bias.get(bias.size()/2);
            medianBiases.set(biases.indexOf(bias), mb);
        }

            for (ArrayList<Double> len:hpdLengths) {
            medianLengths.add(0.0);
        }
        for (ArrayList<Double> len:hpdLengths) {
            Collections.sort(len);
            double ml = len.get(len.size()/2);
            medianLengths.set(hpdLengths.indexOf(len), ml);
        }



        System.out.println("parameter \t trueValue \t median \t error \t bias \t 95% HPD length \t % of times inside 95% HPD");
        for (String parameter:names){
            int parameterIndex = names.indexOf(parameter);
            double percent = (double) 100 * insideHPDCount.get(parameterIndex)/convergenceCount.get(parameterIndex);
            double length = medianLengths.get(parameterIndex);
            //System.out.println(parameter + "\t inside HPD " + insideHPDCount.get(names.indexOf(parameter)) + "\t out of " + convergenceCount.get(names.indexOf(parameter)) + "\t" + percent);
            double error = medianErrors.get(parameterIndex);
            double median = medianMedians.get(parameterIndex);
            double bias = medianBiases.get(parameterIndex);
            if (noTrueValue.contains(parameter)) {
                System.out.format(parameter + " \t - \t - \t %2.4f \t %2.4f \t %2.4f \t %2.2f", error, bias, length, percent);
                //System.out.println("\t out of " + convergenceCount.get(names.indexOf(parameter)));
                System.out.println();
            } else {
                //double error, trueValue;
                //trueValue = Double.parseDouble(trueValues.get(parameterIndex));
                //error = Math.abs((trueValue - median)/trueValue);
                System.out.format(parameter + "\t" + trueValues.get(parameterIndex) + "\t %2.4f \t %2.4f \t %2.4f \t %2.4f \t %2.2f ", median, error, bias, length, percent);
                //System.out.println("\t out of " + convergenceCount.get(names.indexOf(parameter)));
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

        if (args != null & args.length > 0) {
            file = new java.io.File(args[0]);
            String fileName = file.getName();
            int end = fileName.indexOf("_");
            if (end == -1) {
                end = fileName.indexOf(".");
            }
            String seed = fileName.substring(0, end);
            String xmlFileName = "/Users/agav755/Documents/Simulations/simulation_r_jumps/xml_r=05/" + seed + "_SABDSKY.xml";
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
            System.out.println("name \t convergence \t trueValue \t mean \t median \t HPDlength \t insideHPD");
            for (ParameterInfo parameter:parameters) {
                System.out.println(parameter.name+ "\t" + parameter.convergence + "\t" + parameter.trueValue + "\t" + parameter.mean + "\t" + parameter.median + "\t" + (parameter.HPDHigh - parameter.HPDLow) + "\t"+ parameter.insideHPD + "\t"+ parameter.seed );
            }
        } else {
            String message = "Choose logs out file";
            java.awt.Frame frame = new java.awt.Frame();
            java.awt.FileDialog chooser = new java.awt.FileDialog(frame, message,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() != null) {
                file = new java.io.File(chooser.getDirectory(), chooser.getFile());
            } else {
                throw new Exception("File was not chosen");
            }
            chooser.dispose();
            frame.dispose();

            LogAnalysisAnalyser analyser = new LogAnalysisAnalyser();
            analyser.summarise(file);
        }

    }



//    String[] treeHeight, orig_root, birthRate, deathRate, samplingRate2, SACount, r, clock_rate, freqParameter1,
//            freqParameter2, freqParameter3, freqParameter4, rateAC, rateAT, rateCG, rateCT, rateGT;

    private class ParameterInfo {
        String name, insideHPD, convergence;
        double trueValue, mean, median, HPDLow, HPDHigh, ESS;
        int seed;

        public ParameterInfo(String logLine, int newSeed) {
            seed = newSeed;
            String[] fields = logLine.split("\\s+");
            name = fields[0];
            trueValue = assignTrueValue(name);
            mean = Double.parseDouble(fields[1]);
            median = Double.parseDouble(fields[4]);
            HPDLow = Double.parseDouble(fields[5]);
            HPDHigh = Double.parseDouble(fields[6]);
            ESS =  Double.parseDouble(fields[8]);
            if (HPDLow <= trueValue && HPDHigh >= trueValue) {
                insideHPD = "inside";
            }  else insideHPD = "outside";
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
            return 0.5;
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
        if (str.equals("birthdeath")){
            return 0.4;
        }
        if (str.equals("birthsampling")){
            return 0.16;
        }
        return 0.;
    }


}
