package beast.app.tools;

import beast.util.Randomizer;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Alexandra Gavryushkina
 */
public class FileGenerator {

    FileGenerator() {
        //outputFileName = "/Users/sasha/Subversion/sampled-ancestors/" + Integer.toString(Randomizer.nextInt(100000000)) + ".simulatingSeq.xml";
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
        String traitsBegin = "<trait id=\"tipDates\" spec='beast.evolution.tree.TraitSet' traitname='date-backward' units='year' value='";
        String modelBegin = "<BirthDeathSkylineModel spec=\"SABDSkylineModel\" id=\"birthDeath\" tree=\"@tree\" >";

        try {
            fin = new BufferedReader(new FileReader(data_traitsFile));
            String str;
            while (fin.ready()) {
                str = fin.readLine();
                if (str.contains("<data>")) {
                    String outputFileName = "/Users/agav755/Subversion/sampled-ancestors/" + Integer.toString(Randomizer.nextInt(100000000)) + ".SABDSKY.xml";
                    PrintWriter writer = null;
                    BufferedReader finTemplate = null;

                    try {
                        finTemplate = new BufferedReader(new FileReader(templateFile));
                        writer = new PrintWriter(new FileWriter(outputFileName));
                        String strTemplate;
                        while (finTemplate.ready()){
                            strTemplate = finTemplate.readLine();
                            writer.println(strTemplate);
                            if (strTemplate.contains(dataBegin)) {
                                String currentData = fin.readLine();
                                while (!currentData.contains("</data>")) {
                                    writer.println(currentData);
                                    currentData = fin.readLine();
                                }
                            }
                            if (strTemplate.contains(traitsBegin)) {
                                String str1 = fin.readLine();
                                while (!str1.contains("traits")) {
                                    str1 = fin.readLine();
                                }
                                String traits = fin.readLine();
                                writer.println(traits + "'>");
                            }
                            if (strTemplate.contains(modelBegin)){
                                String str1 = fin.readLine();
                                while (!str1.contains("start sampling date")) {
                                    str1 = fin.readLine();
                                }
                                String samplingRateChangeTime = fin.readLine();
                                writer.println(samplingRateChangeTime);
                            }
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

