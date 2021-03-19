/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking;

import java.util.ArrayList;
//import com.eggloop.flow.simhya.simhya.dataprocessing.chart.Line2DChart;
import com.eggloop.flow.simhya.simhya.dataprocessing.chart.LineChart;
import com.eggloop.flow.simhya.simhya.dataprocessing.chart.Plot2DTrajectory;
import com.eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;

/**
 *
 * @author Luca
 */
public interface SMCoutput {
    
    public String shortPrint();
    public String extendedPrint();
    public String signalsToString(boolean all);
    
    public ArrayList<Plot2DTrajectory> getTrajectory();
    public LineChart plotTrajectory();
    public LineChart plotSignals(boolean all);
    public Plot2DTrajectory getTopSignal();
    
    public boolean isTrajectory();
    public boolean isFormula(String f);
    public String getFormula();
    public boolean hasSignals();
    
    public boolean isEstimate();
    public boolean isTest();
    
    public Truth getTestResult();
    public double getEstimateTrue();
    public double getLowerConfidenceBoundTrue();
    public double getUpperConfidenceBoundTrue();
    public double getLowerConfidenceBoundTrue(double newconfidence);
    public double getUpperConfidenceBoundTrue(double newconfidence);
    
    public double getEstimateFalse();
    public double getLowerConfidenceBoundFalse();
    public double getLowerConfidenceBoundFalse(double newconfidence);
    public double getUpperConfidenceBoundFalse();
    public double getUpperConfidenceBoundFalse(double newconfidence);
    
}
