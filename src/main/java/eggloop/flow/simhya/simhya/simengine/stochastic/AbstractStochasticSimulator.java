/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine.stochastic;


import cern.jet.random.engine.RandomEngine;
import eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.model.flat.FlatModel;
import eggloop.flow.simhya.simhya.model.store.Function;
import eggloop.flow.simhya.simhya.model.store.Predicate;
import eggloop.flow.simhya.simhya.model.transition.AtomicReset;
import eggloop.flow.simhya.simhya.model.transition.TType;
import eggloop.flow.simhya.simhya.simengine.ProgressMonitor;
import eggloop.flow.simhya.simhya.simengine.StochasticSimulator;
import eggloop.flow.simhya.simhya.simengine.utils.PrintStreamProgressMonitor;
import eggloop.flow.simhya.simhya.utils.RandomGenerator;

/**
 * @author Luca
 */
public abstract class AbstractStochasticSimulator implements StochasticSimulator {
    DataCollector collector;
    FlatModel model;
    boolean initialized;

    int numberOfTransitions;
    SymbolArray variablesArray;
    SymbolArray parametersArray;
    double[] variables;
    double[] parameters;

    String[] events;
    TType[] types;
    Predicate[] guards;
    Function[] rates;
    AtomicReset[][] resets;
    Function[] firingTimes;
    Function[] delays;
    Predicate[] delayedGuards;
    AtomicReset[][] delayedResets;
    boolean[] stopping;
    boolean[] stoppingAfterDelay;
    boolean[] isDelayed;

    double initialTime;
    double currentTime;
    double finalTime;

    boolean[] currentGuardStatus;

    boolean useCache;

    ProgressMonitor monitor;
    double lastSimulationTimeSec;
    double lastSimulationTimeMillisec;
    long lastSimulationStochasticSteps;
    long lastSimulationInstantaneousSteps;
    long lastSimulationTimedSteps;
    long lastSimulationDelayedSteps;
    long lastSimulationSteps;

    RandomEngine rand;
    NextEvent next;

    public AbstractStochasticSimulator(FlatModel model, DataCollector collector) {
        this.collector = collector;
        this.model = model;

        this.initialized = false;
        this.initialTime = 0;
        this.finalTime = 0;
        this.currentTime = 0;
        rand = RandomGenerator.getNewRandomGenerator();
        this.lastSimulationTimeSec = 0;
        this.lastSimulationTimeMillisec = 0;
        this.lastSimulationStochasticSteps = 0;
        this.lastSimulationInstantaneousSteps = 0;
        this.lastSimulationTimedSteps = 0;
        this.lastSimulationDelayedSteps = 0;
        next = null;
        useCache = false;

        this.monitor = new PrintStreamProgressMonitor(System.out);

        this.initModelInfo();

    }

    private void initModelInfo() {
        model.getStore().newEvaluationRound();

        variablesArray = (SymbolArray) model.getStore().getVariablesReference();
        parametersArray = (SymbolArray) model.getStore().getParametersReference();
        variables = variablesArray.getReferenceToValuesArray();
        parameters = parametersArray.getReferenceToValuesArray();

        numberOfTransitions = model.getNumberOfTransitions();
        events = model.getTransitionEvents();
        types = model.getTransitionTypes();
        guards = model.getTransitionGuards();
        rates = model.getTransitionRates();
        resets = model.getTransitionResets();
        firingTimes = model.getTransitionTimedActivations();
        delays = model.getTransitionDelays();
        delayedGuards = model.getTransitionDelayedGuards();
        delayedResets = model.getTransitionDelayedResets();
        stopping = model.getTransitionStoppingStatus();
        stoppingAfterDelay = model.getTransitionStoppingStatusAfterDelay();
        currentGuardStatus = new boolean[numberOfTransitions];
        isDelayed = new boolean[numberOfTransitions];
        for (int i = 0; i < numberOfTransitions; i++)
            if (delays[i] != null) isDelayed[i] = true;
            else isDelayed[i] = false;
    }

