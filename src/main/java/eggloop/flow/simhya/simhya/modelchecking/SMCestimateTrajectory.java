/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import eggloop.flow.simhya.simhya.dataprocessing.chart.LineChart;
import eggloop.flow.simhya.simhya.dataprocessing.chart.Plot2DTrajectory;
import eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;


/**
 *
 * @author Luca
 */
public class SMCestimateTrajectory implements SMCcontroller, SMCoutput {
    
    int points;
    int current;
    SMCestimator[] estimator;
    String formula = null;
    double [] paramVals = null;
    String paramName;
    long total;
    

    public SMCestimateTrajectory(int points, double[] vals, String name) {
        this.points = points;
        this.estimator = new SMCestimator[points];
        this.current = 0;
        this.paramVals = vals; //?
        this.paramName = name;
        total = 0;
    }

    public boolean isExplorator() {
        return true;
    }
    
    
    public void setEstimators(SMCestimator est) {
        this.estimator[0] = est;
        for (int i=1;i<points;i++)
            this.estimator[i] = est.copy();
    }
    
    public void setCurrent(int c) {
        if (c<0 || c>= this.points)
            throw new RuntimeException("not enough points");
        else
            this.current = c;
    }
    
    
    public void addPoint(Truth[] truth) {
        if (!this.estimator[current].stop())
            this.estimator[current].addPoint(truth);
        if (current == 0) total++;
    }

    public void finalization() {
        for (int i=0;i<points;i++)
            this.estimator[i].finalization();
    }

    public void setFormula(String formula) {
        this.formula=formula;
    }

    public void setSignals(ArrayList<Plot2DTrajectory> signals) {
        this.estimator[0].setSignals(signals);
    }

    public boolean stop() {
        boolean stop = true;
        for (int i=0;i<points;i++) 
            stop = stop && this.estimator[i].stop();
        return stop;
    }

    public String extendedPrint() {
        String s = "";
        s += String.format(Locale.UK, "%30s %30s %30s %30s\n", this.paramName, "estimate", "lowerBound", "upperBound");
        for (int i=0;i<points;i++) {
            s += String.format(Locale.UK, "%30.5f %30.5f %30.5f %30.5f\n", this.paramVals[i], this.estimator[i].getEstimateTrue(), this.estimator[i].getLowerConfidenceBoundTrue(), this.estimator[i].getUpperConfidenceBoundTrue());
        }       
        return s;
    }

    public String getFormula() {
        return this.formula;
    }
    
    
   
    
    
    public Plot2DTrajectory getTopSignal() {
        return this.estimator[0].getTopSignal();
    }

    public ArrayList<Plot2DTrajectory> getTrajectory() {
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        double[] x = new double[points];
        double[] u = new double[points];
        double[] l = new double[points];
        for (int i=0;i<points;i++) {
            x[i] = this.estimator[i].getEstimateTrue();
            l[i] = this.estimator[i].getLowerConfidenceBoundTrue();
            u[i] = this.estimator[i].getUpperConfidenceBoundTrue();
        }  
        Plot2DTrajectory t = new Plot2DTrajectory();
        t.x = this.paramVals;
        t.y = x;
        t.name = this.formula;
        list.add(t);
        t = new Plot2DTrajectory();
        t.x = this.paramVals;
        t.y = l;
        t.name = this.formula + " bound";
        list.add(t);
        t = new Plot2DTrajectory();
        t.x = this.paramVals;
        t.y = u;
        t.name = this.formula + " bound";
        list.add(t);
        return list;
    }



    public boolean hasSignals() {
        return this.estimator[0].hasSignals();
    }

    public boolean isEstimate() {
        return true;
    }

    public boolean isFormula(String f) {
        return this.formula.equals(f);
    }

    public boolean isTest() {
        return false;
    }

    public boolean isTrajectory() {
        return true;
    }

    public LineChart plotSignals(boolean all) {
        return this.estimator[0].plotSignals(all);
    }

    public LineChart plotTrajectory() {
        ArrayList<Plot2DTrajectory> list = this.getTrajectory();
        LineChart chart = new LineChart(list,this.paramName,"probability");
        return chart;
    }

    public String shortPrint() {
        String s = "Formula " + this.formula + ". Total runs " + total + ".\n";
        s += this.extendedPrint();
//        for (int i=0;i<this.points;i++) {
//            s += "* " + this.paramName + " = " + String.format(Locale.UK, "%.5f", this.paramVals[i]) + "\n";
//            s += this.estimator[i].shortPrint();
//        }
        return s;
    }

    public String signalsToString(boolean all) {
        return this.estimator[0].signalsToString(all);
    }
   
    
    
    
    
    
    
    
     public double getEstimateFalse() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public double getEstimateTrue() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public double getLowerConfidenceBoundFalse() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public double getLowerConfidenceBoundFalse(double newconfidence) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public double getLowerConfidenceBoundTrue() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public double getLowerConfidenceBoundTrue(double newconfidence) {
        throw new UnsupportedOperationException("Not supported.");
    }

    
    public Truth getTestResult() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public double getUpperConfidenceBoundFalse() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public double getUpperConfidenceBoundFalse(double newconfidence) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public double getUpperConfidenceBoundTrue() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public double getUpperConfidenceBoundTrue(double newconfidence) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
