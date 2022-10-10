package beast.evolution.speciation;

import beast.base.inference.MCMC;
import beast.base.inference.Operator;
import beast.base.inference.StateNode;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.RealParameter;
import beast.base.core.BEASTInterface;
import beast.base.core.Citation;
import beast.base.core.Description;
import beast.base.core.Function;
import beast.base.core.Input;
import beast.base.core.Log;
import beast.base.evolution.alignment.Taxon;
import beast.base.evolution.operator.Exchange;
import beast.base.evolution.operator.ScaleOperator;
import beast.base.evolution.operator.SubtreeSlide;
import beast.base.evolution.operator.TipDatesRandomWalker;
import beast.base.evolution.operator.Uniform;
import beast.base.evolution.operator.WilsonBalding;
import beast.evolution.operators.*;
import beast.evolution.tree.TreeWOffset;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.evolution.tree.TreeDistribution;
import beast.base.evolution.tree.TreeInterface;

import java.util.List;

/**
 * @author Alexandra Gavryushkina
 */

//The tree density is from: Tanja Stadler et al. "Estimating the Basic Reproductive Number from Viral Sequence Data"

@Description("Calculate tree density under Birth Death Sampling Through Time Model for Epidemics " +
        "that is the BDM where an individual is sampled at a time with a constant rate psi" +
        " and where an individual becomes noninfectious immediately after the sampling" +
        "with a constant probability r")
@Citation(value = "Gavryushkina A, Welch D, Stadler T, Drummond AJ (2014) \n" +
        "Bayesian inference of sampled ancestor trees for epidemiology and fossil calibration. \n" +
        "PLoS Comput Biol 10(12): e1003919. doi:10.1371/journal.pcbi.1003919",
        year = 2014, firstAuthorSurname = "Gavryushkina", DOI="10.1371/journal.pcbi.1003919")
public class SABirthDeathModel extends TreeDistribution {

	public Input<TreeWOffset> treeWOffsetInput =
            new Input<TreeWOffset>("treeWOffset", "Optional fully extinct tree", (TreeWOffset)null);
	
    //'direct' parameters
    public Input<RealParameter> originInput =
            new Input<RealParameter>("origin", "The time when the process started", (RealParameter)null);
    public Input<RealParameter> birthRateInput =
            new Input<RealParameter>("birthRate", "Birth rate", Input.Validate.REQUIRED);
    public Input<Function> deathRateInput =
            new Input<Function>("deathRate", "Death rate", Input.Validate.REQUIRED);
    public Input<RealParameter> samplingRateInput =
            new Input<RealParameter>("samplingRate", "Sampling rate per individual", Input.Validate.REQUIRED);

    //transformed parameters:
    public Input<RealParameter> expectedNInput =
            new Input<RealParameter>("expectedN", "The expected-N-at-present parameterisation of T",(RealParameter)null);
    public Input<RealParameter> diversificationRateInput =
            new Input<RealParameter>("diversificationRate", "Net diversification rate. Birth rate - death rate", Input.Validate.XOR, birthRateInput);
    public Input<Function> turnoverInput =
            new Input<Function>("turnover", "Turnover. Death rate/birth rate", Input.Validate.XOR, deathRateInput);
    public Input<RealParameter> samplingProportionInput =
            new Input<RealParameter>("samplingProportion", "The probability of sampling prior to death. Sampling rate/(sampling rate + death rate)", Input.Validate.XOR, samplingRateInput);


    // r parameter
    public Input<RealParameter> removalProbability =
            new Input<RealParameter>("removalProbability", "The probability that an individual is removed from the process after the sampling", Input.Validate.REQUIRED);

    public Input<RealParameter> rhoProbability =
            new Input<RealParameter>("rho", "Probability of an individual to be sampled at present", (RealParameter)null);

