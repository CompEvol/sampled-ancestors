package beast.app.tools;

import beast.util.Randomizer;
import beast.util.ZeroBranchSATreeParser;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Alexandra Gavryushkina
 */
public class FileGenerator {

    FileGenerator() {
    }

    private String readBefore(BufferedReader fin, String stopStr) throws Exception{
        String str1 = fin.readLine();
        while (!str1.contains(stopStr)) {
            str1 = fin.readLine();
        }
        return str1;
    }

    private void copyBefore(BufferedReader fin, PrintWriter writer, String stopStr) throws Exception{
        String str = fin.readLine();
        while (!str.contains(stopStr)) {
            writer.println(str);
            str = fin.readLine();
        }
    }

    private String[] collectTreeInfo(String stringWithTree, String startSamplingDate) throws Exception{
        String[] treeInfo = new String[8];
        treeInfo[0] = "<!-- Newick tree";
        String newickTree = stringWithTree.substring(12, stringWithTree.length());
        treeInfo[1] = newickTree;
        treeInfo[2] = "Tree height";
        ArrayList<String> taxa = new ArrayList<String>();
        for (int i=0; i<60; i++) {
            taxa.add(Integer.toString(i));
        }
        ZeroBranchSATreeParser tree = new ZeroBranchSATreeParser(taxa, newickTree, 0);
        treeInfo[3] = Double.toString(tree.getRoot().getHeight());
        treeInfo[4] = "SACount";
        treeInfo[5] = Integer.toString(tree.getDirectAncestorNodeCount());
        treeInfo[6] = "Orig_root";

        int begin = startSamplingDate.indexOf("value=\"") + 7;
        int end = startSamplingDate.indexOf(" 0.\"/>");
        double orig_root = Double.parseDouble(startSamplingDate.substring(begin, end)) + 10 - tree.getRoot().getHeight();
        treeInfo[7] = Double.toString(orig_root)+ "-->";
        return treeInfo;

    }

