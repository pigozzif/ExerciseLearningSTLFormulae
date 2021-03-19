/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking.mtl;

import java.util.Locale;


/**
 *
 * @author Luca
 */
public class Threshold {
    ThresholdType type;
    String thresholdParameter; 
    double threshold;

    public Threshold(ThresholdType type, String thresholdParameter) {
        this.type = type;
        try {  
            this.threshold = Double.parseDouble(thresholdParameter);
            this.thresholdParameter = null;
        } catch (NumberFormatException e) {
            this.thresholdParameter = thresholdParameter;
            this.threshold = Double.NEGATIVE_INFINITY;
        }
    }
    
    public void setThresholdValue(double v) {
        this.threshold = v;
    }

    public String getThresholdParameter() {
        return thresholdParameter;
    }

    public ThresholdType getType() {
        return type;
    }
    
    public boolean aboveThreshold() {
        return type == ThresholdType.GREATER || type == ThresholdType.GREATER_EQUAL;
    }
    
    public boolean belowThreshold() {
        return type == ThresholdType.LESS || type == ThresholdType.LESS_EQUAL;
    }
    
    public double getThreshold() {
        return this.threshold;
    }
    
    /**
     * Checks if x satisfies the threshold constraint
     * @param x
     * @return 
     */
    public boolean check(double x) {
        if (threshold == Double.NEGATIVE_INFINITY)
            throw new RuntimeException("Expression for threshold not set!");
        switch (this.type) {
            case LESS:
               return x < threshold;
            case LESS_EQUAL:
               return x <= threshold;
            case GREATER:
               return x > threshold;
            case GREATER_EQUAL:
               return x >= threshold;
            default:
                return false;
        } 
    }

    @Override
    public String toString() {
        return " " + this.type.toString() + " " + 
                (this.thresholdParameter == null ? String.format(Locale.UK, "%.4f", this.threshold) : this.thresholdParameter);
    }
    
    
    
    
    
    
    
}


