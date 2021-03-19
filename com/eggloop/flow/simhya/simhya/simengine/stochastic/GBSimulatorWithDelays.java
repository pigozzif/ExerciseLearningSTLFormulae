/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.stochastic;

import com.eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
import com.eggloop.flow.simhya.simhya.model.transition.TType;
import com.eggloop.flow.simhya.simhya.simengine.SimulationException;
import com.eggloop.flow.simhya.simhya.simengine.utils.EventQueue;
import com.eggloop.flow.simhya.simhya.simengine.utils.InstantaneousEventsManager;
import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;

/**
 * @author luca
 */
public class GBSimulatorWithDelays extends AbstractStochasticSimulator {
    private EventQueue eventQueue;
    private EventQueue delayedEventQueue;
    private InstantaneousEventsManager eventManager;
    private double[] nextStocEventTime;
    private double[] currentRate;

    /**
     * This constant is used to compare a double quantity to zero.
     */
    private final double ZERO = 1e-15;
    /**
     * When an entry in this vector is true, then the next firing time
     * of the transition is infinity, becuse its rate is zero.
     * In order to avoid strange behaviours, we check explicitly this.
     */
    private boolean[] infinity;


    public GBSimulatorWithDelays(FlatModel model, DataCollector collector) {
        super(model, collector);
        eventQueue = null;
        eventManager = null;
        nextStocEventTime = null;
        currentRate = null;
        delayedEventQueue = null;
    }

