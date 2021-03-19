/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.ode;


import cern.jet.random.engine.RandomEngine;
import com.eggloop.flow.simhya.simhya.GlobalOptions;
import com.eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import com.eggloop.flow.simhya.simhya.dataprocessing.OdeDataCollector;
import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
import com.eggloop.flow.simhya.simhya.simengine.DeterministicSimulator;
import com.eggloop.flow.simhya.simhya.simengine.ProgressMonitor;
import com.eggloop.flow.simhya.simhya.simengine.SimulationException;
import com.eggloop.flow.simhya.simhya.simengine.utils.PrintStreamProgressMonitor;
import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderIntegrator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;

/**
 *
 * @author luca
 */
public abstract class AbstractOdeSimulator implements DeterministicSimulator, StepHandler {
    FlatModel model;
    OdeDataCollector collector;
    OdeFunction function;
    
    long numberOfInstantaneousEvents;
    long numberOfTimedEvents;
    long numberOfSteps;

    ProgressMonitor monitor;

    double [] variables;

    boolean useCache;
    boolean initialized;

    //simulation parameters.
    double initialTime;
    double finalTime;
    double absoluteTolerance;
    double relativeTolerance;
    double minStepSize;
    double maxStepSize;
    double maxStepIncrementForEvents;
    double maxErrorForEvents;
    int maxIterationforEvents;

    IntegratorType integratorType;
    RandomEngine rand;

    FirstOrderIntegrator integrator;

    

    public AbstractOdeSimulator(FlatModel model, OdeDataCollector collector) {
        this.model = model;
        this.collector = collector;
        this.variables = model.getStore().getVariablesReference().getReferenceToValuesArray();

        monitor = new PrintStreamProgressMonitor(System.out);
        rand = RandomGenerator.getNewRandomGenerator();

        this.absoluteTolerance = GlobalOptions.defaultAbsoluteTolerance;
        this.relativeTolerance = GlobalOptions.defaultRelativeTolerance;
        this.minStepSize = GlobalOptions.defaultMinStepSize;
        this.maxStepSize = GlobalOptions.defaultMaxStepSize;
        this.maxStepIncrementForEvents = GlobalOptions.defaultMaxStepIncrementForEvents;
        this.maxErrorForEvents = GlobalOptions.defaultMaxErrorForEvents;
        this.maxIterationforEvents = GlobalOptions.defaultMaxIterationforEvents;
        this.integratorType = GlobalOptions.defaultIntegratorType;
        this.initialized = false;
    }


    public void handleStep(StepInterpolator interpolator, boolean isLast) throws DerivativeException {
        collector.setInterpolator(interpolator);
        numberOfSteps++;
        double time = interpolator.getCurrentTime();
        if (isLast)
            collector.putFinalStateOde(time);
        else if (collector.dataNeeded(time,numberOfSteps))
            collector.putDataOde(time);
        monitor.setProgress(time);
    }

    public boolean requiresDenseOutput() {
        return true;
    }

    public void reset() {
        //tells te data collector to store a new trajectory. It must be the only one,
        //as the system is deterministic? It is not clear, hence I do not clear all
        //trajectories...
        //collector.clearAllTrajectories();
        collector.newTrajectory();
    }

    public ProgressMonitor getProgressMonitor() {
        return this.monitor;
    }


    public long getLastSimulationNumberOfInstantaneousEvents() {
        return this.numberOfInstantaneousEvents;
    }

    public long getLastSimulationNumberOfTimedEvents() {
        return this.numberOfTimedEvents;
    }

    public long getLastSimulationSteps() {
        return this.numberOfSteps;
    }

    public double getLastSimulationTimeInMillisecs() {
        return monitor.getLastSimulationTimeInMillisecs();
    }

    public double getLastSimulationTimeInSecs() {
        return monitor.getLastSimulationTimeInSecs();
    }

