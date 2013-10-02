package test.beast.evolution.speciation;

import beast.core.parameter.RealParameter;
import beast.evolution.speciation.SABDSkylineModel;
import beast.evolution.tree.Tree;
import beast.util.TreeParser;
import beast.util.ZeroBranchSATreeParser;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author Alexandra Gavryushkina
 */
public class SABDSkylineModelTest  extends TestCase {

    @Test
    public void testLikelihoodCalculation1() throws Exception {        //TODO make this test actually test something
        SABDSkylineModel model = new SABDSkylineModel();
        Tree tree = new TreeParser("((1:1.0)2:1.0)3:0.0", false, true, false, 1);

        model.setInputValue("tree", tree);
        model.setInputValue("orig_root", new RealParameter("1."));
        model.setInputValue("R0", new RealParameter("2."));
        model.setInputValue("becomeUninfectiousRate", new RealParameter("0.4"));
        model.setInputValue("samplingProportion", new RealParameter("0. 0.4"));
        model.setInputValue("reverseTimeArrays", "false false true false");

        model.setInputValue("becomeNoninfectiousAfterSamplingProbability", new RealParameter("0.0") );
        model.setInputValue("samplingRateChangeTimes", new RealParameter("3.6 0."));
        model.initAndValidate();

        //System.out.println(model.calculateTreeLogLikelihood(tree));

    }

    @Test
    public void testLikelihoodCalculation2() throws Exception {        //TODO make this test actually test something
        SABDSkylineModel model = new SABDSkylineModel();
        String newick ="(((((15:0.9072117015322814)5:2.344954805931658,8:2.555746538150742):3.5142478689380257,((29:0.9245446168771885)24:0.3882787277119544,26:0.6887571305654259):7.398320466425792):0.037086832505206946,(((14:1.1992219324855915,16:1.3107447845913667):5.116260351192503,0:5.29920798325698):0.3471661031356965,1:5.697695243717641):0.08353354362752086):1.1601142400847981,(((28:4.047418176969043,2:1.1748554485602645):0.1542887610529391,(((22:0.9632745788120705,20:0.5192622525808339):0.1075697340747368)10:1.5031024967077764,(18:0.4062215672232341)11:1.533548304754766):0.4331934410768703):1.5258272297761808,((((21:0.3039618290915733)17:0.6300310116485406,(27:1.1452329241703278)23:1.2924829756018568):1.1855883878998474,12:1.5293925069503382):1.1631142615640906,(((25:1.7450071199157016,19:0.7209133127843366):0.3517976222075845)4:1.67964840200941,(((13:0.3009537579938577)9:0.7051470183258033,(7:0.19708572826965742)3:0.2801585747979054):0.7749055179641084,6:1.1610810225875179):0.6160918463315141):0.30869688906978876):0.9113380444042924):4.137074764985794):0.0";

        ArrayList<String> taxa = new ArrayList<String>();
        for (int i=0; i<30; i++) {
            taxa.add(Integer.toString(i));
        }

        ZeroBranchSATreeParser tree = new ZeroBranchSATreeParser(taxa, newick, 0, false);

        model.setInputValue("tree", tree);
        model.setInputValue("orig_root", new RealParameter("1."));
        model.setInputValue("birthRate", new RealParameter("1."));
        model.setInputValue("deathRate", new RealParameter("0.2"));
        model.setInputValue("samplingRate", new RealParameter("0. 0.5"));
        model.setInputValue("reverseTimeArrays", "false false true false");

        model.setInputValue("becomeNoninfectiousAfterSamplingProbability", new RealParameter("0.5") );
        model.setInputValue("samplingRateChangeTimes", new RealParameter("3.1 0."));
        model.initAndValidate();

        System.out.println(model.calculateTreeLogLikelihood(tree));

    }

}
