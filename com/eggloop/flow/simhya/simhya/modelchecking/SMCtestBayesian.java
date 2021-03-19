/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking;

import java.util.Locale;
import com.eggloop.flow.simhya.simhya.modelchecking.mtl.Threshold;
import com.eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;
import umontreal.iro.lecuyer.probdist.BetaDist;

/**
 *
 * @author luca
 */
public class SMCtestBayesian extends SMCtest {
    
    
    BetaPrior prior = new BetaPrior();
    double bayesianTminus1;
    double bayesianT;
    double factor;
    private boolean init = false;
    int bayesianPrecision = 6;
    double Bn;
    double tolerance =  1e-6;
    
    
    
    public SMCtestBayesian(Threshold threshold) {
        super(threshold);
    }
    
    
    @Override
    public void setPrior(double pGood, double pBad) { 
        this.prior = new BetaPrior(pGood,pBad);
    }
    
    
    
    void checkStopCondition() {
        if (!init) {
            this.bayesianTminus1 = Math.min(super.significance,1-super.power);
            this.bayesianT = 1.0/this.bayesianTminus1;
            double Hm = BetaDist.cdf(prior.alpha, prior.beta, this.bayesianPrecision , threshold.getThreshold());
            factor = Hm/(1-Hm);
            Bn = factor;
            init = true;
        }
        double x = BetaDist.cdf(prior.alpha + super.good, prior.beta + super.bad, this.bayesianPrecision , threshold.getThreshold());
        Bn = this.factor*(1.0/x - 1);        
        if (Bn > this.bayesianT || Bn < this.bayesianTminus1)
            stop = true;
        else if (totalCalls >= super.maxCalls)
            stop = true;
    }

    
    @Override
    String getTestStatistics() {
        String s = "Bayesian sequential test. Bn = " + String.format(Locale.UK, "%.5f", Bn) + ";\n";
        s += "Above " + String.format(Locale.UK, "%.5f", threshold.getThreshold()) + " if Bn > " + String.format(Locale.UK, "%.5f", this.bayesianT) + ";\n";
        s += "Below " + String.format(Locale.UK, "%.5f", threshold.getThreshold()) + " if Bn < " + String.format(Locale.UK, "%.5f", this.bayesianTminus1) + ";\n";
        return s;
    }
    
    
    ConfidenceInterval getConfidenceInterval(double K, double N,  boolean good) {
        return this.getConfidenceInterval(K, N, super.confidence, good);
    }
    
    ConfidenceInterval getConfidenceInterval(double K, double N, double conf, boolean good) {
        double alpha = K + (good ? prior.alpha : prior.beta);
        double beta = (N-K) + (good ? prior.beta : prior.alpha);
        double p = alpha/(alpha+beta);
        double d = Math.max(p,1-p);
        double bound = this.bayesianDelta(0, d, alpha, beta, p, conf);
        double l = Math.max(p-bound,0.0);
        double u = Math.min(p+bound,1.0);
        ConfidenceInterval B = new ConfidenceInterval(l,u);
        return B;            
    }
    
    
    
    double bayesianDelta(double lower, double upper, double alpha, double beta, double p, double conf) {
        if (upper-lower < this.tolerance) 
            return upper;
        double middle = (lower+upper)/2;
        double PROB = BetaDist.cdf(alpha, beta, this.bayesianPrecision, Math.min(p+middle,1)) - 
                BetaDist.cdf(alpha, beta, this.bayesianPrecision, Math.max(p-middle,0)); 
        if (PROB > conf) 
            return bayesianDelta(lower,middle,alpha,beta,p,conf);
        else 
            return bayesianDelta(middle,upper,alpha,beta,p,conf);
    }
    
    public void finalization() {
        double g = good + this.prior.alpha;
        double b = bad + this.prior.beta;
        double T = total + this.prior.alpha + this.prior.beta;
        this.estimate1 = g/T;
        this.estimate0 = b/T;
        this.bound1 = getConfidenceInterval(good, total,true);
        this.bound0 = getConfidenceInterval(bad, total,false);  
        
        if (Bn > this.bayesianT) {
            //I am above threshold
            if (threshold.aboveThreshold()) 
                super.testValue = Truth.TRUE;
            else if (threshold.belowThreshold())
                super.testValue = Truth.FALSE;
            else
                super.testValue = Truth.UNDEFINED;          
        }
        else if (Bn < this.bayesianTminus1) 
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
