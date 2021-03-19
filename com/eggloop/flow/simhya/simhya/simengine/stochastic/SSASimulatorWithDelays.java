/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.stochastic;


import com.eggloop.flow.simhya.simhya.GlobalOptions;
import com.eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
import com.eggloop.flow.simhya.simhya.simengine.SimulationException;
import com.eggloop.flow.simhya.simhya.simengine.utils.*;
import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;

/**
 * @author Luca
 */
public class SSASimulatorWithDelays extends AbstractStochasticSimulator {

    private EventQueue timedEventQueue;
    private EventQueue delayedEventQueue;
    private RateManager rateManager;
    private InstantaneousEventsManager eventManager;
    private boolean forceRateManager;
    private RMType forcedState;


    public SSASimulatorWithDelays(FlatModel model, DataCollector collector) {
        super(model, collector);
        rateManager = null;
        timedEventQueue = null;
        delayedEventQueue = null;
        eventManager = null;
        forceRateManager = false;
        forcedState = RMType.LIST;
    }

    public void forceListRateManager() {
        forceRateManager = true;
        forcedState = RMType.LIST;
    }

    public void forceTreeRateManager() {
        forceRateManager = true;
        forcedState = RMType.TREE;
    }

    public void automaticRateManager() {
        forceRateManager = false;
    }


    @Override
    public void initialize() {
        if (this.initialized)
            return;
        if (model == null || collector == null)
            throw new SimulationException("Model or data collector not defined");
        int[] idList;
        //initialize rate manager, for stochastic transitions
        idList = model.getListOfStochasticTransitionID(true);
        double[] initialRates = new double[model.getNumberOfStochasticTransitions(true)];
        for (int i = 0; i < idList.length; i++) {
            int id = idList[i];
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                initialRates[i] = rates[id].compute();
            else
                initialRates[i] = 0.0;
        }
        if (this.forceRateManager) {
            if (forcedState == RMType.LIST)
                rateManager = new RateList(numberOfTransitions, idList, initialRates);
            else
                rateManager = new RateTree(numberOfTransitions, idList, initialRates);
        } else {
            if (model.getNumberOfTransitions() < GlobalOptions.transitionThresholdForRateTree)
                rateManager = new RateList(numberOfTransitions, idList, initialRates);
            else
                rateManager = new RateTree(numberOfTransitions, idList, initialRates);
        }

        //initializing  timed transitions
        idList = model.getListOfTimedTransitionsID();
        timedEventQueue = new EventQueue(numberOfTransitions);
        for (Integer id : idList) {
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                timedEventQueue.addEvent(id, computeNextFiringTime(id, initialTime));
            else
                timedEventQueue.addEvent(id, Double.POSITIVE_INFINITY);
        }

        //initializing instantaneous transitions
        this.eventManager = new InstantaneousEventsManager(model.getNumberOfInstantaneousTransitions());
        this.eventManager.setInitialSimulationTime(initialTime);
        idList = model.getListOfInstantaneousTransitionsID();
        for (Integer id : idList) {
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                eventManager.addEnabledEvent(id, rates[id].compute());
        }

        //initializing an empty delay event queue
        this.delayedEventQueue = new EventQueue(0);

        super.initialized = true;
    }


    @Override
    public void reinitialize() {
        //recompute initial rates
        int[] idList;
        idList = model.getListOfStochasticTransitionID(true);
        for (int i = 0; i < idList.length; i++) {
            int id = idList[i];
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                rateManager.updateRate(id, rates[id].compute());
            else
                rateManager.updateRate(id, 0.0);
        }
        //reinitializing  timed transitions
        idList = model.getListOfTimedTransitionsID();
        for (Integer id : idList) {
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                timedEventQueue.updateEvent(id, computeNextFiringTime(id, initialTime));
            else
                timedEventQueue.updateEvent(id, Double.POSITIVE_INFINITY);
        }
        //initializing instantaneous transitions
        this.eventManager = new InstantaneousEventsManager(model.getNumberOfInstantaneousTransitions());
        this.eventManager.setInitialSimulationTime(initialTime);
        idList = model.getListOfInstantaneousTransitionsID();
        for (Integer id : idList) {
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                eventManager.addEnabledEvent(id, rates[id].compute());
        }
        //initializing an empty delay event queue
        this.delayedEventQueue = new EventQueue(0);
    }

    /**
     * computes the firing time of a timed transition
     *
     * @param id
     * @param time
     * @return
     */
    private double computeNextFiringTime(int id, double time) {
        double ft = firingTimes[id].compute();
        return (ft < time ? Double.POSITIVE_INFINITY : ft);
    }

    /**
     * computes the firing time of a timed transition
     *
     * @param id
     * @param time
     * @return
     */
    private double computeNextFiringTimeCache(int id, double time) {
        double ft = firingTimes[id].computeCache();
        return (ft < time ? Double.POSITIVE_INFINITY : ft);
    }

