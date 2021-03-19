/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing;

import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;

/**
 *
 * @author Luca
 */
public class StochasticDataCollector extends AbstractDataCollector {
    

    public StochasticDataCollector(FlatModel model) {
        super(model);
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
    
    
    
//        
//        
//        
//        int runs,points,vars;
//        double[][] data;
//        Trajectory t;
//        boolean diffFinalTimes = false;
//        boolean unevenLength = false;
//
//        runs = super.trajectories.size();
//        if (runs <= 1)
//            throw new DataException("Too few trajectories to collect statistics!!!");
//        t = super.trajectories.get(0);
//        vars = t.dataDimension;
//        points = t.savedPoints;
//        for (int k=1;k<runs;k++) {
//            t = super.trajectories.get(k);
//            if (t.savedPoints > points) {
//                //trajectories have different length
//                points = t.savedPoints;
//                unevenLength  = true;
//            }
//        }
//        if (!super.saveByStep) {
//            t = super.trajectories.get(0);
//            double ft = t.finalState[0];
//            for (int k=1;k<runs;k++) {
//                t = super.trajectories.get(k);
//                if (t.finalState[0] != ft)
//                    diffFinalTimes = true;
//            }
//        }
//       
//        
//        TrajectoryStatistics s = new TrajectoryStatistics(vars,points,runs);
//        s.modelName = super.modelName;
//        s.fullLatexDocument = super.fullLatexDocument;
//        s.stepTrajectories = super.saveByStep;
//        s.stepSize = super.deltaStep;
//        s.timeStep = super.deltaTime;
//        s.initialSavingTime = super.initialSavingTime;
//        s.differentFinalTimes = diffFinalTimes;
//        s.unevenLengthTrajectories = unevenLength;
//        s.names = super.trajectories.get(0).names;
//        for (int k=0;k<runs;k++) {
//            t = super.trajectories.get(k);
//            data = t.data;
//            for (int i=0;i<vars;i++)
//                for (int j=0;j<t.savedPoints;j++)
//                    s.data[i][j].add(data[i][j]);
//            for (int i=0;i<vars;i++) {
//                s.finalData[i].add(t.finalState[i]);
//                s.finalSteps.add(t.totalSteps);
//            }
//        }
//        s.initialized = true;
//        s.computeStatistics();
//        return s;
//    }

}