    public void insertTrees(java.io.File templateFile, java.io.File treeFile) throws Exception {
        // read trees
        ArrayList<String> trees = new ArrayList<String>();
        ArrayList<String> samplingStartDates = new ArrayList<String>();

        BufferedReader fin = null;

        try {
            fin = new BufferedReader(new FileReader(treeFile));
            String str;
            while (fin.ready()) {
                str = fin.readLine();
                if (str.contains("tree")) {
                    trees.add(fin.readLine());
                    samplingStartDates.add(fin.readLine());
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

        String treeBegin = "<input name='newick'>";
        String ssDateBegin = "</tree>";

        for (int i=0; i<trees.size(); i++) {
            String outputFileName = "/Users/agav755/Subversion/sampled-ancestors/" + Integer.toString(Randomizer.nextInt(100000000)) + ".simulatingSeq.xml";
            PrintWriter writer = null;
            BufferedReader finTo = null;

            try {
                finTo = new BufferedReader(new FileReader(templateFile));
                writer = new PrintWriter(new FileWriter(outputFileName));
                String str;
                while (finTo.ready()){
                    str = finTo.readLine();
                    writer.println(str);
                    if (str.contains(treeBegin)) {
                        writer.println(trees.get(i));
                    }
                    if (str.contains(ssDateBegin)) {
                        writer.println("<!-- start sampling date = " + samplingStartDates.get(i) + "-->");
                    }
                }
            } catch (IOException e) {
                //
            }
            finally {
                if (finTo != null) {
                    finTo.close();
                }
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        java.awt.Frame frame1 = new java.awt.Frame();
        //Custom button text
        Object[] options = {"Sequence simulation",
                "Full analysis",
                "Cancel"};
        int xmlType = JOptionPane.showOptionDialog(frame1,
                "What kind of xml would you like to generate?",
                "Choose the type of xml",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);//Custom button text
        if (xmlType == 2) System.exit(0);

        frame1.dispose();

        java.io.File fileTo, fileFrom;
        String secondMessage;

        if (xmlType == 0) {
            secondMessage = "Choose file with trees";
        } else {
            secondMessage = "Choose file with data and traits";
        }

        FileGenerator generator = new FileGenerator();
        String fileToName = "//Users/agav755/Subversion/sampled-ancestors/simulatingSeqTemplate.xml";


        if (args != null && args.length > 0) {
            fileFrom = new java.io.File(args[0]);
            fileTo = new java.io.File(fileToName);
        } else {
            String message = "Choose template file";
            java.awt.Frame frame = new java.awt.Frame();
            java.awt.FileDialog chooser = new java.awt.FileDialog(frame, message,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() == null) {
                fileTo = new java.io.File(fileToName);
            } else {
                fileTo = new java.io.File(chooser.getDirectory(), chooser.getFile());
            }
            chooser.dispose();
            frame.dispose();
            frame = new java.awt.Frame();
            chooser = new java.awt.FileDialog(frame, secondMessage,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() == null) {
                System.out.println("File was not chosen.");
                System.exit(0);
            }
            fileFrom = new java.io.File(chooser.getDirectory(), chooser.getFile());
            chooser.dispose();
            frame.dispose();
        }

        if (xmlType == 0) {
            generator.insertTrees(fileTo, fileFrom);
        } else {
            generator.insertDataAndTraits(fileTo, fileFrom);
        }




    }

    public void insertDataAndTraits(java.io.File templateFile, java.io.File data_traitsFile) throws Exception {
        // read trees

        BufferedReader fin = null;
        String dataBegin = "<data dataType=\"nucleotide\" id=\"alignment\" name=\"alignment\">";
        String modelBegin = "<BirthDeathSkylineModel spec=\"SABDSkylineModel\" id=\"birthDeath\" tree=\"@tree\" >";
        String logFileSpec = "<logger fileName=\"simulated_SABDSKY.$(seed).log\" id=\"tracelog\" logEvery=\"10000\" mode=\"autodetect\" model=\"@posterior\">";
        String treeFileSpec = "<logger fileName=\"simulated_SABDSKY.$(seed).trees\" id=\"treelog\" logEvery=\"10000\" mode=\"tree\">";
        String traitLine = "<trait id=\"tipDates\" spec='beast.evolution.tree.TraitSet' traitname='date-backward' units='year' value='";


        try {
            fin = new BufferedReader(new FileReader(data_traitsFile));
            String str;
            str = fin.readLine();
            if (str.contains("Writing traits prior to data")) {
                while (fin.ready()) {
                    str = fin.readLine();
                    if (str.contains("traits")) {
                        String seed =  Integer.toString(Randomizer.nextInt(100000000));
                        String outputFileName = "/Users/agav755/Subversion/sampled-ancestors/" + seed + "_SABDSKY.xml";
                        String newLogFileSpec = "<logger fileName=\"" + seed + "_SABDSKY.$(seed).log\" id=\"tracelog\" logEvery=\"10000\" mode=\"autodetect\" model=\"@posterior\">";
                        String newTreeFileSpec = "<logger fileName=\"" + seed + "_SABDSKY.$(seed).trees\" id=\"treelog\" logEvery=\"10000\" mode=\"tree\">";
                        String[] treeInfo = null;
                        PrintWriter writer = null;
                        BufferedReader finTemplate = null;

                        String traits = fin.readLine();
                        readBefore(fin, "start sampling date");
                        String samplingRateChangeTime = fin.readLine();

                        try {
                            finTemplate = new BufferedReader(new FileReader(templateFile));
                            writer = new PrintWriter(new FileWriter(outputFileName));
                            String strTemplate;
                            while (finTemplate.ready()){
                                strTemplate = finTemplate.readLine();
                                if (strTemplate.contains(logFileSpec)) {
                                    writer.println(newLogFileSpec);
                                } else {
                                    if (strTemplate.contains(treeFileSpec)) {
                                        writer.println(newTreeFileSpec);
                                    } else {
                                        if (strTemplate.contains(traitLine)) {

                                            writer.println(traitLine);
                                            writer.println(traits + "'>");
                                        }  else writer.println(strTemplate);
                                    }
                                }
                                if (strTemplate.contains(modelBegin)){
                                    writer.println(samplingRateChangeTime);
                                }
                                if (strTemplate.contains(dataBegin)) {
                                    String newickTree = readBefore(fin, "The tree is");
                                    treeInfo = collectTreeInfo(newickTree, samplingRateChangeTime);
                                    readBefore(fin, "<data>");
                                    copyBefore(fin, writer, "</data>");
                                }


                            }

                            for (int i=0; i<treeInfo.length; i++){
                                writer.println(treeInfo[i]);
                            }
                        } catch (IOException e) {
                            //
                        }
                        finally {
                            if (finTemplate != null) {
                                finTemplate.close();
                            }
                            if (writer != null) {
                                writer.close();
                            }
                        }
                    }
                }
            } else {
                while (fin.ready()) {
                    str = fin.readLine();
                    if (str.contains("The tree is ")) {
                        String seed =  Integer.toString(Randomizer.nextInt(100000000));
                        String outputFileName = "/Users/agav755/Subversion/sampled-ancestors/" + seed + "_SABDSKY.xml";
                        String newLogFileSpec = "<logger fileName=\"" + seed + "_SABDSKY.$(seed).log\" id=\"tracelog\" logEvery=\"10000\" mode=\"autodetect\" model=\"@posterior\">";
                        String newTreeFileSpec = "<logger fileName=\"" + seed + "_SABDSKY.$(seed).trees\" id=\"treelog\" logEvery=\"10000\" mode=\"tree\">";
                        String[] treeInfo = null;
                        String newickTree = str;

                        PrintWriter writer = null;
                        BufferedReader finTemplate = null;


                        try {
                            finTemplate = new BufferedReader(new FileReader(templateFile));
                            writer = new PrintWriter(new FileWriter(outputFileName));
                            String strTemplate;
                            while (finTemplate.ready()){
                                strTemplate = finTemplate.readLine();
                                if (strTemplate.contains(logFileSpec)) {
                                    writer.println(newLogFileSpec);
                                } else {
                                    if (strTemplate.contains(treeFileSpec)) {
                                        writer.println(newTreeFileSpec);
                                    } else {
                                        if (strTemplate.contains(traitLine)) {
                                            readBefore(fin, "traits");
                                            String traits = fin.readLine();
                                            writer.println(traitLine);
                                            writer.println(traits + "'>");
                                        }  else writer.println(strTemplate);
                                    }
                                }

                                if (strTemplate.contains(dataBegin)) {
                                    readBefore(fin, "<data>");
                                    copyBefore(fin, writer, "</data>");
                                }

                                if (strTemplate.contains(modelBegin)){
                                    readBefore(fin, "start sampling date");
                                    String samplingRateChangeTime = fin.readLine();
                                    treeInfo = collectTreeInfo(newickTree, samplingRateChangeTime);
                                    writer.println(samplingRateChangeTime);
                                }
                            }
                            for (int i=0; i<treeInfo.length; i++){
                                writer.println(treeInfo[i]);
                            }
                        } catch (IOException e) {
                            //
                        }
                        finally {
                            if (finTemplate != null) {
                                finTemplate.close();
                            }
                            if (writer != null) {
                                writer.close();
                            }
                        }
                    }
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
}

