/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;

import java.util.Locale;
import eggloop.flow.simhya.simhya.modelchecking.mtl.Threshold;
import eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;
import umontreal.iro.lecuyer.probdist.NormalDist;

/**
 *
 * @author luca
 */
public class SMCtestWald extends SMCtest {
    boolean Wilson = true;
    private boolean init = false;
    double epsilon = 0.01;
    double wald;
    double aboveComp, belowComp, deltaTrue, deltaFalse;
    
    
    
    
    public SMCtestWald(Threshold threshold) {
        super(threshold);
    }
    
    
    @Override
    public void setThresholdTolerance(double tol) { 
        this.epsilon = tol;
    }
    
    
    void checkStopCondition() {
        if (!init) {
            wald = 0;
            aboveComp = Math.log((1-power)/(1-significance));
            belowComp = Math.log(power/significance);
            double p = threshold.getThreshold();
            deltaTrue = Math.log((p-epsilon)/(p+epsilon));
            deltaFalse = Math.log((1-p+epsilon)/(1-p-epsilon));  
            init = true;
        }
        if (last == Truth.TRUE) {
            wald += deltaTrue;
        } else if (last == Truth.FALSE) {
            wald += deltaFalse;
        }
        if (wald <= aboveComp || wald >= belowComp)
            stop = true;
        else if (totalCalls >= super.maxCalls)
            stop = true;            
    }
    
    
    @Override
    String getTestStatistics() {
        String s = "Wald sequential test. Wn = " + String.format(Locale.UK, "%.5f", wald) + ".\n";
        s += "Above " + String.format(Locale.UK, "%.5f", threshold.getThreshold() + epsilon) + " if Wn <= " + String.format(Locale.UK, "%.5f", this.aboveComp) + ".\n";
        s += "Below " + String.format(Locale.UK, "%.5f", threshold.getThreshold() - epsilon) + " if Wn >= " + String.format(Locale.UK, "%.5f", this.belowComp) + ".\n";
        return s;
    }

    @Override
    public void useNormal() {
        this.Wilson = false;
    }

    @Override
    public void useWilson() {
        this.Wilson = true;
    }
  
    
    ConfidenceInterval getConfidenceInterval(double K, double N, boolean good) {
        return getConfidenceInterval(K, N, confidence, good);
    }
    
    ConfidenceInterval getConfidenceInterval(double K, double N, double conf, boolean good) {
        double z = NormalDist.inverseF01(1-(1-conf)/2);
        double l = 0;
        double u = 0;
        double P = K/N;
        if (this.Wilson) {
            double PP = (P + 1/(2*N)*z*z);
            double p = z*Math.sqrt(P*(1-P)/N + z*z/(4*N*N));
            l = Math.max((PP - p)/(1+z*z/N),0.0);
            u = Math.min((PP + p)/(1+z*z/N),1.0);
        } else {
           double p = z*Math.sqrt(P*(1-P)/N);
           l = Math.max(P-p,0.0);
           u = Math.min(P+p,1.0);
        }
        ConfidenceInterval B = new ConfidenceInterval(l,u);
        return B;
    }

    
    
    
    
    
    
    public void finalization() {
        double g = good;
        double b = bad;
        double T = total;
        this.estimate1 = g/T;
        this.estimate0 = b/T;
        this.bound1 = getConfidenceInterval(good, total,true);
        this.bound0 = getConfidenceInterval(bad, total,false);
        
        if (wald <= aboveComp) {
            //I am above threshold
            if (threshold.aboveThreshold()) 
                super.testValue = Truth.TRUE;
            else if (threshold.belowThreshold())
                super.testValue = Truth.FALSE;
            else
                super.testValue = Truth.UNDEFINED;          
        }
        else if (wald >= belowComp) 
            //I am below threshold
            if (threshold.aboveThreshold()) 
                super.testValue = Truth.FALSE;
            else if (threshold.belowThreshold())
                super.testValue = Truth.TRUE;
            else
                super.testValue = Truth.UNDEFINED;   
        else 
            super.testValue = Truth.UNDEFINED;    
        this.finalised = true;
    }
    

    
    
    
}
