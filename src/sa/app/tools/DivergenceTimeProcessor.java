package sa.app.tools;

import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.parser.NexusParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * @authot Alexandra Gavryushkina
 */
public class DivergenceTimeProcessor {

    NexusParser parser;
    List<BitSet> cladeList;
    List<Double> trueCladeAgeList;


    DivergenceTimeProcessor(String trueDTFileName, String treeFileName) throws Exception {
        File trueDTFile = new File(trueDTFileName);
        File treeFile = new File(treeFileName);

        readDT(trueDTFile);

        parser = new NexusParser();
        parser.parseFile(treeFile);

        PrintStream logWriter = null;
        PrintStream trueDTWriter = null;

        try {
            logWriter = new PrintStream(new File("DT.log"));

            int cladeCount = cladeList.size();
            logWriter.print("sample"+"\t");
            for (int i=0; i<cladeCount-1; i++) {
                logWriter.print("clade" + i + "\t");
            }
            logWriter.print("clade" + (cladeCount - 1) + "\n");

            int sampleIndex=0;
            for (Tree tree :parser.trees) {
                double[] divergenceTimes = new double[cladeCount];
                Arrays.fill(divergenceTimes, -1.0); 
                collectDT(tree.getRoot(), null, divergenceTimes);
                logWriter.print(sampleIndex+"\t");
                for (int i=0; i<cladeCount; i++) {
                    logWriter.print(divergenceTimes[i] + "\t");
                }
                logWriter.print(divergenceTimes[cladeCount-1] + "\n");
                sampleIndex++;

            }

            trueDTWriter = new PrintStream(new File("DT.txt"));

            trueDTWriter.print("sample"+"\t");
            for (int i=0; i<cladeCount; i++) {
                trueDTWriter.print("clade" + i + "\t");
            }
            trueDTWriter.println();
            trueDTWriter.print("0"+"\t");
            for (int i=0; i<cladeCount; i++) {
                trueDTWriter.print(trueCladeAgeList.get(i) + "\t");
            }


        } catch (IOException e) {
            //
        }
        finally {
            if (logWriter != null) {
                logWriter.close();
            }
        }

    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("There have to be two arguments: first, the file with true divergence times and second, the file with trees");
            System.exit(0);
        }

        DivergenceTimeProcessor processor = new DivergenceTimeProcessor(args[0], args[1]);

    }

    private void readDT(File file) throws Exception {

        FileReader reader = null;

        try {
            reader = new FileReader(file);
        }
        catch (IOException e) {
        }
        finally {
            if (reader != null) {
            }
            if (reader != null) {
            }
        }
        
        final BufferedReader fin;
        fin = new BufferedReader(reader);
        int lineNr = 0;
        try {
            boolean startProcessing = false;
            while (fin.ready()) {
                final String line = fin.readLine();
                lineNr++;

                if (!startProcessing && line.contains("divergence times")) {
                    startProcessing = true;
                    cladeList = new ArrayList<>();
                    trueCladeAgeList = new ArrayList<>();
                } else {
                    if (startProcessing && !line.contains("fossil count")) {
                        String[] cladeDetails = line.split("\\[|\\]");
                        trueCladeAgeList.add(Double.parseDouble(cladeDetails[0]));
                        cladeList.add(convertToBitSet(cladeDetails[1]));
                    } else {
                        startProcessing = false;
                    }
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Around line " + lineNr + "\n" + e.getMessage());
        }
    }

    private BitSet convertToBitSet(String cladeStr) {
        BitSet bits = new BitSet();
        cladeStr = cladeStr.replaceAll("\\s", "");
        String[] taxaInClade = cladeStr.split(",");

        for (String taxon : taxaInClade) {
            int index = Integer.parseInt(taxon.replace("A",""));
            bits.set(index);
        }
        return bits;
    }

    private void collectDT(Node node, BitSet bits, double[] divergenceTimes) {


        if (node.isLeaf()) {
            bits.set(Integer.parseInt(node.getID().replaceAll("A","")));
        } else {

            BitSet bits2 = new BitSet();
            for (int i = 0; i < node.getChildCount(); i++) {
                Node childNode = node.getChild(i);
                collectDT(childNode, bits2, divergenceTimes);
            }

            for (BitSet cladeBits:cladeList) {
                BitSet cladeBitsClone = (BitSet)cladeBits.clone();
                cladeBitsClone.andNot(bits2);
                if (cladeBitsClone.isEmpty()) {
                    int index = cladeList.indexOf(cladeBits);
                    if (divergenceTimes[index] < 0) {
                        divergenceTimes[index] = node.getHeight();
                    }
                }
            }

            if (bits != null) {
                bits.or(bits2);
            }
        }


    }

}
