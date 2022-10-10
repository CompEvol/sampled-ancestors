package sa.app.tools;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import sa.util.SANexusParser;

/**
 * @author Alexandra Gavryushkina
 */

/** converts nexus files with SA trees to nexus file of binary trees 
 * Usage: SATreeToBinaryTreeConverter <inputFile> <outpuFile>
 * Default outputFile = /tmp/x.trees
 * **/
public class SATreeToBinaryTreeConverter {

    public static void perform(SANexusParser trace, boolean useNumbers, String outputFile) throws Exception {
    	PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
    	
    	trace.trees.get(0).init(out);
    	out.println();
    	for (int i=0; i< trace.trees.size(); i++) {
    		out.print("tree STATE_" + i + " = ");
    	    out.print(trace.trees.get(i).getRoot().toSortedNewick(new int[]{0}, false));
    	    out.println(";");
    	}
    	out.println("End;");
    	out.close();
    }
	
    public static void main(String[] args) throws IOException, Exception {

        boolean useNumbers = false;
        
        java.io.File file;
        String outputFile = "/tmp/x.trees";
        if (args != null && args.length > 0) {
            file = new java.io.File(args[0]);
            if (args.length == 2) {
            	outputFile = args[1];
            }
        } else {
            String message = "Choose file .trees";
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

        FileReader reader = null;

        try {
            System.err.println("Reading file " + file.getName());
            reader = new FileReader(file);
            SANexusParser parser = new SANexusParser();
            parser.parseFile(file);
            perform(parser, useNumbers, outputFile);
        }
        catch (IOException e) {
            //
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

}
