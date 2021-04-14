/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.script;
import eggloop.flow.simhya.simhya.simengine.*;
import eggloop.flow.simhya.simhya.GlobalOptions;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;
import eggloop.flow.simhya.simhya.simengine.ode.LinearNoiseQuery;
import eggloop.flow.simhya.simhya.model.flat.*;
import eggloop.flow.simhya.simhya.dataprocessing.DataCollector;

/**
 *
 * @author Luca
 */
public class LinearNoiseManager implements LinearNoiseQueryManager {
    String collectorVar;
    String modelVar;
    CommandManager cmd;
    public double tf = -1;
    public double t0 = 0.0;
    public int verbose = 0;
    public boolean useCache = false;
    public int points = GlobalOptions.samplePoints;
    public boolean finalStateOnly = false;
    public double atol = -1;
    public double rtol = -1;
    public double maxStep = -1;
    public double minStep = -1;
    public double maxEventStep = -1;
    public int maxEventIter = -1;
    public double maxEventError = -1;
    public IntegratorType odetype = null;
    private ArrayList<LinearNoiseQuery> lnQuery;
    private FlatModel model;
    private LinearNoiseFlatModel lnModel;

    public LinearNoiseManager(String collectorVar, String modelVar, CommandManager cmd) {
        this.collectorVar = collectorVar;
        this.modelVar = modelVar;
        this.cmd = cmd;
        lnQuery = new ArrayList<LinearNoiseQuery>();
        model = cmd.getModel(modelVar);
        if (model.isLinearNoiseApproximation())
            lnModel = (LinearNoiseFlatModel)model;
        else
            lnModel = (LinearNoiseFlatModel)model.generateLinearNoiseApproximation();
    }

    
    public void addQuery(String average, String stdev, double size, 
            double threshold, boolean greater, boolean onPop) {
        this.lnQuery.add(new LinearNoiseQuery(this.lnModel.getStore(),average,stdev,size,
                threshold,greater,onPop,this.lnModel.getOriginalModelVariables()));        
    }
    
    
    
    public void addQuery(String average, String stdev, double size, 
            double lower, double upper, boolean onPop) {
        this.lnQuery.add(new LinearNoiseQuery(this.lnModel.getStore(),average,stdev,size,
                lower,upper,onPop,this.lnModel.getOriginalModelVariables()));

    }
        
        
    public void simulate() throws ScriptException {
        if (tf <= t0)
            throw new ScriptException("Invalid or missing final simulation time.\n "
                    + "It must be specified by \"tf = floatnumber\" or \"time = floatnumber\",\n"
                    + "and it must be greater than the initial time (0.0 by default).");
        OdeSimulationManager odeSim;
        odeSim = cmd.newLnOdeSimulation(modelVar, lnModel, tf);
        this.initSimulation(odeSim);
        cmd.runOdeSimulation(odeSim, collectorVar);
        //do ln analysis
        DataCollector data = cmd.getCollector(collectorVar);
        int id = 1;
        for (LinearNoiseQuery q : this.lnQuery) {
            String v;
            if (this.finalStateOnly)
                v = q.computeFinalStateOnly(id++, data);
            else v = q.compute(id++,  data);
            cmd.printMessage("Query " + q.toString() + " computed and stored in model variable " + v);
        }
    }

    
    private void initSimulation(OdeSimulationManager sim) {
        if (this.finalStateOnly)
            sim.saveOnlyFinalState();
        else
            sim.setSamplingConditionByTime(points);
        sim.setVerbosityLevel(this.verbose);
        sim.useChache(useCache);
        sim.setInitialTime(t0);
        sim.saveAllVariables();
        if (atol > 0)
            sim.setAbsoluteTolerance(atol);
        if (rtol > 0)
            sim.setRelativeTolerance(rtol);
        if (this.maxStep > 0)
            sim.setMaximumStepSize(maxStep);
        if (this.minStep > 0)
            sim.setMinimumStepSize(this.minStep);
        if (this.maxEventError > 0)
            sim.setMaxErrorForEvents(this.maxEventError);
        if (this.maxEventStep > 0)
            sim.setMaxStepIncrementForEvents(this.maxEventStep);
        if (this.maxEventIter > 0)
            sim.setMaxIterationforEvents(maxEventIter);
        if (this.odetype != null)
            sim.setIntegrator(odetype);
    }
    
    
    public String convertForStdev(String x) {
        if (this.isVariable(x))
            return x;
        if (this.isNumber(x)) {
            try {
                double z = Double.parseDouble(x);
                if (z < 0) z = -z;
                return Double.toString(z);
            } catch (Exception e) {
                
            }
        }
        if (this.isParameter(x)) {
            double z = this.getParameterValue(x);
            if (z < 0) z = -z;
            return Double.toString(z);
        }
        throw new RuntimeException(x + " is not a number, nor a variable, nor a parameter!");
    }
    
    public double getParameterValue(String x) {
        int id = lnModel.getStore().getParametersReference().getSymbolId(x);
        return lnModel.getStore().getParametersReference().getValue(id);
    }
    
    public boolean isAllowed(String x) {
        return this.isParameter(x)||this.isVariable(x)||this.isNumber(x);
    }
    
    public boolean isParameter(String x) {
        return lnModel.getStore().getParametersReference().isDefined(x);
    }
    
    public boolean isVariable(String x) {
        return lnModel.getStore().getVariablesReference().isDefined(x);
    }
    
    public boolean isNumber(String x) {
        try {
            Double.parseDouble(x);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
}
