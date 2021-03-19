/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.script;
import com.eggloop.flow.simhya.simhya.simengine.*;
import com.eggloop.flow.simhya.simhya.GlobalOptions;
import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;
import com.eggloop.flow.simhya.simhya.simengine.hybrid.GlobalSwitchType;


/**
 *
 * @author luca
 */
public class SimulationManager {
    String collectorVar;
    String modelVar;
    CommandManager cmd;
    public SimType type = SimType.SSA;
    public double tf = -1;
    public double t0 = 0.0;
    public int runs = 1;
    public double burnout = -1;
    public int verbose = 0;
    public boolean useCache = false;
    public double timestep = -1;
    public int points = GlobalOptions.samplePoints;
    public boolean finalStateOnly = false;
    public ArrayList<String> varsToSave = null;
    public double atol = -1;
    public double rtol = -1;
    public double maxStep = -1;
    public double minStep = -1;
    public double maxEventStep = -1;
    public int maxEventIter = -1;
    public double maxEventError = -1;
    public IntegratorType odetype = null;
    public GlobalSwitchType switchType = GlobalSwitchType.RULES;
    public double discrete2continuousThreshold = -1; 
    public double continuous2discreteThreshold = -1;
    public String ruleFile = null;
    
    public final int STORE_AUTO = 3;
    public final int STORE_ALL = 2;
    public final int STORE_FINAL = 1;
    public final int STORE_STAT = 0;
    public int storeLevel = STORE_AUTO;
    

    public SimulationManager(String modelVar, String collectorVar, CommandManager cmd) {
        this.modelVar = modelVar;
        this.collectorVar = collectorVar;
        this.cmd = cmd;
    }

    public void simulate() throws ScriptException {
        if (tf <= t0)
            throw new ScriptException("Invalid or missinf final simulation time.\n "
                    + "It must be specified by \"tf = floatnumber\" or \"time = floatnumber\",\n"
                    + "and it must be greater than the initial time (0.0 by default).");
        StochasticSimulationManager stocSim;
        OdeSimulationManager odeSim;
        HybridSimulationManager hybridSim;
        switch(type) {
            case SSA:
            case GB:
                stocSim = cmd.newStochasticSimulation(modelVar, type, tf, runs);
                this.initSimulation(stocSim);
                cmd.runStochasticSimulation(stocSim, collectorVar);
                break;
            case ODE:
                odeSim = cmd.newOdeSimulation(modelVar, tf);
                this.initSimulation(odeSim);
                cmd.runOdeSimulation(odeSim, collectorVar);
                break;
            case HYBRID:
                hybridSim = cmd.newHybridSimulation(modelVar, tf, runs);
                this.initSimulation(hybridSim);
                cmd.runHybridSimulation(hybridSim, collectorVar);
                break;
        }
    }

    
    private void initSimulation(StochasticSimulationManager sim) {
        if (this.finalStateOnly)
            sim.saveOnlyFinalState();
        else if (burnout > 0 && timestep > 0)
            sim.setSamplingConditionByTime(burnout, timestep);
        else if (burnout > 0)
            sim.setSamplingConditionByTime(burnout, points);
        else if (timestep > 0)
            sim.setSamplingConditionByTime(t0, timestep);
        else
            sim.setSamplingConditionByTime(points);
        sim.setVerbosityLevel(this.verbose);
        sim.useChache(useCache);
        sim.setInitialTime(t0);
        if (this.varsToSave != null)
            sim.setVarsToBeSaved(varsToSave);
        else
            sim.saveAllVariables(); 
        switch(storeLevel) {
            case STORE_AUTO:
                sim.automaticStoreStrategy(runs);
                break;
            case STORE_ALL:
                sim.storeWholeTrajectoryData(runs);
                break;
            case STORE_FINAL:
                sim.storeFinalStateDataOnly(runs);
                break;
            case STORE_STAT:
                sim.storeStatisticsOnly(runs);
                break;
            default:
                sim.automaticStoreStrategy(runs);
                break;
        }
    }

    private void initSimulation(OdeSimulationManager sim) {
        if (this.finalStateOnly)
            sim.saveOnlyFinalState();
        else if (burnout > 0 && timestep > 0)
            sim.setSamplingConditionByTime(burnout, timestep);
        else if (burnout > 0)
            sim.setSamplingConditionByTime(burnout, points);
        else if (timestep > 0)
            sim.setSamplingConditionByTime(t0, timestep);
        else
            sim.setSamplingConditionByTime(points);
        sim.setVerbosityLevel(this.verbose);
        sim.useChache(useCache);
        sim.setInitialTime(t0);
        if (this.varsToSave != null)
            sim.setVarsToBeSaved(varsToSave);
        else
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

    private void initSimulation(HybridSimulationManager sim) throws ScriptException {
        switch (switchType) {
            case NO_SWITCH:
                sim.newSimulatorWithNoSwitching();
                break;
            case RULES:
                break;
            case POPULATION:
                if (this.continuous2discreteThreshold < 0) 
                    throw new ScriptException("Illegal switching threshold " + continuous2discreteThreshold);
                if (this.discrete2continuousThreshold < 0)
                    throw new ScriptException("Illegal switching threshold " + discrete2continuousThreshold);
                sim.newSimulatorWithPopulationSwitching(discrete2continuousThreshold, continuous2discreteThreshold);
                break;
            case RATE:
                throw new ScriptException(switchType.toString() + " not implemented yet");
            case POPULATION_AND_RATE:
                throw new ScriptException(switchType.toString() + " not implemented yet");
            default:
                throw new ScriptException("Unknown global swithing type");        
        }
        if (switchType != GlobalSwitchType.NO_SWITCH && this.ruleFile != null)
            sim.addGlobalSwitchingRuleFromFile(cmd.getCorrectFilename(ruleFile));
        if (this.finalStateOnly)
            sim.saveOnlyFinalState();
        else if (burnout > 0 && timestep > 0)
            sim.setSamplingConditionByTime(burnout, timestep);
        else if (burnout > 0)
            sim.setSamplingConditionByTime(burnout, points);
        else if (timestep > 0)
            sim.setSamplingConditionByTime(t0, timestep);
        else
            sim.setSamplingConditionByTime(points);
        sim.setVerbosityLevel(this.verbose);
        sim.useChache(useCache);
        sim.setInitialTime(t0);
        switch(storeLevel) {
            case STORE_AUTO:
                sim.automaticStoreStrategy(runs);
                break;
            case STORE_ALL:
                sim.storeWholeTrajectoryData(runs);
                break;
            case STORE_FINAL:
                sim.storeFinalStateDataOnly(runs);
                break;
            case STORE_STAT:
                sim.storeStatisticsOnly(runs);
                break;
            default:
                sim.automaticStoreStrategy(runs);
                break;
        }
        if (this.varsToSave != null)
            sim.setVarsToBeSaved(varsToSave);
        else
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



}
