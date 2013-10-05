package beast.app.tools;

import beast.util.Randomizer;

import java.io.*;
import java.util.ArrayList;

/**
 * Alexandra Gavryushkina
 */
public class FileGenerator {

    private String fileToName = "/Users/sasha/Subversion/sampled-ancestors/simulatingSeqTemplate.xml";
    private String insertionPlace = "<input name='newick'>";
    //private String outputFileName;

    FileGenerator() {
        //outputFileName = "/Users/sasha/Subversion/sampled-ancestors/" + Integer.toString(Randomizer.nextInt(100000000)) + ".simulatingSeq.xml";
    }

    public void insertTrees(java.io.File templateFile, java.io.File treeFile) throws Exception {
        // read trees
        ArrayList<String> trees = new ArrayList<String>();

        BufferedReader fin = null;

        try {
            fin = new BufferedReader(new FileReader(treeFile));
            String str;
            while (fin.ready()) {
                str = fin.readLine();
                int semicolonIndex = str.indexOf(";");
                if (semicolonIndex < str.length()-1) {
                   throw new Exception("There has to be no line terminators within a newick tree and it has to be ended with '; + line terminator'");
                }
                str = str.substring(0, semicolonIndex);
                trees.add(str);
            }
        } catch (IOException e) {
            //
        }
        finally {
            if (fin != null) {
                fin.close();
            }
        }

        for (int i=0; i<trees.size(); i++) {
            String outputFileName = "/Users/sasha/Subversion/sampled-ancestors/" + Integer.toString(Randomizer.nextInt(100000000)) + ".simulatingSeq.xml";
            PrintWriter writer = null;
            BufferedReader finTo = null;

            try {
                finTo = new BufferedReader(new FileReader(templateFile));
                writer = new PrintWriter(new FileWriter(outputFileName));
                String str;
                while (finTo.ready()){
                    str = finTo.readLine();
                    writer.println(str);
                    if (str.indexOf(insertionPlace) != -1) {
                        writer.println(trees.get(i));
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

        java.io.File fileTo, fileFrom;

        FileGenerator generator = new FileGenerator();

        if (args != null && args.length > 0) {
            fileFrom = new java.io.File(args[0]);
            fileTo = new java.io.File(generator.fileToName);
        } else {
            String message = "Choose the template file";
            java.awt.Frame frame = new java.awt.Frame();
            java.awt.FileDialog chooser = new java.awt.FileDialog(frame, message,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() == null) {
                fileTo = new java.io.File(generator.fileToName);
            } else {
                fileTo = new java.io.File(chooser.getDirectory(), chooser.getFile());
            }
            chooser.dispose();
            frame.dispose();
            message = "Choose the file with data";
            frame = new java.awt.Frame();
            chooser = new java.awt.FileDialog(frame, message,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() == null) {
                System.out.println("The file was not chosen.");
                System.exit(0);
            }
            fileFrom = new java.io.File(chooser.getDirectory(), chooser.getFile());
            chooser.dispose();
            frame.dispose();
        }

        //generator.insertTrees(fileTo, fileFrom);
        generator.insertDataAndTraits(fileTo, fileFrom);


    }

    public void insertDataAndTraits(java.io.File templateFile, java.io.File data_traitsFile) throws Exception {
        // read trees

        BufferedReader fin = null;
        String dataBegin = "<data dataType=\"nucleotide\" id=\"alignment\" name=\"alignment\">";
        String traitsBegin = "<trait id=\"tipDates\" spec='beast.evolution.tree.TraitSet' traitname='date-backward' units='year' value='";

        try {
            fin = new BufferedReader(new FileReader(data_traitsFile));
            String str;
            while (fin.ready()) {
                str = fin.readLine();
                if (str.indexOf("<data>") != -1) {
                    String outputFileName = "/Users/sasha/Subversion/sampled-ancestors/" + Integer.toString(Randomizer.nextInt(100000000)) + ".SABDSKY.xml";
                    PrintWriter writer = null;
                    BufferedReader finTemplate = null;

                    try {
                        finTemplate = new BufferedReader(new FileReader(templateFile));
                        writer = new PrintWriter(new FileWriter(outputFileName));
                        String strTemplate;
                        while (finTemplate.ready()){
                            strTemplate = finTemplate.readLine();
                            writer.println(strTemplate);
                            if (strTemplate.indexOf(dataBegin) != -1) {
                                String currentData = fin.readLine();
                                while (currentData.indexOf("</data>") == -1) {
                                    writer.println(currentData);
                                    currentData = fin.readLine();
                                }
                            }
                            if (strTemplate.indexOf(traitsBegin) != -1) {
                                String str1 = fin.readLine();
                                while (str1.indexOf("traits") == -1) {
                                    str1 = fin.readLine();
                                }
                                String traits = fin.readLine();
                                writer.println(traits + "'>");
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

