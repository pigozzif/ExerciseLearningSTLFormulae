/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matlab;
import cern.colt.Arrays;
import eggloop.flow.simhya.simhya.model.store.faststore.FastStore;
import eggloop.flow.simhya.simhya.model.store.faststore.FastStore;
import eggloop.flow.simhya.simhya.modelchecking.SMCestimator;
import eggloop.flow.simhya.simhya.modelchecking.SMCenvironment;
import eggloop.flow.simhya.simhya.modelchecking.SMCfrequentistEstimator;
import eggloop.flow.simhya.simhya.modelchecking.mtl.MTLformula;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;

/**
 *
 * @author luca
 */
public class HmmMatlab {
    
    HmmHeartSimulator simulator = null;
    String[] symbols;
    FastStore store;
    SMCenvironment smc;
    ArrayList<String> parameterNames;
    double[] parameterValues;
    boolean regularise = true;

    
    public HmmMatlab(String[] symbols) {
        this.symbols = symbols;
        store = new FastStore();
        for (String s : symbols)
            store.addVariable(s, 0);
        store.finalizeVariableInitialization();
        store.finalizeInitialization();
        smc = new SMCenvironment(store);
    }
    
    
    public void loadFormulae(String file) {
        try { 
           smc.loadEnvironment(file);
           this.parameterNames = smc.getFormulaeParameterNames();
           this.parameterValues = this.getFormulaParameterValues();
        } catch (Exception e) {
           System.err.println(e);
        }
    }
    
      
    public void initialiseSimulator(double[] initial_distribution, double[][] transition_matrix, 
            double[][] observation_emission_matrix,
            double[] gaussian_emission_mean, double[] gaussian_emission_std) {
        simulator = new HmmHeartSimulator(initial_distribution,transition_matrix,
                observation_emission_matrix,gaussian_emission_mean,gaussian_emission_std);
        
    }
    
    public void setRegularise(boolean reg) {
        this.regularise = reg;
    }
    
    public double[] getFormulaParameterValues() {
        return smc.getFormulaeParameterValues();
    }
    
    public String[] getFormulaParameterNames() {
        
        return this.parameterNames.toArray(new String[0]);
    }
    
    public void setFormulaParameters(String[] names, double[] values) {
        if (smc!=null)
            this.smc.setFormulaeParameters(names, values);
    }
    
    public void resetFormulaParameters() {
        String[] names = this.parameterNames.toArray(new String[0]);
        this.setFormulaParameters(names, this.parameterValues);
    }
    
   
    public double smc(String formulaName, double finalTime, int runs) {
        if (!this.isSimulatorReady())
            throw new RuntimeException("HMM data not loaded");
        MTLformula formula = smc.getMTLformula(formulaName);
        SMCestimator estimator = new SMCfrequentistEstimator();
        estimator.setFixed(runs);
        if (this.regularise) {
           estimator.addGood(1);
           estimator.addBad(1);
        }
        formula.initializeForEstimate(estimator);
        boolean done = false;
        do {
            double[][] x = simulator.sample(finalTime);
            
//            System.out.println(Arrays.toString(x[0]));
//            System.out.println(Arrays.toString(x[1]));
//            System.out.println(Arrays.toString(x[2]));
            
            done = formula.modelCheckNextTrajectoryPointwiseSemantics(x);
        } while (!done);
        return estimator.getEstimateTrue();
    }
    
    
    public boolean check(String formulaName, double[][] trajectory) {
        MTLformula formula = smc.getMTLformula(formulaName);
        SMCestimator estimator = new SMCfrequentistEstimator();
        estimator.setFixed(1);
        formula.initializeForEstimate(estimator);
        formula.modelCheckNextTrajectoryPointwiseSemantics(trajectory);
        if (estimator.getLast() == Truth.TRUE)
            return true;
        else 
            return false;
    }
    
    
    public double[][] sample(double finalTime) {
        if (!this.isSimulatorReady())
            throw new RuntimeException("HMM data not loaded");
        return simulator.sample(finalTime);
    }
    
    
    boolean isSimulatorReady() {
        return simulator != null;
    }
}
