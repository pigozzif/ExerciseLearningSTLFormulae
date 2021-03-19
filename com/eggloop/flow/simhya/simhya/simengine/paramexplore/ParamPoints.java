/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.paramexplore;

import java.util.ArrayList;

/**
 *
 * @author Luca
 */
public class ParamPoints  extends ParamValueSet  {
    

    private double[] values;
    private int points;

    public ParamPoints(String name, boolean isParameter,  double[] values) {
        super(isParameter,name);
        if (values.length == 0)
            throw new IllegalArgumentException("values array must contain at least one point");
        this.points = values.length;
        this.values = java.util.Arrays.copyOf(values, points);
        this.currentPoint = 0;
        java.util.Arrays.sort(this.values);
    }

    public ParamPoints(String name, boolean isParameter,  ArrayList<Double> values) {
        super(isParameter,name);
        if (values.isEmpty())
            throw new IllegalArgumentException("values list must contain at least one point");
        this.points = values.size();
        this.values  = new double[points];
        for (int i=0;i<points;i++)
            this.values[i] = values.get(i);
        this.currentPoint = 0;
        java.util.Arrays.sort(this.values);
    }

    public int getPoints() {
        return points;
    }
    
    public ArrayList<Double> getPointValues() {
        ArrayList<Double> list = new ArrayList<Double>();
        for (double d : this.values)
            list.add(d);
        return list;
    }
   

    public double getCurrentValue() {
        return this.values[this.currentPoint];
    }

    public int getIndexOfClosestPoint(double x) {
        double d0, distance = Math.abs(values[0]-x);
        for (int i=1;i<points;i++) {
            d0 = Math.abs(values[i]-x);
            if (d0 > distance)
                return i-1;
            else
                distance = d0;
        }
        return points-1;
    }

   

    public double getValue(int i) {
        if (i < 0 || i >= points )
            throw new IllegalArgumentException("Incorrect point value, must be between 0 and " + (points-1));
        return this.values[i];
    }

    public boolean isAdmissible(double x) {
        for (int i=0;i<points;i++)
            if (Math.abs(values[i] - x) < TOLERANCE)
                return true;
        return false;
    }

    public void setCurrentPoint(int i) {
        if (i < 0 || i >= points )
            throw new IllegalArgumentException("Incorrect point value, must be between 0 and " + (points-1));
        this.currentPoint = i;
    }





}
