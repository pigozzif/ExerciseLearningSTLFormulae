/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.dataprocessing;

import com.eggloop.flow.simhya.simhya.dataprocessing.chart.*;
import com.eggloop.flow.simhya.simhya.model.store.Store;
import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import cern.jet.stat.Descriptive;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Luca
 */
public abstract class AbstractDataCollector implements DataCollector {
    
    //data variables
    protected Trajectory trajectory;
    /**
     * stores the last trajectory fully (all steps), useful for model checking.
     * First row is time, then all variables!
     */
    double[][] fullTrajectory;
    /**
     * here we store trajectory data
     */
    double[][][] data;
    /**
     * Here we store the final state data for each trajectory
     */
    double[][] finalData;
    /**
     * average of trajectories for each sample point. Makes sense only for stochastic systems
     */
    double[][] average;
    /**
     * covariance of trajectories for each sample point. Makes sense only for stochastic systems
     */
    double[][][] covariance;
    /**
     * skew of trajectories for each sample point. Makes sense only for stochastic systems
     */
    double[][] skew;
    /**
     * kutosis of trajectories for each sample point. Makes sense only for stochastic systems
     */
    double[][] kurtosis;
    
    /**
     * average of trajectories for final state. Makes sense only for stochastic systems
     */
    double[] finalAverage;
    /**
     * covariance of trajectories for final state. Makes sense only for stochastic systems
     */
    double[][] finalCovariance;
    /**
     * skewness of trajectories for final state. Makes sense only for stochastic systems
     */
    double[] finalSkew;
    /**
     * kurtosis of trajectories for final state. Makes sense only for stochastic systems
     */
    double[] finalKurtosis;
    /**
     * Number of steps in each simulation run;
     */
    double[] totalSteps;
    /**
     * average number of steps;
     */
    double averageTotalSteps;
    /**
     * variance of Total steps;
     */
    double varianceTotalSteps;
    
    //model variables
    protected Store store;
    String modelName;
    /**
     * Names of the variables and expression variables that correspond to data and finalData
     * the vector is constructed as such: varNames[0] = "time", then we have model variables
     * and then we have model expression variables. The order is the same as the one in varcodes 
     * and expvarcodes. 
     */
    String[] varNames;
    /**
     * integer codes of variables to save, in t
     */
    int[] varCodes;
    /**
     * integer codes of expression variables to save
     */
    int[] expVarCodes;
    
    // number of runs, variables, and points er trajectory stored
    int runs;
    int vars;
    int points;
    int currentRun;
    int currentPoint;
    int currentRunFinalState;
    int completedRuns;
    int statRuns;
    int statRunsFinalState;
    /*
     * This is the ID in which saving the current trajectory
     */
    int saveID;
    
    

    final int FULL_TRAJECTORY_INITIAL_SIZE = 10000;
    /**
     * current number of performed steps
     */
    int currentStep;
    /**
     * needed to store correctly full trajectory data
     */
    double previous_time;
    /**
     * if true, the last trajectory is stored fully
     */
    boolean storeLastFullTrajectory;
    //boolean variables describing the saving strategy
    /**
     * Stores data about all trajectories and computes statistics at the end of the simulation
     */
    boolean storeTrajectoryData;
    /**
     * stores data about the final state of each run only, computing statistics for the other data
     */
    boolean storeFinalStateDataOnly;
    /**
     * stores only statistical data, computing statistics at the end or on the fly
     */
    boolean storeStatisticsOnly;
    /**
     * Store strategy for SMC
     */
    boolean SMCstoreStrategy;
    /**
     * if true computes statistics for trajectories on the fly every {@link computeStatisticsEveryNRuns} runs
     */
    boolean onTheFlyStatisticsForTrajectories;
    /**
     * if true computes statistics for final state on the fly every {@link computeStatisticsEveryNRuns} runs
     */
    boolean onTheFlyStatisticsForFinalState;
    /**
     * number of runs after which statistics are computed for trajectories. This is estimated iven the memory available.
     */
    int computeTrajectoryStatisticsEveryNRuns;
    /**
     * number of runs after which statistics are computed for final state. This is estimated iven the memory available.
     */
    int computeFinalStateStatisticsEveryNRuns;
    
    //sampling specification
 //   boolean saveByStep; ///????
    boolean saveOnlyFinalState;
    double deltaTime;
 //   int deltaStep;
    double finalTime;
    double initialSavingTime;
    
    double lastPrintTime;
//    long lastPrintStep;
//    int expectedPoints;
    boolean samplingConditionInitialized;
    boolean saveStrategyInitialized;
    
    boolean initialized;    

    int[] varsToBePrinted;
    ArrayList<String> paramsToBePrinted;

//
//    ArrayList<String> varsToSave;
//    ArrayList<String> expVarsToSave;
//
//
//
//
    public boolean fullLatexDocument = false;
//
//    
    
    public AbstractDataCollector(FlatModel model) {
        this.store = model.getStore();
        this.modelName  = model.getName();
        trajectory = null;
        saveOnlyFinalState = false;
        deltaTime = 1;
        lastPrintTime = 0;
        initialSavingTime = 0;
        finalTime = 0;
        initialized = false;
        samplingConditionInitialized = false;
        saveStrategyInitialized = false;
        this.storeLastFullTrajectory = false;
        this.varsToBePrinted = null;
        this.saveAllVariables();
    }


    public void setVarsToBeSaved(ArrayList<String> varsToSave) {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        ArrayList<String> v = store.getNameOfAllVariables();
        ArrayList<String> ev = store.getNameOfAllExpressionVariables();
        //correctness check
        for (String s : varsToSave) {
            if (!v.contains(s) && !ev.contains(s))
                throw new DataException("Variable " + s + " is not defined in model " + this.modelName);
        }
        int n = varsToSave.size() + 1;
        varNames = new String[n];
        varNames[0] = "time";
        int c = 1;
        //set variable names and codes
        ArrayList<Integer> x = new ArrayList<Integer>();
        for (String s : varsToSave) 
            if (v.contains(s)) {
                varNames[c++] = s;
                x.add(store.getVariablesReference().getSymbolId(s));
            }
        varCodes = new int[x.size()];
        for (int i=0;i<x.size();i++)
            varCodes[i] = x.get(i);
        //set expvar names and codes
        x = new ArrayList<Integer>();
        for (String s : varsToSave) 
            if (ev.contains(s)) {
                varNames[c++] = s;
                x.add(store.getExpressionVariablesReference().getSymbolId(s));
            }
        expVarCodes = new int[x.size()];
        for (int i=0;i<x.size();i++)
            expVarCodes[i] = x.get(i);
        vars= varNames.length;
    }
    public void saveAllVariables() {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        int n1 = store.getNumberOfVariables();
        int n2 = store.getNumberOfExpressionVariables();        
        int n = 1 + n1 + n2;
        varNames = new String[n];
        varNames[0] = "time";
        int c = 1;
        for (String s : store.getNameOfAllVariables())
            varNames[c++] = s;
        for (String s : store.getNameOfAllExpressionVariables())
            varNames[c++] = s;
        varCodes = new int[n1];
        for (int i=0;i<n1;i++)
            varCodes[i] = i;
        expVarCodes = new int[n2];
        for (int i=0;i<n2;i++)
            expVarCodes[i] = i;
        vars= varNames.length;
    }
    public List<String> getNameOfSavedVariables() {
        return Arrays.asList(varNames);
    }

