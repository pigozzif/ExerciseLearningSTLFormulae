/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.dataprocessing.chart;

import cern.colt.list.DoubleArrayList;

/**
 *
 * @author luca
 */
public class PlotDistributionState {
    public DoubleArrayList data;
    public String name;

    public PlotDistributionState() {
    }

    public PlotDistributionState(String name, double[] data) {
        this.data = new DoubleArrayList(data);
        this.name = name;
    }

}
