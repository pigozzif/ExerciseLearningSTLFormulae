/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;

import umontreal.iro.lecuyer.probdist.BetaDist;

/**
 *
 * @author luca
 */
public class SMCbayesianEstimator extends SMCestimator {
    
    BetaPrior prior = new BetaPrior();
    double toleranceFactor = 0.01;
    int bayesianPrecision = 6;
    
    
    
    @Override
    public void setPrior(double alpha, double beta) {
        this.prior = new BetaPrior(alpha,beta);
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
        if (upper-lower < this.error*this.toleranceFactor) 
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
        this.finalised = true;
    }
    
    
    @Override
    public SMCestimator copy() {
        SMCbayesianEstimator ne = new SMCbayesianEstimator();
        ne.adaptiveSample = this.adaptiveSample;
        ne.adaptiveStep = this.adaptiveStep;
        ne.bad = this.bad;
        ne.bound0 = this.bound0;
        ne.bound1 = this.bound1;
        ne.confidence = this.confidence;
        ne.error = this.error;
        ne.estimate0 = this.estimate0;
        ne.estimate1 = this.estimate1;
        ne.finalised = this.finalised;
        ne.formula = this.formula;
        ne.good = this.good;
        ne.last = this.last;
        ne.maxCalls = this.maxCalls;
        ne.samples = this.samples;
        ne.signals = this.signals;
        ne.stop = this.stop;
        ne.total = this.total;
        ne.totalCalls = this.totalCalls;
              
        ne.bayesianPrecision = this.bayesianPrecision;
        ne.prior = this.prior;
        ne.toleranceFactor = this.toleranceFactor;
        
        return ne;
    }
    
    
}