    @Override
    public void reset(DataCollector dc) {
        super.collector = dc;
        super.resetModel(true);
        rateManager = null;
        timedEventQueue = null;
        delayedEventQueue = null;
        eventManager = null;
        forceRateManager = false;
        forcedState = RMType.LIST;
        super.initialized = false;
    }


    @Override
    void selectNextTransition() {
        double nextTime, nextEventTime, nextDelayedEventTime;

        if (this.eventManager.areInstantaneousEventsEnabled()) {
            lastSimulationInstantaneousSteps++;
            //fire an instantaneous transition
            next.nextTime = currentTime;
            next.nextTransition = eventManager.getNextEvent(rand, currentTime);
            next.isDelayed = false;
            eventManager.removeEvent(next.nextTransition);
            if (stopping[next.nextTransition])
                next.stop = true;
            return;
        }

        double exitRate = rateManager.getExitRate();
        if (exitRate < GlobalOptions.TOLERANCE) {
            next.deadlock = true;
            return;
        }
        nextTime = currentTime + RandomGenerator.expDist(exitRate, rand);
        nextEventTime = timedEventQueue.getNextFiringTime();
        nextDelayedEventTime = delayedEventQueue.getNextFiringTime();
        if (nextDelayedEventTime <= nextEventTime && nextDelayedEventTime <= nextTime) {
            //firing a delayed event!!!
            next.nextTime = nextDelayedEventTime;
            next.isDelayed = true;
            next.nextTransition = delayedEventQueue.extractFiringEvent();
            this.lastSimulationDelayedSteps++;
            if (stoppingAfterDelay[next.nextTransition])
                next.stop = true;
            return;
        } else if (nextEventTime <= nextTime) {
            next.nextTime = nextEventTime;
            //this is not a delayed event!
            next.isDelayed = false;
            next.nextTransition = timedEventQueue.extractFiringEvent(Double.POSITIVE_INFINITY);
            this.lastSimulationTimedSteps++;
            if (stopping[next.nextTransition])
                next.stop = true;
            return;
        } else {
            this.lastSimulationStochasticSteps++;
            next.nextTime = nextTime;
            next.isDelayed = false;
            next.nextTransition = rateManager.sampleNextTransition(rand);
            if (stopping[next.nextTransition])
                next.stop = true;
            return;
        }
    }

    @Override
    void executeNextTransition() {
        if (next.isDelayed)
            delayedExecute(next.nextTransition);
        else {
            if (isDelayed[next.nextTransition]) {
                double delayedTime = currentTime + delays[next.nextTransition].compute();
                this.delayedEventQueue.addDelayedEvent(next.nextTransition, delayedTime);
            }
            execute(next.nextTransition);
        }
    }

    @Override
    void updateModel() {
        Integer[] guardList, rateList, firingTimeList;
        if (next.isDelayed) {
            guardList = this.model.getListOfUpdatedGuardsAfterDelay(next.nextTransition);
            rateList = this.model.getListOfUpdatedRatesAfterDelay(next.nextTransition);
            firingTimeList = this.model.getListOfUpdatedFiringTimesAfterDelay(next.nextTransition);
        } else {
            guardList = this.model.getListOfUpdatedGuards(next.nextTransition);
            rateList = this.model.getListOfUpdatedRates(next.nextTransition);
            firingTimeList = this.model.getListOfUpdatedFiringTimes(next.nextTransition);
        }

        //update guards
        if (GlobalOptions.alwaysComputeRateAfterGuard) {
            for (Integer j : guardList) {
                currentGuardStatus[j] = guards[j].evaluate();
                switch (types[j]) {
                    case STOCHASTIC:
                        if (currentGuardStatus[j])
                            rateManager.updateRate(j, rates[j].compute());
                        else
                            rateManager.updateRate(j, 0.0);
                        break;
                    case INSTANTANEOUS:
                        if (currentGuardStatus[j])
                            this.eventManager.addEnabledEvent(j, rates[j].compute());
                        else
                            this.eventManager.removeEvent(j);
                        break;
                    case TIMED:
                        if (currentGuardStatus[j])
                            timedEventQueue.updateEvent(j, computeNextFiringTime(j, currentTime));
                        else
                            timedEventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
                        break;
                    default:
                        throw new SimulationException("Incompatible transition type");
                }
            }
        } else {
            for (Integer j : guardList) {
                boolean oldStatus = currentGuardStatus[j];
                currentGuardStatus[j] = guards[j].evaluate();
                if (oldStatus != currentGuardStatus[j]) {
                    switch (types[j]) {
                        case STOCHASTIC:
                            if (!oldStatus)
                                rateManager.updateRate(j, rates[j].compute());
                            else
                                rateManager.updateRate(j, 0.0);
                            break;
                        case INSTANTANEOUS:
                            if (!oldStatus)
                                this.eventManager.addEnabledEvent(j, rates[j].compute());
                            else
                                this.eventManager.removeEvent(j);
                            break;
                        case TIMED:
                            if (!oldStatus)
                                timedEventQueue.updateEvent(j, computeNextFiringTime(j, currentTime));
                            else
                                timedEventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
                            break;
                        default:
                            throw new SimulationException("Incompatible transition type");
                    }
                }
            }
        }
        for (Integer j : rateList) {
            switch (types[j]) {
                case STOCHASTIC:
                    if (currentGuardStatus[j])
                        rateManager.updateRate(j, rates[j].compute());
                    break;
                case INSTANTANEOUS:
                    if (currentGuardStatus[j])
                        eventManager.updatePriority(j, rates[j].compute());
                    break;
                case TIMED:
                    throw new SimulationException("Trying to compute rate for a timed transitions, that has no rate.");
                default:
                    throw new SimulationException("Incompatible transition type");
            }
        }
        for (Integer j : firingTimeList)
            timedEventQueue.updateEvent(j, computeNextFiringTime(j, currentTime));
    }

