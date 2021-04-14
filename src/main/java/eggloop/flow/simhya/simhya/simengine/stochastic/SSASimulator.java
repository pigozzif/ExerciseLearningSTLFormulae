/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine.stochastic;

import eggloop.flow.simhya.simhya.utils.RandomGenerator;
import eggloop.flow.simhya.simhya.model.flat.*;
import eggloop.flow.simhya.simhya.simengine.utils.*;
import eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import eggloop.flow.simhya.simhya.simengine.*;
import eggloop.flow.simhya.simhya.GlobalOptions;


/**
 *
 * @author Luca
 */
public class SSASimulator extends AbstractStochasticSimulator {
    private EventQueue eventQueue;
    private RateManager rateManager;
    private InstantaneousEventsManager eventManager;
    private boolean forceRateManager;
    private RMType  forcedState;


    public SSASimulator(FlatModel model, DataCollector collector) {
        super(model, collector);
        rateManager = null;
        eventQueue = null;
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
        //checks if there are delayed transitions, if so, terminate
        if (model.containsDelayedTransitions())
            throw new SimulationException("This is not a simulator for delayed models");

        
        int[] idList;
        //initialize rate manager, for stochastic transitions
        idList = model.getListOfStochasticTransitionID(true);
        double[] initialRates = new double[model.getNumberOfStochasticTransitions(true)];
        for (int i=0;i<idList.length;i++) {
            int id = idList[i];
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                initialRates[i] = rates[id].compute();
            else
                initialRates[i] = 0.0;
        }
        if (this.forceRateManager) {
            if (forcedState == RMType.LIST)
                rateManager = new RateList(numberOfTransitions,idList,initialRates);
            else
                rateManager = new RateTree(numberOfTransitions,idList,initialRates);
        } else {
            if (model.getNumberOfTransitions() < GlobalOptions.transitionThresholdForRateTree)
                rateManager = new RateList(numberOfTransitions,idList,initialRates);
            else
                rateManager = new RateTree(numberOfTransitions,idList,initialRates);
        }

        //initializing  timed transitions
        idList = model.getListOfTimedTransitionsID();
        eventQueue = new EventQueue(numberOfTransitions);
        for (Integer id : idList) {
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                eventQueue.addEvent(id, computeNextFiringTime(id, initialTime));
            else
                eventQueue.addEvent(id,Double.POSITIVE_INFINITY);
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
        super.initialized = true;
    }


    @Override
    public void reinitialize() {
        //recompute initial rates
        int[] idList;
        idList = model.getListOfStochasticTransitionID(true);
        for (int i=0;i<idList.length;i++) {
            int id = idList[i];
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                rateManager.updateRate(id,  rates[id].compute());
            else
                rateManager.updateRate(id,  0.0);
        }
        //reinitializing  timed transitions
        idList = model.getListOfTimedTransitionsID();
        for (Integer id : idList) {
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                eventQueue.updateEvent(id, computeNextFiringTime(id, initialTime));
            else
                eventQueue.updateEvent(id, Double.POSITIVE_INFINITY);
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
    }

    /**
     * computes the firing time of a timed transition
     * @param id
     * @param time
     * @return
     */
    private double computeNextFiringTime(int id, double time) {
        double ft =firingTimes[id].compute();
        return (ft < time ? Double.POSITIVE_INFINITY : ft);
    }

    /**
     * computes the firing time of a timed transition
     * @param id
     * @param time
     * @return
     */
    private double computeNextFiringTimeCache(int id, double time) {
        double ft =firingTimes[id].computeCache();
        return (ft < time ? Double.POSITIVE_INFINITY : ft);
    }

    @Override
    public void reset(DataCollector dc) {
        super.collector = dc;
        super.resetModel(true);
        rateManager = null;
        eventQueue = null;
        eventManager = null;
        forceRateManager = false;
        forcedState = RMType.LIST;
        super.initialized  = false;
    }

   


    
   

    @Override
    void selectNextTransition() {
        double nextTime, nextEventTime;

        if (this.eventManager.areInstantaneousEventsEnabled()) {
            lastSimulationInstantaneousSteps++;
            //fire an instantaneous transition
            next.nextTime = currentTime;
            next.nextTransition = eventManager.getNextEvent(rand,currentTime);
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
        nextEventTime = eventQueue.getNextFiringTime();
        if (nextEventTime <= nextTime) {
            next.nextTime = nextEventTime;
            //this is not a delayed event!
            next.nextTransition = eventQueue.extractFiringEvent(Double.POSITIVE_INFINITY);
            this.lastSimulationTimedSteps++;
            if (stopping[next.nextTransition])
                next.stop = true;
            return;
        }
        else {
            this.lastSimulationStochasticSteps++;
            next.nextTime = nextTime;
            next.nextTransition = rateManager.sampleNextTransition(rand);
            if (stopping[next.nextTransition])
                next.stop = true;
            return;
        }
    }

     @Override
    void executeNextTransition() {
        execute(next.nextTransition);
//        switch ( types[next.nextTransition] ) {
//            case STOCHASTIC :
//                execute(next.nextTransition);
//                break;
//            case INSTANTANEOUS :
//                //an event should become inactive due to its update
//                //this.eventManager.removeEvent(next.nextTransition);
//                execute(next.nextTransition);
//                break;
//            case TIMED :
//                execute(next.nextTransition);
//                //non dovrebbe piu' servire!!!
////                double newFiringTime = model.computeNextFiringTime(next.nextTransition, currentTime);
////                if (newFiringTime > this.currentTime)
////                    eventQueue.addEvent(next.nextTransition, newFiringTime);
////                else
////                    eventQueue.addEvent(next.nextTransition, Double.POSITIVE_INFINITY);
//                break;
//            default:
//                throw new SimulationException("Incompatible transition type: " + model.getTransitionType(next.nextTransition) + ", aborting!");
//        }
    }

    @Override
    void updateModel() {
        Integer[] guardList,rateList,firingTimeList;
        guardList = this.model.getListOfUpdatedGuards(next.nextTransition);
        rateList = this.model.getListOfUpdatedRates(next.nextTransition);
        firingTimeList = this.model.getListOfUpdatedFiringTimes(next.nextTransition);

        //update guards
        if (GlobalOptions.alwaysComputeRateAfterGuard) {
            for (Integer j : guardList) {
                currentGuardStatus[j] = guards[j].evaluate();
                switch (types[j]) {
                   case STOCHASTIC :
                       if (currentGuardStatus[j])
                          rateManager.updateRate(j, rates[j].compute());
                       else
                          rateManager.updateRate(j, 0.0);
                       break;
                   case INSTANTANEOUS :
                        if (currentGuardStatus[j])
                            this.eventManager.addEnabledEvent(j, rates[j].compute());
                        else
                            this.eventManager.removeEvent(j);
                        break;
                    case TIMED :
                        if (currentGuardStatus[j])
                           eventQueue.updateEvent(j, computeNextFiringTime(j, currentTime));
                        else
                           eventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
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
                        case STOCHASTIC :
                            if (!oldStatus)
                                rateManager.updateRate(j, rates[j].compute());
                            else
                                rateManager.updateRate(j, 0.0);
                        break;
                        case INSTANTANEOUS :
                            if (!oldStatus)
                                this.eventManager.addEnabledEvent(j, rates[j].compute());
                            else
                                this.eventManager.removeEvent(j);
                            break;
                        case TIMED :
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
        }
        for (Integer j : rateList) {
            switch (types[j]) {
                case STOCHASTIC :
                    if (currentGuardStatus[j])
                        rateManager.updateRate(j, rates[j].compute());
                    break;
                case INSTANTANEOUS :
                    if (currentGuardStatus[j])
                        eventManager.updatePriority(j, rates[j].compute());
                    break;
                case TIMED :
                    throw new SimulationException("Trying to compute rate for a timed transitions, that has no rate.");
                default:
                    throw new SimulationException("Incompatible transition type");
            }
        }
        for (Integer j : firingTimeList)
            eventQueue.updateEvent(j, computeNextFiringTime(j, currentTime));
    }

    @Override
    void executeNextTransitionWithCache() {
         executeCache(next.nextTransition);
    }

    @Override
    void updateModelWithCache() {
        Integer[] guardList,rateList,firingTimeList;
        guardList = this.model.getListOfUpdatedGuards(next.nextTransition);
        rateList = this.model.getListOfUpdatedRates(next.nextTransition);
        firingTimeList = this.model.getListOfUpdatedFiringTimes(next.nextTransition);

        //new evaluation round
        model.getStore().newEvaluationRound();
        //update guards
        if (GlobalOptions.alwaysComputeRateAfterGuard) {
            for (Integer j : guardList) {
                currentGuardStatus[j] = guards[j].evaluateCache();
                switch (types[j]) {
                   case STOCHASTIC :
                       if (currentGuardStatus[j])
                          rateManager.updateRate(j, rates[j].computeCache());
                       else
                          rateManager.updateRate(j, 0.0);
                       break;
                   case INSTANTANEOUS :
                        if (currentGuardStatus[j])
                            this.eventManager.addEnabledEvent(j, rates[j].computeCache());
                        else
                            this.eventManager.removeEvent(j);
                        break;
                    case TIMED :
                        if (currentGuardStatus[j])
                           eventQueue.updateEvent(j, computeNextFiringTimeCache(j, currentTime));
                        else
                           eventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
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
                        case STOCHASTIC :
                            if (!oldStatus)
                                rateManager.updateRate(j, rates[j].computeCache());
                            else
                                rateManager.updateRate(j, 0.0);
                        break;
                        case INSTANTANEOUS :
                            if (!oldStatus)
                                this.eventManager.addEnabledEvent(j, rates[j].computeCache());
                            else
                                this.eventManager.removeEvent(j);
                            break;
                        case TIMED :
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
        }
        for (Integer j : rateList) {
            switch (types[j]) {
                case STOCHASTIC :
                    if (currentGuardStatus[j])
                        rateManager.updateRate(j, rates[j].computeCache());
                    break;
                case INSTANTANEOUS :
                    if (currentGuardStatus[j])
                        eventManager.updatePriority(j, rates[j].computeCache());
                    break;
                case TIMED :
                    throw new SimulationException("Trying to compute rate for a timed transitions, that has no rate.");
                default:
                    throw new SimulationException("Incompatible transition type");
            }
        }
        for (Integer j : firingTimeList)
            eventQueue.updateEvent(j, computeNextFiringTimeCache(j, currentTime));
    }




    

}
