/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing.chart;

/**
 * A class used to store a single XY trajectory, containing the data and the name of the
 * traectory. It is used by the ExploratorDataProcessor to generate trajectories in the parameter space.
 * @author Luca
 */
public class Plot2DTrajectory {
    public String name;
    public double [] x;
    public double [] y;

    public Plot2DTrajectory() {
        name = "";
        x = null;
        y = null;
    }

 
    public Plot2DTrajectory(int points) {
        x = new double[points];
        y = new double[points];
    }
    
    /**
     * constructs a Plot2DTrajectory from series i and j of trace
     * @param trace
     * @param i
     * @param j 
     */
    public Plot2DTrajectory(double[][] trace, int i, int j) {
        if (i >= trace.length || j >= trace.length)
            throw new PlotException("No time series with given id");
        x = trace[i];
        y = trace[j];
    }

}