    public void setPrintConditionByTime(int points, double finalTime) {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        if (points <= 0 )
            throw new DataException("Number of points must be positive");
        this.deltaTime  = finalTime/points;
//        this.saveByStep = false;
        initialSavingTime = 0;
        this.points = points + 1;
//        this.expectedPoints = points + 1;
        this.samplingConditionInitialized = true;
        this.finalTime = finalTime;
    }
    public void setPrintConditionByTime(double burnout, int points, double finalTime) {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        if (points <= 0 )
            throw new DataException("Number of points must be positive");
        this.deltaTime  = (finalTime-burnout)/points;
//        this.saveByStep = false;
        initialSavingTime = burnout;
//        this.expectedPoints = points + 1;
        this.points = points+1;
        this.samplingConditionInitialized = true;
        this.finalTime = finalTime;
    }
    
    /*    
     public void setPrintConditionByTime(double burnout, double step) {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        if (step <= 0 )
            throw new DataException("Print step must be positive");
        this.deltaTime  = step;
        this.saveByStep = false;
        initialSavingTime = burnout;
        finalTime = burnout - 1;
        this.expectedPoints = -1;
        this.samplingConditionInitialized = true;
    } 
    */
    
    public void setPrintConditionByTime(double burnout, double step, double finalTime) {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        if (step <= 0 )
            throw new DataException("Step must be positive");
        this.deltaTime  = step;
        initialSavingTime = burnout;
        this.points = computeNumberOfPoints(finalTime);
        this.samplingConditionInitialized = true;
        this.finalTime = finalTime;
    }

    /* 
     public void setPrintConditionByStep(int step) {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        if (step <= 0 )
            throw new DataException("Print step must be positive");
        this.deltaStep = step;
        this.saveByStep = true;
        this.expectedPoints = -1;
        this.initialSavingTime = 0;
        this.samplingConditionInitialized = true;
     }

     */

    public void saveLastFullTrajectory() {
        this.storeLastFullTrajectory = true;
    }
    
    
    
    public void saveOnlyFinalState() {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        this.saveOnlyFinalState = true;
        this.samplingConditionInitialized = true;
        this.points = 0;
    }
    
