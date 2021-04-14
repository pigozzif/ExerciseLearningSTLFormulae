/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.script;

import eggloop.flow.simhya.simhya.simengine.hybrid.GlobalSwitchType;
import eggloop.flow.simhya.simhya.simengine.*;
import eggloop.flow.simhya.simhya.GlobalOptions;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.model.flat.FlatModel;
import eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;
import eggloop.flow.simhya.simhya.simengine.paramexplore.*;
//import eggloop.flow.simhya.simhya.simengine.hybrid.GlobalSwitchType;


/**
 *
 * @author luca
 */
public class ExplorationManager implements LinearNoiseQueryManager {
    String collectorVar;
    String modelVar;
    CommandManager cmd;
    public SimType type = SimType.SSA;
    public double tf = -1;
    public double t0 = 0.0;
    public int runs = 100;
    public double burnout = -1;
    public int verbose = 0;
    public boolean useCache = false;
    public int points = GlobalOptions.samplePoints;
    public boolean finalStateOnly = true;
    public ArrayList<String> varsToSave = null;
    public double atol = -1;
    public double rtol = -1;
    public double maxStep = -1;
    public double minStep = -1;
    public double maxEventStep = -1;
    public int maxEventIter = -1;
    public double maxEventError = -1;
    public IntegratorType odetype = null;
    public String filename = null;
    public String fileTemplate = null;
    public boolean storeTrajectories = false;
    public boolean storeDistributions = true;
    public ArrayList<String> paramRangeNames;
    public ArrayList<Double> paramRangeFirst;
    public ArrayList<Double> paramRangeLast;
    public ArrayList<Integer> paramRangePoints;
    public ArrayList<Boolean> paramRangeLog;
    public ArrayList<String> paramValuesNames;
    public ArrayList<ArrayList<Double>> paramValuesData;
    public ArrayList<LinearNoiseQueryElement> queryList;
    public GlobalSwitchType switchType = GlobalSwitchType.RULES;
    public double discrete2continuousThreshold = -1;
    public double continuous2discreteThreshold = -1;
    public String ruleFile = null;
    FlatModel model;


    public ExplorationManager(String modelVar, String collectorVar, CommandManager cmd) {
        this.modelVar = modelVar;
        model = cmd.getModel(modelVar);
        this.collectorVar = collectorVar;
        this.cmd = cmd;
        paramRangeNames = new ArrayList<String>();
        paramRangeFirst = new ArrayList<Double>();
        paramRangeLast = new ArrayList<Double>();
        paramRangePoints = new ArrayList<Integer>();
        paramRangeLog = new ArrayList<Boolean>();
        paramValuesNames = new ArrayList<String>();
        paramValuesData = new ArrayList<ArrayList<Double>>();
        queryList = new ArrayList<LinearNoiseQueryElement>();
    }

    public void addParamRange(String name, double first, double last, int steps, boolean log) throws ScriptException {
        if (this.paramRangeNames.contains(name) || this.paramValuesNames.contains(name))
            throw new ScriptException("Already added range for " + name);
        this.paramRangeNames.add(name);
        this.paramRangeFirst.add(first);
        this.paramRangeLast.add(last);
        this.paramRangePoints.add(steps);
        this.paramRangeLog.add(log);
    }

    public void addParamRange(String name, ArrayList<Double> values) throws ScriptException {
        if (this.paramRangeNames.contains(name) || this.paramValuesNames.contains(name))
            throw new ScriptException("Already added range for " + name);
        this.paramValuesNames.add(name);
        this.paramValuesData.add(values);
    }

    public void simulate() throws ScriptException {
        if (tf <= t0)
            throw new ScriptException("Invalid or missing final simulation time.\n "
                    + "It must be specified by \"tf = floatnumber\" or \"time = floatnumber\",\n"
                    + "and it must be greater than the initial time (0.0 by default).");
        StochasticParamExplorator stocExp;
        DeterministicParamExplorator odeExp;
        HybridParamExplorator hybridExp;
        LinearNoiseParamExplorator lnExp;
        switch(type) {
            case SSA:
            case GB:
                stocExp = cmd.newStochasticExplorator(modelVar, type);
                this.initSimulation(stocExp);
                this.initParamRangeData(stocExp);
                cmd.runStochasticExploration(stocExp, collectorVar);
                break;
            case ODE:
                odeExp = cmd.newDeterministicExplorator(modelVar);
                this.initSimulation(odeExp);
                this.initParamRangeData(odeExp);
                cmd.runDeterministicExploration(odeExp, collectorVar);
                break;
            case LN:
                lnExp = cmd.newLinearNoiseExplorator(modelVar);
                this.initSimulation(lnExp);
                this.initParamRangeData(lnExp);
                cmd.runLinearNoiseExploration(lnExp, collectorVar);
                break;
            case HYBRID:
                hybridExp = cmd.newHybridExplorator(modelVar);
                this.initSimulation(hybridExp);
                this.initParamRangeData(hybridExp);
                cmd.runHybridExploration(hybridExp, collectorVar);
                break;
        }
    }


