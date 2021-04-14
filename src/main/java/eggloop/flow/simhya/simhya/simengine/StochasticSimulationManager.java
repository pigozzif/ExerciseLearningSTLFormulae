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


/**
 * This class takes a model, some simulation parameters,
 * runs the simulation and returns a data collector with
 * the collected data. 
 * @author Luca
 */
public class StochasticSimulationManager {
    StochasticSimulator simulator;
    StochasticDataCollector collector;
    double finalTime;
    int runs;
    FlatModel model;
    /**
     * verbosity level:
     * 0: no verbosity, just prints total execution time
     * 1: moderate verbosity. Prints execution time of each run
     * 2: very verbose. Print data of each simulation.
     */
    int verbosityLevel = 0;
    final int MAX_VERBOSITY_LEVEL = 2;
    PrintStream outStream = System.out;


    public StochasticSimulationManager(FlatModel model, SimType type, double finalTime, int runs) {
        this.finalTime = finalTime;
        this.runs = runs;
        this.model = model;
        this.collector = new StochasticDataCollector(model);
        collector.setPrintConditionByTime(GlobalOptions.samplePoints, finalTime);
        switch (type) {
            case SSA:
                this.simulator = SimulatorFactory.newSSAsimulator(model, collector);
                break;
            case GB:
                this.simulator = SimulatorFactory.newGBsimulator(model, collector);
                break;
            default:
                throw new SimulationException("Simulation type is not stochastic");
        }
        simulator.setFinalTime(finalTime);
        this.setVerbosityOfMonitor(simulator.getProgressMonitor());
    }

    

    public void useChache(boolean useChache) {
        simulator.useChache(useChache);
    }

    private void setVerbosityOfMonitor(ProgressMonitor monitor) {
        if (this.verbosityLevel == 0 || this.verbosityLevel == 1)
            monitor.setSilent(true);
        else
            monitor.setSilent(false);
    }

    public void setProgressMonitor(ProgressMonitor monitor) {
        this.setVerbosityOfMonitor(monitor);
        simulator.setProgressMonitor(monitor);
    }

    public void setInitialTime(double time) {
        simulator.setInitialTime(time);
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

    public void saveOnlyFinalState() {
        collector.saveOnlyFinalState();
    }

    public void saveAllVariables() {
        collector.saveAllVariables();
    }

    public void setVarsToBeSaved(ArrayList<String> varsToSave) {
        collector.setVarsToBeSaved(varsToSave);
    }

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
    
    
    public void setVerbosityLevel(int level) {
        if (level >= this.MAX_VERBOSITY_LEVEL)
            this.verbosityLevel = this.MAX_VERBOSITY_LEVEL;
        if (level<= 0 )
            this.verbosityLevel = 0;
        else
            this.verbosityLevel = level;
        this.setVerbosityOfMonitor(simulator.getProgressMonitor());
    }


    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
        simulator.getProgressMonitor().setPrintStream(outStream);
    }


    public StochasticDataCollector getDataCollector() {
        return collector;
    }
    
    public StochasticDataCollector run() {
        long time = System.nanoTime();
        simulator.initialize();
        collector.clearAll();

        for (int i=0;i<runs;i++) {

            if (this.verbosityLevel == 2) outStream.print("******* RUN " + (i+1) +" ");

            collector.newTrajectory();
            if (this.verbosityLevel == 2) outStream.print("*");
            simulator.resetModel(true);
            if (this.verbosityLevel == 2) outStream.print("*");
            simulator.reinitialize();
            if (this.verbosityLevel == 2) outStream.println("*");
            simulator.run();

            if (this.verbosityLevel == 2) {
                outStream.println("Run " + (i+1) + " completed.");
                outStream.println("Simulation time: " + String.format(Locale.US, "%.4f", simulator.getLastSimulationTimeInSecs()) + " seconds. ");
                outStream.println("Steps of simulation: " + simulator.getLastSimulationSteps());
                outStream.println("Stochastic steps: " + simulator.getLastSimulationStochasticSteps());
                outStream.println("Instantaneous steps: " + simulator.getLastSimulationInstantaneousSteps());
                outStream.println("Timed steps: " + simulator.getLastSimulationTimedSteps());
                outStream.println("Delayed steps: " + simulator.getLastSimulationDelayedSteps());
                outStream.println("Average time per step: " + String.format(Locale.US, "%.4f", simulator.getLastSimulationTimeInMillisecs()/simulator.getLastSimulationSteps()) + " milliseconds ");
                outStream.println();
                outStream.println("Time remaining: " + String.format(Locale.US, "%.4f", (((double)(System.nanoTime() - time)/1000000000)/(i+1))*(runs-i-1) ) + " seconds.");
            } else if (this.verbosityLevel == 1) {
                outStream.println("Run " + (i+1) + " completed in " 
                        + String.format(Locale.US, "%.4f", simulator.getLastSimulationTimeInSecs()) + " seconds. "
                        + "Time remaining: " + String.format(Locale.US, "%.4f", (((double)(System.nanoTime() - time)/1000000000)/(i+1))*(runs-i-1) ) + " seconds.");
            }

        }
        time = System.nanoTime() - time;
        outStream.println("Total simulation time: " + String.format(Locale.US, "%.4f", (double)time/1000000000) + " seconds.");
        return collector;
    }

    /*
     * TO DO
     * Introduce support for multithread execution.
     */


}