    int computeNumberOfPoints(double finalTime) {
            long x = (long)Math.ceil((finalTime-initialSavingTime)/deltaTime);
            if (x <= 0 || x >= Integer.MAX_VALUE)
                throw new DataException("Too many data points, please increase the step size or reduce the final time");
            return (int)x + 1;
    }
    
    
    public void storeWholeTrajectoryData(int runs) {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        this.runs = runs;
        this.storeTrajectoryData = true;
        this.storeFinalStateDataOnly = false;
        this.storeStatisticsOnly = false;
        this.onTheFlyStatisticsForFinalState = false;
        this.onTheFlyStatisticsForTrajectories = false;
        this.SMCstoreStrategy = false;
        if (!checkMemoryRequirements())
            throw new DataException("Not enough memory to save whole trajectory data! Save only final state or free some memory");
        saveStrategyInitialized = true;
    }
    public void storeFinalStateDataOnly(int runs) {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        this.runs = runs;
        this.storeTrajectoryData = false;
        this.storeFinalStateDataOnly = true;
        this.storeStatisticsOnly = false;
        this.SMCstoreStrategy = false;
        if (!checkMemoryRequirements())
            throw new DataException("Not enough memory to save final state data! Save only statistics or free some memory");
        this.onTheFlyStatisticsForFinalState = false;
        this.onTheFlyStatisticsForTrajectories = computeTrajectoryStatisticsOnTheFly();
        saveStrategyInitialized = true;
    }
    public void storeStatisticsOnly(int runs) {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        this.runs = runs;
        this.storeTrajectoryData = false;
        this.storeFinalStateDataOnly = false;
        this.storeStatisticsOnly = true;
        this.SMCstoreStrategy = false;
        if (!checkMemoryRequirements())
            throw new DataException("Not enough memory to save data. Free some memory!");
        this.onTheFlyStatisticsForFinalState = computeFinalStateStatisticsOnTheFly();
        this.onTheFlyStatisticsForTrajectories = computeTrajectoryStatisticsOnTheFly();
        saveStrategyInitialized = true;
    }
    public void storeStrategySMC() {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        this.runs = 1;
        this.storeTrajectoryData = false;
        this.storeFinalStateDataOnly = false;
        this.storeStatisticsOnly = false;
        this.SMCstoreStrategy = true;
        this.onTheFlyStatisticsForFinalState = true;
        this.computeFinalStateStatisticsEveryNRuns = 1;
        this.onTheFlyStatisticsForTrajectories = true;
        this.computeTrajectoryStatisticsEveryNRuns = 1;
        this.saveStrategyInitialized = true;
        this.samplingConditionInitialized = true;
        this.saveLastFullTrajectory();
    }
    public void automaticStoreStrategy(int runs) {
        if (initialized)
           throw new DataException("Data collector already initialized!");
        this.runs = runs;
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        //available memory in bytes
        long availableMemory = runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory()) ;
        long m = ((long)this.runs)*(this.points+2)*(this.vars+1)*Double.SIZE + 
                    (this.runs > 1 ? ((long)(this.points+1))*(this.vars+1)*Double.SIZE*3 : 0) + //average 
                    (this.runs > 1 ? ((long)(this.points+1))*(this.vars+1)*(this.vars+1)*Double.SIZE : 0);
        if (m < availableMemory) { 
            this.storeWholeTrajectoryData(runs);
            return;
        }
        m = ((long)this.runs)*(this.vars+1)*Double.SIZE + 
                    (this.runs > 1 ? ((long)(this.points+1))*(this.vars+1)*Double.SIZE*3 : 0) + //average 
                    (this.runs > 1 ? ((long)(this.points+1))*(this.vars+1)*(this.vars+1)*Double.SIZE : 0);
        if (m < availableMemory) { 
            this.storeFinalStateDataOnly(runs);
            return;
        }
        m = ((long)(this.points+1))*(this.vars+1)*Double.SIZE*3  + //average 
            ((long)(this.points+1))*(this.vars+1)*(this.vars+1)*Double.SIZE; //covariance
        if (m < availableMemory) { 
            this.storeStatisticsOnly(runs);
            return;
        } else {
            throw new DataException("Not enough memory to save data. Free some memory!");
        }
    } 
    
   
    boolean checkMemoryRequirements() {
        //call garbage collector to free space
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        //available memory in bytes
        long availableMemory = runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory()) ;  
        return (availableMemory > 3/4*computeMemoryRequirements());
    }
    boolean computeTrajectoryStatisticsOnTheFly() {
        Runtime runtime = Runtime.getRuntime();
        long availableMemory = runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory()) ;  
        availableMemory -= computeMemoryRequirements();
        long excess = ((long)this.runs)*this.points*(this.vars+1)*Double.SIZE - availableMemory/4;
        if (excess > 0) {
            long m = availableMemory/(4*this.points*(this.vars+1)*Double.SIZE);
            this.computeTrajectoryStatisticsEveryNRuns =(m>=1 ? (int)m : 1);
            return true;
        } else return false;       
    }
    boolean computeFinalStateStatisticsOnTheFly() {
        Runtime runtime = Runtime.getRuntime();
        long availableMemory = runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory()) ;  
        availableMemory -= computeMemoryRequirements();
        long excess = ((long)this.runs)*(this.vars+1)*Double.SIZE - availableMemory/4;
        if (excess > 0) {
            long m = availableMemory/(4*(this.vars+1)*Double.SIZE);
            this.computeFinalStateStatisticsEveryNRuns =(m>=1 ? (int)m : 1);
            return true;
        } else return false;       
    }
    long computeMemoryRequirements() {
        if (this.storeTrajectoryData)
            return ((long)this.runs)*(this.points+1)*(this.vars+1)*Double.SIZE + 
                    (this.runs > 1 ? ((long)(this.points+1))*(this.vars+1)*Double.SIZE*3 : 0) + //average 
                    (this.runs > 1 ? ((long)(this.points+1))*(this.vars+1)*(this.vars+1)*Double.SIZE : 0); //covariance
        else if (this.storeFinalStateDataOnly)
            return ((long)this.runs)*(this.vars+1)*Double.SIZE + 
                    (this.runs > 1 ? ((long)(this.points+1))*(this.vars+1)*Double.SIZE*3 : 0) + //average 
                    (this.runs > 1 ? ((long)(this.points+1))*(this.vars+1)*(this.vars+1)*Double.SIZE : 0); //covariance
        else if (this.storeStatisticsOnly)
            return  ((long)(this.points+1))*(this.vars+1)*Double.SIZE*3  + //average 
                    ((long)(this.points+1))*(this.vars+1)*(this.vars+1)*Double.SIZE; //covariance
        else return 0;
    }
    
    
   
    
    
    public boolean dataNeeded(double nextTime) {
        if (this.storeLastFullTrajectory)
            return true;
        if (this.saveOnlyFinalState)
            return false;
        if (nextTime > this.lastPrintTime + this.deltaTime)
            return true;
        else return false;
    }
    public boolean dataNeeded(double nextTime,long stepNumber) {
        if (this.storeLastFullTrajectory)
            return true;
        if (this.saveOnlyFinalState)
            return false;
//        if (this.saveByStep && stepNumber == this.lastPrintStep + this.deltaStep)
//            return true;
        else if (nextTime > this.lastPrintTime + this.deltaTime)
            return true;
        else return false;
    }


    public void putData(double nextTime) {
        if (this.storeLastFullTrajectory) {
            //add data to full trajectory
            this.addToFullTrajectory(nextTime, store.getVariablesValues(), store.getExpressionVariablesValues());
        }
        if (this.SMCstoreStrategy)
            return;
        
//        if (this.saveByStep) {
//            trajectory.add(nextTime, store.getVariablesValues(), store.getExpressionVariablesValues());
//            this.lastPrintStep += this.deltaStep;
//        }
//        else
            while (this.lastPrintTime + this.deltaTime < nextTime) {
                lastPrintTime += deltaTime;
                add(lastPrintTime, store.getVariablesValues(), store.getExpressionVariablesValues());
            }
    }
    public void putFinalState(double time, long steps) {
        boolean doublePointAdded  = false;
        if (this.storeLastFullTrajectory) {
            //add data to full trajectory
            this.addToFullTrajectory(time, store.getVariablesValues(), store.getExpressionVariablesValues());
            if (time < this.finalTime) {
                this.addToFullTrajectory(finalTime, store.getVariablesValues(), store.getExpressionVariablesValues());
                doublePointAdded = true;
            }         
        }
        if (this.SMCstoreStrategy)
            return;
//        if (this.saveByStep || this.saveOnlyFinalState) {
        if (this.saveOnlyFinalState) {
            addFinalState(time, store.getVariablesValues(), store.getExpressionVariablesValues());
            totalSteps[currentRunFinalState] = steps;
        }
        else {
            if (finalTime > this.initialSavingTime) {
                while (this.lastPrintTime + this.deltaTime <= finalTime) {
                    lastPrintTime += deltaTime;
                    add(lastPrintTime, store.getVariablesValues(), store.getExpressionVariablesValues());
                }
            }
            addFinalState(time, store.getVariablesValues(), store.getExpressionVariablesValues());
            totalSteps[currentRunFinalState] = steps;
        }
        this.completedRuns++;
        if (runs > 1)
            this.computeStatistics();
        if (completedRuns == runs)
            purgeData();
        if (this.storeLastFullTrajectory) {
            //trim to correct size
            for (int i=0;i<this.vars;i++)
                fullTrajectory[i] = Arrays.copyOf(fullTrajectory[i], (doublePointAdded ? currentStep + 1 : currentStep) );
        }
    }
    
    void addToFullTrajectory(double time, double[] var, double[] exp) {
        if (currentStep == Integer.MAX_VALUE)
            return;
        if (currentStep == fullTrajectory[0].length) {
            //not enough points, doubling the array
            for (int i=0;i<this.vars;i++)
                fullTrajectory[i] = Arrays.copyOf(fullTrajectory[i], 2*currentStep);
        }
        fullTrajectory[0][currentStep] = previous_time;
        this.previous_time = time;
        int c = 1;
        for (int i=0;i<varCodes.length;i++)
            fullTrajectory[c++][currentStep] = var[varCodes[i]];
        for (int i=0;i<expVarCodes.length;i++)
            fullTrajectory[c++][currentStep] = exp[expVarCodes[i]];
        this.currentStep++;
        //deal correctly with final time
        if (time == this.finalTime) {
            if (currentStep == fullTrajectory[0].length) {
            //not enough points, doubling the array
            for (int i=0;i<this.vars;i++)
                fullTrajectory[i] = Arrays.copyOf(fullTrajectory[i], currentStep+1);
            }
            fullTrajectory[0][currentStep] = time;
            c = 1;
            for (int i=0;i<varCodes.length;i++)
                fullTrajectory[c++][currentStep] = var[varCodes[i]];
            for (int i=0;i<expVarCodes.length;i++)
                fullTrajectory[c++][currentStep] = exp[expVarCodes[i]];
            this.currentStep++;
        }
    }
    
    void add(double time, double[] var, double[] exp) {
//        System.out.println("Adding point " + currentPoint + " to data array of length " + data[currentRun][0].length);
        
        data[currentRun][0][currentPoint] = time;
        int c = 1;
        for (int i=0;i<varCodes.length;i++)
            data[currentRun][c++][currentPoint] = var[varCodes[i]];
        for (int i=0;i<expVarCodes.length;i++)
            data[currentRun][c++][currentPoint] = exp[expVarCodes[i]];
        currentPoint++;
    }
    
    void addFinalState(double time, double[] var, double[] exp) {
//        System.out.println("Adding final point to final data array of length " + finalData.length);

        finalData[currentRunFinalState][0] = time;
        int c = 1;
        for (int i=0;i<varCodes.length;i++)
            finalData[currentRunFinalState][c++] = var[varCodes[i]];
        for (int i=0;i<expVarCodes.length;i++)
            finalData[currentRunFinalState][c++] = exp[expVarCodes[i]];
    } 
    
    void purgeData() {
        if (this.SMCstoreStrategy || this.storeFinalStateDataOnly || this.storeStatisticsOnly) 
            this.data = null;
        if (this.SMCstoreStrategy || this.storeStatisticsOnly)
            this.finalData = null;
        System.gc();
    }
    
    void computeStatistics() {
        if (this.onTheFlyStatisticsForTrajectories) {
            if (this.currentRun == this.computeTrajectoryStatisticsEveryNRuns - 1
                    || this.completedRuns == this.runs) {
                int n1 = this.statRuns;
                int n2 = this.currentRun + 1;
                double q = ((double)n1)/(n1+n2);
                for (int p=0;p<points;p++) {
                    //computing the average
                    for (int v=0;v<vars;v++) {
                        double s = 0;
                        for (int i=0;i<=currentRun;i++)
                            s += data[i][v][p];
                        s /= (n1+n2);
                        average[v][p] = q*average[v][p] + s;
                    }
                    //computing the covariance --- in this case this is the non centred moment, has to be
                    //centred at the end. It is also the covariance without sample correction
                    for (int v1=0;v1<vars;v1++)
                        for (int v2=v1;v2<vars;v2++) {
                            double s = 0;
                            for (int i=0;i<=currentRun;i++)
                                s += (data[i][v1][p])*(data[i][v2][p]);
                            s /= (n1+n2);
                            covariance[v1][v2][p] = q*covariance[v1][v2][p] + s;
                        }
                    for (int v=0;v<vars;v++) {
                        //computing non-centred 3 order moment
                        double s = 0;
                        for (int i=0;i<=currentRun;i++)
                            s += data[i][v][p]*data[i][v][p]*data[i][v][p];
                        s /= (n1+n2);
                        skew[v][p] = q*skew[v][p] + s;
                    }
                    for (int v=0;v<vars;v++) {
                        //computing non-centred 4 order moment.
                        double s = 0;
                        for (int i=0;i<=currentRun;i++)
                            s += data[i][v][p]*data[i][v][p]*data[i][v][p]*data[i][v][p];
                        s /= (n1+n2);
                        kurtosis[v][p] = q*kurtosis[v][p] + s;
                    }
                }
                this.statRuns += n2;
                if (this.completedRuns == this.runs) {
                    for (int p=0;p<points;p++) {
                        for (int v=0;v<vars;v++) {
                            //computing centred 3 and 4 order moments
                            kurtosis[v][p] = kurtosis[v][p] - 4*skew[v][p]*average[v][p] 
                                    + 6*covariance[v][v][p]*average[v][p]*average[v][p] 
                                    - 3*average[v][p]*average[v][p]*average[v][p]*average[v][p];
                            skew[v][p] = skew[v][p] - 3*covariance[v][v][p]*average[v][p] 
                                    + 2*average[v][p]*average[v][p]*average[v][p];
                        }
                        for (int v1=0;v1<vars;v1++)
                            for (int v2=v1;v2<vars;v2++)
                                //compute sample covariance
                                covariance[v1][v2][p] = (((double)runs)/(runs-1))*(covariance[v1][v2][p] - average[v1][p]*average[v2][p]);
                        for (int v=0;v<vars;v++) {
                            //compute sample skew and kurtosis
                            skew[v][p] = Descriptive.sampleSkew(runs, skew[v][p], covariance[v][v][p]);
                            kurtosis[v][p] = Descriptive.sampleKurtosis(runs, kurtosis[v][p], covariance[v][v][p]);
                        }
                    }
                }
            }
        }
        else {
            if (this.completedRuns == runs) {
                for (int p=0;p<points;p++) {
                    //computing the average
                    for (int v=0;v<vars;v++) {
                        average[v][p] = 0;
                        for (int i=0;i<runs;i++)
                            average[v][p] += data[i][v][p];
                        average[v][p] /= runs;
                    }
                    //computing the covariance
                    for (int v1=0;v1<vars;v1++)
                        for (int v2=0;v2<vars;v2++) {
                            covariance[v1][v2][p] = 0;
                            for (int i=0;i<runs;i++)
                                covariance[v1][v2][p] += (data[i][v1][p] - average[v1][p])*(data[i][v2][p] - average[v2][p]);
                            covariance[v1][v2][p] /= (runs-1);
                        }
                    //computing the skew
                    for (int v=0;v<vars;v++) {
                        skew[v][p] = 0;
                        for (int i=0;i<runs;i++) {
                            double x = data[i][v][p] - average[v][p];
                            skew[v][p] += x*x*x;
                        }
                        skew[v][p] /= runs;
                        skew[v][p] = Descriptive.sampleSkew(runs, skew[v][p], covariance[v][v][p]);
                    }
                    //computing the kurtosis
                    for (int v=0;v<vars;v++) {
                        kurtosis[v][p] = 0;
                        for (int i=0;i<runs;i++) {
                            double x = data[i][v][p] - average[v][p];
                            kurtosis[v][p] += x*x*x*x;
                        }
                        kurtosis[v][p] /= runs;
                        kurtosis[v][p] = Descriptive.sampleKurtosis(runs, kurtosis[v][p], covariance[v][v][p]);
                    }
                }
            }
        }
        if (this.onTheFlyStatisticsForFinalState) {
            if (this.currentRunFinalState == this.computeFinalStateStatisticsEveryNRuns - 1
                    || this.completedRuns == this.runs) {
                int n1 = this.statRunsFinalState;
                int n2 = this.currentRunFinalState + 1;
                double q = ((double)n1)/(n1+n2);
                //computing the average
                for (int v=0;v<vars;v++) {
                    double s = 0;
                    for (int i=0;i<=currentRunFinalState;i++)
                        s += finalData[i][v];
                    s /= (n1+n2);
                    finalAverage[v] = q*finalAverage[v] + s;
                }
                {
                    double s = 0;
                    for (int i=0;i<=currentRunFinalState;i++)
                        s += totalSteps[i];
                    s /= (n1+n2);
                    averageTotalSteps = q*averageTotalSteps + s; 
                }
                //computing the covariance --- in this case this is the non centred moment, has to be
                //centred at the end.
                for (int v1=0;v1<vars;v1++)
                    for (int v2=0;v2<vars;v2++) {
                        double s = 0;
                        for (int i=0;i<=currentRunFinalState;i++)
                            s += (finalData[i][v1])*(finalData[i][v2]);
                        s /= (n1+n2);
                        finalCovariance[v1][v2] = q*finalCovariance[v1][v2] + s;
                    }
                {
                    double s = 0;
                    for (int i=0;i<=currentRunFinalState;i++)
                        s += (totalSteps[i])*(totalSteps[i]);
                    s /= (n1+n2);
                    varianceTotalSteps = q*varianceTotalSteps + s;
                }
                for (int v=0;v<vars;v++) {
                    double s = 0;
                    for (int i=0;i<=currentRunFinalState;i++)
                        s += finalData[i][v]*finalData[i][v]*finalData[i][v];
                    s /= (n1+n2);
                    finalSkew[v] = q*finalSkew[v] + s;
                }
                for (int v=0;v<vars;v++) {
                    double s = 0;
                    for (int i=0;i<=currentRunFinalState;i++)
                        s += finalData[i][v]*finalData[i][v]*finalData[i][v]*finalData[i][v];
                    s /= (n1+n2);
                    finalKurtosis[v] = q*finalKurtosis[v] + s;
                }
                statRunsFinalState += n2;
                if (this.completedRuns == this.runs) {
                    for (int v=0;v<vars;v++) {
                        //computing centred 3 and 4 order moments
                        finalKurtosis[v] = finalKurtosis[v] - 4*finalSkew[v]*finalAverage[v] 
                                + 6*finalCovariance[v][v]*finalAverage[v]*finalAverage[v] 
                                - 3*finalAverage[v]*finalAverage[v]*finalAverage[v]*finalAverage[v];
                        finalSkew[v] = finalSkew[v] - 3*finalCovariance[v][v]*finalAverage[v]
                                + 2*finalAverage[v]*finalAverage[v]*finalAverage[v];
                    }
                    for (int v1=0;v1<vars;v1++)
                        for (int v2=v1;v2<vars;v2++)
                             finalCovariance[v1][v2] = (((double)runs)/(runs-1))*
                                     (finalCovariance[v1][v2] - finalAverage[v1]*finalAverage[v2]);
                    varianceTotalSteps = (((double)runs)/(runs-1))*
                            (varianceTotalSteps - averageTotalSteps*averageTotalSteps);
                    for (int v=0;v<vars;v++) {
                        //compute sample skew and kurtosis
                        finalSkew[v] = Descriptive.sampleSkew(runs, finalSkew[v], finalCovariance[v][v]);
                        finalKurtosis[v] = Descriptive.sampleKurtosis(runs, finalKurtosis[v], finalCovariance[v][v]);
                    }
                }
            }
        }
        else {
            if (this.completedRuns == runs) {
                 //computing the average
                for (int v=0;v<vars;v++) {
                    finalAverage[v] = 0;
                    for (int i=0;i<runs;i++)
                        finalAverage[v] += finalData[i][v];
                    finalAverage[v] /= runs;
                }
                //computing the covariance
                for (int v1=0;v1<vars;v1++)
                    for (int v2=0;v2<vars;v2++) {
                        finalCovariance[v1][v2] = 0;
                        for (int i=0;i<runs;i++)
                            finalCovariance[v1][v2] += (finalData[i][v1] - finalAverage[v1])*(finalData[i][v2] - finalAverage[v2]);
                        finalCovariance[v1][v2] /= (runs-1);
                    }
                 //computing the skewness
                for (int v=0;v<vars;v++) {
                    finalSkew[v] = 0;
                    for (int i=0;i<runs;i++) {
                        double x = finalData[i][v] - finalAverage[v];
                        finalSkew[v] += x*x*x;
                    }
                    finalSkew[v] /= runs;
                    finalSkew[v] = Descriptive.sampleSkew(runs, finalSkew[v], finalCovariance[v][v]);
                }
                 //computing the kurtosis
                for (int v=0;v<vars;v++) {
                    finalKurtosis[v] = 0;
                    for (int i=0;i<runs;i++) {
                        double x = finalData[i][v] - finalAverage[v];
                        finalKurtosis[v] += x*x*x*x;
                    }
                    finalKurtosis[v] /= runs;
                    finalKurtosis[v] = Descriptive.sampleKurtosis(runs, finalKurtosis[v], finalCovariance[v][v]);
                }
                averageTotalSteps = 0;
                for (int i=0;i<runs;i++)
                  averageTotalSteps += totalSteps[i];
                averageTotalSteps /= runs;
                varianceTotalSteps = 0;
                for (int i=0;i<runs;i++)
                    varianceTotalSteps += (varianceTotalSteps - averageTotalSteps)*(varianceTotalSteps - averageTotalSteps);
                varianceTotalSteps /= (runs-1);
            }
        }
                
        
    }
    
       
    
    public Trajectory getTrajectory(int id) {
        if (id < 0 && id >= this.completedRuns)
            throw new DataException("Trajectory " + id + " does not exists (yet)");
        if (data != null)
            trajectory.data = this.data[id];
        if (finalData != null)
            trajectory.finalState = this.finalData[id];
        if (this.storeTrajectoryData)
            trajectory.savedPoints = this.points;
        else 
            trajectory.savedPoints = 0;
        trajectory.id = id;
        return this.trajectory;
    }
    public Trajectory getLastFullTrajectory() {
        if (!this.storeLastFullTrajectory || this.fullTrajectory==null)
            throw new DataException("Full Trajectory not saved");
        trajectory.data = this.fullTrajectory;
        trajectory.savedPoints = this.fullTrajectory[0].length;
        trajectory.id=0;
        return trajectory;
    }
    
    
    public void newTrajectory() {
        if (!initialized) {
            if (!this.samplingConditionInitialized)
                throw new DataException("Sampling condition must be initialized first");
            if (!this.saveStrategyInitialized)
                throw new DataException("Save strategy must be initialized first");
            this.initialize();
            this.currentRun = 0;
            this.currentRunFinalState = 0;
            this.initialized  = true;
        } else if (!this.SMCstoreStrategy) {
            if (this.completedRuns == this.runs) 
                throw new DataException("Already completed " + runs + " runs");
            if (this.onTheFlyStatisticsForTrajectories)
                this.currentRun = (this.currentRun + 1) % this.computeTrajectoryStatisticsEveryNRuns;
            else
                this.currentRun++;
            if (this.onTheFlyStatisticsForFinalState)
                this.currentRunFinalState = (this.currentRunFinalState + 1) % this.computeFinalStateStatisticsEveryNRuns;
            else
                this.currentRunFinalState++;
        }
        this.currentPoint = 0;
        //this.lastPrintStep = -this.deltaStep;
        this.lastPrintTime = this.initialSavingTime - this.deltaTime;
        //reset full trajectory
        if (this.storeLastFullTrajectory) {
            this.fullTrajectory = new double[vars][FULL_TRAJECTORY_INITIAL_SIZE];
            this.currentStep = 0;
            this.previous_time = 0;
        }
        //System.gc();
    }
    public void clearAll() {
        this.initialized = false;
        this.data = null;
        this.finalData = null;
        this.average = null;
        this.covariance = null;
        this.skew = null;
        this.kurtosis = null;
        this.finalAverage = null;
        this.finalCovariance = null;
        this.finalKurtosis = null;
        this.finalSkew = null;
        this.trajectory = null;
        this.fullTrajectory=null;
        //System.gc();
    }
    
    void initialize() {
        if (this.SMCstoreStrategy) {
            this.data = null;
        }
        else if (this.storeTrajectoryData || !onTheFlyStatisticsForTrajectories) {
            this.data = new double[runs][vars][points];
        } 
        else {
            this.data = new double[computeTrajectoryStatisticsEveryNRuns][vars][points];
        }
        if (this.SMCstoreStrategy) {
            this.finalData = null;
            this.totalSteps = null;
        }
        else if (this.storeTrajectoryData || this.storeFinalStateDataOnly || !onTheFlyStatisticsForFinalState) {
            this.finalData = new double[runs][vars];
            this.totalSteps = new double[runs];
        } 
        else {
            this.finalData = new double[computeFinalStateStatisticsEveryNRuns][vars];
            this.totalSteps = new double[computeFinalStateStatisticsEveryNRuns];
        }
        if (runs > 1) {
            this.average = new double[vars][points];
            this.covariance = new double[vars][vars][points];
            this.kurtosis = new double[vars][points];
            this.skew = new double[vars][points];
            this.finalAverage = new double[vars];
            this.finalCovariance = new double[vars][vars];
            this.finalKurtosis = new double[vars];
            this.finalSkew = new double[vars];
            this.averageTotalSteps = 0;
            this.varianceTotalSteps = 0;
            this.statRuns = 0;
            this.statRunsFinalState = 0;
        } else {
            this.average = null;
            this.covariance = null;
            this.kurtosis = null;
            this.skew = null;
            this.finalAverage = null;
            this.finalCovariance = null;
            this.finalKurtosis = null;
            this.finalSkew = null;
        }
        //initializes a new template trajectory to use for backward compatibility
        this.trajectory = new Trajectory(store);
        trajectory.names = this.varNames;
        trajectory.varsToSave = this.varCodes;
        trajectory.expVarsToSave = this.expVarCodes;
        trajectory.numberOfVarsToSave = this.varCodes.length;
        trajectory.numberOfExpVarsToSave = this.expVarCodes.length;
        trajectory.dataDimension = this.vars;
        if (this.storeTrajectoryData)
            trajectory.savedPoints = this.points;
        else 
            trajectory.savedPoints = 0;
        trajectory.data = null;
        trajectory.finalState = null;
        this.completedRuns = 0;
    }

    public int getNumberOfTrajectories() {
        return this.runs;
    }
    
    
    
    public boolean containsStatisticsData() {
        return (runs > 1);
    }

    public boolean containsFinalStateData() {
        return this.storeFinalStateDataOnly;
    }

    public boolean containsStatisticsFinalStateDataOnHigherOrderMoments() {
        return containsStatisticsData();
    }

    public boolean containsStatisticsTrajectoryDataOnHigherOrderMoments() {
        return containsStatisticsData();
    }
    
    public boolean containsTrajectoryData() {
        return this.storeTrajectoryData;
    }

    


    
     
     
    
     /**
     * Adds a new time trace to the current trajectory, for variable var
     * If the variable is already defined or if the length of the time trace is not 
     * as that of others, throws an exception
     * @param var
     * @param values 
     */
    public void addTimeTrace(String var, double[] values) {
        if (runs>1)
            throw new DataException("Cannot add a new time trace on a collection of more than one trajectory ");
        //check that variable name is not defined.
        if (this.isVarDefined(var))
            throw new DataException("There is already a variable with name " + var);
        if (values.length != this.points)
            throw new DataException("The trajectory for variable " + var + " has wrong length; "
                    + "it is " + values.length  + " but should be " + this.points);
        
        double [][][] newData = new double[runs][this.vars+1][];
        System.arraycopy(this.data[0], 0, newData[0], 0, this.vars);
        newData[0][this.vars] = Arrays.copyOf(values, this.points);
        this.vars++;
        data = newData;
        this.finalData[0] = Arrays.copyOf(this.finalData[0], this.vars);
        this.finalData[0][this.vars-1] = values[values.length-1];
        this.varNames = Arrays.copyOf(varNames, this.vars);
        this.varNames[this.vars-1] = var;
        this.trajectory.names = varNames;
        this.trajectory.dataDimension = this.vars;
    }

    
    
    /**
     * Adds a new point to the final state of the current trajectory, for variable var
     * If the variable is already defined, throws an exception
     * @param var
     * @param value
     */
    public void addFinalStateTrace(String var, double value) {
        if (runs>1)
            throw new DataException("Cannot add a new time trace on a collection of more than one trajectory ");
        if (!this.storeFinalStateDataOnly && !this.saveOnlyFinalState)
            throw new DataException("Data stored for the whole trajectory, add the whole time trace ");
        //check that variable name is not defined.
        if (this.isVarDefined(var))
            throw new DataException("There is already a variable with name " + var);
        this.vars++;
        this.finalData[0] = Arrays.copyOf(this.finalData[0], this.vars);
        this.finalData[0][this.vars-1] = value;
        this.varNames = Arrays.copyOf(varNames, this.vars);
        this.varNames[this.vars-1] = var;
        this.trajectory.names = varNames;
        this.trajectory.dataDimension = this.vars;
    }
     
     

    /**
     * returns the index of the variable with name n, throws an exception if the variable does
     * not exist
     * @param n
     * @return
     */
    int getVarIndex(String n) {
        for (int i=0;i<this.varNames.length;i++)
            if (this.varNames[i].equals(n))
                return i;
        throw new IllegalArgumentException("Variable " + n + " does not exist.");
    }
    
    /**
     * Returns true is variable n is defined.
     * @param n
     * @return 
     */
    boolean isVarDefined(String n) {
        for (int i=0;i<this.varNames.length;i++)
            if (this.varNames[i].equals(n))
                return true;
        return false;
    }

    public void setVarsToBePrinted(ArrayList<String> vars) {
        if (vars!=null) {
            varsToBePrinted = new int[vars.size()+1];
            varsToBePrinted[0] = 0;
            int c = 1;
            for (String v : vars) 
                varsToBePrinted[c++] = getVarIndex(v);
        } else 
            varsToBePrinted = null;
    }
    
    

    public void setAllVarsToBePrinted() {
        this.varsToBePrinted = null;
    }

    public void setParamsToBePrinted(ArrayList<String> pars) {
        this.paramsToBePrinted = pars;
    }

    public void setNoParamToBePrinted() {
        this.paramsToBePrinted = null;
    }


    
    public void saveSingleTrajectoryToCSV(int trajectory, String filename) {
        int n = runs;
        if (trajectory < 0 || trajectory >= n)
            throw new DataException("There is no trajectory with id " + trajectory);
        Trajectory t = this.getTrajectory(trajectory);
        if (varsToBePrinted != null)
            t.setVariablesToPrint(varsToBePrinted);
        if (paramsToBePrinted != null)
            t.setParametersToPrint(paramsToBePrinted);
        boolean saveFinalState = (finalTime < this.initialSavingTime) || this.saveOnlyFinalState || this.storeFinalStateDataOnly;
        t.saveAsCSV(filename, false, false, saveFinalState );
        //t.saveAsCSV(filename, false, false, false );
    }

    public void saveAllTrajectoriesToCSV(String filename, boolean printRunNumber) {
        int n = runs;
        if (n == 0)
            throw new DataException("There is no trajectory");
        boolean saveFinalState = (finalTime < this.initialSavingTime) || this.saveOnlyFinalState || this.storeFinalStateDataOnly;
        for (int i=0;i<n;i++) {
            Trajectory t = this.getTrajectory(i);
            if (varsToBePrinted != null)
                t.setVariablesToPrint(varsToBePrinted);
            if (paramsToBePrinted != null)
                t.setParametersToPrint(paramsToBePrinted);
            t.saveAsCSV(filename, (i == 0 ? false : true), (n == 1? false : printRunNumber), saveFinalState);
        }
    }


    public void saveAllTrajectoriesToCSV(String filename, boolean printRunNumber, boolean append) {
        int n = runs;
        if (n == 0)
            throw new DataException("There is no trajectory");
        boolean saveFinalState = (finalTime < this.initialSavingTime) || this.saveOnlyFinalState || this.storeFinalStateDataOnly;
        //boolean saveFinalState = true;
        for (int i=0;i<n;i++) {
            Trajectory t = this.getTrajectory(i);
            if (varsToBePrinted != null)
                t.setVariablesToPrint(varsToBePrinted);
            if (paramsToBePrinted != null)
                t.setParametersToPrint(paramsToBePrinted);
            t.saveAsCSV(filename, (i == 0 ? append : true), (n == 1? false : printRunNumber),saveFinalState);
        }
    }

    
    
    protected LineChart plotTrajectories(ArrayList<Integer> trajIds) {
        
        ArrayList<Plot2DTrajectory> plotList = new ArrayList<Plot2DTrajectory>();
        for (Integer i : trajIds) {
            Trajectory t = this.getTrajectory(i);
            if (varsToBePrinted != null)
                t.setVariablesToPrint(varsToBePrinted);
            else t.printAllVariables();
            plotList.addAll(t.getTrajectoriesToPlot());
        }
        LineChart chart = new LineChart(plotList,"time","values");

        chart.fullLatexDocument = this.fullLatexDocument;
        return chart;
    }

    /**
     * Generates a {@link } from  a list of trajectory ids.
     * @param trajIds a list of trjectory ids
     * @return a Line2Dchart (chart using JMathPlot)
     */
    protected Line2DChart plot2DTrajectories(ArrayList<Integer> trajIds) {
        ArrayList<Plot2DTrajectory> plotList = new ArrayList<Plot2DTrajectory>();
        for (Integer i : trajIds) {
            Trajectory t = this.getTrajectory(i);
            if (varsToBePrinted != null)
                t.setVariablesToPrint(varsToBePrinted);
            else t.printAllVariables();
            plotList.addAll(t.getTrajectoriesToPlot());
        }
        Line2DChart chart = new Line2DChart(plotList,"time","values");
        return chart;
    }

    public void plotSingleTrajectory(int id, String filename, PlotFileType target) {
        int n = runs;
        if (id < 0 || id >= n)
            throw new DataException("There is no trajectory with id " + id);
        ArrayList<Integer> traj = new ArrayList<Integer>();
        traj.add(id);
        LineChart chart = plotTrajectories(traj);
        chart.saveToFile(filename,target);
    }

    public void plotAllTrajectories(String filename, PlotFileType target) {
        int n = runs;
        if (n == 0) throw new DataException("There is no trajectory");
        ArrayList<Integer> traj = new ArrayList<Integer>();
        for (int i=0;i<n;i++) traj.add(i);
        LineChart chart = plotTrajectories(traj);
        chart.saveToFile(filename,target);
    }

    public void plotTrajectories(int first, int last, String filename, PlotFileType target) {
        int n = runs;
        if (n == 0) throw new DataException("There is no trajectory");
        if (first >= n) throw new DataException("Invalid first trajectory id");
        if (last < first) throw new DataException("Invalid last trajectory id");
        if (last >= n) last  = n-1;
        if (first < 0) first  = 0;
        ArrayList<Integer> traj = new ArrayList<Integer>();
        for (int i=first;i<=last;i++) traj.add(i);
        LineChart chart = plotTrajectories(traj);
        chart.saveToFile(filename,target);
    }

    public void plotTrajectories(ArrayList<Integer> trajIds, String filename, PlotFileType target) {
        int n = runs;
        if (n == 0) throw new DataException("There is no trajectory");
        for (int i=0;i<trajIds.size();i++)
            if (trajIds.get(i) < 0 || trajIds.get(i)>=n)
                throw new DataException("Invalid trajectory id");
        if (trajIds.isEmpty()) throw new DataException("Empty trajectory list");
        LineChart chart = plotTrajectories(trajIds);
        chart.saveToFile(filename,target);
    }


    
    public JFrame plotSingleTrajectoryToScreen(int id) {
        int n = runs;
        if (id < 0 || id >= n)
            throw new DataException("There is no trajectory with id " + id);
        ArrayList<Integer> traj = new ArrayList<Integer>();
        traj.add(id);
        LineChart chart = plotTrajectories(traj);
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " trajectory " + id);
        return frame;
    }

    public JFrame plotAllTrajectoriesToScreen() {
        int n = runs;
        if (n == 0) throw new DataException("There is no trajectory");
        ArrayList<Integer> traj = new ArrayList<Integer>();
        for (int i=0;i<n;i++) traj.add(i);
        
        LineChart chart = plotTrajectories(traj);
        //testing new 
//        Line2DChart chart = plot2DTrajectories(traj);
        //chart.removeLegend();
        
        
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " all trajectories");
        return frame;
    }

    public JFrame plotTrajectoriesToScreen(int first, int last) {
        int n = runs;
        if (n == 0) throw new DataException("There is no trajectory");
        if (first >= n) throw new DataException("Invalid first trajectory id");
        if (last < first) throw new DataException("Invalid last trajectory id");
        if (last >= n) last  = n-1;
        if (first < 0) first  = 0;
        ArrayList<Integer> traj = new ArrayList<Integer>();
        for (int i=first;i<=last;i++) traj.add(i);
        LineChart chart = plotTrajectories(traj);
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " trajectories " + first + " to " + last);
        return frame;
    }

    public JFrame plotTrajectoriesToScreen(ArrayList<Integer> trajIds) {
        int n = runs;
        if (n == 0) throw new DataException("There is no trajectory");
        for (int i=0;i<trajIds.size();i++)
            if (trajIds.get(i) < 0 || trajIds.get(i)>=n)
                throw new DataException("Invalid trajectory id");
        if (trajIds.isEmpty()) throw new DataException("Empty trajectory list");
        LineChart chart = plotTrajectories(trajIds);
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " selected trajectories");
        return frame;
    }
    
    public JPanel plotSingleTrajectoryToPanel(int id) {
        int n = runs;
        if (id < 0 || id >= n)
            throw new DataException("There is no trajectory with id " + id);
        ArrayList<Integer> traj = new ArrayList<Integer>();
        traj.add(id);
        LineChart chart = plotTrajectories(traj);
        JPanel frame = chart.getPanel();
        return frame;
    }
    
     public JPanel plotAllTrajectoriesToPanel() {
        int n = runs;
        if (n == 0) throw new DataException("There is no trajectory");
        ArrayList<Integer> traj = new ArrayList<Integer>();
        for (int i=0;i<n;i++) traj.add(i);
        
        LineChart chart = plotTrajectories(traj);
        //testing new 
//        Line2DChart chart = plot2DTrajectories(traj);
        //chart.removeLegend();
        
        JPanel panel = chart.getPanel();
        return panel;
    }

    public JPanel plotTrajectoriesToPanel(int first, int last) {
        int n = runs;
        if (n == 0) throw new DataException("There is no trajectory");
        if (first >= n) throw new DataException("Invalid first trajectory id");
        if (last < first) throw new DataException("Invalid last trajectory id");
        if (last >= n) last  = n-1;
        if (first < 0) first  = 0;
        ArrayList<Integer> traj = new ArrayList<Integer>();
        for (int i=first;i<=last;i++) traj.add(i);
        LineChart chart = plotTrajectories(traj);
        JPanel panel = chart.getPanel();
        return panel;
    }

    public JPanel plotTrajectoriesToPanel(ArrayList<Integer> trajIds) {
        int n = runs;
        if (n == 0) throw new DataException("There is no trajectory");
        for (int i=0;i<trajIds.size();i++)
            if (trajIds.get(i) < 0 || trajIds.get(i)>=n)
                throw new DataException("Invalid trajectory id");
        if (trajIds.isEmpty()) throw new DataException("Empty trajectory list");
        LineChart chart = plotTrajectories(trajIds);
        JPanel panel = chart.getPanel();
        return panel;
    }
    
    
    /**
     * constructs a list of {@link Plot2DTrajectory} from all trajectories for the two given variables
     * @param var1
     * @param var2
     * @return 
     */
    protected ArrayList<Plot2DTrajectory> getPhasePlaneTrajectories(String var1, String var2) {
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        for (int j=0;j<runs;j++) {
            Trajectory t = this.getTrajectory(j);
            Plot2DTrajectory pt = new Plot2DTrajectory(points);
            pt.name = var1 + "--" + var2;
            int id1 = t.getVarId(var1); //throws dataexception if not present
            int id2 = t.getVarId(var2);
            for (int i=0;i<points;i++) {
                pt.x[i] = t.data[id1][i];
                pt.y[i] = t.data[id2][i];
            }
            list.add(pt);
        }
        return list;
    }
    
    /**
     * constructs a list of {@link Plot2DTrajectory} from a subset of trajectories for the two given variables
     * @param var1
     * @param var2
     * @param ids a list of trajectory ids
     * @return 
     */
    protected ArrayList<Plot2DTrajectory> getPhasePlaneTrajectories(String var1, String var2, int... ids) {
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        for (int j=0;j<ids.length;j++) {
            Trajectory t = this.getTrajectory(ids[j]);
            Plot2DTrajectory pt = new Plot2DTrajectory(t.savedPoints);
            pt.name = var1 + "--" + var2;
            int id1 = t.getVarId(var1); //throws dataexception if not present
            int id2 = t.getVarId(var2);
            for (int i=0;i<t.savedPoints;i++) {
                pt.x[i] = t.data[id1][i];
                pt.y[i] = t.data[id2][i];
            }
            list.add(pt);
        }
        return list;
    }
    
    
     /**
     * constructs a list of {@link Plot3DTrajectory} from all trajectories for the three given variables
     * @param var1
     * @param var2
     * @param var3
     * @return 
     */
    protected ArrayList<Plot3DTrajectory> getPhaseSpaceTrajectories(String var1, String var2, String var3) {
        ArrayList<Plot3DTrajectory> list = new ArrayList<Plot3DTrajectory>();
        for (int j=0;j<runs;j++) {
            Trajectory t = this.getTrajectory(j);
            Plot3DTrajectory pt = new Plot3DTrajectory(t.savedPoints);
            pt.name = var1 + "--" + var2 + "--" + var3;
            int id1 = t.getVarId(var1); //throws dataexception if not present
            int id2 = t.getVarId(var2);
            int id3 = t.getVarId(var2);
            for (int i=0;i<t.savedPoints;i++) {
                pt.x[i] = t.data[id1][i];
                pt.y[i] = t.data[id2][i];
                pt.z[i] = t.data[id3][i];
            }
            list.add(pt);
        }
        return list;
    }
    
    
    /**
     * constructs a list of {@link Plot3DTrajectory} from a subset of trajectories for the three given variables
     * @param var1
     * @param var2
     * @param var3
     * @param ids a list of trajectory ids
     * @return 
     */
    protected ArrayList<Plot3DTrajectory> getPhaseSpaceTrajectories(String var1, String var2, String var3, int... ids) {
        ArrayList<Plot3DTrajectory> list = new ArrayList<Plot3DTrajectory>();
        for (int j=0;j<ids.length;j++) {
            Trajectory t = this.getTrajectory(ids[j]);
            Plot3DTrajectory pt = new Plot3DTrajectory(t.savedPoints);
            pt.name = var1 + "--" + var2 + "--" + var3;
            int id1 = t.getVarId(var1); //throws dataexception if not present
            int id2 = t.getVarId(var2);
            int id3 = t.getVarId(var2);
            for (int i=0;i<t.savedPoints;i++) {
                pt.x[i] = t.data[id1][i];
                pt.y[i] = t.data[id2][i];
                pt.z[i] = t.data[id3][i];
            }
            list.add(pt);
        }
        return list;
    }
    
    public JFrame phasePlanePlotToScreen(String var1, String var2, int... ids) {
        ArrayList<Plot2DTrajectory> traj = this.getPhasePlaneTrajectories(var1, var2, ids);
        Line2DChart chart = new Line2DChart(traj,var1,var2);
        chart.removeLegend();
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " phase plane " + var1 + "--" + var2);
        return frame;
    }
    
    public JFrame phasePlanePlotToScreen(String var1, String var2) {
        ArrayList<Plot2DTrajectory> traj = this.getPhasePlaneTrajectories(var1, var2);
        Line2DChart chart = new Line2DChart(traj,var1,var2);
        chart.removeLegend();
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " phase plane " + var1 + "--" + var2);
        return frame;
    }
    
    public JPanel phasePlanePlotToPanel(String var1, String var2, int... ids) {
        ArrayList<Plot2DTrajectory> traj = this.getPhasePlaneTrajectories(var1, var2, ids);
        Line2DChart chart = new Line2DChart(traj,var1,var2);
        chart.removeLegend();
        JPanel panel = chart.getPanel();
        return panel;
    }
    
    public JPanel phasePlanePlotToPanel(String var1, String var2) {
        ArrayList<Plot2DTrajectory> traj = this.getPhasePlaneTrajectories(var1, var2);
        Line2DChart chart = new Line2DChart(traj,var1,var2);
        chart.removeLegend();
        JPanel panel = chart.getPanel();
        return panel;
    }
    
    public void phasePlanePlot(String var1, String var2, String filename, PlotFileType target, int... ids) {
        ArrayList<Plot2DTrajectory> traj = this.getPhasePlaneTrajectories(var1, var2, ids);
        Line2DChart chart = new Line2DChart(traj,var1,var2);
        chart.removeLegend();
        chart.saveToFile(filename, target);
    }
    
    public void phasePlanePlot(String var1, String var2, String filename, PlotFileType target) {
        ArrayList<Plot2DTrajectory> traj = this.getPhasePlaneTrajectories(var1, var2);
        Line2DChart chart = new Line2DChart(traj,var1,var2);
        chart.removeLegend();
        chart.saveToFile(filename, target);
    }
    
    public JFrame phaseSpacePlotToScreen(String var1, String var2, String var3, int... ids) {
        ArrayList<Plot3DTrajectory> traj = this.getPhaseSpaceTrajectories(var1, var2, var3, ids);
        Line3DChart chart = new Line3DChart(traj,var1,var2,var3);
        chart.removeLegend();
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " phase space " + var1 + "--" + var2 + "--" + var3);
        return frame;
    }
    
    public JFrame phaseSpacePlotToScreen(String var1, String var2, String var3) {
        ArrayList<Plot3DTrajectory> traj = this.getPhaseSpaceTrajectories(var1, var2, var3);
        Line3DChart chart = new Line3DChart(traj,var1,var2,var3);
        chart.removeLegend();
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " phase space " + var1 + "--" + var2 + "--" + var3);
        return frame;
    }
    
    public JPanel phaseSpacePlotToPanel(String var1, String var2, String var3, int... ids) {
        ArrayList<Plot3DTrajectory> traj = this.getPhaseSpaceTrajectories(var1, var2, var3, ids);
        Line3DChart chart = new Line3DChart(traj,var1,var2,var3);
        chart.removeLegend();
        JPanel panel = chart.getPanel();
        return panel;
    }
    
    public JPanel phaseSpacePlotToPanel(String var1, String var2, String var3) {
        ArrayList<Plot3DTrajectory> traj = this.getPhaseSpaceTrajectories(var1, var2, var3);
        Line3DChart chart = new Line3DChart(traj,var1,var2,var3);
        chart.removeLegend();
        JPanel panel = chart.getPanel();
        return panel;
    }

    
    public void phaseSpacePlot(String var1, String var2, String var3, String filename, PlotFileType target, int... ids) {
        ArrayList<Plot3DTrajectory> traj = this.getPhaseSpaceTrajectories(var1, var2, var3, ids);
        Line3DChart chart = new Line3DChart(traj,var1,var2,var3);
        chart.removeLegend();
        chart.saveToFile(filename, target);
    }
    
    public void phaseSpacePlot(String var1, String var2, String var3, String filename, PlotFileType target) {
        ArrayList<Plot3DTrajectory> traj = this.getPhaseSpaceTrajectories(var1, var2, var3);
        Line3DChart chart = new Line3DChart(traj,var1,var2,var3);
        chart.removeLegend();
        chart.saveToFile(filename, target);
    }

   
    public TrajectoryStatistics getTrajectoryStatistics() {
        throw new UnsupportedOperationException("Statistics computation not supported by this data");
    }
    
    
}