    // if the tree likelihood is condition on sampling at least one individual then set to true one of the inputs:
    public Input<Boolean> conditionOnSamplingInput = new Input<Boolean>("conditionOnSampling", "the tree " +
            "likelihood is conditioned on sampling at least one individual if condition on origin or at least one individual on both sides of the root if condition on root", false);
    public Input<Boolean> conditionOnRhoSamplingInput = new Input<Boolean>("conditionOnRhoSampling", "the tree " +
            "likelihood is conditioned on sampling at least one individual in present if condition on origin or at lease one extant individual on both sides of the root if condition on root", false);

    public Input<Boolean> conditionOnRootInput = new Input<Boolean>("conditionOnRoot", "the tree " +
            "likelihood is conditioned on the root height otherwise on the time of origin", false);

    public Input<Taxon> taxonInput = new Input<Taxon>("taxon", "a name of the taxon for which to calculate the prior probability of" +
            "being sampled ancestor under the model", (Taxon) null);

    public final Input<IntegerParameter> SATaxonInput = new Input<IntegerParameter>("SAtaxon", "A binary parameter is equal to zero " +
            "if the taxon is not a sampled ancestor (that is, it does not have sampled descendants) and to one " +
            "if it is a sampled ancestor (that is, it has sampled descendants)", (IntegerParameter)null);

    protected double r;
    protected double lambda;
    protected double mu;
    protected double psi;
    protected double c1;
    protected double c2;
    protected double origin;
    protected double rho;
    protected boolean transform; //is true if the model is parametrised through transformed parameters

    protected boolean transformT = false;
    protected boolean originSpecified;

    protected boolean lambdaExceedsMu = false;
    protected String taxonName;
    protected double taxonAge;
    
    TreeWOffset combinedTree;

