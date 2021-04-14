/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;

import umontreal.iro.lecuyer.probdist.NormalDist;

/**
 *
 * @author luca
 */
public class SMCfrequentistEstimator extends SMCestimator {
    
    boolean Wilson = true;
    boolean regularise = false;
    int reg_good = 1;
    int reg_bad = 1;

   
    
    
    
    public void setChernoff() { 
        super.adaptiveSample = false;
        super.samples = chernoffN(error,confidence);
    }
    
    public void useWilson() {
        this.Wilson = true;
    }
    
    public void useNormal() {
        this.Wilson = false;
    }
    
    public void regularise(int good, int bad) { 
        this.regularise = true;
        this.reg_good = good;
        this.reg_bad = bad;
    }
    
    
    public long chernoffN(double error, double confidence) {
        double N = -2*Math.log(1-confidence)/Math.pow(error, 2);
        return (long)Math.ceil(N);
    }
    
    public double chernoffE(long N, double confidence) {
        return  Math.sqrt(-2*Math.log(1-confidence)/N);
    }
    
    public double chernoffP(long N, double error) {
        return 1-Math.exp(-Math.pow(error, 2)*N/2);
    }
    
    
    
    ConfidenceInterval getConfidenceInterval(double K, double N, boolean good) {
        return getConfidenceInterval(K, N, confidence, good);
    }
    
    ConfidenceInterval getConfidenceInterval(double K, double N, double conf, boolean good) {
        double z = NormalDist.inverseF01(1-(1-conf)/2);
        double l = 0;
        double u = 0;
        if (regularise) {
            K = K + (good ? this.reg_good : this.reg_bad);
            N = N + this.reg_bad + this.reg_good ;
        }
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
        double g = (regularise ? good + this.reg_good : good);
        double b = (regularise ? bad  + this.reg_bad  : bad );
        double T = (regularise ? total + this.reg_good + this.reg_bad  : total);
        this.estimate1 = g/T;
        this.estimate0 = b/T;
        this.bound1 = getConfidenceInterval(good, total,true);
        this.bound0 = getConfidenceInterval(bad, total,false);
        this.finalised = true;
    }

    @Override
    public SMCestimator copy() {
        SMCfrequentistEstimator ne = new SMCfrequentistEstimator();
        ne.Wilson = this.Wilson;
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
              
        ne.reg_bad = this.reg_bad;
        ne.reg_good = this.reg_good;
        ne.regularise = this.regularise;
        
        return ne;
    }
    
    
    
    
}
