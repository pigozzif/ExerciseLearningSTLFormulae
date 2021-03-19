/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing.chart;

import cern.colt.list.DoubleArrayList;

/**
 *
 * @author luca
 */
public class PlotDistributionTrajectory {

    public double[] x;
    public DoubleArrayList[] data;
    public String name;

    public PlotDistributionTrajectory(int points) {
        this.x = new double[points];
        this.data = new DoubleArrayList[points];
    }

    public PlotDistributionTrajectory(String name, int points) {
        this.name = name;
        this.x = new double[points];
        this.data = new DoubleArrayList[points];
    }


}
