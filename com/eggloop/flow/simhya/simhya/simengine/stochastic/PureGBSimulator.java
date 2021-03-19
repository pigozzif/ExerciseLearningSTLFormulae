/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.stochastic;

import com.eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;
import com.eggloop.flow.simhya.simhya.model.flat.*;
import com.eggloop.flow.simhya.simhya.simengine.utils.*;
import com.eggloop.flow.simhya.simhya.simengine.*;


/**
 *
 * @author luca
 */
public class PureGBSimulator extends AbstractStochasticSimulator {
    private EventQueue eventQueue;
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
    

    public PureGBSimulator(FlatModel model, DataCollector collector) {
        super(model,collector);
        eventQueue = null;
        nextStocEventTime = null;
        currentRate = null;
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
        if (!model.containsOnlyStochasticTransitions(true))
             throw new SimulationException("This is not a simulator for models with instantaneous or delayed transitions");
        if (model.containsGuardedTransitions())
             throw new SimulationException("This is not a simulator for models with non-trivial guarded transitions");
        model.recomputeDependencyGraphs(false);

        int[] idList;
        eventQueue = new EventQueue(model.getNumberOfStochasticTransitions(true));

        nextStocEventTime = new double[model.getNumberOfTransitions()];
        currentRate = new double[model.getNumberOfTransitions()];
        infinity = new boolean[model.getNumberOfTransitions()];
        java.util.Arrays.fill(infinity, false);
        java.util.Arrays.fill(currentRate, 0.0);
        java.util.Arrays.fill(this.nextStocEventTime, Double.POSITIVE_INFINITY);

        idList = model.getListOfStochasticTransitionID(true);
        for (int i=0;i<idList.length;i++) {
            int id = idList[i];
            currentRate[id] = rates[id].compute();
            if (currentRate[id] <= ZERO) {
                infinity[id] = true;
                nextStocEventTime[id] = Double.POSITIVE_INFINITY;
            } else
                nextStocEventTime[id] = initialTime  +
                        RandomGenerator.expDist(currentRate[id], rand);
            eventQueue.addEvent(id, nextStocEventTime[id]);
        }
        super.initialized = true;
    }

    @Override
    public void reinitialize() {
        int[] idList;
        eventQueue = new EventQueue(model.getNumberOfStochasticTransitions(true));
        this.nextStocEventTime = new double[model.getNumberOfTransitions()];
        currentRate = new double[model.getNumberOfTransitions()];
        infinity = new boolean[model.getNumberOfTransitions()];
        java.util.Arrays.fill(infinity, false);
        java.util.Arrays.fill(currentRate, 0.0);
        java.util.Arrays.fill(this.nextStocEventTime, Double.POSITIVE_INFINITY);

//        System.out.println("Initial system state is " + java.util.Arrays.toString(variables));

        idList = model.getListOfStochasticTransitionID(true);
        for (int i=0;i<idList.length;i++) {
            int id = idList[i];
            currentRate[id] = rates[id].compute();
            currentRate[id] = rates[id].compute();
            if (currentRate[id] <= ZERO) {
                infinity[id] = true;
                nextStocEventTime[id] = Double.POSITIVE_INFINITY;
            } else
                nextStocEventTime[id] = initialTime  +
                        RandomGenerator.expDist(currentRate[id], rand);
            eventQueue.addEvent(id, nextStocEventTime[id]);
//
//            System.out.println("Transition " + id);
//            System.out.println("Initial rate is " + currentRate[id]);
//            System.out.println("First firing time is " + nextStocEventTime[id]);
        }

        
       
        
    }

    @Override
    public void reset(DataCollector dc) {
        super.collector = dc;
        super.resetModel(true);
        eventQueue = null;
        currentRate = null;
        this.nextStocEventTime = null;
        super.initialized  = false;
    }


    @Override
    void selectNextTransition() {
        next.nextTime  = eventQueue.getNextFiringTime();
        next.nextTransition = eventQueue.extractFiringEvent(Double.POSITIVE_INFINITY);
        this.lastSimulationStochasticSteps++;
        if (stopping[next.nextTransition])
                next.stop = true;
        return;
    }
    

     @Override
    void executeNextTransition() {
        execute(next.nextTransition);
    }

    @Override
    void executeNextTransitionWithCache() {
        executeCache(next.nextTransition);
    }



    @Override
    void updateModel() {
        Integer[] rateList;
        rateList = this.model.getListOfUpdatedRates(next.nextTransition);

//        System.out.println("Step " + this.lastSimulationSteps);
//        System.out.println("Fired transition " + next.nextTransition + " at time " +currentTime);
//        System.out.println("New system state is " + java.util.Arrays.toString(variables));

        for (Integer j : rateList) {
            double r = rates[j].compute();

//            System.out.println("New rate for transition " + j +" is " + r);

            if (j != next.nextTransition) {
              double t;
               if (r <= ZERO) {
                   infinity[j] = true;
                   t = Double.POSITIVE_INFINITY;
               } else if (infinity[j]) {
                   t = currentTime +
                         RandomGenerator.expDist(r, rand);
                   infinity[j] = false;
               } else {
                   t = currentTime +
                          (currentRate[j]/r)*(this.nextStocEventTime[j] - currentTime);
               }
                this.nextStocEventTime[j] = t;
              eventQueue.updateEvent(j, nextStocEventTime[j]);

//              System.out.println("New firing time for transition " + j +" is " + nextStocEventTime[j]);

            }
            this.currentRate[j] = r;
        }

        //compute next time for fired transition, if transition is stochastic.
        if (currentRate[next.nextTransition] <= ZERO) {
            infinity[next.nextTransition] = true;
            nextStocEventTime[next.nextTransition] = Double.POSITIVE_INFINITY;
        } else
            nextStocEventTime[next.nextTransition] = currentTime +
                    RandomGenerator.expDist(currentRate[next.nextTransition], rand);
        eventQueue.updateEvent(next.nextTransition, nextStocEventTime[next.nextTransition]);
            
//        System.out.println("New firing time for fired transition is " + nextStocEventTime[next.nextTransition]);
        

    }

    @Override
    void updateModelWithCache() {
        Integer[] rateList;
        rateList = this.model.getListOfUpdatedRates(next.nextTransition);

        for (Integer j : rateList) {
            double r = rates[j].computeCache();
            if (j != next.nextTransition) {
              double t;
               if (r <= ZERO) {
                   infinity[j] = true;
                   t = Double.POSITIVE_INFINITY;
               } else if (infinity[j]) {
                   t = currentTime +
                         RandomGenerator.expDist(r, rand);
                   infinity[j] = false;
               } else {
                   t = currentTime +
                          (currentRate[j]/r)*(this.nextStocEventTime[j] - currentTime);
               }
                this.nextStocEventTime[j] = t;
              eventQueue.updateEvent(j, nextStocEventTime[j]);
            }
            this.currentRate[j] = r;
        }

        //compute next time for fired transition, if transition is stochastic.
        if (currentRate[next.nextTransition] <= ZERO) {
            infinity[next.nextTransition] = true;
            nextStocEventTime[next.nextTransition] = Double.POSITIVE_INFINITY;
        } else
            nextStocEventTime[next.nextTransition] = currentTime +
                    RandomGenerator.expDist(currentRate[next.nextTransition], rand);
        eventQueue.updateEvent(next.nextTransition, nextStocEventTime[next.nextTransition]);
    }

}
