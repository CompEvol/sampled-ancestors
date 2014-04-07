package beast.util;

import beast.evolution.tree.Tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * @author Alexandra Gavryushkina
 */
public class SANexusParser extends NexusParser {

    //private List<NexusParserListener> listenersSA = new ArrayList<NexusParserListener>();

    /**
     * try to reconstruct Beast II objects from the given reader or just read newicks
     * if  parseAsNewicks = true.
     *
     * @param id     a name to give to the parsed results
     * @param reader a reader to parse from
     */
    @Override
    public void parseFile(String id, Reader reader) throws Exception {
        lineNr = 0;
        BufferedReader fin = null;
        if (reader instanceof BufferedReader) {
            fin = (BufferedReader) reader;
        } else {
            fin = new BufferedReader(reader);
        }
        try {
            while (fin.ready()) {
                String sStr = nextLine(fin);
                if (sStr == null) {
                    return;
                }
                if (sStr.toLowerCase().matches("^\\s*begin\\s+data;\\s*$") || sStr.toLowerCase().matches("^\\s*begin\\s+characters;\\s*$")) {
                    m_alignment = parseDataBlock(fin);
                    m_alignment.setID(id);
                } else if (sStr.toLowerCase().matches("^\\s*begin\\s+calibration;\\s*$")) {
                    traitSet = parseCalibrationsBlock(fin);
                } else if (sStr.toLowerCase().matches("^\\s*begin\\s+assumptions;\\s*$") ||
                        sStr.toLowerCase().matches("^\\s*begin\\s+sets;\\s*$") ||
                        sStr.toLowerCase().matches("^\\s*begin\\s+mrbayes;\\s*$")) {
                    parseAssumptionsBlock(fin);
                } else if (sStr.toLowerCase().matches("^\\s*begin\\s+taxa;\\s*$")) {
                    parseSATaxaBlock(fin);
                } else if (sStr.toLowerCase().matches("^\\s*begin\\s+trees;\\s*$")) {
                    parseSATreesBlock(fin);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Around line " + lineNr + "\n" + e.getMessage());
        }
    } // parseFile

    private void parseSATreesBlock(BufferedReader fin) throws Exception {
        trees = new ArrayList<Tree>();
        // read to first non-empty line within trees block
        String sStr = fin.readLine().trim();
        while (sStr.equals("")) {
            sStr = fin.readLine().trim();
        }

        int origin = -1;

        Map<String,String> translationMap = null;
        // if first non-empty line is "translate" then parse translate block
        if (sStr.toLowerCase().indexOf("translate") >= 0) {
            translationMap = parseSATranslateBlock(fin);
            origin = getSAIndexedTranslationMapOrigin(translationMap);
            if (origin != -1) {
                taxa = getSAIndexedTranslationMap(translationMap, origin);
            }
        }

        // read trees
        while (sStr != null) {
            if (sStr.toLowerCase().startsWith("tree ")) {
                int i = sStr.indexOf('(');
                if (i > 0) {
                    sStr = sStr.substring(i);
                }

                ZeroBranchSATreeParser ZeroBranchSATreeParser = null;

                if (origin != -1) {
                    ZeroBranchSATreeParser = new ZeroBranchSATreeParser (taxa, sStr, origin);
                } else {
                    try {
                        ZeroBranchSATreeParser = new ZeroBranchSATreeParser (taxa, sStr, 0);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        ZeroBranchSATreeParser = new ZeroBranchSATreeParser (taxa, sStr, 1);
                    }
                }
//                catch (NullPointerException e) {
//                    ZeroBranchSATreeParser = new ZeroBranchSATreeParser (m_taxa, sStr, 1);
//                }

//                for (NexusParserListener listener : listenersSA) {
//                    listener.treeParsed(trees.size(), ZeroBranchSATreeParser );
//                }

                //if (translationMap != null) ZeroBranchSATreeParser .translateLeafIds(translationMap);

                trees.add(ZeroBranchSATreeParser);

//				Node tree = ZeroBranchSATreeParser .getRoot();
//				tree.sort();
//				tree.labelInternalNodes(nNrOfLabels);
            }
            sStr = fin.readLine();
            if (sStr != null) sStr = sStr.trim();
        }
    }

    /**
     * @param reader a reader
     * @return a map of taxa translations, keys are generally integer node number starting from 1
     * whereas values are generally descriptive strings.
     * @throws java.io.IOException
     */
    private Map<String, String> parseSATranslateBlock(BufferedReader reader) throws IOException {

        Map<String,String> translationMap = new HashMap<String, String>();

        String line = reader.readLine();
        StringBuilder translateBlock = new StringBuilder();
        while (line != null && !line.trim().toLowerCase().equals(";")) {
            translateBlock.append(line.trim());
            line = reader.readLine();
        }
        String[] taxaTranslations = translateBlock.toString().split(",");
        for (String taxaTranslation : taxaTranslations) {
            String[] translation = taxaTranslation.split("[\t ]+");
            if (translation.length == 2) {
                translationMap.put(translation[0],translation[1]);
//                System.out.println(translation[0] + " -> " + translation[1]);
            } else {
                System.err.println("Ignoring translation:" + Arrays.toString(translation));
            }
        }
        return translationMap;
    }

    private void parseSATaxaBlock(BufferedReader fin) throws Exception {
        taxa = new ArrayList<String>();
        int nTaxaExpected = -1;
        String sStr;
        do {
            sStr = nextLine(fin);
            if (sStr.toLowerCase().matches("\\s*dimensions\\s.*")) {
                sStr = sStr.substring(sStr.toLowerCase().indexOf("ntax=") + 5);
                sStr = sStr.replaceAll(";", "");
                nTaxaExpected = Integer.parseInt(sStr.trim());
            } else if (sStr.toLowerCase().trim().equals("taxlabels")) {
                do {
                    sStr = nextLine(fin);
                    sStr = sStr.replaceAll(";", "");
                    sStr = sStr.trim();
                    if (sStr.length() > 0 && !sStr.toLowerCase().equals("end")) {
                        for (String taxon : sStr.split("\\s+")) {
                            taxa.add(taxon);
                        }
                    }
                } while (!sStr.toLowerCase().equals("end"));
            }
        } while (!sStr.toLowerCase().equals("end"));
        if (nTaxaExpected >= 0 && taxa.size() != nTaxaExpected) {
            throw new Exception("Taxa block: # taxa is not equal to dimension");
        }
    }

    private  List<String> getSAIndexedTranslationMap(Map<String, String> translationMap, int origin) {

        System.out.println("translation map size = " + translationMap.size());

        String[] taxa = new String[translationMap.size()];

        for (String key : translationMap.keySet()) {
            taxa[Integer.parseInt(key)-origin] = translationMap.get(key);
        }
        return Arrays.asList(taxa);
    }

    /**
     * @param translationMap
     * @return minimum key value if keys are a contiguous set of integers starting from zero or one, -1 otherwise
     */
    private int getSAIndexedTranslationMapOrigin(Map<String, String> translationMap) {

        SortedSet<Integer> indices = new TreeSet<Integer>();

        int count = 0;
        for (String key : translationMap.keySet()) {
            int index = Integer.parseInt(key);
            indices.add(index);
            count += 1;
        }
        if ((indices.last() - indices.first() == count - 1) && (indices.first() == 0 || indices.first() == 1)) {
            return indices.first();
        }
        return -1;
    }

}
