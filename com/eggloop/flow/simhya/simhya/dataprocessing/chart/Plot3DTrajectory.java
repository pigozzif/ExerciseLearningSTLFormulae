/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.dataprocessing.chart;

/**
 *
 * @author Luca
 */
public class Plot3DTrajectory {
    public String name;
    public double [] x;
    public double [] y;
    public double [] z;

    public Plot3DTrajectory(int points) {
        x = new double[points];
        y = new double[points];
        z = new double[points];
    }
}
