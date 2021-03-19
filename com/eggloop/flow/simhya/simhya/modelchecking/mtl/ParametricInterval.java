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
public class ParametricInterval {
    String lowerParameter;
    String upperParameter;
    double lower;
    double upper;
    boolean lowerIsZero;
    boolean upperIsInfinity;
    boolean undefined;
    

    
    public ParametricInterval() {
        this.lowerParameter = null;
        this.upperParameter = null;
        this.lower = 0.0;
        this.upper = Double.POSITIVE_INFINITY;
        this.upperIsInfinity = true;
        this.lowerIsZero = true;
        this.undefined = false;
    }
    
    public void setUndefined() {
        this.undefined =  true;
    }

    
    
    @Override
    public boolean equals(Object obj) {
       ParametricInterval interval; 
       if (!(obj instanceof  ParametricInterval))
           return false;
       else interval = (ParametricInterval)obj;
       if (this.undefined || interval.undefined)
            return true;
        boolean answer = true;
        if (lowerParameter != null) {
           if (interval.lowerParameter != null)
               answer = answer &&  this.lowerParameter.equals(interval.lowerParameter);
           else answer =  false;
        } else {
            if (interval.lowerParameter != null)
               answer = false;
           else {
                answer = answer && this.lower == interval.lower;
           }
        }
        if (this.upperParameter != null) {
           if (interval.upperParameter != null)
               answer = answer &&  this.upperParameter.equals(interval.upperParameter);
           else answer =  false;
        } else {
           if (this.upperIsInfinity)  { 
               if(!interval.upperIsInfinity)
                   answer = false;
           } else {
               if(interval.upperIsInfinity)
                   answer = false;
           }
           if (interval.upperParameter != null)
               answer = false;
           else {
                answer = answer && this.upper == interval.upper;
           }
        }
        return answer;
    }

    
    /**
     * Checks if the interval contains the given parameter.
     * @param parameter
     * @return 
     */
    public boolean contains(String parameter) {
        if (parameter == null)
            return true;
        if (this.lowerParameter != null && this.lowerParameter.equals(parameter))
            return true;
        else if (this.upperParameter != null && this.upperParameter.equals(parameter))
            return true;
        else
            return false;
    }
    
    
    
    public void setLowerValue(double v) {
        this.lower = v;
        if (v > 0.0)
            this.lowerIsZero = false;
        else {
            this.lower = 0.0;
            this.lowerIsZero = true;
        }
    }
    
    public void setUpperValue(double v) {
        this.upper = v;
        if (v < 0)
            throw new RuntimeException("Upper value of time bound intervals in formulae cannot be negative!");
        if (!Double.isInfinite(v))
            this.upperIsInfinity = false;
        else 
            this.upperIsInfinity = true;
    }
    
    public boolean checkConsistency() {
        return (lower <= upper);
    }

    public String getLowerParameter() {
        return lowerParameter;
    }

    public String getUpperParameter() {
        return upperParameter;
    }

    public void setLower(String lower) {
        try {  
            this.lower = Double.parseDouble(lower);
        } catch (NumberFormatException e) {
            this.lowerParameter = lower;
        } finally {
            if (this.lower > 0 || this.lowerParameter != null)
                this.lowerIsZero = false;
            else {
                this.lower = 0.0;
                this.lowerIsZero = true;
            }
        }
        
    }

    public void setUpper(String upper) {
        try {  
            this.upper = Double.parseDouble(upper);
        } catch (NumberFormatException e) {
            this.upperParameter = upper;
        } finally {
            if (!Double.isInfinite(this.upper)  || this.upperParameter != null )
            this.upperIsInfinity = false;
        else 
            this.upperIsInfinity = true;
        }
    }

    public void setUpperToInfinity() {
        this.upperIsInfinity = true;
        this.upper = Double.POSITIVE_INFINITY;
        this.upperParameter = null;
    }

    public void setLowerToZero() {
        this.lowerIsZero = true;
        this.lower = 0.0;
        this.lowerParameter = null;
    }

    public double getLower() {
        return lower;
    }

    public boolean isLowerZero() {
        return lowerIsZero;
    }

    public double getUpper() {
        return upper;
    }

    public boolean isUpperInfinity() {
        return upperIsInfinity;
    }
    
    
    
   

    @Override
    public String toString() {
        String s = "[";
        s += (this.lowerIsZero ? "0" : 
                (this.lowerParameter == null ? String.format(Locale.UK, "%.4f", this.lower) : 
                 this.lowerParameter) );
        s += ", ";
        s += (this.upperIsInfinity ? "infinity" : 
                (this.upperParameter == null ? String.format(Locale.UK, "%.4f", this.upper) : 
                 this.upperParameter) );
        s+= "]";        
        return s;
    }
    
}