    public void useChache(boolean useCache) {
        this.useCache = useCache;
    }

    public void setInitialTime(double time) {
//        if (this.initialized)
//            throw new SimulationException("Cannot change initial time: simualtor already initialized");
        this.initialTime = time;
    }

    public void setFinalTime(double time) {
//        if (this.initialized)
//            throw new SimulationException("Cannot change final time: simualtor already initialized");
        this.finalTime = time;
    }


    /**
     * resets the model and the variables/parameters to the initial state.
     */
    public void resetModel(boolean resetToOriginalParameterValues) {
        if (resetToOriginalParameterValues)
            model.resetToInitialState();
        variablesArray = (SymbolArray) model.getStore().getVariablesReference();
        parametersArray = (SymbolArray) model.getStore().getParametersReference();
        variables = variablesArray.getReferenceToValuesArray();
        parameters = parametersArray.getReferenceToValuesArray();
    }


    /**
     * applies the reset of transition id
     *
     * @param id
     */
    void execute(int id) {
        for (int j = 0; j < resets[id].length; j++)
            resets[id][j].computeNewValues();
        for (int j = 0; j < resets[id].length; j++)
            resets[id][j].updateStoreVariables(variables);
//        for (int j=0;j<resets[id].length;j++)
//            this.variablesArray.setValue(resets[id][j].variable, resets[id][j].newValue);
    }


    /**
     * applies the delayed reset of transition id, only if guard is true
     *
     * @param id
     */
    void delayedExecute(int id) {
        if (delayedGuards[id].evaluate()) {
            for (int j = 0; j < delayedResets[id].length; j++)
                delayedResets[id][j].computeNewValues();
            for (int j = 0; j < delayedResets[id].length; j++)
                delayedResets[id][j].updateStoreVariables(variables);
//            for (int j=0;j<delayedResets[id].length;j++)
//                this.variablesArray.setValue(delayedResets[id][j].variable, delayedResets[id][j].newValue);
        }
    }

    /**
     * applies the reset of transition id
     *
     * @param id
     */
    void executeCache(int id) {
        for (int j = 0; j < resets[id].length; j++)
            resets[id][j].computeNewValuesCache();
        for (int j = 0; j < resets[id].length; j++)
            resets[id][j].updateStoreVariables(variables);
//        for (int j=0;j<resets[id].length;j++)
//            this.variablesArray.setValue(resets[id][j].variable, resets[id][j].newValue);
    }


    /**
     * applies the delayed reset of transition id, only if guard is true
     *
     * @param id
     */
    void delayedExecuteCache(int id) {
        if (delayedGuards[id].evaluate()) {
            for (int j = 0; j < delayedResets[id].length; j++)
                delayedResets[id][j].computeNewValuesCache();
            for (int j = 0; j < delayedResets[id].length; j++)
                delayedResets[id][j].updateStoreVariables(variables);
//            for (int j=0;j<delayedResets[id].length;j++)
//                this.variablesArray.setValue(delayedResets[id][j].variable, delayedResets[id][j].newValue);
        }
    }

    public ProgressMonitor getProgressMonitor() {
        return this.monitor;
    }


    public void run() {
        next = new NextEvent();
        this.lastSimulationStochasticSteps = 0;
        this.lastSimulationInstantaneousSteps = 0;
        this.lastSimulationTimedSteps = 0;
        this.lastSimulationDelayedSteps = 0;
        this.lastSimulationSteps = 0;
        monitor.setInitialTime(initialTime);
        monitor.setFinalTime(finalTime);

        //Debugger.put("Simulation started");
        currentTime = this.initialTime;
        //collector.putInitialState(currentTime);
        monitor.start();


        if (useCache)
            runWithCache();
        else
            runWithoutCache();


        monitor.stop();
        this.lastSimulationTimeSec = monitor.getLastSimulationTimeInSecs();
        this.lastSimulationTimeMillisec = monitor.getLastSimulationTimeInMillisecs();

    }