    public void initAndValidate() {

        if (originInput.get() != null && expectedNInput.get() != null){
            throw new IllegalArgumentException("Only one of (origin, expectedN) inputs may be specified.");
        }

        originSpecified = originInput.get() != null || expectedNInput.get() != null;
        transformT = expectedNInput.get() != null;

        if (!originSpecified && !conditionOnRootInput.get()) {
            throw new IllegalArgumentException("Specify one of (origin, expectedN) inputs or set conditionOnRoot input to \"true\"");
        }

        if (originSpecified && conditionOnRootInput.get()) {
            throw new RuntimeException("Remove (origin/expectedN) input or set conditionOnRoot input to \"false\"");
        }

        if (conditionOnSamplingInput.get() && conditionOnRhoSamplingInput.get()){
            throw new IllegalArgumentException("Either set to \"true\" only one of conditionOnSampling and conditionOnRhoSampling inputs or don't specify both!");
        }

        if (birthRateInput.get() != null && deathRateInput.get() != null && samplingRateInput.get() != null) {

            transform = false;

        } else if (diversificationRateInput.get() != null && turnoverInput.get() != null && samplingProportionInput.get() != null) {

            transform = true;

        } else {
            throw new IllegalArgumentException("Either specify birthRate, deathRate and samplingRate OR specify diversificationRate, turnover and samplingProportion!");
        }
        
        combinedTree = treeWOffsetInput.get();
        if(combinedTree == null) {
        	combinedTree = new TreeWOffset();
        	combinedTree.setInputValue("tree", treeInput.get());
        	combinedTree.initAndValidate();
        }
        else if(treeInput.get() != null) {
        	System.err.println("Both tree and treeWOffset specified as inputs, using treeWOffset.");
        }
        double rootHeight = combinedTree.getTree().getRoot().getHeight();
        if (originSpecified && origin() < rootHeight){
            throw new IllegalArgumentException("Initial value of origin (" + origin() + ") should be greater than initial root height (" +rootHeight + ")");
        }

        if (conditionOnRootInput.get() && !conditionOnRhoSamplingInput.get() && !conditionOnSamplingInput.get()) {
            throw new IllegalArgumentException("When conditioning on the root, we always assume that both sides of the initial bifurcation event are sampled. Please set either " +
                    "conditionOnSampling or conditionOnRhoSampling to true.");
        }


//        r = becomeNoninfectiousAfterSamplingProbability.get().getValue();
//        if (rhoProbability.get() != null ) {
//            rho = rhoProbability.get().getValue();
//        } else {
//            rho = 0.;
//        }
//        c1 = Math.sqrt((lambda - mu - psi) * (lambda - mu - psi) + 4 * lambda * psi);
//        c2 = -(lambda - mu - 2*lambda*rho - psi) / c1;
//        origin = originInput.get().getValue();
        
        // sanity check for sampled ancestor analysis
    	boolean isSAAnalysis = false;
    	if (removalProbability.get() != null && removalProbability.get().getValue() >= 1.0 && removalProbability.get().isEstimatedInput.get()) {
    		// default parameters have estimated=true by default.
    		// check there is an operator on this parameter
    		for (BEASTInterface o : removalProbability.get().getOutputs()) {
    			if (o instanceof Operator) {
    				isSAAnalysis = true;
    			}
    		}
    	}
        if (removalProbability.get() != null && removalProbability.get().getValue() < 1.0 || isSAAnalysis) {
        	// this is a sampled ancestor analysis
        	// check that there are no invalid operators in this analysis
        	List<Operator> operators = getOperators(this);
        	if (operators != null) {
        		for (Operator op : operators) {
        			boolean isOK = true;
        			if (op.getClass().isAssignableFrom(TipDatesRandomWalker.class) || 
                            (op.getClass().isAssignableFrom(SubtreeSlide.class) && ((SubtreeSlide)op).treeInput.get() == treeInput.get()) ||
                            (op.getClass().isAssignableFrom(WilsonBalding.class) && ((WilsonBalding)op).treeInput.get() == treeInput.get()) ||
                            (op.getClass().isAssignableFrom(Uniform.class) && ((Uniform)op).treeInput.get() == treeInput.get()) ||
                            (op.getClass().isAssignableFrom(Exchange.class) && ((Exchange)op).treeInput.get() == treeInput.get())) {

                                isOK = false;

        			} else if (op.getClass().isAssignableFrom(ScaleOperator.class)) {
        				// scale operators on Trees should be replaced with SAScaleOperator
        				for (StateNode o : op.listStateNodes()) {
        					if (o == treeInput.get()) {
        						isOK = false;
        					}
        				}
        			}        		 	
        			if (!isOK) {
        				Log.err.println("ERROR: " + op.getClass().getSimpleName() + 
        						" is not a valid operator for a sampled ancestor analysis.\n" + 
        						"Either remove the operator (id=" + op.getID() + ") or fix the " +
        					    "removal probability to 1.0 so this is not a sampled ancestor " +
        					    "analysis any more. The current analysis is not valid.");
        			}
        		}
        	}
        }

        if (taxonInput.get() != null) {
    	    if (SATaxonInput == null) {
                throw new IllegalArgumentException("If the taxon input is specified SAInput also has to be specified");
            }
            if (conditionOnRootInput.get()) {
                throw new RuntimeException("Calculate the prior probability of a taxon is not implemented under the model" +
                        "with conditionOnTheRoot option!");
            }
    	    taxonName = taxonInput.get().getID();
    	    TreeInterface tree = treeInput.get();
    	    taxonAge = 0.0;
    	    for (int i=0; i<tree.getLeafNodeCount(); i++) {
    	        Node node=tree.getNode(i);
    	        if (taxonName.equals(node.getID())) {
    	            taxonAge = node.getHeight();
                }
            }
        }
    }

    private List<Operator> getOperators(BEASTInterface o) {
    	for (BEASTInterface out : o.getOutputs()) {		
    		if (out instanceof MCMC) {
    			return ((MCMC)out).operatorsInput.get();
    		} else {
    			List<Operator> list = getOperators(out);
    			if (list != null) {
    				return list;
    			}
    		}
    	}
		return null;
	}

