/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking.mtl.signal;

/**
 *
 * @author luca
 */
public class TimeInterval {
    double lower;
    double upper;
    boolean value;
    boolean lowerClosed;
    boolean upperClosed;
    

    public TimeInterval(double start, double end, boolean value) {
        this.lower = start;
        this.upper = end;
        this.value = value;
        this.lowerClosed = true;
        this.upperClosed = true;
    }

    
    
    public TimeInterval(double left, double right, boolean value, boolean leftClosed, boolean rightClosed) {
        this.lower = left;
        this.upper = right;
        this.value = value;
        this.lowerClosed = leftClosed;
        this.upperClosed = rightClosed;
    }
    
    
        
    TimeInterval intersection(TimeInterval I) {
        double u,l; boolean uc,lc;
        if (this.upper < I.upper) {
            u = this.upper;
            uc = this.upperClosed;
        } else if (I.upper < this.upper) {
            u = I.upper;
            uc = I.upperClosed;
        } else {
            u = I.upper;
            uc = this.upperClosed && I.upperClosed;
        }
        if (this.lower > I.lower) {
            l = this.lower;
            lc = this.lowerClosed;
        } else if (I.lower > this.lower) {
            l = I.lower;
            lc = I.lowerClosed;
        } else {
            l = I.lower;
            lc = this.lowerClosed && I.lowerClosed;
        }
        if (l<u || (l==u && lc && uc)) { 
            return new TimeInterval(l,u,this.value && I.value,lc,uc);
        } 
        else return null;
    }
    
    
    
    boolean intersects(TimeInterval I) {
        double u,l; boolean uc,lc;
        if (this.upper < I.upper) {
            u = this.upper;
            uc = this.upperClosed;
        } else if (I.upper < this.upper) {
            u = I.upper;
            uc = I.upperClosed;
        } else {
            u = I.upper;
            uc = this.upperClosed && I.upperClosed;
        }
        if (this.lower > I.lower) {
            l = this.lower;
            lc = this.lowerClosed;
        } else if (I.lower > this.lower) {
            l = I.lower;
            lc = I.lowerClosed;
        } else {
            l = I.lower;
            lc = this.lowerClosed && I.lowerClosed;
        }
        return (l<u || (l==u && lc && uc));
    }
    
    
    
    boolean intersectsNonemptyInterior(TimeInterval I) {
        return Math.max(this.lower, I.lower) < Math.min(this.upper, I.upper); 
    }

    public double getLower() {
        return lower;
    }

    public boolean isLowerClosed() {
        return lowerClosed;
    }

    public double getUpper() {
        return upper;
    }

    public boolean isUpperClosed() {
        return upperClosed;
    }

    public boolean getValue() {
        return value;
    }
    
    
    
    
    public boolean containsInInterior(double t) {
        return lower < t && t < upper;
    }
    
    public boolean isLower(double t) {
        return lower == t;
    }
      
    public boolean isUpper(double t) {
        return upper == t;
    }
    
    public boolean contains(double t) {
        return ( lowerClosed ?  lower <= t : lower < t) 
                && (upperClosed ?  t <= upper : t < upper);
    }
    
    public boolean isOnTheRightOf(double t) {
        return (lowerClosed ? t < lower : t <= lower );
    }
    
    public boolean isOnTheLeftOf(double t) {
        return (upperClosed ? t > upper : t >= upper);
    }
    
    public boolean isPoint() {
        return ( this.lower == this.upper && this.lowerClosed && this.upperClosed);
    }
    
    public boolean isEmpty() {
        return this.lower > this.upper || ( this.lower == this.upper && (!this.lowerClosed || !this.upperClosed));
    }
    
    TimeInterval backshift(double t1, double t2) {
        if (t1>t2) {
            double t = t1; t1=t2; t2=t;
        }
        if (t1 < 0) t1=0;
        double s = Math.max(this.lower - t2,0);
        double e = Math.max(this.upper - t1,0);
        if (e-s > 0) 
            return new TimeInterval(s,e,this.value,this.lowerClosed,this.upperClosed);
        else if (e-s == 0 && this.upper - t1 >= 0 && this.lowerClosed && this.upperClosed  ) 
            return new TimeInterval(s,e,this.value,this.lowerClosed,this.upperClosed);
        else 
            return null;   
    }
    
    
    TimeInterval copy() {
        return new TimeInterval(this.lower,this.upper,this.value,this.lowerClosed,this.upperClosed);
    }
    
    
}
