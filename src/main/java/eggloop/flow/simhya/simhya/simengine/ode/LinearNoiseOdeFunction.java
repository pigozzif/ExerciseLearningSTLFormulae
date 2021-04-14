/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.simengine.ode;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import eggloop.flow.simhya.simhya.model.flat.FlatModel;
import eggloop.flow.simhya.simhya.model.flat.LinearNoiseFlatModel;
import eggloop.flow.simhya.simhya.model.transition.AtomicReset;
import eggloop.flow.simhya.simhya.model.store.Function;
import eggloop.flow.simhya.simhya.model.store.Predicate;
import eggloop.flow.simhya.simhya.simengine.GuardChecker;
import org.apache.commons.math.ode.DerivativeException;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.simengine.SimulationException;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;



import eggloop.flow.simhya.simhya.simengine.Stoichiometry;

/**
 *
 * @author Luca
 */
public class LinearNoiseOdeFunction extends OdeFunction implements FirstOrderDifferentialEquations, GuardChecker  {
    LinearNoiseFlatModel model;

    Function [] rate;
    Stoichiometry[][] stoich;
    Stoichiometry[][] jacobianStoich;
    Function [][] jacobianRate;
    boolean [] hasNonTrivialFlow;
    int[] transitions;
    int dimension;
    int totalDimension;
    int transitionNumber;
    DenseDoubleMatrix2D cov;
    DenseDoubleMatrix2D jacobian;
    //double[][] diffusion;
    double[][][] diffusionCoeff;
    DenseDoubleMatrix2D covPrime,covPrimeSupp;
    double[] rateVal;
    
       
    SymbolArray vars; 

    public LinearNoiseOdeFunction(LinearNoiseFlatModel model)  {
        super();
        this.model = model;
        this.totalDimension = model.getStore().getVariablesReference().getNumberOfSymbols();
        this.dimension = model.getOriginalModelVariables().size();
        this.transitionNumber = model.getNumberOfContinuousTransitions(true);
        if (transitionNumber != model.getNumberOfTransitions() ) {
            throw new SimulationException("Linear Noise approximation does not support events!");
        }
        if (model.containsContinuousGuardedTransitions(true)) {
            throw new SimulationException("Transitions in Linear Noise approximation cannot be guarded!");
        }
        
        rate = model.getTransitionRates();
        stoich = new Stoichiometry[transitionNumber][];
        transitions = model.getListOfContinuousTransitionID(true);
        hasNonTrivialFlow = new boolean[totalDimension];
        java.util.Arrays.fill(hasNonTrivialFlow, false);
        AtomicReset[][] resets = model.getTransitionResets();
        for (Integer j : transitions) {
            stoich[j] = new Stoichiometry[resets[j].length];
            for (int i=0;i<resets[j].length;i++) {
                int k = resets[j][i].getUpdatedVariables().get(0);
                stoich[j][i] = new Stoichiometry(k,
                        resets[j][i].getConstantIncrement());
                hasNonTrivialFlow[k] = true;
            }
        }
        for (int i=dimension;i<totalDimension;i++)
            hasNonTrivialFlow[i] = true;
        this.model.getStore().getVariablesReference().setContinuousEvolutionReference(hasNonTrivialFlow);
        
        vars = SymbolArray.getNewFastEvalSymbolArray(this.totalDimension);
        
        this.cov = new DenseDoubleMatrix2D(dimension,dimension);
        this.covPrime  = new DenseDoubleMatrix2D(dimension,dimension);
        this.covPrimeSupp  = new DenseDoubleMatrix2D(dimension,dimension);
        this.jacobian = new DenseDoubleMatrix2D(dimension,dimension);
        //this.diffusion = new double[dimension][dimension];
        this.diffusionCoeff = new double[dimension][dimension][transitionNumber];
        this.jacobianRate = model.getJacobianFunctions();
        this.jacobianStoich = model.getJacobianStoichiometry();
        this.rateVal = new double[this.transitionNumber];
        
        for (int k=0;k<transitionNumber;k++)
            for (int i=0;i<dimension;i++)
                for (int j=0;j<dimension;j++)
                    this.diffusionCoeff[i][j][k] = 0;
        
        for (int k=0;k<transitionNumber;k++) 
            for (int k1 = 0;k1<jacobianStoich[k].length;k1++)
                for (int k2 = 0;k2<jacobianStoich[k].length;k2++) {
                    int i = jacobianStoich[k][k1].variable;
                    double c = jacobianStoich[k][k1].coefficient;
                    int j = jacobianStoich[k][k2].variable;
                    c *= jacobianStoich[k][k2].coefficient;
                    this.diffusionCoeff[i][j][k] = c;
                }
        //debug
        //System.out.println("Jacobian");
        //System.out.println(model.printJacobian());
        //System.out.println("Diffusion");
        //System.out.println(printDiffusion());
        
    }
    
    
    private String printDiffusion() {
        String s = "";
        for (int i=0;i<dimension;i++) {
            for (int j=0;j<dimension;j++) {
                String w =  "";
                for (int k=0;k<transitionNumber;k++) {
                    if (diffusionCoeff[i][j][k] != 0) {
                        w += (w.equals("") ? "" : " + " ) + "(" + 
                                    diffusionCoeff[i][j][k] + ")*( " +
                                    rate[k].toModelLanguage() + ")";
                    }
                }
                if (w.equals("")) {
                    w = "0";
                }
                s += String.format("%50s", w);
            }
            s += "\n";
        }
        
        
        
        return s;
    }
    