    @Override
    void executeNextTransitionWithCache() {
        if (next.isDelayed)
            delayedExecuteCache(next.nextTransition);
        else {
            if (isDelayed[next.nextTransition]) {
                double delayedTime = currentTime + delays[next.nextTransition].computeCache();
                this.delayedEventQueue.addDelayedEvent(next.nextTransition, delayedTime);
            }
            executeCache(next.nextTransition);
        }
    }

    @Override
    void updateModelWithCache() {
        Integer[] guardList, rateList, firingTimeList;
        if (next.isDelayed) {
            guardList = this.model.getListOfUpdatedGuardsAfterDelay(next.nextTransition);
            rateList = this.model.getListOfUpdatedRatesAfterDelay(next.nextTransition);
            firingTimeList = this.model.getListOfUpdatedFiringTimesAfterDelay(next.nextTransition);
        } else {
            guardList = this.model.getListOfUpdatedGuards(next.nextTransition);
            rateList = this.model.getListOfUpdatedRates(next.nextTransition);
            firingTimeList = this.model.getListOfUpdatedFiringTimes(next.nextTransition);
        }

        //new evaluation round
        model.getStore().newEvaluationRound();
        //update guards
        if (GlobalOptions.alwaysComputeRateAfterGuard) {
            for (Integer j : guardList) {
                currentGuardStatus[j] = guards[j].evaluateCache();
                switch (types[j]) {
                    case STOCHASTIC:
                        if (currentGuardStatus[j])
                            rateManager.updateRate(j, rates[j].computeCache());
                        else
                            rateManager.updateRate(j, 0.0);
                        break;
                    case INSTANTANEOUS:
                        if (currentGuardStatus[j])
                            this.eventManager.addEnabledEvent(j, rates[j].computeCache());
                        else
                            this.eventManager.removeEvent(j);
                        break;
                    case TIMED:
                        if (currentGuardStatus[j])
                            timedEventQueue.updateEvent(j, computeNextFiringTimeCache(j, currentTime));
                        else
                            timedEventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
                        break;
                    default:
                        throw new SimulationException("Incompatible transition type");
                }
            }
        } else {
            for (Integer j : guardList) {
                boolean oldStatus = currentGuardStatus[j];
                currentGuardStatus[j] = guards[j].evaluateCache();
                if (oldStatus != currentGuardStatus[j]) {
                    switch (types[j]) {
                        case STOCHASTIC:
                            if (!oldStatus)
                                rateManager.updateRate(j, rates[j].computeCache());
                            else
                                rateManager.updateRate(j, 0.0);
                            break;
                        case INSTANTANEOUS:
                            if (!oldStatus)
                                this.eventManager.addEnabledEvent(j, rates[j].computeCache());
                            else
                                this.eventManager.removeEvent(j);
                            break;
                        case TIMED:
                            if (!oldStatus)
                                timedEventQueue.updateEvent(j, computeNextFiringTimeCache(j, currentTime));
                            else
                                timedEventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
                            break;
                        default:
                            throw new SimulationException("Incompatible transition type");
                    }
                }
            }
        }
        for (Integer j : rateList) {
            switch (types[j]) {
                case STOCHASTIC:
                    if (currentGuardStatus[j])
                        rateManager.updateRate(j, rates[j].computeCache());
                    break;
                case INSTANTANEOUS:
                    if (currentGuardStatus[j])
                        eventManager.updatePriority(j, rates[j].computeCache());
                    break;
                case TIMED:
                    throw new SimulationException("Trying to compute rate for a timed transitions, that has no rate.");
                default:
                    throw new SimulationException("Incompatible transition type");
            }
        }
        for (Integer j : firingTimeList)
            timedEventQueue.updateEvent(j, computeNextFiringTimeCache(j, currentTime));
    }

}