	private double p0s(double t, double c1, double c2) {
        double p0 = (lambda + mu + psi - c1 * ((1 + c2) - Math.exp(-c1 * t) * (1 - c2)) / ((1 + c2) + Math.exp(-c1 * t) * (1 - c2))) / (2 * lambda);
        return r + (1 - r) * p0;
    }

    protected double log_p0s(double t, double c1, double c2) {
        double p0 = (lambda + mu + psi - c1 * ((1 + c2) - Math.exp(-c1 * t) * (1 - c2)) / ((1 + c2) + Math.exp(-c1 * t) * (1 - c2))) / (2 * lambda);
        return Math.log(r + (1 - r) * p0);
    }

    protected double oneMinusP0(double t, double c1, double c2) {
        return 1 - (lambda + mu + psi - c1 * ((1 + c2) - Math.exp(-c1 * t) * (1 - c2)) / ((1 + c2) + Math.exp(-c1 * t) * (1 - c2))) / (2 * lambda);
    }

    protected double log_oneMinusP0(double t, double c1, double c2) {
        return Math.log(1 - (lambda + mu + psi - c1 * ((1 + c2) - Math.exp(-c1 * t) * (1 - c2)) / ((1 + c2) + Math.exp(-c1 * t) * (1 - c2))) / (2 * lambda));
    }

    protected double oneMinusP0Hat(double t, double c1, double c2) {
        return rho*(lambda-mu)/(lambda*rho + (lambda*(1-rho) - mu)* Math.exp((mu-lambda) * t)) ;
    }

    protected double log_oneMinusP0Hat(double t, double c1, double c2) {
        return Math.log(rho) + Math.log(lambda-mu)- Math.log(lambda*rho + (lambda*(1-rho) - mu)* Math.exp((mu-lambda) * t)) ;
    }

    protected double pS(double t) {
        return psi*Math.exp((lambda - mu - psi*r) * t);
    }

    protected double q(double t, double c1, double c2) {
        return Math.exp(c1 * t) * (1 + c2) * (1 + c2) + Math.exp(-c1 * t) * (1 - c2) * (1 - c2) + 2 * (1 - c2 * c2);
    }

    protected double log_q(double t, double c1, double c2) {
        return Math.log(Math.exp(c1 * t) * (1 + c2) * (1 + c2) + Math.exp(-c1 * t) * (1 - c2) * (1 - c2) + 2 * (1 - c2 * c2));
    }

    /**
     * @return the current origin, regardless of parameterization
     */
    private double origin() {
        if (transformT) {
            double N = expectedNInput.get().getValue();
            return Math.log((1.0 - turnover())*N + turnover())/d();
        }
        return originInput.get().getValue();
    }

    /**
     * @return the current diversification rate, regardless of parametrization.
     */
    private double d() {
        if (transform) return diversificationRateInput.get().getValue();

        double lambda = birthRateInput.get().getValue();
        return lambda * (1.0 - turnover());
    }

    /**
     * @return the current turnover, regardless of parametrization.
     */
    private double turnover() {
        if (transform) return turnoverInput.get().getArrayValue();

        double lambda = birthRateInput.get().getValue();
        double mu = deathRateInput.get().getArrayValue();

        return mu/lambda;
    }

    private void transformParameters() {
        double d = diversificationRateInput.get().getValue();
        double r_turnover = turnoverInput.get().getArrayValue();
        double s = samplingProportionInput.get().getValue();
        lambda = d/(1-r_turnover);
        mu = r_turnover*lambda;
        psi = mu*s/(1-s);
    }

    protected void updateParameters() {

        if (transform) {
            transformParameters();
        } else {
            lambda = birthRateInput.get().getValue();
            mu = deathRateInput.get().getArrayValue();
            psi = samplingRateInput.get().getValue();
        }

        r = removalProbability.get().getValue();
        if (rhoProbability.get() != null ) {
            rho = rhoProbability.get().getValue();
        } else {
            rho = 0.;
        }
        c1 = Math.sqrt((lambda - mu - psi) * (lambda - mu - psi) + 4 * lambda * psi);
        c2 = -(lambda - mu - 2*lambda*rho - psi) / c1;
        if (originSpecified){
            origin = origin();
        }  else {
            origin = Double.POSITIVE_INFINITY;
        }
    }

