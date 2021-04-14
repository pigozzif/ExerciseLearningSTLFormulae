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
 * @author luca
 */
public class PureGuardedSSASimulator extends AbstractStochasticSimulator {
    private RateManager rateManager;
    private boolean forceRateManager;
    private RMType  forcedState;
    private boolean useDependencyGraph;

    public PureGuardedSSASimulator(FlatModel model, DataCollector collector) {
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
        if (model == null || collector == null)
            throw new SimulationException("Model or data collector not defined");
        //checks if there are delayed,  instantaneous, or timed transitions, if so, terminate
        if (model.containsDelayedTransitions())
                throw new SimulationException("This is not a simulator for delayed models");
        if (model.getNumberOfStochasticTransitions(true) != model.getNumberOfTransitions())
             throw new SimulationException("This is not a simulator for models with instantaneous or delayed transitions");
        
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
        super.initialized = true;
    }

    @Override
    public void reinitialize() {
        int[] idList;
        //recompute initial rates
        idList = model.getListOfStochasticTransitionID(true);
        for (int i=0;i<idList.length;i++) {
            int id = idList[i];
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id])
                rateManager.updateRate(id,  rates[id].compute());
            else
                rateManager.updateRate(id,  0.0);
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
        Integer[] guardList,rateList;
        if (this.useDependencyGraph) {
            guardList = this.model.getListOfUpdatedGuards(next.nextTransition);
            rateList = this.model.getListOfUpdatedRates(next.nextTransition);
            //update guards
            if (GlobalOptions.alwaysComputeRateAfterGuard) {
                for (Integer j : guardList) {
                   currentGuardStatus[j] = guards[j].evaluate();
                   if (currentGuardStatus[j])
                      rateManager.updateRate(j, rates[j].compute());
                   else
                      rateManager.updateRate(j, 0.0);
                }
            } else {
                for (Integer j : guardList) {
                    boolean oldStatus = currentGuardStatus[j];
                    currentGuardStatus[j] = guards[j].evaluate();
                    if (oldStatus != currentGuardStatus[j]) {
                        if (!oldStatus)
                            rateManager.updateRate(j, rates[j].compute());
                        else
                            rateManager.updateRate(j, 0.0);
                    }
                }
            }
            for (Integer j : rateList) {
                        if (currentGuardStatus[j])
                            rateManager.updateRate(j, rates[j].compute());
            }

        } else {
            for (int j=0;j<numberOfTransitions;j++) {
                currentGuardStatus[j] = guards[j].evaluate();
                if (currentGuardStatus[j])
                    rateManager.updateRate(j, rates[j].compute());
                else
                    rateManager.updateRate(j, 0.0);
            }
        }
    }

    @Override
    void executeNextTransitionWithCache() {
        executeCache(next.nextTransition);
    }

    
    @Override
    void updateModelWithCache() {
        Integer[] guardList,rateList;
        if (this.useDependencyGraph) {
            guardList = this.model.getListOfUpdatedGuards(next.nextTransition);
            rateList = this.model.getListOfUpdatedRates(next.nextTransition);
            //update guards
            if (GlobalOptions.alwaysComputeRateAfterGuard) {
                for (Integer j : guardList) {
                   currentGuardStatus[j] = guards[j].evaluateCache();
                   if (currentGuardStatus[j])
                      rateManager.updateRate(j, rates[j].computeCache());
                   else
                      rateManager.updateRate(j, 0.0);
                }
            } else {
                for (Integer j : guardList) {
                    boolean oldStatus = currentGuardStatus[j];
                    currentGuardStatus[j] = guards[j].evaluateCache();
                    if (oldStatus != currentGuardStatus[j]) {
                        if (!oldStatus)
                            rateManager.updateRate(j, rates[j].computeCache());
                        else
                            rateManager.updateRate(j, 0.0);
                    }
                }
            }
            for (Integer j : rateList) {
                        if (currentGuardStatus[j])
                            rateManager.updateRate(j, rates[j].computeCache());
            }

        } else {
            for (int j=0;j<numberOfTransitions;j++) {
                currentGuardStatus[j] = guards[j].evaluateCache();
                if (currentGuardStatus[j])
                    rateManager.updateRate(j, rates[j].computeCache());
            }
        }
    }
}
