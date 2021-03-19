/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.matlab;

import cern.colt.Arrays;
import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
import com.eggloop.flow.simhya.simhya.dataprocessing.*;
import com.eggloop.flow.simhya.simhya.modelchecking.*;
import com.eggloop.flow.simhya.simhya.modelchecking.mtl.MTLformula;
import com.eggloop.flow.simhya.simhya.simengine.*;
import com.eggloop.flow.simhya.simhya.model.flat.parser.FlatParser;
import com.eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;
import com.eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;
import com.eggloop.flow.simhya.simhya.simengine.ode.OdeSimulator;
//import com.eggloop.flow.simhya.simhya.simengine.ode.OdeSimulatorWithEvents;
import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;




/**
 *
 * @author luca
 */
public class SimHyAModel {
    FlatModel model;
    Simulator simulator;
    SMCenvironment checker;
    DataCollector collector;
    SMCestimator estimator;
    
    int points = 2000;
    final int MAX_RUNS = 100000;
    SimType simType = SimType.SSA; 
    boolean regularise = true;
    double lastFinalTime = 0.0;
    int lastRuns = 0;
    boolean useEuler = false;
    double stepSize = 0.01;
    boolean pureStochasticSimulator;
    boolean finalTimeOnly = false;
    
    
    public SimHyAModel() {
        model = null;
        simulator = null;
        checker = null;
        estimator = null;
    }
    
    public void setRGSeed(int seed) {
        RandomGenerator.forceSeed(true);
        RandomGenerator.setSeed(seed);
    }
    
    public void randomRGSeed() {
        RandomGenerator.forceSeed(false);
        RandomGenerator.resetSeed();
    }
    
    public FlatModel getModel() {
        return this.model;
    }
    