    @Override
    public double calculateLogP() {
    	Tree tree = combinedTree.getTree(); 
    	
        int nodeCount = tree.getNodeCount();
        updateParameters();
        if (lambdaExceedsMu && lambda <= mu) {
            return Double.NEGATIVE_INFINITY;
        }

        if (lambda < 0 || mu < 0 || psi < 0) {
            return Double.NEGATIVE_INFINITY;
        }

        //double x0 = tree.getRoot().getHeight() + origToRootDistance;
        double x0 = origin;

        if (taxonInput.get() != null) {

            if (taxonAge > origin) {
                return Double.NEGATIVE_INFINITY;
            }
            logP = 0.0;

            if (conditionOnSamplingInput.get()) {
                logP -= Math.log(oneMinusP0(x0, c1, c2));
            }

            if (conditionOnRhoSamplingInput.get()) {
                logP -= Math.log(oneMinusP0Hat(x0, c1, c2));
            }

            if (SATaxonInput.get().getValue() == 0) {
                logP += Math.log(1 - oneMinusP0(taxonAge, c1, c2));
            } else {
                logP += Math.log(oneMinusP0(taxonAge, c1, c2));
            }

            return logP;
        }

        double x1 = combinedTree.getHeightOfNode(tree.getRoot().getNr());

        if (x0 < x1 || r==1 && tree.getDirectAncestorNodeCount() > 0) {
            return Double.NEGATIVE_INFINITY;
        }
        
        logP = 0;
        if (!conditionOnRootInput.get()){
            logP = -Math.log(q(x0, c1, c2));
        } else {
            if (tree.getRoot().isFake()){ //when conditioning on the root we assume the process
                //starts at the time of the first branching event and
                //that means that the root can not be a sampled ancestor
                return Double.NEGATIVE_INFINITY;
            } else {
                logP = -Math.log(q(x1, c1, c2));
            }
        }

        if (conditionOnSamplingInput.get()) {
            if (conditionOnRootInput.get()) {
                logP -= Math.log(lambda*oneMinusP0(x1, c1, c2)* oneMinusP0(x1, c1, c2));
            } else {
                logP -= Math.log(oneMinusP0(x0, c1, c2));
            }

        }

        if (conditionOnRhoSamplingInput.get()) {
            if (conditionOnRootInput.get()) {
                logP -= Math.log(lambda*oneMinusP0Hat(x1, c1, c2)* oneMinusP0Hat(x1, c1, c2));
            }  else {
                logP -= Math.log(oneMinusP0Hat(x0, c1, c2));
            }
        }

        int internalNodeCount = tree.getLeafNodeCount() - tree.getDirectAncestorNodeCount() - 1;

        logP += internalNodeCount*Math.log(2);
        
        for (int i = 0; i < nodeCount; i++) {
            if (tree.getNode(i).isLeaf()) {
                if  (!tree.getNode(i).isDirectAncestor())  {
                    if (combinedTree.getHeightOfNode(i) > 0.000000000005 || rho == 0.) {
                        logP += Math.log(psi) + Math.log(q(combinedTree.getHeightOfNode(i), c1, c2)) + Math.log(p0s(combinedTree.getHeightOfNode(i), c1, c2));
                    } else {
                        logP += Math.log(4*rho);
                    }
                }
            } else {
                if (tree.getNode(i).isFake()) {
                    if (r == 1) {
                        System.out.println("r = 1 but there are sampled ancestors in the tree");
                        System.exit(0);
                    }
                    logP += Math.log(psi) + Math.log(1 - r);
                } else {
                    logP += Math.log(lambda) - Math.log(q(combinedTree.getHeightOfNode(i), c1, c2));
                }
            }
        }

        return logP;
    }

    @Override
    protected boolean requiresRecalculation() {
        return true;
    }

}

