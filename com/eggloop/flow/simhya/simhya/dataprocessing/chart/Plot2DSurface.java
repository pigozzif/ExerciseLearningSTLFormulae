/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing.chart;

/**
 *
 * @author Luca
 */
public class Plot2DSurface {
    public String name;
    public double [] x;
    public double [] y;
    public double [][] z;

    public Plot2DSurface(int Xpoints, int Ypoints) {
        x = new double[Xpoints];
        y = new double[Ypoints];
        z = new double[Xpoints][];
        for (int i=0;i<Xpoints;i++)
            z[i] = new double[Ypoints];
    }

    @Override
    public String toString() {
        String s = "";
        for (int i=0;i<x.length;i++)
            for (int j=0;j<y.length;j++)
                s += x[i] + "  " + y[j] + "  " + z[i][j] + "\n";
        return s;
    }




}
