package beast.app.tools;

import beast.util.SANexusParser;

import javax.swing.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * @uathor Alexandra Gavryushkina
 */
public class BayesFactorForSampledAncestors {

    public static void main(String[] args) throws Exception{


        java.io.File filePosterior=null;
        java.io.File filePrior=null;


        if (args != null && args.length == 2) {
            filePrior = new java.io.File(args[0]);
            filePosterior = new java.io.File(args[1]);
        } else {
            String message = "Choose tree file with sample from prior tree distribution";
            java.awt.Frame frame = new java.awt.Frame();
            java.awt.FileDialog chooser = new java.awt.FileDialog(frame, message,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() != null) {
                filePrior = new java.io.File(chooser.getDirectory(), chooser.getFile());
            } else {
                System.out.println("File was not chosen.");
                System.exit(0);
            }
            chooser.dispose();
            frame.dispose();
            message = "Choose tree-file with sample from posterior tree distribution";
            frame = new java.awt.Frame();
            chooser = new java.awt.FileDialog(frame, message,
                    java.awt.FileDialog.LOAD);
            chooser.setVisible(true);
            if (chooser.getFile() != null) {
                filePosterior = new java.io.File(chooser.getDirectory(), chooser.getFile());
            } else {
                System.out.println("File was not chosen.");
                System.exit(0);
            }
            chooser.dispose();
            frame.dispose();
        }

        FrequencySet<String> priorSAFreq = new FrequencySet<String>();
        FrequencySet<String> posteriorSAFreq = new FrequencySet<String>();
        int treeCountPr = 0;
        int treeCountPost=0;

        FileReader readerPr = null;
        FileReader readerPost = null;

        try {
            readerPr = new FileReader(filePrior);
            SANexusParser parserPr = new SANexusParser();
            parserPr.parseFile(filePrior);
            SampledAncestorTreeTrace tracePr = new SampledAncestorTreeTrace(parserPr);
            treeCountPr = tracePr.treeCount;
            SampledAncestorTreeAnalysis analysisPr = new SampledAncestorTreeAnalysis(tracePr, 95);

            priorSAFreq = analysisPr.countSAFrequencies(false);

            readerPost = new FileReader(filePosterior);
            SANexusParser parserPost = new SANexusParser();
            parserPost.parseFile(filePosterior);
            SampledAncestorTreeTrace tracePost = new SampledAncestorTreeTrace(parserPost);
            treeCountPost = tracePost.treeCount;
            SampledAncestorTreeAnalysis analysisPost = new SampledAncestorTreeAnalysis(tracePost, 95);

            posteriorSAFreq = analysisPost.countSAFrequencies(false);
        }
        catch (IOException e) {
            //
        }
        finally {
            if (readerPr != null) {
                readerPr.close();
            }
            if (readerPost != null) {
                readerPost.close();
            }
        }

//
//        System.out.println("Node label \t Bayes Factor to support that the node is SA");
//
//        for (int i=0; i<posteriorSAFreq.size(); i++) {
//            String sa = posteriorSAFreq.get(i);
//            double pPost = (double)posteriorSAFreq.getFrequency(i)/treeCountPost;
//            if (priorSAFreq.getFrequency(sa) != null){
//                double pPr = (double)priorSAFreq.getFrequency(sa)/treeCountPr;
//                System.out.format(sa + "\t %1.4f",((pPr/(1-pPr))/pPost/(1-pPost)));
//                System.out.println();
//            }
//        }

        HashSet<String> labels = new HashSet<String>();

        for (int i=0; i<priorSAFreq.size(); i++){
            labels.add(priorSAFreq.get(i));
        }
        for (int i=0; i<posteriorSAFreq.size(); i++){
            labels.add(posteriorSAFreq.get(i));
        }

        System.out.println("Label \t prior \t posterior");

        for (String sa:labels){
            double freqPost, freqPr;
            if (posteriorSAFreq.getFrequency(sa) != null) {
                freqPost = (double)posteriorSAFreq.getFrequency(sa)/treeCountPost;
            } else freqPost=0.;
            if (priorSAFreq.getFrequency(sa) != null) {
                freqPr = (double)priorSAFreq.getFrequency(sa)/treeCountPr;
            } else freqPr=0.;
            System.out.format(sa+"\t %2.4f \t %2.4f", freqPr, freqPost);
            System.out.println();
        }


    }
}
