package beast.app.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

/**
 * @author Alexandra Gavryushkina
 */

public class AnalysisAnalyser {

    public static void main(String[] args) throws Exception {
        java.io.File file;

        if (args != null && args.length > 0) {
            file = new java.io.File(args[0]);
        } else {
            String message = "Choose file .txt";
            java.awt.Frame frame = new java.awt.Frame();
            java.awt.FileDialog chooser = new java.awt.FileDialog(frame, message,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() == null) {
                System.out.println("The file was not chosen.");
                System.exit(0);
            }
            file = new java.io.File(chooser.getDirectory(), chooser.getFile());
            chooser.dispose();
            frame.dispose();
        }

        int[] values = null;
        String[] treeStrings = new String[] {"((1,2),3)", "((1,2))3", "((1)2,3)", "((1,3),2)", "(1,(2,3))",
                "(1,(2)3)", "((1)3,2)"};
        Reader reader = null;
        try {
            reader = new FileReader(file);
            AnalysisAnalyser analyser = new AnalysisAnalyser();
            values = analyser.countCorrectValues(reader, treeStrings);
        } catch (IOException e) {
            //
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (values != null) {
            for (int i=0; i <values.length; i++) {
                System.out.println("The probability of tree " + treeStrings[i] + " has been calculated correctly " + values[i] + " times");
            }
        }  else {
            System.out.println("Something went wrong. May be file does not exist or is broken.");
        }


    }

    private int[] countCorrectValues(Reader reader, String[] treeStrings) throws Exception{

        int[] values = new int[treeStrings.length];
        Arrays.fill(values, 0);
        String str;
        Boolean inValuesBlock = false;
        str = readLine(reader);
        while (str != null) {
            if (str.lastIndexOf("Topology") > 0) {
                inValuesBlock = true;
                str = readLine(reader);
                continue;
            }
            if (inValuesBlock) {
                if (str.lastIndexOf("Total")>0){
                    inValuesBlock = false;
                }  else {
                    for (int i =0; i < treeStrings.length; i++) {
                        if (str.lastIndexOf(treeStrings[i]) > 0 && str.lastIndexOf("incorrect") == -1) {
                            values[i]++;
                            break;
                        }
                    }
                }
            }
            str = readLine(reader);
        }
        return values;
    }

    private String readLine(Reader reader) throws Exception {
        String out = "";
        int ch;
        do {
            ch = reader.read();
            if (ch == -1) {
                out = null;
                break;
            }
            out += (char)ch;
        } while(ch != -1 && (char)ch != '\n' && (char)ch != '\r');
        return out;
    }
}