    private void initParamRangeData(ParamExplorator exp) {
        for (int i=0;i<this.paramRangeNames.size();i++)
            exp.addParameterToExplore(this.paramRangeNames.get(i), this.paramRangeFirst.get(i),
                    this.paramRangeLast.get(i), this.paramRangePoints.get(i), this.paramRangeLog.get(i));
        for (int i=0;i<this.paramValuesNames.size();i++)
            exp.addParameterToExplore(this.paramValuesNames.get(i), this.paramValuesData.get(i));
    }

    private void initSimulation(StochasticParamExplorator exp) {
        if (this.finalStateOnly)
            exp.saveFinalStateOnly(tf, storeDistributions);
        else if (this.storeTrajectories && burnout > t0)
            exp.saveTrajectoryData(burnout, tf, points, storeDistributions);
        else
            exp.saveTrajectoryData(tf, points, storeDistributions);
        if (filename != null)
            exp.saveTrajectoriesToSameFile(cmd.getCorrectFilename(filename));
        else if (this.fileTemplate != null)
            exp.saveTrajectoriesToDifferentFiles(cmd.getCorrectFilename(fileTemplate));
        exp.setVerbosityLevel(this.verbose);
        exp.setUseCache(useCache);
        exp.setRunsPerConfiguration(runs);
        if (this.varsToSave != null)
            exp.setVariablesToSave(varsToSave);
    }

    private void initSimulation(DeterministicParamExplorator exp) {
        if (this.finalStateOnly)
            exp.saveFinalStateOnly(tf);
        else if (this.storeTrajectories && burnout > t0)
            exp.saveTrajectoryData(burnout, tf, points);
        else
            exp.saveTrajectoryData(tf, points);
        if (filename != null)
            exp.saveTrajectoriesToSameFile(cmd.getCorrectFilename(filename));
        else if (this.fileTemplate != null)
            exp.saveTrajectoriesToDifferentFiles(cmd.getCorrectFilename(this.fileTemplate));
        exp.setVerbosityLevel(this.verbose);
        exp.setUseCache(useCache);
        if (this.varsToSave != null)
            exp.setVariablesToSave(varsToSave);
        if (atol > 0)
            exp.setAbsoluteTolerance(atol);
        if (rtol > 0)
            exp.setRelativeTolerance(rtol);
        if (this.maxStep > 0)
            exp.setMaximumStepSize(maxStep);
        if (this.minStep > 0)
            exp.setMinimumStepSize(this.minStep);
        if (this.maxEventError > 0)
            exp.setMaxErrorForEvents(this.maxEventError);
        if (this.maxEventStep > 0)
            exp.setMaxStepIncrementForEvents(this.maxEventStep);
        if (this.maxEventIter > 0)
            exp.setMaxIterationforEvents(maxEventIter);
        if (this.odetype != null)
            exp.setIntegrator(odetype);
    }


    private void initSimulation(LinearNoiseParamExplorator exp) {
        if (this.finalStateOnly)
            exp.saveFinalStateOnly(tf);
        else if (this.storeTrajectories && burnout > t0)
            exp.saveTrajectoryData(burnout, tf, points);
        else
            exp.saveTrajectoryData(tf, points);
        if (filename != null)
            exp.saveTrajectoriesToSameFile(cmd.getCorrectFilename(filename));
        else if (this.fileTemplate != null)
            exp.saveTrajectoriesToDifferentFiles(cmd.getCorrectFilename(this.fileTemplate));
        exp.setVerbosityLevel(this.verbose);
        exp.setUseCache(useCache);
        if (this.varsToSave != null)
            exp.setVariablesToSave(varsToSave);
        if (atol > 0)
            exp.setAbsoluteTolerance(atol);
        if (rtol > 0)
            exp.setRelativeTolerance(rtol);
        if (this.maxStep > 0)
            exp.setMaximumStepSize(maxStep);
        if (this.minStep > 0)
            exp.setMinimumStepSize(this.minStep);
        if (this.maxEventError > 0)
            exp.setMaxErrorForEvents(this.maxEventError);
        if (this.maxEventStep > 0)
            exp.setMaxStepIncrementForEvents(this.maxEventStep);
        if (this.maxEventIter > 0)
            exp.setMaxIterationforEvents(maxEventIter);
        if (this.odetype != null)
            exp.setIntegrator(odetype);
        //add queries to explorator.
        for (LinearNoiseQueryElement q : this.queryList) {
            if (q.singleThreshold)
                exp.addQuery(q.average, q.stdev, q.size, q.threshold, q.greater, q.onPop);
            else
                exp.addQuery(q.average, q.stdev, q.size, q.lower, q.upper, q.onPop);
        }
    }


