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
public class OdeSimulationManager {
    DeterministicSimulator simulator;
    OdeDataCollector collector;
    double finalTime;
    FlatModel model;
    /**
     * verbosity level:
     * 0: no verbosity, just prints total execution time
     * 1: very verbose. Print data of each simulation.
     * 
     */
    int verbosityLevel = 0;
    final int MAX_VERBOSITY_LEVEL = 1;
    PrintStream outStream = System.out;

    public OdeSimulationManager(FlatModel model, double finalTime) {
        this.finalTime = finalTime;
        this.model = model;
        this.collector = new OdeDataCollector(model);
        collector.setPrintConditionByTime(GlobalOptions.samplePoints, finalTime);
        collector.storeWholeTrajectoryData(1);
        simulator = SimulatorFactory.newODEsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        this.setVerbosityOfMonitor(simulator.getProgressMonitor());
    }


    private void setVerbosityOfMonitor(ProgressMonitor monitor) {
        if (this.verbosityLevel == 0) {
            monitor.setSilent(true);
            monitor.setPrintEvents(true);
        }
        else {
            monitor.setSilent(false);
            monitor.setPrintEvents(false);
        }
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

    
    public void saveAllVariables() {
        collector.saveAllVariables();
    }

    public void useChache(boolean useChache) {
        simulator.useChache(useChache);
    }

    public void setProgressMonitor(ProgressMonitor monitor) {
        simulator.setProgressMonitor(monitor);
    }

    public void setInitialTime(double time) {
        simulator.setInitialTime(time);
    }

    public void setRelativeTolerance(double tol) {
        simulator.setRelativeTolerance(tol);
    }

    public void setMinimumStepSize(double stepSize) {
        simulator.setMinimumStepSize(stepSize);
    }

    public void setMaximumStepSize(double stepSize) {
        simulator.setMaximumStepSize(stepSize);
    }

    public void setMaxStepIncrementForEvents(double maxStepIncrementForEvents) {
        simulator.setMaxStepIncrementForEvents(maxStepIncrementForEvents);
    }

    public void setMaxIterationforEvents(int maxIterationforEvents) {
        simulator.setMaxIterationforEvents(maxIterationforEvents);
    }

    public void setMaxErrorForEvents(double maxErrorForEvents) {
        simulator.setMaxErrorForEvents(maxErrorForEvents);
    }

    public void setIntegrator(IntegratorType integ) {
        simulator.setIntegrator(integ);
    }

    public void setAbsoluteTolerance(double tol) {
        simulator.setAbsoluteTolerance(tol);
    }



    public void setVerbosityLevel(int level) {
        if (level >= this.MAX_VERBOSITY_LEVEL)
            this.verbosityLevel = this.MAX_VERBOSITY_LEVEL;
        else if (level<= 0 )
            this.verbosityLevel = 0;
        else
            this.verbosityLevel = level;
        this.setVerbosityOfMonitor(simulator.getProgressMonitor());
        //there is an issue with event handlers, at the stage in which this function is
        //called, event handlers have not been initialised yet. Moving it in run.
//        if (level >= 1) {
//            simulator.printEvents(true);
//        }
    }
    
    private void setSimulatorPrintEvents() {
        if (this.verbosityLevel >= 1)
            simulator.printEvents(true);
    }


    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
        simulator.getProgressMonitor().setPrintStream(outStream);
    }


    public OdeDataCollector getDataCollector() {
        return collector;
    }

    public OdeDataCollector run() {
        long time = System.nanoTime();
        simulator.resetModel(true);
        simulator.initialize();
        setSimulatorPrintEvents();
        simulator.run();
        time = System.nanoTime() - time;
        outStream.println("Simulation completed in " + String.format(Locale.US, "%.4f", (double)time/1000000000) + " seconds.");
        if (this.verbosityLevel == 1) {
            outStream.println("Integration time " + String.format(Locale.US, "%.4f", simulator.getLastSimulationTimeInSecs()) + " seconds ");
            outStream.println("Steps of simulation: " + simulator.getLastSimulationSteps());
            outStream.println("Instantaneous steps: " + simulator.getLastSimulationNumberOfInstantaneousEvents());
            outStream.println("Timed steps: " + simulator.getLastSimulationNumberOfTimedEvents());
            outStream.println("Average time per step: " + String.format(Locale.US, "%.4f", simulator.getLastSimulationTimeInMillisecs()/simulator.getLastSimulationSteps()) + " milliseconds ");
            outStream.println();
        }
        return collector;
    }
    

}
