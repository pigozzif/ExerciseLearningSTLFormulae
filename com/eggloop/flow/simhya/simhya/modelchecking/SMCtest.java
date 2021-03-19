/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking;

import com.eggloop.flow.simhya.simhya.modelchecking.mtl.Threshold;
import com.eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;

import java.util.Locale;

/**
 * @author Luca
 */
public abstract class SMCtest extends SMCcontrollerBasePoint {

    double significance = 0.05;
    double power = 0.95;
    Threshold threshold;
    Truth testValue;


    public SMCtest(Threshold threshold) {
        this.threshold = threshold;
    }


    public void setTestPower(double significance, double power) {
        this.significance = significance;
        this.power = power;
    }


    public Truth getTestResult() {
        return testValue;
    }

    public boolean isEstimate() {
        return false;
    }

    public boolean isTest() {
        return true;
    }


    public void setThresholdTolerance(double tol) {
    }

    abstract String getTestStatistics();


    public String shortPrint() {
        String s = "Formula " + this.formula + " (testing probability " + threshold.toString() + ")\n";
        if (this.finalised) {
            if (this.testValue == Truth.TRUE)
                s += "Property is TRUE.\n";
            else if (this.testValue == Truth.FALSE)
                s += "Property is FALSE.\n";
            else
                s += "Property is UNDECIDABLE after " + super.totalCalls + " simulation runs.\n";
            s += this.getTestStatistics();
            s += String.format(Locale.UK, "Total simulations: %d; defined: %d; true: %d; false: %d.\n", totalCalls, total, good, bad);
            s += "Estimated probability values\n";
            s += String.format(Locale.UK, "%s est: %.5f; int: [%.5f, %.5f]; conf: %.5f;  \n", "satisfaction prob: ", this.estimate1, this.bound1.lower, this.bound1.upper, confidence);
            s += String.format(Locale.UK, "%s est: %.5f; int: [%.5f, %.5f]; conf: %.5f;  \n", "falsification prob: ", this.estimate0, this.bound0.lower, this.bound0.upper, confidence);
        } else {
            s += String.format(Locale.UK, "test not completed! current estimate: %.5f\n", (double) good / total);
        }
        return s;
    }


    public String extendedPrint() {
        return this.shortPrint();
    }


}
