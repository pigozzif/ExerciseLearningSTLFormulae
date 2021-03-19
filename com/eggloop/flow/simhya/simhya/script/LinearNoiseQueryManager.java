/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.script;

/**
 *
 * @author luca
 */
public interface LinearNoiseQueryManager {
    
    /**
     * Converts the string x into a suitable format for standard deviation
     * representation of a linear noise query.
     * @param x
     * @return 
     */
    public String convertForStdev(String x);
    public double getParameterValue(String x);
    public boolean isAllowed(String x);
    public boolean isParameter(String x);
    public boolean isVariable(String x);
    public boolean isNumber(String x);
    /**
     * Adds a query to the query list
     * @param average the expression  containing the linear combination of variables for the average value
     * @param stdev the expression containing the linear combination of variables for the standard deviation
     * @param size the system size
     * @param threshold the threshold to compare with
     * @param greater true if we want to compute the probability of being over the threshold
     * @param onPop true if the query is on population rather than on densities.
     */
    public void addQuery(String average, String stdev, double size,
                         double threshold, boolean greater, boolean onPop);
    /**
     * Adds a query to the query list
      * @param average the expression  containing the linear combination of variables for the average value
     * @param stdev the expression containing the linear combination of variables for the standard deviation
     * @param size the system size
     * @param lower the lower bound of the interval for which we want to estimate the probability
     * @param upper the upper bound of the same interval
     * @param onPop true if the query is on population rather than on densities.
     */
    public void addQuery(String average, String stdev, double size,
                         double lower, double upper, boolean onPop);
}
