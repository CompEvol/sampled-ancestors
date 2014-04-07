package beast.app.tools;

import beast.evolution.tree.Tree;
import beast.util.NexusParser;
import beast.util.SANexusParser;

import java.io.*;

/**
 * @author Alexandra Gavryushkina
 */

public class SampledAncestorTreeAnalyser {

    public static void main(String[] args) throws IOException, Exception {

        int percentCredSet = 100;
        boolean useNumbers = true;

//        BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
//        String a;
//        try {
//            System.out.println("Type the size of credible set in percents: ");
//            a = buf.readLine();
//            percentCredSet = Integer.parseInt(a);
//            System.out.println("Would you like to use taxa names for tip labels? If so type 'yes'. Otherwise numbers will be used.");
//            a = buf.readLine();
//            if (a.equals("yes")) {
//                useNumbers = false;
//            }
//        } catch (IOException err) {
//            System.out.println("Error");
//        }


//        if (0 >= percentCredSet || percentCredSet > 100) {
//            System.out.println("The percent for credible set is out of the interval (0,100]" + " 95% credible set will be shown");
//            percentCredSet = 95;
//        }

        java.io.File file;

        if (args != null && args.length > 0) {
            file = new java.io.File(args[0]);
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
            System.out.println("Reading file " + file.getName());
            reader = new FileReader(file);
            SANexusParser parser = new SANexusParser();
            parser.parseFile(file);
            SampledAncestorTreeTrace trace = new SampledAncestorTreeTrace(parser);
            SampledAncestorTreeAnalysis analysis = new SampledAncestorTreeAnalysis(trace, percentCredSet);

            analysis.perform(useNumbers);
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