    private void runWithoutCache() {

        while (true) {
            monitor.setProgress(currentTime);
            //System.out.println("#prog " + lastSimulationSteps);
            //identify next transition
            //System.out.println("#state " + lastSimulationSteps + ": " + java.util.Arrays.toString(variables));
            selectNextTransition();
            //System.out.println("#next " + lastSimulationSteps + " next.time " + next.nextTime);
            //print + check termination
            if (next.deadlock) {
                collector.putFinalState(currentTime, lastSimulationSteps);
                break;
            }
            //termination by end time reached
            if (next.nextTime > this.finalTime) {
                //System.out.println("#end " + lastSimulationSteps);
                if (collector.dataNeeded(this.finalTime, lastSimulationSteps))
                    collector.putData(this.finalTime);
                collector.putFinalState(finalTime, lastSimulationSteps);
                //System.out.println("#end " + lastSimulationSteps);
                break;
            }
            if (collector.dataNeeded(next.nextTime, lastSimulationSteps)) {
                //System.out.println("#data " + lastSimulationSteps);
                collector.putData(next.nextTime);
                //System.out.println("#data " + lastSimulationSteps);
            }
            //update time
            currentTime = next.nextTime;
            //System.out.println("#next " + lastSimulationSteps);
            //update model
            executeNextTransition();
            //System.out.println("#exec " + lastSimulationSteps);
            updateModel();
            //System.out.println("#update " + lastSimulationSteps);
            //termination by forced stop
            if (next.stop) {
                //System.out.println("#stop " + lastSimulationSteps);
                collector.putFinalState(currentTime, lastSimulationSteps);
                //System.out.println("#stop " + lastSimulationSteps);
                break;
            }
            this.lastSimulationSteps++;
        }
    }

    private void runWithCache() {
        while (true) {
            monitor.setProgress(currentTime);
            //identify next transition
            selectNextTransition();
            //print + check termination
            if (next.deadlock) {
                collector.putFinalState(currentTime, lastSimulationSteps);
                break;
            }
            //termination by end time reached
            if (next.nextTime > this.finalTime) {
                if (collector.dataNeeded(this.finalTime, lastSimulationSteps))
                    collector.putData(this.finalTime);
                collector.putFinalState(finalTime, lastSimulationSteps);
                break;
            }
            if (collector.dataNeeded(next.nextTime, lastSimulationSteps)) {
                collector.putData(next.nextTime);
            }
            //update time
            currentTime = next.nextTime;
            //update model
            executeNextTransitionWithCache();
            updateModelWithCache();
            //termination by forced stop
            if (next.stop) {
                collector.putFinalState(currentTime, lastSimulationSteps);
                break;
            }
            this.lastSimulationSteps++;
        }
    }


    public abstract void initialize();

    public abstract void reinitialize();

    public abstract void reset(DataCollector dc);

    abstract void selectNextTransition();

    abstract void executeNextTransition();

    abstract void updateModel();

    abstract void executeNextTransitionWithCache();

    abstract void updateModelWithCache();


    public void setProgressMonitor(ProgressMonitor monitor) {
        this.monitor = monitor;
    }

    public double getLastSimulationTimeInSecs() {
        return this.lastSimulationTimeSec;
    }

    public double getLastSimulationTimeInMillisecs() {
        return this.lastSimulationTimeMillisec;
    }

    public long getLastSimulationSteps() {
        return this.lastSimulationSteps;
    }

    public long getLastSimulationDelayedSteps() {
        return lastSimulationDelayedSteps;
    }

    public long getLastSimulationInstantaneousSteps() {
        return lastSimulationInstantaneousSteps;
    }

    public long getLastSimulationStochasticSteps() {
        return lastSimulationStochasticSteps;
    }

    public long getLastSimulationTimedSteps() {
        return lastSimulationTimedSteps;
    }
}
