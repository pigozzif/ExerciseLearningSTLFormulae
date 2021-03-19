/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.dataprocessing;

import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.sampling.StepInterpolator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Luca
 */
public abstract class AbstractContinuousDataCollector extends AbstractDataCollector implements ContinuousDataCollector, EventDataCollector {

    /**
     * We keep a list of data about events that fired for each run
     */
    ArrayList<EventInfo>[] firedEvents;

    StepInterpolator interpolator;
    long steps;
    boolean collectEventData;

    public AbstractContinuousDataCollector(FlatModel model) {
        super(model);
        collectEventData = true;
    }


    public void setInterpolator(StepInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    public void collectEventData(boolean collect) {
        this.collectEventData = collect;
    }

    public void putNextInstantaneousEvent(double time, double[] y, String name) {
        if (super.storeLastFullTrajectory) {
            store.setAllVariableValues(y);
            this.addToFullTrajectory(time, y, store.getExpressionVariablesValues());
        }

        if (this.collectEventData && (super.storeFinalStateDataOnly || super.storeTrajectoryData)) {
            EventInfo e = new EventInfo(name, time, EventInfo.INSTANTANEOUS);
            this.firedEvents[currentRunFinalState].add(e);
        }
    }

    public void putNextTimedEvent(double time, double[] y, String name) {
        if (super.storeLastFullTrajectory) {
            store.setAllVariableValues(y);
            this.addToFullTrajectory(time, y, store.getExpressionVariablesValues());
        }
        if (this.collectEventData && (super.storeFinalStateDataOnly || super.storeTrajectoryData)) {
            EventInfo e = new EventInfo(name, time, EventInfo.TIMED);
            this.firedEvents[currentRunFinalState].add(e);
        }
    }

    public void saveEventList(String filename) {
        if (!(this.collectEventData && (super.storeFinalStateDataOnly || super.storeTrajectoryData)))
            throw new DataException("Event data not collected.");
        try {
            PrintStream p = new PrintStream(filename);
            p.println("Run\t\tTime\t\tEvent\t\tType");
            for (int i = 0; i < runs; i++) {
                p.print(this.getTrajectory(i).eventListToString());
            }
            p.close();
        } catch (java.io.IOException e) {
            throw new DataException("Cannot save event list to file " + filename);
        }
    }


    @Override
    public Trajectory getTrajectory(int id) {
        super.getTrajectory(id);
        super.trajectory.eventList = this.firedEvents[id];
        return this.trajectory;
    }

    @Override
    void initialize() {
        super.initialize();
        this.firedEvents = new ArrayList[runs];
    }

    @Override
    public void newTrajectory() {
        super.newTrajectory();
        this.firedEvents[super.currentRunFinalState] = new ArrayList<EventInfo>();
    }


    @Override
    public boolean dataNeeded(double nextTime) {
        if (this.saveOnlyFinalState)
            return false;
        else if (nextTime > this.lastPrintTime + this.deltaTime)
            return true;
        else return false;
    }

    @Override
    public boolean dataNeeded(double nextTime, long stepNumber) {
        this.steps = stepNumber;
        if (this.saveOnlyFinalState)
            return false;
//        else if (this.saveByStep && steps == this.lastPrintStep + this.deltaStep)
//            return true;
        else if (nextTime > this.lastPrintTime + this.deltaTime)
            return true;
        else return false;
    }


    /**
     * stores data at intermediate time points
     *
     * @param nextTime
     * @throws DerivativeException
     */
    public void putDataOde(double nextTime) throws DerivativeException {
//        if (this.saveByStep) {
//            interpolator.setInterpolatedTime(nextTime);
//            double [] x = interpolator.getInterpolatedState();
//            store.setAllVariableValues(x);
//            trajectory.add(nextTime, x, store.getExpressionVariablesValues());
//            this.lastPrintStep += this.deltaStep;
//        }
//        else
        while (lastPrintTime + deltaTime < nextTime) {
            lastPrintTime += deltaTime;
            interpolator.setInterpolatedTime(lastPrintTime);
            double[] x = interpolator.getInterpolatedState();
            store.setAllVariableValues(x);
            if (!this.SMCstoreStrategy)
                add(lastPrintTime, x, store.getExpressionVariablesValues());
            if (super.storeLastFullTrajectory)
                this.addToFullTrajectory(lastPrintTime, x, store.getExpressionVariablesValues());
        }
    }


    /**
     * stores the final state and all intermediate points needed.
     *
     * @param time the final
     * @throws DerivativeException
     */
    public void putFinalStateOde(double time) throws DerivativeException {

//        System.out.println("Called print final state");

        double[] x;
//        if (this.saveByStep || this.saveOnlyFinalState)
        if (this.saveOnlyFinalState) {
            interpolator.setInterpolatedTime(time);
            x = interpolator.getInterpolatedState();
            store.setAllVariableValues(x);
            if (!this.SMCstoreStrategy)
                addFinalState(time, x, store.getExpressionVariablesValues());
            totalSteps[currentRunFinalState] = steps;

//            System.out.println("Final state :: " + time + " " + java.util.Arrays.toString(x) +
//                    java.util.Arrays.toString(store.getExpressionVariablesValues()));

        } else {
            if (finalTime > this.initialSavingTime) {
                while (this.lastPrintTime + this.deltaTime <= finalTime) {
                    lastPrintTime += deltaTime;
                    if (lastPrintTime <= time) {
                        interpolator.setInterpolatedTime(lastPrintTime);
                        x = interpolator.getInterpolatedState();
                        store.setAllVariableValues(x);
                    } else {
                        interpolator.setInterpolatedTime(time);
                        x = interpolator.getInterpolatedState();
                        store.setAllVariableValues(x);
                    }
                    if (!this.SMCstoreStrategy)
                        add(lastPrintTime, x, store.getExpressionVariablesValues());
                    if (super.storeLastFullTrajectory)
                        addToFullTrajectory(lastPrintTime, x, store.getExpressionVariablesValues());
                }
            }
            interpolator.setInterpolatedTime(time);
            x = interpolator.getInterpolatedState();
            store.setAllVariableValues(x);
            if (!this.SMCstoreStrategy)
                addFinalState(time, x, store.getExpressionVariablesValues());
            if (super.storeLastFullTrajectory)
                this.addToFullTrajectory(time, x, store.getExpressionVariablesValues());
            if (!this.SMCstoreStrategy)
                totalSteps[currentRunFinalState] = steps;
        }
        this.completedRuns++;
        if (runs > 1)
            this.computeStatistics();
        if (!this.SMCstoreStrategy && completedRuns == runs)
            purgeData();
    }


    @Override
    void addToFullTrajectory(double time, double[] var, double[] exp) {
        if (currentStep == Integer.MAX_VALUE)
            return;
        if (currentStep == fullTrajectory[0].length) {
            //not enough points, doubling the array
            for (int i = 0; i < this.vars; i++)
                fullTrajectory[i] = Arrays.copyOf(fullTrajectory[i], 2 * currentStep);
        }
        fullTrajectory[0][currentStep] = time;
        int c = 1;
        for (int i = 0; i < varCodes.length; i++)
            fullTrajectory[c++][currentStep] = var[varCodes[i]];
        for (int i = 0; i < expVarCodes.length; i++)
            fullTrajectory[c++][currentStep] = exp[expVarCodes[i]];
        this.currentStep++;
    }

}