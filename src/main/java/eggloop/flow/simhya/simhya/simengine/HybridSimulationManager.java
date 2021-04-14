/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine;

import java.io.PrintStream;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.OdeDataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.StochasticDataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.HybridDataCollector;
import eggloop.flow.simhya.simhya.model.flat.FlatModel;
import eggloop.flow.simhya.simhya.GlobalOptions;

import java.util.Locale;
import eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;



/**
 *
 * @author luca
 */
public class HybridSimulationManager {
    HybridSimulator simulator;
    HybridDataCollector collector;
    double finalTime;
    int runs;
    FlatModel model;
     /**
     * verbosity level:
     * 0: no verbosity, just prints total execution time
     * 1: moderate verbosity. Prints execution time of each run
     * 2: verbose: prints execution time of each run and events. 
     * 3: very verbose. Print data of each simulation and events.
     */
    int verbosityLevel = 0;
    final int MAX_VERBOSITY_LEVEL = 3;
    PrintStream outStream = System.out;
    boolean modifiedSimulator;

    public HybridSimulationManager(FlatModel model, double finalTime, int runs) {
        this.finalTime = finalTime;
        this.runs = runs;
        this.model = model;
        this.collector = new HybridDataCollector(model);
        collector.setPrintConditionByTime(GlobalOptions.samplePoints, finalTime);
        simulator = SimulatorFactory.newHybridSimulator(model, collector);
        simulator.setFinalTime(finalTime);
        this.setVerbosityOfMonitor(simulator.getProgressMonitor());
        modifiedSimulator = false;
    }
    
    
    public HybridSimulationManager(FlatModel model, double finalTime, int runs, boolean switching) {
        this.finalTime = finalTime;
        this.runs = runs;
        this.model = model;
        this.collector = new HybridDataCollector(model);
        collector.setPrintConditionByTime(GlobalOptions.samplePoints, finalTime);
        simulator = SimulatorFactory.newHybridSimulator(model, collector,switching);
        simulator.setFinalTime(finalTime);
        this.setVerbosityOfMonitor(simulator.getProgressMonitor());
        modifiedSimulator = false;
    }
    
    public HybridSimulationManager(FlatModel model, double finalTime, int runs,
            double discrete2continuousThreshold, double continuous2discreteThreshold) {
        this.finalTime = finalTime;
        this.runs = runs;
        this.model = model;
        this.collector = new HybridDataCollector(model);
        collector.setPrintConditionByTime(GlobalOptions.samplePoints, finalTime);
        simulator = SimulatorFactory.newHybridSimulator(model, collector,
                discrete2continuousThreshold,continuous2discreteThreshold);
        simulator.setFinalTime(finalTime);
        this.setVerbosityOfMonitor(simulator.getProgressMonitor());
        modifiedSimulator = false;
    }

    private void setVerbosityOfMonitor(ProgressMonitor monitor) {
        if (this.verbosityLevel == 0 || this.verbosityLevel == 1 || this.verbosityLevel == 2)
            monitor.setSilent(true);
        else
            monitor.setSilent(false);
        if (this.verbosityLevel == 2 || this.verbosityLevel == 3)
            monitor.setPrintEvents(true);
        else 
            monitor.setPrintEvents(false);
    }

    public void setVarsToBeSaved(ArrayList<String> varsToSave) {
        collector.setVarsToBeSaved(varsToSave);
    }

    public void saveOnlyFinalState() {
        collector.saveOnlyFinalState();
    }



    public void setSamplingConditionByTime(double burnout, double step) {
        collector.setPrintConditionByTime(burnout, step, finalTime);
    }

    public void setSamplingConditionByTime(double burnout, int points) {
        collector.setPrintConditionByTime(burnout, points, finalTime);
    }

    public void setSamplingConditionByTime(int points) {
        collector.setPrintConditionByTime(points, finalTime);
    }

//    public void setSamplingConditionByStep(int step) {
//        collector.setPrintConditionByStep(step);
//    }
    
    public void storeWholeTrajectoryData(int runs) {
        collector.storeWholeTrajectoryData(runs);
    }

    public void storeStatisticsOnly(int runs) {
        collector.storeStatisticsOnly(runs);
    }

    public void storeFinalStateDataOnly(int runs) {
        collector.storeFinalStateDataOnly(runs);
    }

    public void automaticStoreStrategy(int runs) {
        collector.automaticStoreStrategy(runs);
    }

    public void saveAllVariables() {
        collector.saveAllVariables();
    }

    public void useChache(boolean useChache) {
        simulator.useChache(useChache);
        this.modifiedSimulator = true;
    }

    public void setProgressMonitor(ProgressMonitor monitor) {
        simulator.setProgressMonitor(monitor);
        this.modifiedSimulator = true;
    }

    public void setInitialTime(double time) {
        simulator.setInitialTime(time);
        this.modifiedSimulator = true;
    }

    public void setRelativeTolerance(double tol) {
        simulator.setRelativeTolerance(tol);
        this.modifiedSimulator = true;
    }

    public void setMinimumStepSize(double stepSize) {
        simulator.setMinimumStepSize(stepSize);
        this.modifiedSimulator = true;
    }

    public void setMaximumStepSize(double stepSize) {
        simulator.setMaximumStepSize(stepSize);
        this.modifiedSimulator = true;
    }

