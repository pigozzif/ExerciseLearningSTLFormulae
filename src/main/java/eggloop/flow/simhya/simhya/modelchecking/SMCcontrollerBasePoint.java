/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;

import java.util.ArrayList;
import java.util.Locale;
import eggloop.flow.simhya.simhya.dataprocessing.chart.LineChart;
import eggloop.flow.simhya.simhya.dataprocessing.chart.Plot2DTrajectory;
import eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;

/**
 *
 * @author luca
 */
public abstract class SMCcontrollerBasePoint implements SMCoutput, SMCcontroller {
    
    double estimate1,estimate0;
    ConfidenceInterval bound1,bound0;
    
    long total;
    long good;
    long bad;
    long totalCalls;
    
    String formula;
    Truth last;
   
    boolean stop = false;
    boolean finalised = false;
    
    ArrayList<Plot2DTrajectory> signals = null;
    
    double confidence;
    long maxCalls;
    

    public SMCcontrollerBasePoint() {
        stop = false;
        estimate1 = 0;
        estimate0 = 0;
        bound0 = null;
        bound1 = null;
        total = 0;
        bad = 0;
        good = 0;
        totalCalls = 0;
        confidence = 0.95;
        finalised = false;
        maxCalls = 100000;
    }
    
    public void setMaxCalls(long mc) {
        this.maxCalls = mc;
    }
    
    public void setConfidence(double confidence) {
        if (0 < confidence && confidence < 1)
         this.confidence = confidence;
    }

    public void setCurrent(int c) { }
    
    
    
    abstract ConfidenceInterval getConfidenceInterval(double K, double N, boolean good);
    abstract ConfidenceInterval getConfidenceInterval(double K, double N, double conf, boolean good);
    
    
    public double getEstimateTrue() {
        return estimate1;
    }
    public double getLowerConfidenceBoundTrue() {
        return bound1.lower;
    }
    public double getUpperConfidenceBoundTrue() {
        return bound1.upper;
    }
    public double getLowerConfidenceBoundTrue(double newconfidence) {
        newconfidence = (0< newconfidence && newconfidence < 1 ? newconfidence : confidence);
        return getConfidenceInterval(good, total, newconfidence,true).lower;
    }
    public double getUpperConfidenceBoundTrue(double newconfidence) {
        newconfidence = (0< newconfidence && newconfidence < 1 ? newconfidence : confidence);
        return getConfidenceInterval(good, total, newconfidence,true).upper;
    }
    
    public double getEstimateFalse() {
        return estimate0;
    }
    public double getLowerConfidenceBoundFalse() {
        return bound0.lower;
    }
    public double getLowerConfidenceBoundFalse(double newconfidence) {
        newconfidence = (0< newconfidence && newconfidence < 1 ? newconfidence : confidence);
        return getConfidenceInterval(bad, total, newconfidence,false).lower;
    }
    public double getUpperConfidenceBoundFalse() {
        return bound0.upper;
    }
    public double getUpperConfidenceBoundFalse(double newconfidence) {
        newconfidence = (0< newconfidence && newconfidence < 1 ? newconfidence : confidence);
        return getConfidenceInterval(bad, total, newconfidence,false).upper;
    }
    
    
    
    
    public boolean hasSignals() {
        return this.signals != null;
    }

    public void setSignals(ArrayList<Plot2DTrajectory> signals) {
        this.signals = signals;
    }

    public LineChart plotSignals(boolean all) {
        if (this.signals != null) {
            if (!this.signalSeparated())
                this.separateSignals();
            LineChart chart;
            if (all)
                chart = new LineChart(this.signals,"time","truth value");
            else 
                chart = new LineChart(this.signals.get(0),"time","truth value");
            return chart;
        } else
            return null;
    }
    
    private void separateSignals() {
        if (this.signals == null)
            return;
        for (int i=0;i<this.signals.size();i++) {
            Plot2DTrajectory T = this.signals.get(i);
            for (int j=0;j<T.y.length;j++)
                T.y[j] *= 1.0 + ((double)i)/10.0; 
        }
    }
    
    private boolean signalSeparated() {
        if (this.signals == null)
            return false;
        if (this.signals.size() == 1)
            return true;
        else {
            Plot2DTrajectory T = this.signals.get(1);
            for (int i=0;i<T.y.length;i++)
                if (T.y[i] > 1)
                    return true;
            return false;
        }    
    }
    
    public Plot2DTrajectory getTopSignal() {
        if (this.signals == null)
            return null;
        else return this.signals.get(0);
    }

    public String signalsToString(boolean all) {
        if (this.signals != null) {
            String s = "";
            if (all) {
                //header
                for (Plot2DTrajectory T : this.signals) {
                    s += String.format(Locale.UK, "%30s", "time");
                    s += String.format(Locale.UK, "%30s", T.name);
                }
                s += "\n";
                boolean stop = false;
                int i = 0;
                while (!stop) {
                    stop = true;
                    for (Plot2DTrajectory T : this.signals) {
                        if (i < T.x.length) {
                            s += String.format(Locale.UK, "%30.5f", T.x[i]);
                            s += String.format(Locale.UK, "%30.5f", T.y[i]);
                            stop = false;
                        }
                        else {
                            s += String.format(Locale.UK, "%30s", "");
                            s += String.format(Locale.UK, "%30s", "");
                        }
                    }
                    s += "\n";
                    i++;
                }
            }
            else  {
                Plot2DTrajectory T = this.signals.get(0);
                s += String.format(Locale.UK, "%30s", "time");
                s += String.format(Locale.UK, "%30s", T.name);
                s += "\n";
                for (int i=0;i<T.x.length;i++) {
                    s += String.format(Locale.UK, "%30.5f", T.x[i]);
                    s += String.format(Locale.UK, "%30.5f", T.y[i]);
                    s += "\n";
                }
            }
            return s;
        } else
            return null;        
    }

    
    public Truth getLast() {
        return this.last;
    }
    
    
    public ArrayList<Plot2DTrajectory> getTrajectory() {
        return null;
    }

    public boolean isExplorator() {
        return false;
    }

    
    public LineChart plotTrajectory() {
        return null;
    }
    

    public boolean isTrajectory() {
        return false;
    }
   
    
    

    
    
    
    
    public void setFormula(String formula) {
        this.formula = formula;
    }
    
    public boolean isFormula(String f) {
        return this.formula.equals(f);
    }
    
    public String getFormula() {
        return this.formula;
    }
    
    
    public void setPrior(double alpha, double beta) { }
    public void useWilson() { }
    public void useNormal() { }
    
    public boolean stop() {
        return stop;
    }
    
    public void addPoint(Truth[] truth) {
        totalCalls++;
        if (truth[0]==Truth.TRUE) { good++; total++;}
        else if (truth[0]==Truth.FALSE) { bad++; total++;}     
        last = truth[0];
        this.checkStopCondition();  
    }
    
    abstract void checkStopCondition();
    
    public void addGood(int n) {
        this.good += n;
        this.total += n;
    }
    
    public void addBad(int n) {
        this.bad += n;
        this.total += n;
    }
    
    
    
    
    
}