    public void reset(DataCollector dc) {
        if (!(dc instanceof OdeDataCollector))
            throw new IllegalArgumentException("Requires a OdeDataCollector");
        this.collector = (OdeDataCollector)dc;
        this.resetModel(true);

        rand = RandomGenerator.getNewRandomGenerator();

        this.absoluteTolerance = GlobalOptions.defaultAbsoluteTolerance;
        this.relativeTolerance = GlobalOptions.defaultRelativeTolerance;
        this.minStepSize = GlobalOptions.defaultMinStepSize;
        this.maxStepSize = GlobalOptions.defaultMaxStepSize;
        this.maxStepIncrementForEvents = GlobalOptions.defaultMaxStepIncrementForEvents;
        this.maxErrorForEvents = GlobalOptions.defaultMaxErrorForEvents;
        this.maxIterationforEvents = GlobalOptions.defaultMaxIterationforEvents;
        this.integratorType = GlobalOptions.defaultIntegratorType;
        this.initialized = false;
    }




    public void resetModel(boolean resetToOriginalValues) {
        if (resetToOriginalValues)
            model.resetToInitialState();
        variables = model.getStore().getVariablesReference().getReferenceToValuesArray();
    }

    public void run() {
        if (initialized) {
            //pass monitor to collector
            monitor.setInitialTime(initialTime);
            monitor.setFinalTime(finalTime);
            double[] y = new double[variables.length];
            function.setUseCache(useCache);
            monitor.start();
            try {
            integrator.integrate(function, initialTime, variables, finalTime, y);
            monitor.stop();
            }
            catch (org.apache.commons.math.ode.DerivativeException e) {
                throw new SimulationException("Error while " +
                        "computing derivatives: " + e );
            }
            catch (org.apache.commons.math.ode.IntegratorException e) {
                throw new SimulationException("Error while " +
                        "integrating ode: " + e );
            }
        }
    }

    public void setFinalTime(double time) {
        this.finalTime = time;
    }

    public void setInitialTime(double time) {
        this.initialTime = time;
    }

    public void setProgressMonitor(ProgressMonitor monitor) {
        if (monitor != null)
            this.monitor = monitor;
    }

    public void useChache(boolean useCache) {
        this.useCache = useCache;
    }

    public void setAbsoluteTolerance(double tol) {
        this.absoluteTolerance = tol;
    }

    public void setIntegrator(IntegratorType integ) {
        this.integratorType = integ;
    }

    public void setMaximumStepSize(double stepSize) {
        if (stepSize >= this.minStepSize)
            this.maxStepSize = stepSize;
        else {
            this.maxStepSize = stepSize;
            this.minStepSize = stepSize;
        }
    }

    public void setMinimumStepSize(double stepSize) {
         if (stepSize <= this.maxStepSize)
            this.minStepSize = stepSize;
        else {
            this.maxStepSize = stepSize;
            this.minStepSize = stepSize;
        }
    }

    public void setRelativeTolerance(double tol) {
        this.relativeTolerance = tol;
    }

    public void setMaxErrorForEvents(double maxErrorForEvents) {
        this.maxErrorForEvents = maxErrorForEvents;
    }

    public void setMaxIterationforEvents(int maxIterationforEvents) {
        this.maxIterationforEvents = maxIterationforEvents;
    }

    public void setMaxStepIncrementForEvents(double maxStepIncrementForEvents) {
        this.maxStepIncrementForEvents = maxStepIncrementForEvents;
    }

    
    

    FirstOrderIntegrator newIntegrator() {
        switch(this.integratorType) {
            case DP85:
                return new org.apache.commons.math.ode.nonstiff.DormandPrince853Integrator(minStepSize, maxStepSize, absoluteTolerance, relativeTolerance);
            case RK4:
                return new org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegrator(minStepSize);
            case EULER:
                return new org.apache.commons.math.ode.nonstiff.EulerIntegrator(minStepSize);
        }
        return null;
    }

    
       /**
     * Activate or deactivates the mechanism to print events.
     * @param print 
     */
    public void printEvents(boolean print) {
     
    }


}
