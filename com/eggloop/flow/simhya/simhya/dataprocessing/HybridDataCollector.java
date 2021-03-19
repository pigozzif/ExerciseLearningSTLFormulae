/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing;


import java.util.Locale;
import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.apache.commons.math.ode.DerivativeException;
import java.util.ArrayList;
import java.io.PrintStream;

/**
 *
 * @author luca
 */
public class HybridDataCollector extends AbstractContinuousDataCollector implements StochasticEventCollector  {
    boolean collectStochasticEventData;
    int storeDimension;
   

    public HybridDataCollector(FlatModel model) {
        super(model);
        collectStochasticEventData = false;
        storeDimension = store.getNumberOfVariables();
    }

    @Override
    public void putNextStochasticEvent(double time, double[] y, String name) {
        if (super.storeLastFullTrajectory) {
            store.setAllVariableValues(y);
            this.addToFullTrajectory(time, y, store.getExpressionVariablesValues());
        }
        if (this.collectEventData && this.collectStochasticEventData && (super.storeFinalStateDataOnly || super.storeTrajectoryData) ) {
            EventInfo e = new EventInfo(name,time,EventInfo.STOCHASTIC);
            this.firedEvents[currentRunFinalState].add(e);
        }        
    }

    public void collectStochasticEventData(boolean collectStochasticEventData) {
        this.collectStochasticEventData = collectStochasticEventData;
    }


    public TrajectoryStatistics getTrajectoryStatistics() {    
        TrajectoryStatistics stat = new TrajectoryStatistics();
        stat.data = super.data;
        stat.finalData = super.finalData;
        stat.average = super.average;
        stat.covariance = super.covariance;
        stat.finalAverage = super.finalAverage;
        stat.finalCovariance = super.finalCovariance;
        stat.finalKurtosis = super.finalKurtosis;
        stat.finalSkew = super.finalSkew;
        stat.skew = super.skew;
        stat.kurtosis = super.kurtosis;
        stat.totalSteps = super.totalSteps;
        stat.averageTotalSteps = super.averageTotalSteps;
        stat.varianceTotalSteps = super.varianceTotalSteps;
        stat.runs = super.runs;
        stat.vars = super.vars;
        stat.points = super.points;
        stat.names = super.varNames;
        stat.modelName = super.modelName;
        stat.initialized = true;
        stat.statisticsComputed = true;
        stat.rawDataDeleted = stat.data == null;
        stat.keepFinalData = stat.finalData == null;
        stat.differentFinalTimes = stat.finalCovariance[0][0] != 0;
        stat.stepTrajectories = false;
        stat.unevenLengthTrajectories = false;
        stat.stepSize = 0;
        stat.timeStep = super.deltaTime;
        stat.initialSavingTime = super.initialSavingTime;
        return stat;   
    }

    
}
