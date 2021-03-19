/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.stochastic;

import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;
import com.eggloop.flow.simhya.simhya.model.flat.*;
import com.eggloop.flow.simhya.simhya.simengine.utils.*;
import com.eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import com.eggloop.flow.simhya.simhya.simengine.*;
import com.eggloop.flow.simhya.simhya.GlobalOptions;


/**
 * This simulator can be used for models without guards, instantaneous, timed or delayed transitions
 *
 * @author Luca
 */
public class PureSSASimulator extends  AbstractStochasticSimulator {
    private RateManager rateManager;
    private boolean forceRateManager;
    private RMType  forcedState;
    private boolean useDependencyGraph;

    public PureSSASimulator(FlatModel model, DataCollector collector) {
        super(model, collector);
        rateManager = null;
        forceRateManager = false;
        forcedState = RMType.LIST;
        useDependencyGraph = false;
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

    public void setUseDependencyGraph(boolean useDependencyGraph) {
        this.useDependencyGraph = useDependencyGraph;
    }

    @Override
    public void initialize() {
        if (initialized)
            return;
        //checks if there are delayed, guarded, instantaneous, or timed transitions, if so, terminate
        for (int i=0;i<numberOfTransitions;i++)
            if (delays[i] != null)
                throw new SimulationException("This is not a simulator for delayed models");
        if (model.getNumberOfStochasticTransitions(true) != model.getNumberOfTransitions())
             throw new SimulationException("This is not a simulator for models with instantaneous or delayed transitions");
        for (int i=0;i<numberOfTransitions;i++)
            if (!guards[i].isTautology())
                throw new SimulationException("This is not a simulator for models with non-trivial guarded transitions");
        if (model == null || collector == null)
            throw new SimulationException("Model or data collector not defined");
        
        int[] idList;
        //initialize rate manager, for stochastic transitions
        idList = model.getListOfStochasticTransitionID(true);
        double[] initialRates = new double[model.getNumberOfStochasticTransitions(true)];
        for (int i=0;i<idList.length;i++) {
            int id = idList[i];
            initialRates[i] = rates[id].compute();
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
        super.initialized = true;
    }

    @Override
    public void reinitialize() {
        int[] idList;
        //recompute initial rates
        idList = model.getListOfStochasticTransitionID(true);
        for (int i=0;i<idList.length;i++) {
            int id = idList[i];
            rateManager.updateRate(id,  rates[id].compute());
        }
    }


    
    @Override
    public void reset(DataCollector dc) {
        super.collector = dc;
        super.resetModel(true);
        rateManager = null;
        forceRateManager = false;
        forcedState = RMType.LIST;
        super.initialized  = false;
    }
     
    @Override
    void selectNextTransition() {
        double exitRate = rateManager.getExitRate();
        if (exitRate < GlobalOptions.TOLERANCE) {
            next.deadlock = true;
            return;
        }
        next.nextTime = currentTime + RandomGenerator.expDist(exitRate, rand);
        this.lastSimulationStochasticSteps++;
        next.nextTransition = rateManager.sampleNextTransition(rand);
    }

     @Override
    void executeNextTransition() {
        execute(next.nextTransition);
    }

    @Override
    void updateModel() {
        Integer[] depList;
        if (this.useDependencyGraph) {
            depList = this.model.getListOfUpdatedRates(next.nextTransition);
            for (Integer j : depList)
                rateManager.updateRate(j, rates[j].compute());
        } else {
            for (int j=0;j<numberOfTransitions;j++)
                rateManager.updateRate(j, rates[j].compute());
        }
    }

    @Override
    void executeNextTransitionWithCache() {
        executeCache(next.nextTransition);
    }

    @Override
    void updateModelWithCache() {
        Integer[] depList;
        if (this.useDependencyGraph) {
            depList = this.model.getListOfUpdatedRates(next.nextTransition);
            for (Integer j : depList)
                rateManager.updateRate(j, rates[j].computeCache());
        } else {
            for (int j=0;j<numberOfTransitions;j++)
                rateManager.updateRate(j, rates[j].computeCache());
        }
    }




}
