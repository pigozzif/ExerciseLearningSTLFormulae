/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.paramexplore;

/**
 *
 * @author Luca
 */
public abstract class ParamValueSet {
    private boolean isParameter;
    private String name;
    int currentPoint;
    final double TOLERANCE = 1e-15;

    public ParamValueSet(boolean isParameter, String name) {
        this.isParameter = isParameter;
        this.name = name;
    }


    

    /**
     * 
     * @return the name of the parameter/variable
     */
    public String getName() {
        return name;
    }
    /**
     *
     * @return true if the valueset is for a parameter, false if it is for the (initial) value
     * of a variable.
     */
    public boolean isParameter() {
        return isParameter;
    }
    /**
     *
     * @return the number of points in the set
     */
    public abstract int getPoints();
    /**
     *
     * @return the current point stored in the set
     */
    public int getCurrentPoint() {
        return currentPoint;
    }
    /**
     *
     * @return the current value of the parameter/variable;
     */
    public abstract double getCurrentValue();
    /**
     * Sets the current point to i
     * @param i the new current point
     */
    public abstract void setCurrentPoint(int i);
    /**
     * Gets the value of the point i
     * @param i the point of which the value is requestes
     * @return a double value
     */
    public abstract double getValue(int i);
    /**
     *
     * @param x a double value
     * @return true if x is a value in the parameter value set.
     */
    public abstract boolean isAdmissible(double x);
    /**
     * Gets the index of the closest point in the set to x.
     * @param x a double value
     * @return an index
     */
    public abstract int getIndexOfClosestPoint(double x);
}