    /**
     * Loads a simhya flat model
     * @param file the file containing the model specification
     * @return 
     */
    public boolean loadModel(String file) {
        FlatParser p = new FlatParser();
        try {
            model = p.parseFromFile(file);
            reset();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    
    private void reset() {
        if (model!= null) {
            model.resetToBasicInitialState();
        }
        simulator = null;
        checker = null;
        estimator = null;
    }    
    
    public boolean hasModel() {
        return model != null;
    }
    
    public boolean hasSimulator() {
        return simulator != null;
    }
    
    public boolean hasChecker() {
        return checker != null;
    }
    
    public void setSSA() {
        this.simType = SimType.SSA;
    }
    
    public void setGB() {
        this.simType = SimType.GB;
    }
    
    public void setODE() {
        this.simType = SimType.ODE;
        this.useEuler = false;
    }
    public void setODEeuler(double step) {
        this.simType = SimType.ODE;
        this.useEuler = true;
        this.stepSize = step;
    }
    
    public void setHybrid() {
        this.simType = SimType.HYBRID;
    }
    
    public void setLN() {
        this.simType = SimType.LN;
    }
    
    public void setTrajectoryPoints(int p) {
        this.points = p;
    }
    
    
    void initSSASimulator(double finalTime, boolean saveAll, int runs) {
        if (!this.hasModel())
            throw new RuntimeException("Load model first");
        
        collector = new StochasticDataCollector(model);
        collector.setPrintConditionByTime(points, finalTime);
        if (saveAll)
            collector.automaticStoreStrategy(runs);
        else {  
            collector.storeFinalStateDataOnly(MAX_RUNS);
            collector.saveLastFullTrajectory();
        }
        collector.clearAll();
        simulator = SimulatorFactory.newSSAsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
        this.finalTimeOnly = false;
        this.lastFinalTime = finalTime;
        this.lastRuns = runs;

        
        collector.clearAll();
    }
    
    void initGBSimulator(double finalTime, boolean saveAll, int runs) {
        if (!this.hasModel())
            throw new RuntimeException("Load model first");
        
        collector = new StochasticDataCollector(model);
        collector.setPrintConditionByTime(points, finalTime);
        if (saveAll)
            collector.automaticStoreStrategy(runs);
        else {  
            collector.storeFinalStateDataOnly(MAX_RUNS);
            collector.saveLastFullTrajectory();
        }
        collector.clearAll();
        simulator = SimulatorFactory.newGBsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
        this.finalTimeOnly = false;
        this.lastFinalTime = finalTime;
        this.lastRuns = runs;
    }
    
    
    
    void initHybridSimulator(double finalTime, boolean saveAll, int runs) {
        if (!this.hasModel())
            throw new RuntimeException("Load model first");
        
        collector = new HybridDataCollector(model);
        collector.setPrintConditionByTime(points, finalTime);
        if (saveAll)
            collector.automaticStoreStrategy(runs);
        else {  
            collector.storeFinalStateDataOnly(MAX_RUNS);
            collector.saveLastFullTrajectory();
        }
        collector.clearAll();
        simulator = SimulatorFactory.newHybridSimulator(model, collector, false);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
        this.finalTimeOnly = false;
        this.lastFinalTime = finalTime;
        this.lastRuns = runs;
        
    }
    
    
     void initSSASimulatorFTonly(double finalTime, int runs) {
        if (!this.hasModel())
            throw new RuntimeException("Load model first");
        
        collector = new StochasticDataCollector(model);
        collector.saveOnlyFinalState();
        collector.storeFinalStateDataOnly(runs);
        collector.clearAll();
        simulator = SimulatorFactory.newSSAsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
        this.finalTimeOnly = true;
        this.lastFinalTime = finalTime;
        this.lastRuns = runs;
    }
    
    void initGBSimulatorFTonly(double finalTime,  int runs) {
        if (!this.hasModel())
            throw new RuntimeException("Load model first");
        
        collector = new StochasticDataCollector(model);
        collector.saveOnlyFinalState();
        collector.storeFinalStateDataOnly(runs);
        collector.clearAll();
        simulator = SimulatorFactory.newGBsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
        this.finalTimeOnly = true;
        this.lastFinalTime = finalTime;
        this.lastRuns = runs;
    }
    
    
    
    void initHybridSimulatorFTonly(double finalTime, int runs) {
        if (!this.hasModel())
            throw new RuntimeException("Load model first");
        
        collector = new HybridDataCollector(model);
        collector.saveOnlyFinalState();
        collector.storeFinalStateDataOnly(runs);
        collector.clearAll();
        simulator = SimulatorFactory.newHybridSimulator(model, collector, false);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
        this.finalTimeOnly = true;
        this.lastFinalTime = finalTime;
        this.lastRuns = runs;
        
    }
    
    
    void initODESimulator(double finalTime) {
        if (!this.hasModel())
            throw new RuntimeException("Load model first");
        
        collector = new OdeDataCollector(model);
        collector.setPrintConditionByTime(points, finalTime);
        collector.automaticStoreStrategy(1);
        collector.saveLastFullTrajectory();
        collector.clearAll();
        simulator = SimulatorFactory.newODEsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        if (this.useEuler) {
            if (simulator instanceof OdeSimulator) {
                OdeSimulator sim = (OdeSimulator)simulator;
                sim.setMinimumStepSize(stepSize);
                sim.setIntegrator(IntegratorType.EULER);
            }
//            else if (simulator instanceof OdeSimulatorWithEvents) {
//                OdeSimulatorWithEvents sim = (OdeSimulatorWithEvents)simulator;
//                sim.setMinimumStepSize(stepSize);
//                sim.setIntegrator(IntegratorType.EULER);
//            }
        }
        simulator.initialize();
        this.finalTimeOnly = false;
        this.lastFinalTime = finalTime;
    }
    
    
    void initLNSimulator(double finalTime) {
        if (!this.hasModel())
            throw new RuntimeException("Load model first");
        FlatModel m =  model.generateLinearNoiseApproximation();
        
        collector = new OdeDataCollector(model);
        collector.setPrintConditionByTime(points, finalTime);
        collector.automaticStoreStrategy(1);
        collector.saveLastFullTrajectory();
        collector.clearAll();
        simulator = SimulatorFactory.newODEsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
        this.finalTimeOnly = false;
        this.lastFinalTime = finalTime;
    }
    
    
    public void resetSimulator() {
        collector = null;
        simulator = null;
    }
    
    
    public double[][] simulateAtT(double finalTime, int runs) {
        if (simulator == null || !this.finalTimeOnly ||
                this.lastFinalTime != finalTime || this.lastRuns != runs) {
            //initialise simulator
            switch (this.simType) {
                case SSA : 
                    this.initSSASimulatorFTonly(finalTime, runs);
                    this.pureStochasticSimulator = true;
                    break;
                case GB: 
                    this.initGBSimulatorFTonly(finalTime, runs);
                    this.pureStochasticSimulator = true;
                    break;
                case HYBRID: 
                    this.initHybridSimulator(finalTime, false, runs);
                    this.pureStochasticSimulator = false;
                    break;
                case ODE: case LN:
                    throw new RuntimeException("Command available only for stochastic models!!!");
                default :
                    throw new RuntimeException("Some incredible mess happened here!!!");
            }
        }
        else 
            collector.clearAll();
        for (int i=0;i<runs;i++) {
            if (this.pureStochasticSimulator)
                collector.newTrajectory();
            simulator.resetModel(true);
            simulator.reinitialize();
            simulator.run();
        }
        return collector.getTrajectoryStatistics().getFinalData();
    }
    
    public double[][] simulate(double finalTime) {
        if (simulator == null || this.lastFinalTime != finalTime) {
            //initialise simulator
            switch (this.simType) {
                case SSA : 
                    this.initSSASimulator(finalTime, false, 1);
                    this.pureStochasticSimulator = true;
                    break;
                case GB: 
                    this.initGBSimulator(finalTime, false, 1);
                    this.pureStochasticSimulator = true;
                    break;
                case ODE: 
                    this.initODESimulator(finalTime);
                    this.pureStochasticSimulator = false;
                    break;
                case HYBRID: 
                    this.initHybridSimulator(finalTime, false, 1);
                    this.pureStochasticSimulator = false;
                    break;
                case LN: 
                    this.initLNSimulator(finalTime);
                    this.pureStochasticSimulator = false;
                    break;
                default :
                    throw new RuntimeException("Some incredible mess happened here!!!");
            }
        }
        else if (simType.equals(SimType.ODE)) {
            this.initODESimulator(finalTime);
            this.pureStochasticSimulator = false;
        }
        else if (simType.equals(SimType.LN)) {
            this.initLNSimulator(finalTime);
            this.pureStochasticSimulator = false;
        }
        if (this.pureStochasticSimulator)
            collector.newTrajectory();
        simulator.resetModel(true);
        simulator.reinitialize();
        simulator.run();
        this.lastFinalTime = finalTime;
        return collector.getLastFullTrajectory().getAllData();
    }
    
    
    /**
     * returns the average if runs>1, or the trajectory with the chose number of points.
     * @param runs
     * @return 
     */
    public double[][] simulate(double finalTime, int runs) {
        switch (this.simType) {
            case SSA : 
                this.initSSASimulator(finalTime, true, runs);
                this.pureStochasticSimulator = true;
                break;
            case GB: 
                this.initGBSimulator(finalTime, true, runs);
                this.pureStochasticSimulator = true;
                break;
            case ODE: 
                return this.simulate(finalTime);
            case HYBRID: 
                this.pureStochasticSimulator = false;
                this.initHybridSimulator(finalTime, true, runs);
                break;
            case LN: 
                return this.simulate(finalTime);
            default :
                throw new RuntimeException("Some incredible mess happened here!!!");
        }
        //long t1 = 0, t2 = 0, t3 = 0, t4 = 0;
        //long time = System.nanoTime(); 
        for (int i=0;i<runs;i++) {
            //time = System.nanoTime();
            if (this.pureStochasticSimulator)
                collector.newTrajectory();
           // time = System.nanoTime() - time;
           // t1 += time;
           // time = System.nanoTime();
            simulator.resetModel(true);
           // time = System.nanoTime() - time;
           // t2 += time;
           // time = System.nanoTime();
            simulator.reinitialize();
           // time = System.nanoTime() - time;
           // t3 += time;
           // time = System.nanoTime();
            simulator.run();
           // time = System.nanoTime() - time;
           // t4 += time;
        }
      //  System.out.println("t1 - newT " + (double)t1/1000000000);
      //  System.out.println("t2 - res " + (double)t2/1000000000);
      //  System.out.println("t3 - reinit " + (double)t3/100000000);
      //  System.out.println("t4 - run " + (double)t4/100000000);
        
        simulator = null;
        return collector.getTrajectoryStatistics().getAverage();
    }
    
    
    
    public void loadSMCformulae(String file) {
        try {
           this.checker = new SMCenvironment(model);
           checker.loadEnvironment(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
            
    }
    
    
    public void setRegularisationSMC(boolean reg) {
        this.regularise = reg;
    }
    
    
    public double smc(String phi, double finalTime, int runs) {
        if (model == null) 
            throw new RuntimeException("Load model and formulae first");
        if (checker == null)
            throw new RuntimeException("Load MTL formulae first");
        if (simType.equals(SimType.ODE) || simType.equals(SimType.LN))
            runs=1;
        switch (this.simType) {
            case SSA : 
                this.initSSASimulator(finalTime, false, 1);
                this.pureStochasticSimulator = true;
                break;
            case GB: 
                this.initGBSimulator(finalTime, false, 1);
                this.pureStochasticSimulator = true;
                break;
            case ODE: 
                 this.initODESimulator(finalTime);
                 this.pureStochasticSimulator = false;
                break;
            case HYBRID: 
                this.initHybridSimulator(finalTime, false, 1);
                this.pureStochasticSimulator = false;
                break;
            case LN: 
                this.initLNSimulator(finalTime);
                this.pureStochasticSimulator = false;
                break;
            default :
                throw new RuntimeException("Some incredible mess happened here!!!");
        }
        
        this.estimator = new SMCfrequentistEstimator();
        estimator.setFixed(runs);
        if (runs>1 && this.regularise) {
            this.estimator.addGood(1);
            this.estimator.addBad(1);
        }
        
        //estimator = new SMCestimator(true,0.005,0.95);
        
        
        
        MTLformula formula = checker.getMTLformula(phi);
        formula.initializeForEstimate(estimator);
        boolean finished;
        do {
            if (this.pureStochasticSimulator)
                collector.newTrajectory();
            else if (simType.equals(SimType.ODE)) {
                this.initODESimulator(finalTime);
                this.pureStochasticSimulator = false;
            }
            else if (simType.equals(SimType.LN)) {
                this.initLNSimulator(finalTime);
                this.pureStochasticSimulator = false;
            }
             simulator.resetModel(true);
             simulator.reinitialize();
             simulator.run();
             double[][] traj = collector.getLastFullTrajectory().getAllData();
             //double[][] traj = this.simulate(finalTime);
             finished = formula.modelCheckNextTrajectoryPointwiseSemantics(traj);
             if (estimator.getLast().isUndefined())
                   throw new RuntimeException("Trajectory is not lasting enough and cannot be checked. Simulate longer");

           } while (!finished);
        
        //System.out.println(estimator.shortPrint());
        
        return estimator.getEstimateTrue();
    }
    
    
    
    
    
    public boolean[][] smc_explore(String phi, String[] names, double[][] values, double finalTime, int runs) {
        if (model == null) 
            throw new RuntimeException("Load model and formulae first");
        if (checker == null)
            throw new RuntimeException("Load MTL formulae first");
        if (simType.equals(SimType.ODE) || simType.equals(SimType.LN))
            runs=1;
        
        
        int count = 0;
        MTLformula formula = checker.getMTLformula(phi);
        int size = values.length;
        boolean[][] checked = new boolean[size][runs];
        for (int i=0;i<size;i++) {
            if (count == 0 || count + runs >= this.MAX_RUNS) {
                switch (this.simType) {
                    case SSA : 
                        this.initSSASimulator(finalTime, false, 1);
                        this.pureStochasticSimulator = true;
                        break;
                    case GB: 
                        this.initGBSimulator(finalTime, false, 1);
                        this.pureStochasticSimulator = true;
                        break;
                    case ODE: 
                         this.initODESimulator(finalTime);
                         this.pureStochasticSimulator = false;
                        break;
                    case HYBRID: 
                        this.initHybridSimulator(finalTime, false, 1);
                        this.pureStochasticSimulator = false;
                        break;
                    case LN: 
                        this.initLNSimulator(finalTime);
                        this.pureStochasticSimulator = false;
                        break;
                    default :
                        throw new RuntimeException("Some incredible mess happened here!!!");
                }
            }
            this.setParameters(names, values[i]);
            estimator = new SMCfrequentistEstimator();
            estimator.setFixed(runs);
            formula.initializeForEstimate(estimator);
            for (int j=0;j<runs;j++) {
                if (this.pureStochasticSimulator)
                    collector.newTrajectory();
               else if (simType.equals(SimType.ODE)) {
                    this.initODESimulator(finalTime);
                }
                else if (simType.equals(SimType.LN)) {
                    this.initLNSimulator(finalTime);
                }
               simulator.resetModel(true);
               simulator.reinitialize();
               simulator.run();
               count++;
               double[][] traj = collector.getLastFullTrajectory().getAllData();
               formula.modelCheckNextTrajectoryPointwiseSemantics(traj);
               if (estimator.getLast().isUndefined())
                   throw new RuntimeException("Trajectory is not lasting enough and cannot be checked. Simulate longer");
               checked[i][j] = estimator.getLast().isTrue();
            }
                
        }
        return checked;
    }
    
    public double[] smc_explore_deep(String phi, String[] names, double[][] values, double finalTime, int runs) {
        if (model == null) 
            throw new RuntimeException("Load model and formulae first");
        if (checker == null)
            throw new RuntimeException("Load MTL formulae first");
        if (simType.equals(SimType.ODE) || simType.equals(SimType.LN))
            runs=1;
        switch (this.simType) {
            case SSA : 
                this.initSSASimulator(finalTime, false, 1);
                this.pureStochasticSimulator = true;
                break;
            case GB: 
                this.initGBSimulator(finalTime, false, 1);
                this.pureStochasticSimulator = true;
                break;
            case ODE: 
                 this.initODESimulator(finalTime);
                 this.pureStochasticSimulator = false;
                break;
            case HYBRID: 
                this.initHybridSimulator(finalTime, false, 1);
                this.pureStochasticSimulator = false;
                break;
            case LN: 
                this.initLNSimulator(finalTime);
                this.pureStochasticSimulator = false;
                break;
            default :
                throw new RuntimeException("Some incredible mess happened here!!!");
        }
        
        int count = 0;
        MTLformula formula = checker.getMTLformula(phi);
        int size = values.length;
        double[] prob = new double[size];
        for (int i=0;i<size;i++) {
            if (count + runs >= this.MAX_RUNS) {
                switch (this.simType) {
                    case SSA : 
                        this.initSSASimulator(finalTime, false, 1);
                        this.pureStochasticSimulator = true;
                        break;
                    case GB: 
                        this.initGBSimulator(finalTime, false, 1);
                        this.pureStochasticSimulator = true;
                        break;
                    case ODE: 
                         this.initODESimulator(finalTime);
                         this.pureStochasticSimulator = false;
                        break;
                    case HYBRID: 
                        this.initHybridSimulator(finalTime, false, 1);
                        this.pureStochasticSimulator = false;
                        break;
                    case LN: 
                        this.initLNSimulator(finalTime);
                        this.pureStochasticSimulator = false;
                        break;
                    default :
                        throw new RuntimeException("Some incredible mess happened here!!!");
                }
            }
            this.setParameters(names, values[i]);
            prob[i] = 0;
            estimator = new SMCfrequentistEstimator();
            estimator.setFixed(runs);
            formula.initializeForEstimate(estimator);
            for (int j=0;j<runs;j++) {
                if (this.pureStochasticSimulator)
                    collector.newTrajectory();
               else if (simType.equals(SimType.ODE)) {
                    this.initODESimulator(finalTime);
                }
                else if (simType.equals(SimType.LN)) {
                    this.initLNSimulator(finalTime);
                }
               simulator.resetModel(true);
               simulator.reinitialize();
               simulator.run();
               count++;
               double[][] traj = collector.getLastFullTrajectory().getAllData();
               formula.modelCheckNextTrajectoryPointwiseSemantics(traj);
               if (estimator.getLast().isUndefined())
                   throw new RuntimeException("Trajectory is not lasting enough and cannot be checked. Simulate longer");
               if (estimator.getLast().isTrue())
                   prob[i]++;
            }
            prob[i] /= runs;       
        }
        return prob;
    }
    
    
    public boolean[][] smc_set(String[] phis, double finalTime, int runs) {
        if (model == null) 
            throw new RuntimeException("Load model and formulae first");
        if (checker == null)
            throw new RuntimeException("Load MTL formulae first");
//        if (simType.equals(SimType.ODE) || simType.equals(SimType.LN))
//            runs=1;
        switch (this.simType) {
            case SSA : 
                this.pureStochasticSimulator = true;
                this.initSSASimulator(finalTime, false, 1);
                break;
            case GB: 
                this.pureStochasticSimulator = true;
                this.initGBSimulator(finalTime, false, 1);
                break;
            case ODE: 
                this.pureStochasticSimulator = false;
                 this.initODESimulator(finalTime);
                break;
            case HYBRID: 
                this.initHybridSimulator(finalTime, false, 1);
                break;
            case LN:
                this.pureStochasticSimulator = false;
                this.initLNSimulator(finalTime);
                break;
            default :
                throw new RuntimeException("Some incredible mess happened here!!!");
        }
        
        int f = phis.length;
        boolean[][] checked = new boolean[runs][f];
        SMCestimator[] est = new SMCestimator[f];
        MTLformula[] formula = new MTLformula[f];
        for (int i=0;i<f;i++) {
            est[i] = new SMCfrequentistEstimator();
            estimator.setFixed(runs);
            formula[i] = checker.getMTLformula(phis[i]);
            formula[i].initializeForEstimate(est[i]);
        }
        for (int i=0;i<runs;i++) {
           if (this.pureStochasticSimulator)
                collector.newTrajectory();
           else if (simType.equals(SimType.ODE)) {
                this.initODESimulator(finalTime);
                this.pureStochasticSimulator = false;
            }
            else if (simType.equals(SimType.LN)) {
                this.initLNSimulator(finalTime);
                this.pureStochasticSimulator = false;
            }
           simulator.resetModel(true);
           simulator.reinitialize();
           simulator.run();
           double[][] traj = collector.getLastFullTrajectory().getAllData();
           for (int j=0;j<f;j++) {
               formula[j].modelCheckNextTrajectoryPointwiseSemantics(traj);
               if (est[j].getLast().isUndefined())
                   throw new RuntimeException("Trajectory is not lasting enough and cannot be checked. Simulate longer");
               checked[i][j] = est[j].getLast().isTrue();
           } 
        }
        return checked;
    }
    
    
    public boolean[] smc_set(String[] phis, double[][] traj) {
        if (model == null) 
            throw new RuntimeException("Load model and formulae first");
        if (checker == null)
            throw new RuntimeException("Load MTL formulae first");
        int f = phis.length;
        boolean[] checked = new boolean[f];
        SMCestimator[] est = new SMCestimator[f];
        MTLformula[] formula = new MTLformula[f];
        for (int i=0;i<f;i++) {
            est[i] = new SMCfrequentistEstimator();
            estimator.setFixed(1);
            formula[i] = checker.getMTLformula(phis[i]);
            formula[i].initializeForEstimate(est[i]);
        }
        for (int j=0;j<f;j++) {
             formula[j].modelCheckNextTrajectoryPointwiseSemantics(traj);
             if (est[j].getLast().isUndefined()) {
                  System.out.println("WARNING: Trajectory is not lasting enough and cannot be checked. Simulate longer"); 
                  
                 
                 
                 //throw new RuntimeException("Trajectory is not lasting enough and cannot be checked. Simulate longer");
             
             }
             checked[j] = est[j].getLast().isTrue();
        }
        return checked;
    }
    
    
    public boolean[] smc_sim(String phi, double tf, BasicSimulator simulator, int runs) {
        if (model == null) 
            throw new RuntimeException("Load model and formulae first");
        if (checker == null)
            throw new RuntimeException("Load MTL formulae first");
        SMCestimator est = new SMCfrequentistEstimator();
        estimator.setFixed(1);
        MTLformula formula = checker.getMTLformula(phi);
        formula.initializeForEstimate(est);
        boolean [] checked = new boolean[runs];       
        for (int i=0;i<runs;i++) {
            double [][] traj = simulator.simulate(tf);
            formula.modelCheckNextTrajectoryPointwiseSemantics(traj);
            if (est.getLast().isUndefined()) {
                System.out.println("WARNING: Trajectory is not lasting enough and cannot be checked. Simulate longer"); 
                int n = traj[0].length-1;
                double finalTime = traj[0][n];
                double formulaTime = formula.getTimeDepth();
                System.out.println("Trajectory time: " + finalTime + "; formula time depth: " + formulaTime);
                System.out.println("Formula: " + formula.toString());
            }
            checked[i] = est.getLast().isTrue();
        }
        return checked;
    }
    
    
    public boolean smc(String phi, double finalTime) {
        if (model == null) 
            throw new RuntimeException("Load model and formulae first");
        if (checker == null)
            throw new RuntimeException("Load MTL formulae first");
        this.estimator = new SMCfrequentistEstimator();
        estimator.setFixed(1);
        MTLformula formula = checker.getMTLformula(phi);
        formula.initializeForEstimate(estimator);
        double[][] traj = this.simulate(finalTime);
        formula.modelCheckNextTrajectoryPointwiseSemantics(traj);
        if (estimator.getLast().isUndefined())
                   throw new RuntimeException("Trajectory is not lasting enough and cannot be checked. Simulate longer");
        return estimator.getLast().isTrue();
    }
    
    
    
    public void setParameters(String[] names, double[] values) {
        ArrayList<String> fp = new  ArrayList<String>();
        double[] fv = new double[values.length];
        int c = 0;
        for (int i=0;i<names.length;i++) {
            String s = names[i];
            double v = values[i];
            if (model.containsExplorableParameter(s)) {
                model.changeInitialValueOfParameter(s, v);
            } 
            else if (model.containsExplorableVariable(s)) {
                model.changeInitialValueOfVariable(s, v);
            }
            else {
                fp.add(s);
                fv[c++] = v;
            }
        }
        fv = Arrays.trimToCapacity(fv, c);
        if (checker != null)
            checker.setFormulaeParameters(fp.toArray(new String[0]), fv);
        model.computeInitialValues();
    }
    
    
    public void plotTrajectory() {
        collector.setVarsToBePrinted(null);
        collector.plotSingleTrajectoryToScreen(0);  
    }
    
    public void plotTrajectory(ArrayList<String> vars) {
        collector.setVarsToBePrinted(vars);
        collector.plotSingleTrajectoryToScreen(0);  
    }
    
    
}