    private void initSimulation(HybridParamExplorator exp) {
        if (this.finalStateOnly)
            exp.saveFinalStateOnly(tf, storeDistributions);
        else if (this.storeTrajectories && burnout > t0)
            exp.saveTrajectoryData(burnout, tf, points, storeDistributions);
        else
            exp.saveTrajectoryData(tf, points, storeDistributions);
        if (filename != null)
            exp.saveTrajectoriesToSameFile(cmd.getCorrectFilename(filename));
        else if (this.fileTemplate != null)
            exp.saveTrajectoriesToDifferentFiles(cmd.getCorrectFilename(this.fileTemplate));
        exp.setVerbosityLevel(this.verbose);
        exp.setUseCache(useCache);
        exp.setRunsPerConfiguration(runs);
        if (this.varsToSave != null)
            exp.setVariablesToSave(varsToSave);
        if (atol > 0)
            exp.setAbsoluteTolerance(atol);
        if (rtol > 0)
            exp.setRelativeTolerance(rtol);
        if (this.maxStep > 0)
            exp.setMaximumStepSize(maxStep);
        if (this.minStep > 0)
            exp.setMinimumStepSize(this.minStep);
        if (this.maxEventError > 0)
            exp.setMaxErrorForEvents(this.maxEventError);
        if (this.maxEventStep > 0)
            exp.setMaxStepIncrementForEvents(this.maxEventStep);
        if (this.maxEventIter > 0)
            exp.setMaxIterationforEvents(maxEventIter);
        if (this.odetype != null)
            exp.setIntegrator(odetype);
        if (this.continuous2discreteThreshold >= 0)
            exp.setContinuous2discreteThreshold(continuous2discreteThreshold);
        if (this.discrete2continuousThreshold >= 0)
            exp.setDiscrete2continuousThreshold(discrete2continuousThreshold);
        if (this.ruleFile != null)
            exp.setRuleFile(cmd.getCorrectFilename(ruleFile));
        exp.setSwitchType(switchType);
    }

    public void addQuery(String average, String stdev, double size,
            double threshold, boolean greater, boolean onPop) {
        this.queryList.add(new LinearNoiseQueryElement(average,stdev,size,threshold,greater,onPop));
    }



    public void addQuery(String average, String stdev, double size,
            double lower, double upper, boolean onPop) {
        this.queryList.add(new LinearNoiseQueryElement(average,stdev,size,lower,upper,onPop));
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
        int id = model.getStore().getParametersReference().getSymbolId(x);
        return model.getStore().getParametersReference().getValue(id);
    }

    public boolean isAllowed(String x) {
        return this.isParameter(x)||this.isVariable(x)||this.isNumber(x);
    }

    public boolean isParameter(String x) {
        return model.getStore().getParametersReference().isDefined(x);
    }

    public boolean isVariable(String x) {
        return model.getStore().getVariablesReference().isDefined(x);
    }

    public boolean isNumber(String x) {
        try {
            Double.parseDouble(x);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private class LinearNoiseQueryElement {
        boolean singleThreshold;
        String average;
        String stdev;
        double size;
        double threshold;
        boolean greater;
        boolean onPop;
        double lower;
        double upper;

        public LinearNoiseQueryElement(String average, String stdev, double size,
                double threshold, boolean greater, boolean onPop) {
            this.singleThreshold = true;
            this.average = average;
            this.stdev = stdev;
            this.size = size;
            this.threshold = threshold;
            this.greater = greater;
            this.onPop = onPop;
        }

        public LinearNoiseQueryElement(String average, String stdev, double size,
                double lower, double upper, boolean onPop) {
            this.singleThreshold = false;
            this.average = average;
            this.stdev = stdev;
            this.size = size;
            this.onPop = onPop;
            this.lower = lower;
            this.upper = upper;
        }
    }




}
