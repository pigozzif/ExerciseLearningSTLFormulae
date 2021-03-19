/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.paramexplore;

/**
 *
 * @author Luca
 */
public class ParamRange extends ParamValueSet {
 

    private double first;
    private double last;
    private double step;
    private boolean logarithmic;
    private double logarithmicFirst;
    private int points;
    

    public ParamRange(boolean isParam, String name,  double first, double last, int points, boolean logarithmic) {
        super(isParam,name);
        if (last <= first)
            throw new ExploratorException("The final value must be greater than the first one");
        if (points < 2)
            throw new ExploratorException("Need at least two points");
        if (logarithmic && first <= 0)
            throw new ExploratorException("For logarithmic exploration, numbers in the range must be positive");
        this.first = first;
        this.last = last;
        this.points = points;
        this.logarithmic = logarithmic;
        if (logarithmic) {
            this.logarithmicFirst = Math.log10(first);
            this.step = (Math.log10(last) - Math.log10(first))/(double)(points-1);
        }
        else
            this.step = (last - first)/(double)(points-1);
        this.currentPoint = 0;
    }


    public int getPoints() {
        return points;
    }

    public double getCurrentValue() {
        if (!this.logarithmic)
            return first + currentPoint*step;
        else
            return Math.pow(10, this.logarithmicFirst + currentPoint*step);
    }

    public void setCurrentPoint(int i) {
        if (i < 0 || i >= points )
            throw new IllegalArgumentException("Incorrect point value, must be between 0 and " + (points-1));
        this.currentPoint = i;
    }

    public double getValue(int i) {
          if (i < 0 || i >= points )
            throw new IllegalArgumentException("Incorrect point value, must be between 0 and " + (points-1));
          if (!this.logarithmic)
            return first + i*step;
          else
            return Math.pow(10, this.logarithmicFirst + i*step);
    }

    public boolean isAdmissible(double x) {
        if (!this.logarithmic) {
            if (x < first - TOLERANCE)
                return false;
            double y = (x-first)/step;
            return ( Math.abs(y - Math.floor(y)) < TOLERANCE );
        }
        else {
            if (Math.log10(x) < this.logarithmicFirst - TOLERANCE)
                return false;
            double y = (Math.log10(x)-this.logarithmicFirst)/step;
            return ( Math.abs(y - Math.floor(y)) < TOLERANCE );
        }
    }

    public int getIndexOfClosestPoint(double x) {
        if (!this.logarithmic) {
            int i = (int)Math.round( (x - first)/step );
            if (i < 0) i = 0;
            if (i >= points) i = points-1;
            return i;
        } else {
            int i = (int)Math.round( (Math.log10(x) - this.logarithmicFirst)/step );
            if (i < 0) i = 0;
            if (i >= points) i = points-1;
            return i;
        }
    }
    
    public double getFirst() {
        return this.first;
    }
    
    public double getLast() {
        return this.last;
    }
    
    public boolean isLog() {
        return this.logarithmic;
    }
    
}