    public void setMaxStepIncrementForEvents(double maxStepIncrementForEvents) {
        simulator.setMaxStepIncrementForEvents(maxStepIncrementForEvents);
        this.modifiedSimulator = true;
    }

    public void setMaxIterationforEvents(int maxIterationforEvents) {
        simulator.setMaxIterationforEvents(maxIterationforEvents);
        this.modifiedSimulator = true;
    }

    public void setMaxErrorForEvents(double maxErrorForEvents) {
        simulator.setMaxErrorForEvents(maxErrorForEvents);
        this.modifiedSimulator = true;
    }

    public void setIntegrator(IntegratorType integ) {
        simulator.setIntegrator(integ);
        this.modifiedSimulator = true;
    }

    public void setAbsoluteTolerance(double tol) {
        simulator.setAbsoluteTolerance(tol);
        this.modifiedSimulator = true;this.modifiedSimulator = true;
    }



    public void setVerbosityLevel(int level) {
        if (level >= this.MAX_VERBOSITY_LEVEL)
            this.verbosityLevel = this.MAX_VERBOSITY_LEVEL;
        if (level<= 0 )
            this.verbosityLevel = 0;
        else
            this.verbosityLevel = level;
        this.setVerbosityOfMonitor(simulator.getProgressMonitor());
        this.modifiedSimulator = true;
        if (level >= 2) {
            simulator.printEvents(true);
        }
    }

    public void addGlobalSwitchingRuleFromFile(String filename) {
        simulator.addGlobalSwitchingRuleFromFile(filename);
    }

    public void addGlobalSwitchingRule(String rule) {
        simulator.addGlobalSwitchingRule(rule);
    }
    
    

    
    public void newSimulatorWithNoSwitching() {
        if (modifiedSimulator)
            throw new SimulationException("Cannot reinitialize the simulator, options have already been changed");
        simulator = SimulatorFactory.newHybridSimulator(model, collector, false);
        simulator.setFinalTime(finalTime);
        this.setVerbosityOfMonitor(simulator.getProgressMonitor());
        modifiedSimulator = false;
    }
    
    public void newSimulatorWithPopulationSwitching(double discrete2continuousThreshold, double continuous2discreteThreshold) {
        if (modifiedSimulator)
            throw new SimulationException("Cannot reinitialize the simulator, options have already been changed");
        simulator = SimulatorFactory.newHybridSimulator(model, collector,
                discrete2continuousThreshold,continuous2discreteThreshold);
        simulator.setFinalTime(finalTime);
        this.setVerbosityOfMonitor(simulator.getProgressMonitor());
        modifiedSimulator = false;
    }
    

    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
        simulator.getProgressMonitor().setPrintStream(outStream);
    }


    public HybridDataCollector getDataCollector() {
        return collector;
    }

    public HybridDataCollector run() {
        long time = System.nanoTime();
        simulator.initialize();
        collector.clearAll();
        
        for (int i=0;i<runs;i++) {

            if (this.verbosityLevel == 3) outStream.print("******* RUN " + (i+1) +" ");

            //collector.newTrajectory();
            //if (this.verbosityLevel == 2) outStream.print("*");
            simulator.resetModel(true);
            if (this.verbosityLevel == 3) outStream.print("*");
            simulator.reinitialize();
            if (this.verbosityLevel == 3) outStream.println("*");
            simulator.run();

            if (this.verbosityLevel == 3) {
                outStream.println("Run " + (i+1) + " completed.");
                outStream.println("Simulation time: " + String.format(Locale.US, "%.4f", simulator.getLastSimulationTimeInSecs()) + " seconds. ");
                outStream.println("Steps of simulation: " + simulator.getLastSimulationSteps());
                outStream.println("Stochastic steps: " + simulator.getLastSimulationNumberOfStochasticEvents());
                outStream.println("Instantaneous steps: " + simulator.getLastSimulationNumberOfInstantaneousEvents());
                outStream.println("Timed steps: " + simulator.getLastSimulationNumberOfTimedEvents());
                outStream.println("Average time per step: " + String.format(Locale.US, "%.4f", simulator.getLastSimulationTimeInMillisecs()/simulator.getLastSimulationSteps()) + " milliseconds ");
                outStream.println();
                outStream.println("Time remaining: " + String.format(Locale.US, "%.4f", ((double)(System.nanoTime() - time)/1000000000)/(i+1)*(runs-i-1) ) + " seconds.");
            } else if (this.verbosityLevel == 1 || this.verbosityLevel == 2) {
                outStream.println("Run " + (i+1) + " completed in "
                        + String.format(Locale.US, "%.4f", simulator.getLastSimulationTimeInSecs()) + " seconds. "
                        + "Time remaining: " + String.format(Locale.US, "%.4f", ((double)(System.nanoTime() - time)/1000000000)/(i+1)*(runs-i-1) ) + " seconds.");
            }

        }
        time = System.nanoTime() - time;
        outStream.println("Total simulation time: " + String.format(Locale.US, "%.4f", (double)time/1000000000) + " seconds.");
        return collector;
    }

}


/*
 * TO DO:
 * implement support for global switching with global rules and with default rules.
 * Need a PartitioningType class, with different forms of global partitioning
 *
 */
