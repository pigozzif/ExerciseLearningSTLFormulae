/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking;

import com.eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;

import java.util.Locale;


/**
 * @author Luca
 */
public abstract class SMCestimator extends SMCcontrollerBasePoint {

    long samples = 0;
    boolean adaptiveSample;
    int adaptiveStep = 10;
    double error = 0.01;


    public Truth getTestResult() {
        return Truth.UNDEFINED;
    }

    public boolean isEstimate() {
        return true;
    }

    public boolean isTest() {
        return false;
    }


    public void regularise(double good, double bad) {
    }


    public void setFixed(long samples) {
        this.samples = samples;
        this.adaptiveSample = false;
    }

    public void setAdaptive() {
        this.adaptiveSample = true;
        this.adaptiveStep = 10;
    }

    public void setAdaptive(int step) {
        this.adaptiveSample = true;
        this.adaptiveStep = step;
    }

    public void setError(double error) {
        this.error = error;
    }

    public void setChernoff() {
    }


    public void setAdaptiveStep(int adaptiveStep) {
        this.adaptiveStep = adaptiveStep;
    }


    void checkStopCondition() {
        if (this.adaptiveSample) {
            if (total % this.adaptiveStep == 0) {
                ConfidenceInterval b;
                b = getConfidenceInterval(good, total, true);
                if (b.length() <= 2 * error)
                    stop = true;
            }
        } else if (total >= this.samples)
            stop = true;
        else if (totalCalls >= super.maxCalls)
            stop = true;
    }


    public long getGood() {
        return this.good;
    }

    public long getBad() {
        return this.bad;
    }

    public long getTotal() {
        return this.total;
    }


    public String shortPrint() {
        String s = "Formula " + this.formula + ":\n";
        if (this.finalised) {
            s += String.format(Locale.UK, "%s est: %.5f; int: [%.5f, %.5f]; conf: %.5f; runs: %d\n", "satisfaction prob: ", this.estimate1, this.bound1.lower, this.bound1.upper, confidence, total);
            s += String.format(Locale.UK, "%s est: %.5f; int: [%.5f, %.5f]; conf: %.5f; runs: %d\n", "falsification prob: ", this.estimate0, this.bound0.lower, this.bound0.upper, confidence, total);
        } else {
            ConfidenceInterval B = this.getConfidenceInterval(good, total, true);
            s += String.format(Locale.UK, "%s est: %.5f; int: [%.5f, %.5f]; conf: %.5f; runs: %d\n", "satisfaction prob: ", (double) good / total, B.lower, B.upper, confidence, total);
            B = this.getConfidenceInterval(bad, total, false);
            s += String.format(Locale.UK, "%s est: %.5f; int: [%.5f, %.5f]; conf: %.5f; runs: %d\n", "falsification prob: ", (double) bad / total, B.lower, B.upper, confidence, total);
        }
        return s;
    }

    public String extendedPrint() {
        String s = "Formula " + this.formula + ":\n";
        s += String.format(Locale.UK, "%20s %20s %20s %20s %20s %20s\n", "type", "estimate", "lower_bound", "upper_bound", "confidence", "samples");
        if (this.finalised) {
            s += String.format(Locale.UK, "%20s %20.5f %20.5f %20.5f %20.5f %20d\n", "true_prob: ", this.estimate1, this.bound1.lower, this.bound1.upper, confidence, total);
            s += String.format(Locale.UK, "%20s %20.5f %20.5f %20.5f %20.5f %20d\n", "false_prob: ", this.estimate0, this.bound0.lower, this.bound0.upper, confidence, total);
        } else {
            ConfidenceInterval B = this.getConfidenceInterval(good, total, true);
            s += String.format(Locale.UK, "%20s %20.5f %20.5f %20.5f %20.5f %20d\n", "true_prob: ", (double) good / total, B.lower, B.upper, confidence, total);
            B = this.getConfidenceInterval(bad, total, false);
            s += String.format(Locale.UK, "%20s %20.5f %20.5f %20.5f %20.5f %20d\n", "false_prob: ", (double) bad / total, B.lower, B.upper, confidence, total);
        }
        return s;
    }


    abstract public SMCestimator copy();


}
