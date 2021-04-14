/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine.stochastic;

import eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import eggloop.flow.simhya.simhya.model.flat.FlatModel;
import eggloop.flow.simhya.simhya.simengine.SimulationException;
import eggloop.flow.simhya.simhya.simengine.utils.EventQueue;
import eggloop.flow.simhya.simhya.utils.RandomGenerator;

/**
 * @author luca
 */
public class PureGuardedGBSimulator extends AbstractStochasticSimulator {
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


    public PureGuardedGBSimulator(FlatModel model, DataCollector collector) {
        super(model, collector);
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
        for (int i = 0; i < idList.length; i++) {
            int id = idList[i];
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id]) {
                currentRate[id] = rates[id].compute();
                this.newFiringTimeInit(id);
            }
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

        idList = model.getListOfStochasticTransitionID(true);
        for (int i = 0; i < idList.length; i++) {
            int id = idList[i];
            currentGuardStatus[id] = guards[id].evaluate();
            if (currentGuardStatus[id]) {
                currentRate[id] = rates[id].compute();
                this.newFiringTimeInit(id);
            }
            eventQueue.addEvent(id, nextStocEventTime[id]);
        }
    }

    @Override
    public void reset(DataCollector dc) {
        super.collector = dc;
        super.resetModel(true);
        eventQueue = null;
        currentRate = null;
        this.nextStocEventTime = null;
        super.initialized = false;
    }


    @Override
    void selectNextTransition() {
        next.nextTime = eventQueue.getNextFiringTime();
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
        Integer[] guardList, rateList;
        guardList = this.model.getListOfUpdatedGuards(next.nextTransition);
        rateList = this.model.getListOfUpdatedRates(next.nextTransition);

        //update guards
        for (Integer j : guardList) {
            boolean oldStatus = currentGuardStatus[j];
            currentGuardStatus[j] = guards[j].evaluate();
            if (oldStatus != currentGuardStatus[j]) {
                if (j != next.nextTransition) {
                    if (!oldStatus) {
                        //if the guard becomes active again, one has to
                        //resample the firing time!!!
                        currentRate[j] = rates[j].compute();
                        newFiringTime(j);
                        eventQueue.updateEvent(j, nextStocEventTime[j]);
                    } else {
                        eventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
                        this.currentRate[j] = 0.0;
                        this.nextStocEventTime[j] = Double.POSITIVE_INFINITY;
                        infinity[j] = false; //infinity is used only for transitions
                        //with true guards
                    }
                }

            }
        }

        for (Integer j : rateList) {
            if (currentGuardStatus[j]) {
                double r = rates[j].compute();
                if (j != next.nextTransition) {
                    updateFiringTime(j, r);
                    eventQueue.updateEvent(j, nextStocEventTime[j]);
                }
                this.currentRate[j] = r;
            }

        }

        //compute next time for fired transition, if transition is stochastic.
        if (currentGuardStatus[next.nextTransition]) {
            newFiringTime(next.nextTransition);
            eventQueue.updateEvent(next.nextTransition, nextStocEventTime[next.nextTransition]);
        }

    }

    /**
     * computes new firing time for stochastic transition j
     * previously inactive
     * and stores it in nextStocEventTime[j]. Also
     * sets infinity vector
     *
     * @param j
     */
    private void newFiringTime(int j) {
        if (currentRate[j] <= ZERO) {
            infinity[j] = true;
            nextStocEventTime[j] = Double.POSITIVE_INFINITY;
        } else
            nextStocEventTime[j] = currentTime +
                    RandomGenerator.expDist(currentRate[j], rand);
    }

    /**
     * computes new firing time for stochastic transition j
     * previously inactive
     * and stores it in nextStocEventTime[j]. Also
     * sets infinity vector
     *
     * @param j
     */
    private void newFiringTimeInit(int j) {
        if (currentRate[j] <= ZERO) {
            infinity[j] = true;
            nextStocEventTime[j] = Double.POSITIVE_INFINITY;
        } else
            nextStocEventTime[j] = initialTime +
                    RandomGenerator.expDist(currentRate[j], rand);
    }


    /**
     * updates firing time for stochastic transition j with
     * new rate r.
     *
     * @param j
     * @param r
     * @return
     */
    private void updateFiringTime(int j, double r) {
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
                    (currentRate[j] / r) * (this.nextStocEventTime[j] - currentTime);
        }
        this.nextStocEventTime[j] = t;
    }

    @Override
    void updateModelWithCache() {
        Integer[] guardList, rateList;
        guardList = this.model.getListOfUpdatedGuards(next.nextTransition);
        rateList = this.model.getListOfUpdatedRates(next.nextTransition);

        //update guards
        for (Integer j : guardList) {
            boolean oldStatus = currentGuardStatus[j];
            currentGuardStatus[j] = guards[j].evaluateCache();
            if (oldStatus != currentGuardStatus[j]) {
                if (j != next.nextTransition) {
                    if (!oldStatus) {
                        //if the guard becomes active again, one has to
                        //resample the firing time!!!
                        currentRate[j] = rates[j].computeCache();
                        newFiringTime(j);
                        eventQueue.updateEvent(j, nextStocEventTime[j]);
                    } else {
                        eventQueue.updateEvent(j, Double.POSITIVE_INFINITY);
                        this.currentRate[j] = 0.0;
                        this.nextStocEventTime[j] = Double.POSITIVE_INFINITY;
                        infinity[j] = false; //infinity is used only for transitions
                        //with true guards
                    }
                }

            }
        }

        for (Integer j : rateList) {
            if (currentGuardStatus[j]) {
                double r = rates[j].computeCache();
                if (j != next.nextTransition) {
                    updateFiringTime(j, r);
                    eventQueue.updateEvent(j, nextStocEventTime[j]);
                }
                this.currentRate[j] = r;
            }

        }

        //compute next time for fired transition, if transition is stochastic.
        if (currentGuardStatus[next.nextTransition]) {
            newFiringTime(next.nextTransition);
            eventQueue.updateEvent(next.nextTransition, nextStocEventTime[next.nextTransition]);
        }

    }


}
