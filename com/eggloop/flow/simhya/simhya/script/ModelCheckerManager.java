/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.script;

import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.modelchecking.ConfidenceBoundsMethod;
import com.eggloop.flow.simhya.simhya.modelchecking.EstimationMethod;
import com.eggloop.flow.simhya.simhya.modelchecking.SMCenvironment;
import com.eggloop.flow.simhya.simhya.modelchecking.SMCoutput;
import com.eggloop.flow.simhya.simhya.modelchecking.SMCtype;
import com.eggloop.flow.simhya.simhya.modelchecking.TestMethod;
import com.eggloop.flow.simhya.simhya.simengine.SimType;
import com.eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;

/**
 *
 * @author luca
 */
public class ModelCheckerManager {
    CommandManager cmd;
    String outputVar;
    String checkingVar;
    
    SMCenvironment checker;
    
    public ArrayList<String> formulae = null;
    public SMCtype type = SMCtype.SIGNAL;
    public boolean adaptive = true;
    public boolean bayesian = false;
    public boolean chernoff = false;
    public ConfidenceBoundsMethod confidenceType = ConfidenceBoundsMethod.WILSON;
    public long samples = 10000;
    public double confidence = 0.95;
    public double error = 0.01;
    public int adaptiveStep = 10;
    public boolean regularise = false;
    public double priorGood = 1;
    public double priorBad = 1;
    public long maxRuns = 100000;
    
    public double significance = 0.01;
    public double power = 0.95;
    public double epsilon = 0.01;
    
    public double finalTime = 0.0;
    public SimType simType = SimType.SSA; 
    public IntegratorType integrator = IntegratorType.DP85;
    public boolean constantStepSize = false;
    public double stepSize = 0.1;
    
    public String parameter = null;
    public int points = 100;
    public double first = 0;
    public double last = 0;


    
    public ModelCheckerManager(String checkvar, String outvar, CommandManager cmd)  throws ScriptException {
        this.cmd = cmd;
        this.checkingVar = checkvar;
        this.outputVar = outvar;
        if (!cmd.containsVariable(checkingVar))
            throw new ScriptException("Variable $" + checkingVar + " does not exist.");
        if (cmd.isModelCheckingVariable(checkvar)) {
            this.checker = cmd.getSMCenvinronment(checkingVar);
        } else
            throw new ScriptException("Variable $" + checkingVar + " is not a model checking variable.");      
    }
    
    
    public boolean isFormula(String f) {
        return this.checker.isFormula(f);
    }
    
    public void setAllFormulas() {
        this.formulae = this.checker.getFormulaNames();
    }
    
    public boolean checkFormulaConsistency() {
        if (this.formulae == null)
            return false;
        for (String f : this.formulae) {
            if (!this.isFormula(f))
                return false;
        }
        return true;
    }
    
    public String getInconsistentFormula() {
        if (this.formulae == null)
            return null;
        for (String f : this.formulae) {
            if (!this.isFormula(f))
                return f;
        }
        return null;
    }
    
    public void setChernoff() {
        this.chernoff = true;
        this.adaptive = false;
        this.bayesian = false;
    }
    
    public void setSamples(long samples) {
        this.chernoff = false;
        this.adaptive = false;
        this.samples = samples;
        
    }
    
    public void setBayesian() {
        this.chernoff = false;
        this.bayesian = true;
    }
    
    
    public void setExplore(String param, double first, double last)   throws ScriptException  {
         if (first >= last)
             throw new ScriptException("Wrong parameter range");
         this.parameter = param;
         this.first = first;
         this.last = last;
    }
    
    public void check() {
        if (formulae == null)
            formulae = checker.getFormulaNames();
        if (!bayesian && adaptive)
            checker.options.estimationType = EstimationMethod.FREQUENTIST_ADAPTIVE;
        else if (!bayesian && chernoff)
            checker.options.estimationType = EstimationMethod.FREQUENTIST_CHERNOFF;
        else if (!bayesian && !chernoff &&!adaptive)
            checker.options.estimationType = EstimationMethod.FREQUENTIST_FIXED;
        else if (bayesian && adaptive)
            checker.options.estimationType = EstimationMethod.BAYESIAN_ADAPTIVE;
        else if (bayesian && !adaptive)
            checker.options.estimationType = EstimationMethod.BAYESIAN_FIXED;
        
        checker.options.regularise = this.regularise;
        checker.options.priorGood = this.priorGood;
        checker.options.priorBad = this.priorBad;
        checker.options.samples = this.samples;
        checker.options.adaptiveStep = this.adaptiveStep;
        checker.options.confidence = this.confidence;
        checker.options.error = this.error;
        checker.options.significance = this.significance;
        checker.options.power = this.power;
        checker.options.epsilon = this.epsilon;
        checker.options.maxRuns = this.maxRuns;
        checker.options.confidenceType = this.confidenceType;
        if (bayesian)
            checker.options.testType = TestMethod.BAYESIAN;
        else 
            checker.options.testType = TestMethod.WALD;
        checker.options.type = this.type;
        
        if (this.finalTime > 0.0)
            checker.simoptions.finalTime = this.finalTime;
        else
            checker.adaptFinalTime(formulae);
        
        checker.simoptions.integrator = this.integrator;
        checker.simoptions.simType = this.simType;
        checker.simoptions.stepSize = this.stepSize;
        
        if (parameter == null)
            cmd.runModelChecking(checker, formulae, outputVar);
        else {
           double[] vals = new double[points];
           double st = (last-first)/(points-1);
           for (int i=0;i<points;i++) 
               vals[i] = first + i * st;
           cmd.runModelChecking(checker, formulae,parameter,points,vals, outputVar);   
        }
    }
    
    
}