    @Override
    public void initialize() {
        if (this.initialized)
            return;
        if (model == null || collector == null)
            throw new SimulationException("Model or data collector not defined");
        model.recomputeDependencyGraphs(false);

        int[] idList;
        eventQueue = new EventQueue(model.getNumberOfStochasticTransitions(true) +
                model.getNumberOfTimedTransitions());

        nextStocEventTime = new double[model.getNumberOfTransitions()];
        currentRate = new double[model.getNumberOfTransitions()];
        infinity = new boolean[model.getNumberOfTransitions()];
        java.util.Arrays.fill(infinity, false);
        java.util.Arrays.fill(currentRate, 0.0);
        java.util.Arrays.fill(this.nextStocEventTime, Double.POSITIVE_INFINITY);

        idList = model.getListOfStochasticTransitionID(true);
        for (int i = 0; i < idList.length; i++) {
            int id = idList[i];
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id]) {
                currentRate[id] = rates[id].compute();
                if (currentRate[id] <= ZERO)
                    infinity[id] = true;
                else
                    nextStocEventTime[id] = initialTime + RandomGenerator.expDist(currentRate[id], rand);
            }
            eventQueue.addEvent(id, nextStocEventTime[id]);
        }
        //initializing  timed transitions
        idList = model.getListOfTimedTransitionsID();
        for (Integer id : idList) {
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                eventQueue.addEvent(id, computeNextFiringTime(id, initialTime));
            else
                eventQueue.addEvent(id, Double.POSITIVE_INFINITY);
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
        int[] idList;
        eventQueue = new EventQueue(model.getNumberOfStochasticTransitions(true) +
                model.getNumberOfTimedTransitions());
        this.nextStocEventTime = new double[model.getNumberOfTransitions()];
        currentRate = new double[model.getNumberOfTransitions()];
        infinity = new boolean[model.getNumberOfTransitions()];
        java.util.Arrays.fill(infinity, false);
        java.util.Arrays.fill(currentRate, 0.0);
        java.util.Arrays.fill(this.nextStocEventTime, Double.POSITIVE_INFINITY);

        idList = model.getListOfStochasticTransitionID(true);
        for (int i = 0; i < idList.length; i++) {
            int id = idList[i];

//            System.out.println("Reinitializing transition " + id);

            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id]) {
                currentRate[id] = rates[id].compute();
//                System.out.print("Rate is " + currentRate[id]);
                if (currentRate[id] <= ZERO) {
                    infinity[id] = true;
//                    System.out.println("...infty");
                } else {
                    nextStocEventTime[id] = initialTime + RandomGenerator.expDist(currentRate[id], rand);
//                    System.out.println("..." + nextStocEventTime[id]);
                }
            }
            eventQueue.addEvent(id, nextStocEventTime[id]);
        }
        //initializing  timed transitions
        idList = model.getListOfTimedTransitionsID();
        for (Integer id : idList) {
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                eventQueue.addEvent(id, computeNextFiringTime(id, initialTime));
            else
                eventQueue.addEvent(id, Double.POSITIVE_INFINITY);
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

    @Override
    public void reset(DataCollector dc) {
        super.collector = dc;
        super.resetModel(true);
        eventQueue = null;
        eventManager = null;
        currentRate = null;
        delayedEventQueue = null;
        this.nextStocEventTime = null;
        super.initialized = false;
    }


    @Override
    void selectNextTransition() {
        double nextEventTime, nextDelayedEventTime;
        if (this.eventManager.areInstantaneousEventsEnabled()) {
            lastSimulationInstantaneousSteps++;
            //fire an instantaneous transition
            next.isDelayed = false;
            next.nextTime = currentTime;
            next.nextTransition = eventManager.getNextEvent(rand, currentTime);
            if (stopping[next.nextTransition])
                next.stop = true;
            next.isStochastic = false;
            return;
        }
        nextEventTime = eventQueue.getNextFiringTime();
        nextDelayedEventTime = delayedEventQueue.getNextFiringTime();
        if (nextDelayedEventTime <= nextEventTime) {
            //firing a delayed event!!!
            next.nextTime = nextDelayedEventTime;
            next.isDelayed = true;
            next.nextTransition = delayedEventQueue.extractFiringEvent();
            this.lastSimulationDelayedSteps++;
            if (stoppingAfterDelay[next.nextTransition])
                next.stop = true;
            return;
        } else {
            next.nextTime = nextEventTime;
            next.nextTransition = eventQueue.extractFiringEvent(Double.POSITIVE_INFINITY);
            next.isDelayed = false;
            if (super.types[next.nextTransition] == TType.TIMED) {
                this.lastSimulationTimedSteps++;
                next.isStochastic = false;
            } else {
                next.isStochastic = true;
                this.lastSimulationStochasticSteps++;
            }
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
        for (Integer j : guardList) {
            boolean oldStatus = currentGuardStatus[j];
            currentGuardStatus[j] = guards[j].evaluate();
            if (oldStatus != currentGuardStatus[j]) {
                switch (types[j]) {
                    case STOCHASTIC:
                        if (j != next.nextTransition) {
                            if (!oldStatus) {
                                //if the guard becomes active again, one has to
                                //resample the firing time!!!
                                currentRate[j] = rates[j].compute();
                                if (currentRate[j] <= ZERO) {
                                    infinity[j] = true;
                                    nextStocEventTime[j] = Double.POSITIVE_INFINITY;
                                } else
                                    nextStocEventTime[j] = currentTime +
                                            RandomGenerator.expDist(currentRate[j], rand);
                                eventQueue.updateEvent(j, nextStocEventTime[j]);
                            } else {
                                eventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
                                this.currentRate[j] = 0.0;
                                this.nextStocEventTime[j] = Double.POSITIVE_INFINITY;
                                infinity[j] = false; //infinity is used only for transitions
                                //with true guards
                            }
                        }
                        break;
                    case INSTANTANEOUS:
                        if (!oldStatus)
                            this.eventManager.addEnabledEvent(j, rates[j].compute());
                        else
                            this.eventManager.removeEvent(j);
                        break;
                    case TIMED:
                        if (!oldStatus)
                            eventQueue.updateEvent(j, computeNextFiringTime(j, currentTime));
                        else
                            eventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
                        break;
                    default:
                        throw new SimulationException("Incompatible transition type");
                }
            }
        }

//        System.out.println("Simulation step " + super.lastSimulationSteps);
//        System.out.println("Fired transition " + next.nextTransition);
//        System.out.println("Rate list is " + java.util.Arrays.toString(rateList));
        for (Integer j : rateList) {
            switch (types[j]) {
                case STOCHASTIC:
                    if (currentGuardStatus[j]) {
                        double r = rates[j].compute();
                        //System.out.println("Computing rate of transition " + j + ": " + r);
                        if (j != next.nextTransition) {
                            double t;
                            if (r <= ZERO) {
                                //System.out.print("0");
                                infinity[j] = true;
                                t = Double.POSITIVE_INFINITY;
                            } else if (infinity[j]) {
                                //System.out.print("*");
                                t = currentTime +
                                        RandomGenerator.expDist(r, rand);
                                infinity[j] = false;
                            } else {
                                //System.out.print("+");
                                t = currentTime +
                                        (currentRate[j] / r) * (this.nextStocEventTime[j] - currentTime);
                            }
                            eventQueue.updateEvent(j, t);
                            this.nextStocEventTime[j] = t;
                            //System.out.println("Next firing time of transition " + j + ": " + t);
                        }
                        this.currentRate[j] = r;
                    }
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
            eventQueue.updateEvent(j, computeNextFiringTime(j, currentTime));

        //compute next time for fired transition, if transition is stochastic.
        if (next.isStochastic) {
            if (currentGuardStatus[next.nextTransition]) {
                if (currentRate[next.nextTransition] <= ZERO) {
                    infinity[next.nextTransition] = true;
                    nextStocEventTime[next.nextTransition] = Double.POSITIVE_INFINITY;
                } else
                    nextStocEventTime[next.nextTransition] = currentTime +
                            RandomGenerator.expDist(currentRate[next.nextTransition], rand);
                eventQueue.updateEvent(next.nextTransition, nextStocEventTime[next.nextTransition]);
            }
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

        //update guards
        for (Integer j : guardList) {
            boolean oldStatus = currentGuardStatus[j];
            currentGuardStatus[j] = guards[j].evaluateCache();
            if (oldStatus != currentGuardStatus[j]) {
                switch (types[j]) {
                    case STOCHASTIC:
                        if (j != next.nextTransition) {
                            if (!oldStatus) {
                                //if the guard becomes active again, one has to
                                //resample the firing time!!!
                                currentRate[j] = rates[j].computeCache();
                                if (currentRate[j] <= ZERO) {
                                    infinity[j] = true;
                                    nextStocEventTime[j] = Double.POSITIVE_INFINITY;
                                } else
                                    nextStocEventTime[j] = currentTime +
                                            RandomGenerator.expDist(currentRate[j], rand);
                                eventQueue.updateEvent(j, nextStocEventTime[j]);
                            } else {
                                eventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
                                this.currentRate[j] = 0.0;
                                this.nextStocEventTime[j] = Double.POSITIVE_INFINITY;
                                infinity[j] = false; //infinity is used only for transitions
                                //with true guards
                            }
                        }
                        break;
                    case INSTANTANEOUS:
                        if (!oldStatus)
                            this.eventManager.addEnabledEvent(j, rates[j].computeCache());
                        else
                            this.eventManager.removeEvent(j);
                        break;
                    case TIMED:
                        if (!oldStatus)
                            eventQueue.updateEvent(j, computeNextFiringTimeCache(j, currentTime));
                        else
                            eventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
                        break;
                    default:
                        throw new SimulationException("Incompatible transition type");
                }
            }
        }


        for (Integer j : rateList) {
            switch (types[j]) {
                case STOCHASTIC:
                    if (currentGuardStatus[j]) {
                        double r = rates[j].computeCache();
                        if (j != next.nextTransition) {
                            double t;
                            if (r <= ZERO) {
                                infinity[j] = true;
                                t = Double.POSITIVE_INFINITY;
                            } else if (infinity[j]) {
                                t = currentTime +
                                        RandomGenerator.expDist(currentRate[j], rand);
                                infinity[j] = false;
                            } else
                                t = currentTime +
                                        (currentRate[j] / r) * (this.nextStocEventTime[j] - currentTime);
                            eventQueue.updateEvent(j, t);
                            this.nextStocEventTime[j] = t;
                        }
                        this.currentRate[j] = r;
                    }
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
            eventQueue.updateEvent(j, computeNextFiringTimeCache(j, currentTime));

        //compute next time for fired transition, if transition is stochastic.
        if (next.isStochastic) {
            if (currentGuardStatus[next.nextTransition]) {
                if (currentRate[next.nextTransition] <= ZERO) {
                    infinity[next.nextTransition] = true;
                    nextStocEventTime[next.nextTransition] = Double.POSITIVE_INFINITY;
                } else
                    nextStocEventTime[next.nextTransition] = currentTime +
                            RandomGenerator.expDist(currentRate[next.nextTransition], rand);
                eventQueue.updateEvent(next.nextTransition, nextStocEventTime[next.nextTransition]);
            }
        }


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
}