    @Override
      public int getDimension() {
        return this.totalDimension;
    }
    
    
    private int  code(int n, int i, int j) {
        return n + n*i - i*(i-1)/2 + j - i;
    }
    
    public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
        //resets derivatives to zero
        java.util.Arrays.fill(yDot, 0.0);
        
        //assign correct value to variables
        vars.setValuesReference(y);
        for (int i=0;i<this.transitionNumber;i++) {
            int j = this.transitions[i];
            rateVal[i] = useCache ? rate[j].computeCache(vars) :
                                  rate[j].compute(vars);
            for (Stoichiometry s : stoich[j]) {
                yDot[s.variable] += s.coefficient * rateVal[i];
            }
        }
        for (int i=0;i<dimension;i++) {
            for (int j=i;j<dimension;j++) {
                cov.setQuick(i, j, y[code(dimension,i,j)]);
                if (i<j) cov.setQuick(j, i, y[code(dimension,i,j)]);
            }
        }
        //compute the jacobian!
        jacobian.assign(0);
        for (int i=0;i<this.transitionNumber;i++) {
            for (int j=0;j<this.dimension;j++) {
                if (!jacobianRate[i][j].isConstantZero()) {
                    double x = useCache ? this.jacobianRate[i][j].computeCache(vars) : 
                        this.jacobianRate[i][j].compute(vars);
                    for (int k=0;k<this.jacobianStoich[i].length;k++) {
                        int h = this.jacobianStoich[i][k].variable;
                        jacobian.setQuick(h, j, 
                                jacobian.getQuick(h, j) + x*this.jacobianStoich[i][k].coefficient);
                    }
                }
            }
        }
        jacobian.zMult(cov, covPrime, 1, 0, false, false);
        cov.zMult(jacobian, covPrimeSupp, 1, 0, false, true);
        for (int i=0;i<dimension;i++)
            for (int j=i;j<dimension;j++) {
                double x = covPrime.get(i, j);
                x += covPrimeSupp.getQuick(i, j);
                for (int k=0;k<this.transitionNumber;k++)
                    if (this.diffusionCoeff[i][j][k] != 0)
                        x += this.diffusionCoeff[i][j][k]*this.rateVal[k];
                yDot[code(dimension,i,j)] = x;
                //covPrime.setQuick(i,j,x);
            }
    }

    public void updateGuardStatus(int firedTransition, double t, double[] y) {
        
    }



}


